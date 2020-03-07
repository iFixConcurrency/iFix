package concurrency.ifix.util;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ch.qos.logback.classic.Logger;
import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import edu.tamu.aser.tide.akkabug.BugHub;
import edu.tamu.aser.tide.engine.*;
import edu.tamu.aser.tide.plugin.handlers.ConvertHandler;
import edu.tamu.wala.increpta.ipa.callgraph.propagation.IPAPointerAnalysisImpl;
import edu.tamu.wala.increpta.ipa.callgraph.propagation.IPASSAPropagationCallGraphBuilder;
import edu.tamu.wala.increpta.util.IPAUtil;
import fix.iDebugger.handlers.FixHub;
import fix.iDebugger.nodes.RepairPolicy;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * @author ann
 */
public class BenchmarkUtil {

    private static Logger logger = (Logger) LoggerFactory.getLogger(BenchmarkUtil.class);

    private String mainSignature = ".main([Ljava/lang/String;)V";
    private String benchmark;
    private String mainClassName;
    private String mainMethodSig;
    private String testFile;
    private String excludeFile = "data/DefaultExclusions.txt";

    public static TIDEEngine engine = null;

    public BenchmarkUtil(String benchmark, String mainClassName, String mainMethodSig, String testFile) {
        this.benchmark = benchmark;
        this.mainClassName = mainClassName;
        this.mainMethodSig = mainMethodSig;
        this.testFile = testFile;
    }

    public static class BenchmarkInfo{
        private String mainSignature = ".main([Ljava/lang/String;)V";
        private String benchmark;
        private String mainClassName;
        private String mainMethodSig;
        private String testFile;
        private String excludeFile = "data/DefaultExclusions.txt";

        public BenchmarkInfo(String mainSignature, String benchmark, String mainClassName, String testFile, String excludeFile) {
            this(benchmark, mainClassName, testFile);
            this.mainSignature = mainSignature;
            this.excludeFile = excludeFile;
        }

        public BenchmarkInfo(String benchmark, String mainClassName, String testFile) {
            this.benchmark = benchmark;
            this.mainClassName = mainClassName;
            this.mainMethodSig =  mainClassName + mainSignature;
            this.testFile = testFile;
        }
    }


    public static void main(String[] args) {


        ArrayList<BenchmarkInfo> list = new ArrayList<>();
//        list.add(new BenchmarkInfo("accountsubtype", "accountsubtype/Main", "walainfo/accountsubtype.txt"));
//        list.add(new BenchmarkInfo("airlinetickets", "airlinetickets/Airlinetickets", "walainfo/airlinetickets.txt"));
//        list.add(new BenchmarkInfo("alarmclock", "alarmclock/AlarmClock", "walainfo/alarmclock.txt"));
//        list.add(new BenchmarkInfo("allocationvector", "allocationvector/AllocationTest", "walainfo/allocationvector.txt"));
//        list.add(new BenchmarkInfo("array", "array/Test", "walainfo/array.txt"));
//        list.add(new BenchmarkInfo("atmoerror", "atmoerror/Main", "walainfo/atmoerror.txt"));
//        list.add(new BenchmarkInfo("bakery", "bakery/Bakery", "walainfo/bakery.txt"));
//        list.add(new BenchmarkInfo("boundedbuffer", "boundedbuffer/BoundedBuffer", "walainfo/boundedbuffer.txt"));
//        list.add(new BenchmarkInfo("bubblesort", "bubblesort/BubbleSort", "walainfo/bubblesort.txt"));
//        list.add(new BenchmarkInfo("bufwriter", "bufwriter/BufWriter", "walainfo/bufwriter.txt"));
//        list.add(new BenchmarkInfo("buggyprogram", "buggyprogram/BuggyProgram", "walainfo/buggyprogram.txt"));
//        list.add(new BenchmarkInfo("bugSimplified", "bugSimplified/bugSimplified", "walainfo/bugSimplified.txt"));
//        list.add(new BenchmarkInfo("checkfield", "checkfield/CheckField", "walainfo/checkfield.txt"));
//        list.add(new BenchmarkInfo("consisitency", "consisitency/Main", "walainfo/consisitency.txt"));
//        list.add(new BenchmarkInfo("critical", "critical/Critical", "walainfo/critical.txt"));
//        list.add(new BenchmarkInfo("cyclicdemo", "cyclicDemo/CyclicDemo", "walainfo/cyclicdemo.txt"));
//        list.add(new BenchmarkInfo("datarace", "datarace/Main", "walainfo/datarace.txt"));
//        list.add(new BenchmarkInfo("dekker", "dekker/dekker", "walainfo/dekker.txt"));
        list.add(new BenchmarkInfo("elevator", "elevator/Simulator", "walainfo/elevator.txt"));
//        list.add(new BenchmarkInfo("even", "even/Main", "walainfo/even.txt"));
//        list.add(new BenchmarkInfo("hashcodetest", "hashcodetest/HashCodeTest", "walainfo/hashcodetest.txt"));
//        list.add(new BenchmarkInfo("JGFMolDynBenchSizeA", "JGFMolDynBenchSizeA/JGFMolDynBenchSizeA", "walainfo/JGFMolDynBenchSizeA.txt"));
//          list.add(new BenchmarkInfo("JGFMonteCarloBenchSizeA", "JGFMonteCarloBenchSizeA/JGFMonteCarloBenchSizeA", "walainfo/JGFMonteCarloBenchSizeA.txt"));
//          list.add(new BenchmarkInfo("JGFRayTracerBenchSizeA", "JGFRayTracerBenchSizeA/JGFRayTracerBenchSizeA", "walainfo/JGFRayTracerBenchSizeA.txt"));
//        list.add(new BenchmarkInfo("lamport", "lamport/lamport", "walainfo/lamport.txt"));
//        list.add(new BenchmarkInfo("linkedlist", "linkedlist/BugTester", "walainfo/linkedlist.txt"));
//          list.add(new BenchmarkInfo("mergesort", "mergesort/MergeSort", "walainfo/mergesort.txt"));
//        list.add(new BenchmarkInfo("mix0", "mix0/mix0", "walainfo/mix0.txt"));
//        list.add(new BenchmarkInfo("mix1", "mix1/mix1", "walainfo/mix1.txt"));
//        list.add(new BenchmarkInfo("omcr", "omcr/TicketsOrderSim", "walainfo/omcr.txt"));
//        list.add(new BenchmarkInfo("peterson", "peterson/peterson", "walainfo/peterson.txt"));
//        list.add(new BenchmarkInfo("pingpong", "pingpong/PingPong", "walainfo/pingpong.txt"));
//        list.add(new BenchmarkInfo("pipeline", "pipeline/PipeInttest", "walainfo/pipeline.txt"));
//        list.add(new BenchmarkInfo("producerConsumer", "producerConsumer/ProducerConsumer", "walainfo/producerConsumer.txt"));
//        list.add(new BenchmarkInfo("rax", "rax/START", "walainfo/rax.txt"));
//        list.add(new BenchmarkInfo("reorder1", "reorder1/ReorderTest", "walainfo/reorder1.txt"));
//        list.add(new BenchmarkInfo("sharedobject", "sharedobject/SharedObjecTest", "walainfo/sharedobject.txt"));
//        list.add(new BenchmarkInfo("store", "store/StoreTest", "walainfo/store.txt"));
//        list.add(new BenchmarkInfo("stringbuffer", "stringbuffer/StringBufferTest", "walainfo/stringbuffer.txt"));
//        list.add(new BenchmarkInfo("testArray", "testArray/testArray", "walainfo/testArray.txt"));
//        list.add(new BenchmarkInfo("tso", "tso/tso", "walainfo/tso.txt"));
//        list.add(new BenchmarkInfo("wrongLock", "wrongLock/Main", "walainfo/wrongLock.txt"));
//        list.add(new BenchmarkInfo("wronglock2", "wronglock2/Main", "walainfo/wronglock2.txt"));

        Map<String, List<Double>> resMap = new HashMap<>();

        for(BenchmarkInfo info : list){
            logger.info("Benchmark: " + info.benchmark);
            for(int i = 0; i < 10; i ++){
                BenchmarkUtil instance = new BenchmarkUtil(info.benchmark, info.mainClassName, info.mainMethodSig, info.testFile);
                int scheduletime = 900000;
//		print("Benchmark: " + benchmark, true);
                TIDEEngine.setIsPlugin(false);
                long start = System.currentTimeMillis();
                try{
                    instance.run();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                long detectTime = System.currentTimeMillis() - start;
                start = System.currentTimeMillis();
                FixHub fixHub = FixHub.getInstance();
                fixHub.clear();
                fixHub.handleFix();
                long fixTime = System.currentTimeMillis() - start;
                logger.info(info.benchmark + " | " + detectTime + "  | " + fixTime + " | " + RepairPolicy.getInstance().calSpace.getRes());
                if(i == 0){
                    System.out.println(
                            "var: "+
                            RepairPolicy.getInstance().repository.getVariableList() + "," + "lock:" +
                                    RepairPolicy.getInstance().repository.getLockList());
                }
                resMap.putIfAbsent(info.benchmark, new ArrayList<>());
                resMap.get(info.benchmark).add(RepairPolicy.getInstance().calSpace.getRes());
//            System.out.println();
            }
            for(Map.Entry<String, List<Double>> entry : resMap.entrySet()){
                double add = 0.0;
                for(int i = 0; i < entry.getValue().size(); i ++){
                    add += entry.getValue().get(i);
                }
                System.out.println(entry.getKey() + " : " + (add / 10.0));
            }
            resMap.clear();
        }


//
//
//        String mainSignature = ".main([Ljava/lang/String;)V";
//        String benchmark = "wrongLock";
//        String mainClassName = "wrongLock/Main";
//        String mainMethodSig = mainClassName + mainSignature;
//        String testFile = "data/wronglock.txt";
//
//        BenchmarkUtil instance = new BenchmarkUtil(benchmark, mainClassName, mainMethodSig, testFile);
//        int scheduletime = 900000;
//        logger.info("Benchmark: " + benchmark);
////		print("Benchmark: " + benchmark, true);
//        TIDEEngine.setIsPlugin(false);
//        try{
//            instance.run();
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//        FixHub fixHub = FixHub.getInstance();
//        fixHub.handleFix();
//        System.out.println();
    }



    public void run()
            throws IOException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException {
//        logger.info("start to process benchmark : " + benchmark);

        ClassLoader loader = BenchmarkUtil.class.getClassLoader();
        InputStream stream = loader.getResourceAsStream(testFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try{
            String line = "";
            while((line = reader.readLine()) != null){
                String[] info = line.split(",");
                if("sourceDir".equals(info[2])){
                    File dir = new File(info[3]);
                    for(File sourceFile : dir.listFiles()){
                        ASTUtil.changeSync(sourceFile);
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            reader.close();
        }

        AnalysisScope scope = AnalysisScopeReader.read(new JavaSourceAnalysisScope(), testFile, (new FileProvider()).getFile(excludeFile), loader);

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
        int numofCGNodes = cg.getNumberOfNodes();
        int totalInstanceKey = pta.getInstanceKeys().size();
        int totalPointerKey =((IPAPointerAnalysisImpl)pta).getNumOfPointerKeys();
        int totalPointerEdge = 0;
        int totalClass=cha.getNumberOfClasses();
        Iterator<PointerKey> iter = pta.getPointerKeys().iterator();
        while(iter.hasNext()){
            PointerKey key = iter.next();
            int size = pta.getPointsToSet(key).size();
            totalPointerEdge+=size;
        }
//        logger.info("#Class: "+totalClass);
//        logger.info("#Class: "+totalClass);
//        logger.info("#Method: "+numofCGNodes);
//        logger.info("#Pointer: "+totalPointerKey);
//        logger.info("#Object: "+totalInstanceKey);
//        logger.info("#Edges: "+totalPointerEdge);
//
//        logger.info("graph build success");
        //detector
        Config config = ConfigFactory.load();
        ActorSystem akkasys = ActorSystem.create();
        ActorRef bughub = akkasys.actorOf(Props.create(BugHub.class, 1), "bughub");
//		start_time = System.currentTimeMillis();
        mainMethodSig = mainClassName.replace("/", ".") + mainSignature;
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
//        System.out.println(race);
//        System.out.println("Exhaustive Points-to Analysis + Detection Time: " + (System.currentTimeMillis() - start_time) + "ms\n");
//		System.err.println("Exhaustive Race Detection Time: " + engine.timeForDetectingRaces);
//		System.err.println("Exhaustive Deadlock Detection Time: " + engine.timeForDetectingDL);

//		System.out.println("Running Incremental Points-to Analysis and Detection ... ");
//		builder.getSystem().initialParallelSystem(false, 1);
//		incrementalTest(builder, cg);
    }

    public Iterable<Entrypoint> findEntryPoints(IClassHierarchy classHierarchy, String mainClassNamee, boolean includeAll) {
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
                            if(includeAll || klass.getName().toString().contains(mainClassNamee)) {
                                result.add(new DefaultEntrypoint(method, classHierarchy));
//                                System.out.println("+++++ " + method.toString());
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
