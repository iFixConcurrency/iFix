package edu.tamu.aser.tide.tests;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ch.qos.logback.classic.Logger;
import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.fixpoint.IVariable;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import edu.tamu.aser.tide.akkabug.BugHub;
import edu.tamu.aser.tide.dist.remote.master.DistributeMaster;
import edu.tamu.aser.tide.engine.*;
import edu.tamu.aser.tide.plugin.handlers.ConvertHandler;
import edu.tamu.wala.increpta.ipa.callgraph.propagation.IPAPointerAnalysisImpl;
import edu.tamu.wala.increpta.ipa.callgraph.propagation.IPASSAPropagationCallGraphBuilder;
import edu.tamu.wala.increpta.ipa.callgraph.propagation.IPASSAPropagationCallGraphBuilder.ConstraintVisitor;
import edu.tamu.wala.increpta.util.IPAUtil;
import fix.iDebugger.handlers.FixHub;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.LoggerFactory;
import concurrency.ifix.util.FileUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class ReproduceBenchmarks {
	public static String ROOT_PATH = "";

	private static Logger logger = (Logger) LoggerFactory.getLogger(ReproduceBenchmarks.class);

	static PrintStream ps;
	private static long totaltime;
	public static TIDEEngine engine;

	static boolean includeAllMainEntryPoints = false;

	static String benchmark = null;
	static String mainSignature = ".main([Ljava/lang/String;)V";
	static String mainClassName = null;
	static String mainMethodSig = null;
	static String testFile = null;
	static String excludeFile = "data/DefaultExclusions.txt";
	static long scheduletime = 5400000;

    static String[] benchmark_names_short= new String[] { "avrora_short", "batik_short", "eclipse_short", "fop_short",
			"h2_short", "jython_short", "luindex_short", "lusearch_short", "pmd_short",
			"sunflow_short", "tomcat_short", "tradebeans_short", "tradesoap_short",
			"xalan_short"};

//    static String[] benchmark_names = new String[] { "avrora", "batik", "eclipse", "fop", "h2", "jython",
//			"luindex", "lusearch", "pmd", "sunflow", "tomcat", "tradebeans", "tradesoap",
//			"xalan", "nolock"};
    static String[] benchmark_names = new String[] { "xalan", "pmd", "sunflow","luindex", "lusearch",
		"eclipse", "h2", "jython", "avrora", "batik"};

    public static void run(String benchmarkName, int threadNumber, String rootPath) throws ClassHierarchyException, CallGraphBuilderCancelException {
		benchmark = benchmarkName;
		switch (benchmark) {
			case "avrora":
				mainClassName = "avrora/Main";
				mainMethodSig = "avrora.Main" + mainSignature;
				testFile = "data/avroratestfile.txt";
				break;
			case "batik":
				mainClassName = "rasterizer/Main";
				mainMethodSig = "rasterizer.Main" + mainSignature;
				testFile = "data/batiktestfile.txt";
				break;
			case "eclipse":
				mainClassName = "EclipseStarter";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/eclipsetestfile.txt";
				break;
			case "fop": //no detection
				mainClassName = "TTFFile";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/foptestfile.txt";
				break;
			case "h2":
				mainClassName = "Shell";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/h2testfile.txt";
				break;
			case "jython":
				mainClassName = "jython";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/jythontestfile.txt";
				break;
			case "luindex":
				mainClassName = "IndexFiles";
				mainMethodSig = mainClassName + mainSignature;
				excludeFile = "data/luindexexcludefile.txt";
				testFile = "data/luindextestfile.txt";
				break;
			case "lusearch":
				mainClassName = "IndexHTML";
				mainMethodSig = mainClassName + mainSignature;
				excludeFile = "data/lusearchexcludefile.txt";
				testFile = "data/lusearchtestfile.txt";
				break;
			case "pmd":
				mainClassName = "GUI";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/pmdtestfile.txt";
				break;
			case "sunflow":
				mainClassName = "Benchmark";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/sunflowtestfile.txt";
				break;
			case "tomcat":
				mainClassName = "ExpressionDemo";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/dacapotestfile.txt";
				excludeFile = "data/tomcatexcludefile.txt";
				break;
			case "tradebeans":
				mainClassName = "REUtil";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/dacapotestfile.txt";
				excludeFile = "data/tradebeansexcludefile.txt";
				break;
			case "tradesoap":
				mainClassName = "REUtil";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/dacapotestfile.txt";
				excludeFile = "data/tradesoapexcludefile.txt";
				break;
			case "xalan":
				mainClassName = "XSLProcessorVersion";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/xalantestfile.txt";
				break;

			//short version
			case "avrora_short":
				mainClassName = "avrora/Main";
				mainMethodSig = "avrora.Main" + mainSignature;
				testFile = "data/avroratestfile.txt";
				excludeFile = "data/ShortDefaultExclusions.txt";
				scheduletime = 900000;
				break;
			case "batik_short":
				mainClassName = "rasterizer/Main";
				mainMethodSig = "rasterizer.Main" + mainSignature;
				testFile = "data/batiktestfile.txt";
				excludeFile = "data/ShortDefaultExclusions.txt";
				scheduletime = 900000;
				break;
			case "eclipse_short":
				mainClassName = "EclipseStarter";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/eclipsetestfile.txt";
				excludeFile = "data/ShortDefaultExclusions.txt";
				scheduletime = 900000;
				break;
			case "fop_short": //no detection
				mainClassName = "TTFFile";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/foptestfile.txt";
				excludeFile = "data/ShortDefaultExclusions.txt";
				scheduletime = 900000;
				break;
			case "h2_short":
				mainClassName = "Shell";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/h2testfile.txt";
				excludeFile = "data/ShortDefaultExclusions.txt";
				scheduletime = 900000;
				break;
			case "jython_short":
				mainClassName = "jython";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/jythontestfile.txt";
				excludeFile = "data/ShortDefaultExclusions.txt";
				scheduletime = 900000;
				break;
			case "luindex_short":
				mainClassName = "IndexFiles";
				mainMethodSig = mainClassName + mainSignature;
				excludeFile = "data/luindexexcludefileshort.txt";
				testFile = "data/dacapotestfile.txt";
				scheduletime = 900000;
				break;
			case "lusearch_short":
				mainClassName = "IndexHTML";
				mainMethodSig = mainClassName + mainSignature;
				excludeFile = "data/lusearchexcludefileshort.txt";
				testFile = "data/dacapotestfile.txt";
				scheduletime = 900000;
				break;
			case "pmd_short":
				mainClassName = "GUI";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/pmdtestfile.txt";
				excludeFile = "data/ShortDefaultExclusions.txt";
				scheduletime = 900000;
				break;
			case "sunflow_short":
				mainClassName = "Benchmark";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/sunflowtestfile.txt";
				excludeFile = "data/ShortDefaultExclusions.txt";
				scheduletime = 900000;
				break;
			case "tomcat_short":
				mainClassName = "ExpressionDemo";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/dacapotestfile.txt";
				excludeFile = "data/tomcatexcludefileshort.txt";
				scheduletime = 900000;
				break;
			case "tradebeans_short":
				mainClassName = "REUtil";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/dacapotestfile.txt";
				excludeFile = "data/tradebeansexcludefileshort.txt";
				scheduletime = 900000;
				break;
			case "tradesoap_short":
				mainClassName = "REUtil";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/dacapotestfile.txt";
				excludeFile = "data/tradesoapexcludefileshort.txt";
				scheduletime = 900000;
				break;
			case "xalan_short":
				mainClassName = "XSLProcessorVersion";
				mainMethodSig = mainClassName + mainSignature;
				testFile = "data/xalantestfile.txt";
				excludeFile = "data/ShortDefaultExclusions.txt";
				scheduletime = 900000;
				break;
			case "nolock":
				mainClassName = "Main";
				mainMethodSig = mainClassName + mainSignature;
				excludeFile = "data/ShortDefaultExclusions.txt";
				testFile = "data/nolocktestfile.txt";
				break;
//			case "all_short":
//				if(args.length == 2){
//					String arg2 = args[1];
//					iterateAllBenchmarksShortMultithread(arg2);
//				}else{
//					iterateAllBenchmarksShort();
//				}
//				break;

			default:
				throw new IllegalArgumentException("Invalid argument: " + benchmark);
		}
		scheduletime = 900000;
		//start
		ROOT_PATH = rootPath;
		if(! testFile.startsWith(ROOT_PATH)){
			testFile = ROOT_PATH + "/data/" + testFile;
		}
		if(! excludeFile.startsWith(ROOT_PATH)){
			excludeFile = ROOT_PATH + "/data/" + excludeFile;
		}
		logger.info("Benchmark: " + benchmark);
//			print("Benchmark: " + benchmark, true);
		TIDEEngine.setIsPlugin(false);
		long startTime = System.currentTimeMillis();
		runD4_multithreads(threadNumber);
		logger.info("detection cost " + (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
//			runD4_1();
		FixHub fixHub = FixHub.getInstance();
		fixHub.handleFix();
		logger.info("fix cost " + (System.currentTimeMillis() - startTime));
		System.out.println();
		System.exit(0);
	}

	public static void main(String[] args) throws IOException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException {
    	String benchmark = "all";
    	int thread = 1;
    	String root = "";
		if(args.length >= 1){
			benchmark = args[0];
		}
		if(args.length >= 2){
			thread = Integer.parseInt(args[1]);
		}
		if(args.length >= 3){
			root = args[2];
		}
		if("all".equals(benchmark)){
			System.out.println("RUNNING SHORT TESTS FOR ALL BENCHMARKS WITH " + thread + " ON A SINGLE MACHINE.\n");

			for (int i = 0; i < benchmark_names.length; i++) {
				run(benchmark_names[i], thread, root);
			}

			System.out.println("\n COMPLETE SHORT TESTING ALL BENCHMARKS ON A SINGLE MACHINE.");
		}
		else{
			run(benchmark, thread, root);
		}
//			runD4_48();
//			System.out.println();
//		}
	}

	private static void iterateAllBenchmarksShortMultithread(String arg2)
			throws ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException, IOException{
		System.out.println("RUNNING SHORT TESTS FOR ALL BENCHMARKS WITH " + arg2 + " ON A SINGLE MACHINE.\n");

		for (int i = 0; i < benchmark_names_short.length; i++) {
			String[] arg = new String[]{benchmark_names_short[i], arg2};
			main(arg);
		}

		System.out.println("\n COMPLETE SHORT TESTING ALL BENCHMARKS ON A SINGLE MACHINE.");
	}

	private static void iterateAllBenchmarksShort()
			throws ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException, IOException{
		System.out.println("RUNNING SHORT TESTS FOR ALL BENCHMARKS.\n");

		for (int i = 0; i < benchmark_names_short.length; i++) {
			String[] arg = new String[]{benchmark_names_short[i]};
			main(arg);
		}

		System.out.println("\n COMPLETE SHORT TESTING ALL BENCHMARKS.");
	}

	private static void iterateAllBenchmarksMultithread(String arg2)
			throws ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException, IOException {
		System.out.println("RUNNING FULL TESTS FOR ALL BENCHMARKS WITH " + arg2 + " ON A SINGLE MACHINE.\n");

		for (int i = 0; i < benchmark_names.length; i++) {
			String[] arg = new String[]{benchmark_names[i], arg2};
			main(arg);
		}

		System.out.println("\n COMPLETE FULL TESTING ALL BENCHMARKS ON A SINGLE MACHINE.");
	}

	private static void iterateAllBenchmarks() throws ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException, IOException {
		System.out.println("RUNNING FULL TESTS FOR ALL BENCHMARKS.\n");

		for (int i = 0; i < benchmark_names.length; i++) {
			String[] arg = new String[]{benchmark_names[i]};
			main(arg);
		}

		System.out.println("\n COMPLETE FULL TESTING ALL BENCHMARKS.");
	}

	private static AnalysisScope makeJ2SEAnalysisScope(String scopeFile, String exclusionsFile){
		ClassLoader loader = ReproduceBenchmarks.class.getClassLoader();
		try{
//			return scope;
			AnalysisScope scope = AnalysisScopeReader.readJavaScope(scopeFile, (new FileProvider()).getFile(exclusionsFile), loader);
			return scope;
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}

	private static void runD4_multithreads(int numOfWorkers)
			throws ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException{
		print("D4 with " + numOfWorkers + " threads on a single machine", true);
		System.out.println("Running Exhaustive Points-to Analysis ... ");
		AnalysisScope scope = makeJ2SEAnalysisScope(testFile, excludeFile);
		ClassHierarchy cha = ClassHierarchyFactory.make(scope);
		Iterable<Entrypoint> entrypoints = findEntryPoints(cha, mainClassName, includeAllMainEntryPoints);
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);

		IPASSAPropagationCallGraphBuilder builder = IPAUtil.makeIPAZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope);

		long start_time = System.currentTimeMillis();
		CallGraph cg  = builder.makeCallGraph(options, null);
		IPAPointerAnalysisImpl pta = (IPAPointerAnalysisImpl) builder.getPointerAnalysis();
		System.out.println("Exhaustive Points-to Analysis Time: "+(System.currentTimeMillis()-start_time) + "ms");
		int numofCGNodes = cg.getNumberOfNodes();
		int totalInstanceKey = pta.getInstanceKeys().size();
		int totalPointerKey = pta.getNumOfPointerKeys();
		int totalPointerEdge = 0;
		int totalClass=cha.getNumberOfClasses();
		Iterator<PointerKey> iter = pta.getPointerKeys().iterator();
		while(iter.hasNext()){
			PointerKey key = iter.next();
			int size = pta.getPointsToSet(key).size();
			totalPointerEdge+=size;
		}
		System.out.println("#Class: "+totalClass);
		System.out.println("#Method: "+numofCGNodes);
		System.out.println("#Pointer: "+totalPointerKey);
		System.out.println("#Object: "+totalInstanceKey);
		System.out.println("#Edges: "+totalPointerEdge);
		System.out.println();
		ps.println();

		System.out.println("Running Exhaustive Detection ... ");
		//detector
		ActorSystem akkasys = ActorSystem.create();
		ActorRef bughub = akkasys.actorOf(Props.create(BugHub.class, numOfWorkers), "bughub");
		start_time = System.currentTimeMillis();
		engine = new TIDEEngine(builder, (includeAllMainEntryPoints? mainSignature:mainMethodSig), cg, pta, bughub);
		engine.detectBothBugs(ps);

//		System.out.println("EXHAUSTIVE DETECTION >>>");
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
		System.out.println("Exhaustive Detection Time: " + (System.currentTimeMillis() - start_time) + "ms");
		start_time = System.currentTimeMillis();
		FixHub fixHub = FixHub.getInstance();
		if (fixHub.handleFix()){

		}
		else{
			logger.error("no need to fix");
		}

//		System.err.println("Exhaustive Race Detection Time: " + engine.timeForDetectingRaces);
//		System.err.println("Exhaustive Deadlock Detection :Time " + engine.timeForDetectingDL);

//		System.out.println("Running Incremental Points-to Analysis and Detection ... ");
//		builder.getSystem().initialParallelSystem(false, numOfWorkers);
//		incrementalTest(builder, cg);
	}

	public static void runIDEA(String sourcePath) throws IOException{
		try{
			runD4_1();
		}
		catch (Exception e){
			e.printStackTrace();
		}
    }

	public static void runD4_1()
			throws IOException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException{
		print("D4-1", true);
		System.out.println("Running Exhaustive Points-to Analysis ... ");
//		AnalysisScope scope = makeJ2SEAnalysisScope(testFile, excludeFile);

		ClassLoader loader = ReproduceBenchmarks.class.getClassLoader();
		InputStream stream = loader.getResourceAsStream(testFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		try{
			String line = "";
			while((line = reader.readLine()) != null){
				String[] info = line.split(",");
				if("sourceDir".equals(info[2])){
					File dir = new File(info[3]);
					for(File sourceFile : dir.listFiles()){
						String s = new String(Files.readAllBytes(sourceFile.toPath()), StandardCharsets.UTF_8);
						Document document = new Document(s);
						ASTParser parser = ASTParser.newParser(AST.JLS8);
						parser.setSource(s.toCharArray());
						parser.setKind(ASTParser.K_COMPILATION_UNIT);
						CompilationUnit cu = (CompilationUnit) parser.createAST(null);
						cu.accept(new ASTVisitor() {
							ASTRewrite rewrite = null;
							ListRewrite listRewrite = null;
							@Override
							public boolean visit(MethodDeclaration node) {
								System.out.println("iIN NOde " + node.getName());
								if(Modifier.isSynchronized(node.getModifiers())){
									rewrite = ASTRewrite.create(cu.getAST());
									listRewrite = rewrite.getListRewrite(node, MethodDeclaration.MODIFIERS2_PROPERTY);
									for(int i = 0; i < node.modifiers().size(); i ++){
										if(((Modifier)node.modifiers().get(i)).isSynchronized()){
											listRewrite.remove((Modifier)node.modifiers().get(i), null);
										}
									}
									SynchronizedStatement st = node.getAST().newSynchronizedStatement();
									st.setExpression(st.getAST().newThisExpression());
									st.setBody((Block) ASTNode.copySubtree(st.getAST(), node.getBody()));
									Block block = node.getAST().newBlock();
									block.statements().add(st);
									rewrite.replace(node.getBody(), block, null);
								}
								return super.visit(node);
							}

							@Override
							public void endVisit(CompilationUnit node) {
								System.out.println("end visit");
								if(rewrite != null){
									try{
										TextEdit edit = rewrite.rewriteAST(document, null);
										edit.apply(document);
										FileUtil.writeToFile(sourceFile, document.get());
									}
									catch (Exception e) {
										e.printStackTrace();
									}
								}
								super.endVisit(node);
							}
						});
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
//		ECJJavaSourceAnalysisEngine sengine = new ECJJavaSourceAnalysisEngine();
//		for(ClassLoaderReference r : scope.getLoaders()){
//			if(r.equals(JavaSourceAnalysisScope.SOURCE)){
//				for(Module m : scope.getModules(r)){
//					sengine.addSourceModule(m);
//				}
//			}
//			else{
//				for(Module m : scope.getModules(r)){
//					sengine.addSystemModule(m);
//				}
//			}
//		}
//		sengine.buildAnalysisScope();
//		sengine.buildClassHierarchy();


//		ClassLoaderFactoryImpl factory = new ClassLoaderFactoryImpl(scope.getExclusions());
//		JDTClassLoaderFactory factory = new JDTClassLoaderFactory(scope.getExclusions());
//		ECJClassLoaderFactory factory = new MyECJClassLoaderFactory(scope.getExclusions());
		ECJClassLoaderFactory factory = new ECJClassLoaderFactory(scope.getExclusions());
//		IClassHierarchy cha = sengine.getClassHierarchy();
//		ECJClassLoaderFactory factory = new ECJClassLoaderFactory(scope.getExclusions());
		ClassHierarchy cha = ClassHierarchyFactory.make(scope, factory);

		Iterable<Entrypoint> entrypoints = findEntryPoints(cha, mainClassName, includeAllMainEntryPoints);
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
		System.out.println("#Class: "+totalClass);
		System.out.println("#Method: "+numofCGNodes);
		System.out.println("#Pointer: "+totalPointerKey);
		System.out.println("#Object: "+totalInstanceKey);
		System.out.println("#Edges: "+totalPointerEdge);
		System.out.println();
		ps.println();

		System.out.println("Running Exhaustive Detection ... ");
		//detector
		Config config = ConfigFactory.load();
		ActorSystem akkasys = ActorSystem.create();
		ActorRef bughub = akkasys.actorOf(Props.create(BugHub.class, 1), "bughub");
//		start_time = System.currentTimeMillis();
		mainMethodSig = mainClassName.replace("/", ".") + mainSignature;
		engine = new TIDEEngine(builder, (includeAllMainEntryPoints? mainSignature:mainMethodSig), cg, pta, bughub);
		engine.detectBothBugs(ps);

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
		System.out.println(race);
		System.out.println("Exhaustive Points-to Analysis + Detection Time: " + (System.currentTimeMillis() - start_time) + "ms\n");
//		System.err.println("Exhaustive Race Detection Time: " + engine.timeForDetectingRaces);
//		System.err.println("Exhaustive Deadlock Detection Time: " + engine.timeForDetectingDL);

//		System.out.println("Running Incremental Points-to Analysis and Detection ... ");
//		builder.getSystem().initialParallelSystem(false, 1);
//		incrementalTest(builder, cg);
	}

	public static void runD4_48()
			throws IOException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException{
		print("D4-48", true);
		//dist
		DistributeMaster master = new DistributeMaster();
		master.startClusterSystem(benchmark);

		System.out.println("Running Exhaustive Points-to Analysis ... ");
		AnalysisScope scope = makeJ2SEAnalysisScope(testFile, excludeFile);
		ClassHierarchy cha = ClassHierarchyFactory.make(scope);
		Iterable<Entrypoint> entrypoints = findEntryPoints(cha,mainClassName,includeAllMainEntryPoints);
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);

		IPASSAPropagationCallGraphBuilder builder = IPAUtil.makeIPAZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope);

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
		System.out.println("#Class: "+totalClass);
		System.out.println("#Method: "+numofCGNodes);
		System.out.println("#Pointer: "+totalPointerKey);
		System.out.println("#Object: "+totalInstanceKey);
		System.out.println("#Edges: "+totalPointerEdge);
		System.out.println();
		ps.println();

		System.out.println("Running Exhaustive Detection ... ");
		//detector
		ActorSystem akkasys = ActorSystem.create();
		ActorRef bughub = akkasys.actorOf(Props.create(BugHub.class, 1), "bughub");
//		start_time = System.currentTimeMillis();
		engine = new TIDEEngine(builder, (includeAllMainEntryPoints? mainSignature:mainMethodSig), cg, pta, bughub);
		engine.detectBothBugs(ps);

//		System.out.println("INITIAL DETECTION >>>");
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
//		System.err.println("Exhaustive Race Detection Time: " + engine.timeForDetectingRaces);
//		System.err.println("Exhaustive Deadlock Detection Time: " + engine.timeForDetectingDL);
		System.out.println("Exhaustive Points-to Analysis + Detection Time: " + (System.currentTimeMillis() - start_time) + "ms\n");
		master.awaitRemoteComplete();

		System.out.println("Running Incremental Points-to Analysis and Detection ... ");
		incrementalDistTest(master, builder, cg);
	}

	private static void incrementalDistTest(DistributeMaster master, IPASSAPropagationCallGraphBuilder builder, CallGraph cg) {
		Iterator<CGNode> iter2 = cg.iterator();
		HashSet<CGNode> storeCG = new HashSet<>();
		while(iter2.hasNext()){
			CGNode next = iter2.next();
			if(!next.getMethod().isSynthetic()){
				storeCG.add(next);
			}
		}

		for (CGNode n : storeCG) {
			if(!n.getMethod().getSignature().contains("com.ibm.wala")
					&& !n.getMethod().getSignature().contains(mainSignature)){
				if(!notreach){
					break;
				}
				IR ir = n.getIR();
				if(ir == null) {
					continue;
				}
				master.frontend.tell("METHOD:"+n.toString(), master.frontend);
				master.awaitRemoteComplete();
				if(nextNode){
					nextNode = false;
					continue;
				}
				SSAInstruction[] insts = ir.getInstructions();
				int size = insts.length;
				for(int i=size;i>0;i--){
					SSAInstruction inst = insts[i-1];
					if(inst==null) {
						continue;//skip null
					}
					master.frontend.tell("-STMT:"+ inst.iIndex(), master.frontend);
					master.awaitRemoteComplete();
					master.frontend.tell("+STMT:"+inst.iIndex(), master.frontend);
					master.awaitRemoteComplete();
				}
			}
		}
		if(notreach){
			master.frontend.tell("PERFORMANCE", master.frontend);
			master.awaitRemoteComplete();
		}

		System.out.println("Complete D4-48 Evaluation for " + benchmark + ". Please see the log on remote server.");
	}

	static boolean nextNode = false;
	public static void nextCGNode() {
		nextNode = true;
	}

	static boolean notreach = true;
	public static void terminateEva() {
		notreach = false;
	}


	public static void incrementalTest(IPASSAPropagationCallGraphBuilder builder, CallGraph cg){
		Iterator<CGNode> iter2 = cg.iterator();
		HashSet<CGNode> storeCG = new HashSet<>();
		while(iter2.hasNext()){
			CGNode next = iter2.next();
			if(!next.getMethod().isSynthetic()){
				storeCG.add(next);
			}
		}

		boolean ptachanges = false;
		HashSet<CGNode> changedNodes = new HashSet<>();
		Set<ITIDEBug> bugs = new HashSet<>();
		for (CGNode n : storeCG) {
			if(!n.getMethod().getSignature().contains("com.ibm.wala")
					&& !n.getMethod().getSignature().contains(mainSignature)){
				builder.getSystem().setChange(true);
				IR ir = n.getIR();
				if(ir == null) {
					continue;
				}

				if(totaltime >= scheduletime)//900000  5400000
				{
					break;
				}

				DefUse du = new DefUse(ir);
				ConstraintVisitor v = builder.makeVisitor(n);
				v.setIR(ir);
				v.setDefUse(du);

				ControlFlowGraph<SSAInstruction, ISSABasicBlock> cfg = ir.getControlFlowGraph();
				SSAInstruction[] insts = ir.getInstructions();
				int size = insts.length;
				changedNodes.add(n);
				for(int i=size;i>0;i--){
					SSAInstruction inst = insts[i-1];

					if(inst==null) {
						continue;//skip null
					}

					ISSABasicBlock bb = cfg.getBlockForInstruction(inst.iIndex());
					//delete
					try{

						builder.setDelete(true);
						builder.getSystem().setFirstDel(true);
						v.setBasicBlock(bb);

						long delete_start_time = System.currentTimeMillis();
						inst.visit(v);
						//del
						builder.getSystem().setFirstDel(false);
						do{
							builder.getSystem().solveDel(null);
						}while(!builder.getSystem().emptyWorkList());
						builder.setDelete(false);
						HashSet<IVariable> resultsadd = builder.getSystem().changes;
						if(resultsadd.size() > 0){
							ptachanges = true;
						}else{
							ptachanges = false;
						}
						long delete_end_time = System.currentTimeMillis();
						long deldetect_start_time = System.currentTimeMillis();
						long ptadelete_time = (deldetect_start_time - delete_start_time);
						long deldetect_time = 0;
						if(!benchmark.contains("fop")){
							engine.setDelete(true);
							engine.updateEngineToEvaluate(changedNodes, ptachanges, inst, ps);
							engine.setDelete(false);
							deldetect_time = (delete_end_time - deldetect_start_time);
						}else{
							ps.print(0+" "+0+" ");
						}

						builder.getSystem().clearChanges();
						//add
						long add_start_time = System.currentTimeMillis();
						inst.visit(v);
						do{
							builder.getSystem().solveAdd(null);
							builder.addConstraintsFromNewNodes(null);
						} while (!builder.getSystem().emptyWorkList());

						HashSet<IVariable> resultsdel = builder.getSystem().changes;
						if(resultsdel.size() > 0){
							ptachanges = true;
						}else{
							ptachanges = false;
						}
						long adddetect_start_time = System.currentTimeMillis();
						long add_end_time = System.currentTimeMillis();
						long ptaadd_time = (adddetect_start_time-add_start_time);
						long adddetect_time = 0;
						if(!benchmark.contains("fop")){
							engine.updateEngine(changedNodes, new HashSet<>(), new HashSet<>(), ptachanges, ps);
							adddetect_time = (add_end_time - adddetect_start_time);
						}else{
							ps.print(0+" "+0+" ");
						}

						builder.getSystem().clearChanges();
						ps.print(ptadelete_time+" "+ptaadd_time+" ");
						//incre_race_time+" "+incre_dl_time+" "+ptadelete_time+" "+ptaadd_time+" "
						totaltime = totaltime + ptadelete_time + ptaadd_time + deldetect_time + adddetect_time;
					}catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				changedNodes.clear();
				ps.println();
			}
		}
		totaltime = 0;

		DataAnalyze analyze = new DataAnalyze();
		try {
			analyze.analyze(benchmark);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public static Iterable<Entrypoint> findEntryPoints(IClassHierarchy classHierarchy, String mainClassName, boolean includeAll) {
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
								System.out.println("+++++ " + method.toString());
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


	public static void print(String msg, boolean printErr){
		try{
			if(ps==null) {
				ps = new PrintStream(new FileOutputStream("log_" + benchmark));
			}

			ps.println(msg);

			if(printErr) {
				System.err.println(msg);
			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}



}
