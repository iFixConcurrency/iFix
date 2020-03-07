package fix.iDebugger.handlers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.MapUtil;
import com.ibm.wala.util.config.FileOfClasses;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import edu.tamu.aser.tide.akkabug.BugHub;
import edu.tamu.aser.tide.engine.*;
import edu.tamu.aser.tide.plugin.handlers.ConvertHandler;
import edu.tamu.aser.tide.tests.ReproduceBenchmarks;
import edu.tamu.wala.increpta.ipa.callgraph.propagation.IPAPointerAnalysisImpl;
import edu.tamu.wala.increpta.ipa.callgraph.propagation.IPASSAPropagationCallGraphBuilder;
import edu.tamu.wala.increpta.util.IPAUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarFile;

/**
 * @author ann
 */
public class DetectionHub implements IDetction {

    public static TIDEEngine engine;

    Map<ClassLoaderReference, List<Module>> modules;

    private String mainClassName;

    private List<String> changedFileList = new ArrayList<>();

    public DetectionHub(Map<String, List<String>> modules, String className) throws IOException {
        this.modules = new HashMap<>();
        for(Map.Entry<String, List<String>> entry : modules.entrySet()) {
            switch (entry.getKey()) {
                case "SOURCE":
                    MapUtil.findOrCreateList(this.modules, JavaSourceAnalysisScope.SOURCE);
                    for (String filePath : entry.getValue()) {
                        this.modules.get(JavaSourceAnalysisScope.SOURCE).add(new SourceDirectoryTreeModule(new File(filePath)));
                    }
                    break;
                case "Primordial":
                    MapUtil.findOrCreateList(this.modules, ClassLoaderReference.Primordial);
                    for (String filePath : entry.getValue()) {
                        this.modules.get(ClassLoaderReference.Primordial).add(new JarFileModule(new JarFile(filePath)));
                    }
                    break;
                case "Application":
                    MapUtil.findOrCreateList(this.modules, ClassLoaderReference.Application);
                    for (String filePath : entry.getValue()) {
                        this.modules.get(ClassLoaderReference.Application).add(new JarFileModule(new JarFile(filePath)));
                    }
                    break;
                case "Extension":
                    MapUtil.findOrCreateList(this.modules, ClassLoaderReference.Extension);
                    for (String filePath : entry.getValue()) {
                        this.modules.get(ClassLoaderReference.Extension).add(new JarFileModule(new JarFile(filePath)));
                    }
                    break;
                default:

                    break;
            }
        }
        this.mainClassName = className;
    }

    @Override
    public void detect() throws IOException, ClassHierarchyException, CallGraphBuilderCancelException {
        AnalysisScope scope = new JavaSourceAnalysisScope();
        for(Map.Entry<ClassLoaderReference, List<Module>> entry : this.modules.entrySet()){
            for(Module module : entry.getValue()){
                scope.addToScope(entry.getKey(), module);
            }
        }
        InputStream is = ReproduceBenchmarks.class.getClassLoader().getResourceAsStream("data/EclipseDefaultExclusions.txt");
        scope.setExclusions(new FileOfClasses(is));
        ECJClassLoaderFactory factory = new ECJClassLoaderFactory(scope.getExclusions());
        ClassHierarchy cha = ClassHierarchyFactory.make(scope, factory);

        Iterable<Entrypoint> entrypoints = findEntryPoints(cha, mainClassName, false);
        AnalysisOptions options = new AnalysisOptions(scope, entrypoints);

        IPASSAPropagationCallGraphBuilder builder = (IPASSAPropagationCallGraphBuilder) IPAUtil.makeIPAAstZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(AstIRFactory.makeDefaultFactory()), cha, scope);
//		IPASSAPropagationCallGraphBuilder builder = IPAUtil.makeIPAZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope);
        long start_time = System.currentTimeMillis();
        CallGraph cg  = builder.makeCallGraph(options, null);
        IPAPointerAnalysisImpl pta = (IPAPointerAnalysisImpl) builder.getPointerAnalysis();
        System.out.println("Exhaustive Points-to Analysis Time: "+(System.currentTimeMillis()-start_time) + "ms");
//        System.out.println("Running Exhaustive Detection ... ");
        Config config = ConfigFactory.load(DetectionHub.class.getClassLoader());
        ActorSystem akkasys = ActorSystem.create("ifix", config, DetectionHub.class.getClassLoader());
        ActorRef bughub = akkasys.actorOf(Props.create(BugHub.class, 1), "bughub");
        String mainMethodSig = mainClassName.replace("/", ".") + ".main([Ljava/lang/String;)V";
        engine = new TIDEEngine(builder, mainMethodSig, cg, pta, bughub);
        engine.detectBothBugs(null);

        int race = 0;
        int dl = 0;
        HashSet<ITIDEBug> bugs = new HashSet<>();
        bugs.addAll(engine.addeddeadlocks);
        bugs.addAll(engine.addedraces);
        for(ITIDEBug bug : bugs){
            if(bug instanceof TIDERace){
                race++;
            }else if (bug instanceof TIDEDeadlock){
                dl++;
            }
        }
        System.out.println("Race Count : " + engine.races.size());
        System.out.println("Exhaustive Points-to Analysis + Detection Time: " + (System.currentTimeMillis() - start_time) + "ms\n");
    }

    public Iterable<Entrypoint> findEntryPoints(IClassHierarchy classHierarchy, String mainClassName, boolean includeAll) {
        final Set<Entrypoint> result = HashSetFactory.make();
        Iterator<IClass> classIterator = classHierarchy.iterator();
        while (classIterator.hasNext()) {
            IClass klass = classIterator.next();
            if (!AnalysisUtils.isJDKClass(klass)) {
                for (IMethod method : klass.getDeclaredMethods()) {
                    try {
                        if(method.isStatic()&&method.isPublic()
                                &&method.getName().toString().equals("main")
                                &&method.getDescriptor().toString().equals(ConvertHandler.DESC_MAIN))
                        {
                            if(includeAll || klass.getName().toString().contains(mainClassName)) {
                                result.add(new DefaultEntrypoint(method, classHierarchy));
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return new Iterable<Entrypoint>() {
            @Override
            public Iterator<Entrypoint> iterator() {
                return result.iterator();
            }
        };
    }
}
