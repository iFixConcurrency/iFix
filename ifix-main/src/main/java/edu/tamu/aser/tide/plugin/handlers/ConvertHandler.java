package edu.tamu.aser.tide.plugin.handlers;

import java.util.*;

import com.ibm.wala.types.Selector;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceType;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.ui.PartInitException;
//import org.eclipse.ui.handlers.HandlerUtil;
//import org.eclipse.swt.widgets.DirectoryDialog;
//import org.eclipse.swt.widgets.Shell;

import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.loader.AstClass;
import com.ibm.wala.classLoader.ClassLoaderImpl;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.strings.Atom;

import edu.tamu.aser.tide.engine.ITIDEBug;
import edu.tamu.aser.tide.engine.TIDECGModel;
//import edu.tamu.aser.tide.plugin.Activator;
import edu.tamu.aser.tide.plugin.ChangedItem;
//import edu.tamu.aser.tide.plugin.MyJavaElementChangeCollector;
//import edu.tamu.aser.tide.views.EchoDLView;
//import edu.tamu.aser.tide.views.EchoRaceView;
//import edu.tamu.aser.tide.views.EchoReadWriteView;
//import edu.tamu.aser.tide.views.fix.PFixView;
import edu.tamu.wala.increpta.callgraph.impl.IPAExplicitCallGraph.IPAExplicitNode;
import edu.tamu.wala.increpta.util.IPAAbstractFixedPointSolver;
import fix.iDebugger.handlers.FixHub;

public class ConvertHandler extends AbstractHandler {

	public static final String DESC_MAIN = "([Ljava/lang/String;)V";
////	public ExcludeView excludeView;
//	public EchoRaceView echoRaceView;
//	public EchoReadWriteView echoRWView;
//	public EchoDLView echoDLView;
//	private static String EXCLUSIONS =
//			//wala
////			"java\\/awt\\/.*\n" +
////			"javax\\/swing\\/.*\n" +
////			"sun\\/awt\\/.*\n" +
////			"sun\\/swing\\/.*\n" +
////			"com\\/sun\\/.*\n" +
////			"sun\\/.*\n" +
////			"org\\/netbeans\\/.*\n" +
////			"org\\/openide\\/.*\n" +
////			"com\\/ibm\\/crypto\\/.*\n" +
////			"com\\/ibm\\/security\\/.*\n" +
////			"org\\/apache\\/xerces\\/.*\n" +
////			"java\\/security\\/.*\n" + "";
//	//echo
//	"javax\\/.*\n" +
//	"java\\/.*\n" +
//	"sun\\/.*\n" +
//	"sunw\\/.*\n" +
//	"com\\/sun\\/.*\n" +
//	"com\\/ibm\\/.*\n" +
//	"com\\/apple\\/.*\n" +
//	"com\\/oracle\\/.*\n" +
//	"apple\\/.*\n" +
//	"org\\/xml\\/.*\n" +
//	"jdbm\\/.*\n" +
//	"";


	public ConvertHandler(){
		super();
//		Activator.getDefault().setCHandler(this);
	}

//	public EchoRaceView getEchoRaceView(){
//		return echoRaceView;
//	}

//	public PFixView getPFixView(){
//		return pFixView;
//	}
	
//	public EchoDLView getEchoDLView(){
//		return echoDLView;
//	}

//	public EchoReadWriteView getEchoReadWriteView(){
//		return echoRWView;
//	}

	private HashMap<IJavaProject,TIDECGModel> modelMap = new HashMap<IJavaProject,TIDECGModel>();
	private TIDECGModel currentModel;
	private IJavaProject currentProject;
//	private ClassLoaderImpl classloader;

	public HashSet<CGNode> changedNodes = new HashSet<>();
	public HashSet<CGNode> changedModifiers = new HashSet<>();
	public HashSet<CGNode> ignoreNodes = new HashSet<>();
	public HashSet<CGNode> considerNodes = new HashSet<>();
	public HashSet<CGNode> updateIRNodes = new HashSet<>();

	long start_time = System.currentTimeMillis();

	public TIDECGModel getCurrentModel(){
		return currentModel;
	}

	public IJavaProject getCurrentProject(){
		return currentProject;
	}

	/**
	 * consider ignored functions back
	 * @param javaProject
	 * @param file
	 * @param consider_method
	 */
	public void handleConsiderMethod(IJavaProject javaProject, IFile file, ChangedItem consider_method) {
		considerNodes.clear();
		TIDECGModel model = currentModel;//modelMap.get(javaProject);
		if(model == null) {
			return;
		}
		IClassHierarchy cha = model.getClassHierarchy();

		try{
			IClassLoader parent = cha.getLoader(ClassLoaderReference.Application);
			IClassLoader loader_old = cha.getLoader(JavaSourceAnalysisScope.SOURCE);

//			ClassLoaderImpl cl = new JDTSourceLoaderImpl(JavaSourceAnalysisScope.SOURCE, parent, cha);
			ClassLoaderImpl cl = null;
			List<Module> modules = new LinkedList<Module>();

//			modules.add(EclipseSourceFileModule.createEclipseSourceFileModule(file));
			cl.init(modules);
			Iterator<IClass> iter = cl.iterateAllClasses();
			while(iter.hasNext()){
				IClass javaClass = iter.next();
				String className = javaClass.getName().getClassName().toString();
				Atom apackage = javaClass.getName().getPackage();
				String apackage_s = null;
				if(apackage != null){
					apackage_s = apackage.toString().replace('/', '.');
				}

				if((apackage==null&&consider_method.packageName.isEmpty())
						||apackage!=null&&apackage_s.equals(consider_method.packageName)){

					if(className.contains(consider_method.className)){

						for(com.ibm.wala.classLoader.IMethod m : javaClass.getDeclaredMethods()){
							String mName = m.getName().toString();//JEFF TODO

							if(mName.equals(consider_method.methodName)){
								TypeName typeName = m.getDeclaringClass().getName();
								IClass class_old = loader_old.lookupClass(typeName);
								//									System.out.println("class_old hash: " + System.identityHashCode(class_old)) ;
								if(class_old==null) {
									return;//TODO: other kinds of changes
								}

								com.ibm.wala.classLoader.IMethod m_old = class_old.getMethod(m.getSelector());// cha.resolveMethod(m.getReference());
								CGNode node = model.getOldCGNode(m_old);
								considerNodes.add(node);
							}
						}
					}
				}
			}
			if(considerNodes.size() != 0){
				letUsConsider(file, model);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	/**
	 * ignore function/method
	 * @param ignore_method
	 */
	public void handleIgnoreMethod(IJavaProject javaProject, IFile file, ChangedItem ignore_method) {
		ignoreNodes.clear();
		TIDECGModel model = currentModel;//modelMap.get(javaProject);
		if(model == null) {
			return;
		}
		IClassHierarchy cha = model.getClassHierarchy();

		try{
			IClassLoader parent = cha.getLoader(ClassLoaderReference.Application);
			IClassLoader loader_old = cha.getLoader(JavaSourceAnalysisScope.SOURCE);

//			ClassLoaderImpl cl = new JDTSourceLoaderImpl(JavaSourceAnalysisScope.SOURCE, parent, cha);
			ClassLoaderImpl cl = null;
			List<Module> modules = new LinkedList<Module>();
//			modules.add(EclipseSourceFileModule.createEclipseSourceFileModule(file));
			cl.init(modules);
			Iterator<IClass> iter = cl.iterateAllClasses();
			while(iter.hasNext()){
				IClass javaClass = iter.next();
				String className = javaClass.getName().getClassName().toString();
				Atom apackage = javaClass.getName().getPackage();
				String apackage_s = null;
				if(apackage != null){
					apackage_s = apackage.toString().replace('/', '.');
				}

				if((apackage==null&&ignore_method.packageName.isEmpty())
						||apackage!=null&&apackage_s.equals(ignore_method.packageName)){

					if(className.contains(ignore_method.className)){

						for(com.ibm.wala.classLoader.IMethod m : javaClass.getDeclaredMethods()){
							String mName = m.getName().toString();

							if(mName.equals(ignore_method.methodName)){
								TypeName typeName = m.getDeclaringClass().getName();
								IClass class_old = loader_old.lookupClass(typeName);
								if(class_old == null) {
									return;//TODO: other kinds of changes
								}

								com.ibm.wala.classLoader.IMethod m_old = class_old.getMethod(m.getSelector());// cha.resolveMethod(m.getReference());
								CGNode node = model.getOldCGNode(m_old);
								ignoreNodes.add(node);
							}
						}
					}
				}
			}
			if(ignoreNodes.size() != 0){
				letUsIgnore(file, model);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	/**
	 * TriggerCheckHandler
	 * @param javaProject
	 * @param sigChanges
	 */
	public void handleMethodChanges(IJavaProject javaProject, HashMap<String, IFile> sigFiles,
			HashMap<String, ArrayList<ChangedItem>> sigChanges) {
		//see the changes
//		for (ArrayList<ChangedItem> changedItems : sigChanges.values()) {
//			for (ChangedItem changedItem : changedItems) {
//				System.out.println("CHANGED ITEM: " + changedItem.packageName + " " + changedItem.className + " " + changedItem.methodName);
//			}
//		}

		changedNodes.clear();
		changedModifiers.clear();

		TIDECGModel model = currentModel;//modelMap.get(javaProject);
		if(model == null) {
			return;
		}

		IFile file0 = null;
		Set<String> sigs = sigChanges.keySet();
		for (String sig : sigs) {
			IFile file = sigFiles.get(sig);
			if(file0 == null) {
				file0 = file;
			}
			ArrayList<ChangedItem> changedItems = sigChanges.get(sig);
			discoverChangedCGNodes(javaProject, file, changedItems);
		}

		boolean ptachanges = !IPAAbstractFixedPointSolver.changes.isEmpty();
		if(!changedNodes.isEmpty() || ptachanges || !changedModifiers.isEmpty()){//hChanges.isEmpty()
			//process further check when lock/start changes or pts changes
			letUsRockIncremental(javaProject, file0, model, ptachanges, true);
		}

	}


	/**
	 * handle program changes
	 * @param javaProject
	 * @param file
	 * @param changedItems
	 */
	public void handleMethodChange(IJavaProject javaProject, IFile file, ArrayList<ChangedItem> changedItems) {
		//see the changes
		for (Iterator iterator = changedItems.iterator(); iterator.hasNext();) {
			ChangedItem changedItem = (ChangedItem) iterator.next();
			System.out.println("CHANGED ITEM: " + changedItem.packageName + " " + changedItem.className + " " + changedItem.methodName);
		}

		changedNodes.clear();
		changedModifiers.clear();

		TIDECGModel model = currentModel;//modelMap.get(javaProject);
		if(model == null) {
			return;
		}

		discoverChangedCGNodes(javaProject, file, changedItems);

		boolean ptachanges = !IPAAbstractFixedPointSolver.changes.isEmpty();
		if(!changedNodes.isEmpty() || ptachanges || !changedModifiers.isEmpty()){//hChanges.isEmpty()
			//update cgnode for changed markers
			updateCGNodeInSameClass(model, file);
			//process further check when lock/start changes or pts changes
			letUsRockIncremental(javaProject, file, model, ptachanges, false);
		}

		try {
			Thread.currentThread().sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void updateCGNodeInSameClass(TIDECGModel model, IFile file) {
		updateIRNodes.clear();
		HashMap<String, HashSet<String>> class2Methods = model.getBugEngine().class2Methods;
		for (CGNode node : changedNodes) {
			String classname_tar = node.getMethod().getDeclaringClass().getName().getClassName().toString();
			HashSet<String> methods = class2Methods.get(classname_tar);
			String exclude = node.getMethod().getName().toString();
			for (String methodname_tar : methods) {
				if(methodname_tar.equals(exclude)){
					continue;
				}
				//match
				IClassHierarchy cha = model.getClassHierarchy();
				try{
					IClassLoader parent = cha.getLoader(ClassLoaderReference.Application);
					IClassLoader loader_old = cha.getLoader(JavaSourceAnalysisScope.SOURCE);
//					ClassLoaderImpl cl = new JDTSourceLoaderImpl(JavaSourceAnalysisScope.SOURCE, parent, cha);
					ClassLoaderImpl cl = null;
					Iterator<IClass> iter = cl.iterateAllClasses();
					while(iter.hasNext()){
						IClass javaClass = iter.next();
						String className = javaClass.getName().getClassName().toString();
						if(className.contains(classname_tar)){

							for(com.ibm.wala.classLoader.IMethod m : javaClass.getDeclaredMethods()){
								String mName = m.getName().toString();
								if(mName.equals(methodname_tar)){
									TypeName typeName = m.getDeclaringClass().getName();
									IClass class_old = loader_old.lookupClass(typeName);

									if(class_old==null) {
										return;//TODO: other kinds of changes
									}

									com.ibm.wala.classLoader.IMethod m_old = class_old.getMethod(m.getSelector());
									//save new method
									if(class_old instanceof AstClass){
										AstClass ast_old = (AstClass) class_old;
										Map<Selector, IMethod> declaredMethods = (Map<Selector, IMethod>)ast_old.getDeclaredMethods();

										updateMethod(ast_old, m.getSelector(), m);
//										ast_old.updateMethod(m.getSelector(), m);
									}

//									IR ir_old = model.getCache().getSSACache().findOrCreateIR(m_old, Everywhere.EVERYWHERE, model.getOptions().getSSAOptions());
									IR ir = model.getCache().getSSACache().findOrCreateIR(m, Everywhere.EVERYWHERE, model.getOptions().getSSAOptions());
									DefUse du = model.getCache().getSSACache().findOrCreateDU(ir, Everywhere.EVERYWHERE);

									System.out.println(m_old.hashCode());
									CGNode cgnode = model.getOldCGNode(m_old);
									if(cgnode instanceof IPAExplicitNode){
										IPAExplicitNode astCGNode = (IPAExplicitNode) cgnode;
										astCGNode.updateMethod(m, ir);
										updateIRNodes.add(cgnode);
									}
									System.err.println("Updated Item: " + classname_tar + " " + methodname_tar);
								}
							}
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	private void discoverChangedCGNodes(IJavaProject javaProject, IFile file, ArrayList<ChangedItem> changedItems ){
		TIDECGModel model = currentModel;//modelMap.get(javaProject);
		if(model == null) {
			return;
		}

		ArrayList<ChangedItem> furItems = new ArrayList<>();
		IClassHierarchy cha = model.getClassHierarchy();

		try{
			IClassLoader parent = cha.getLoader(ClassLoaderReference.Application);
			IClassLoader loader_old = cha.getLoader(JavaSourceAnalysisScope.SOURCE);

//			ClassLoaderImpl cl = new JDTSourceLoaderImpl(JavaSourceAnalysisScope.SOURCE, parent, cha);
			ClassLoaderImpl cl = null;
			List<Module> modules = new LinkedList<Module>();
//			modules.add(EclipseSourceFileModule.createEclipseSourceFileModule(file));
			cl.init(modules);
			boolean onlyModifier = false;
			boolean modifier = false;

			Iterator<IClass> iter = cl.iterateAllClasses();
			while(iter.hasNext()){
				IClass javaClass = iter.next();
				String className = javaClass.getName().getClassName().toString();
				Atom apackage = javaClass.getName().getPackage();
				String apackage_s = null;
				if(apackage != null){
					apackage_s = apackage.toString().replace('/', '.');
				}
//				//list all involved packages/class/method
//				for(com.ibm.wala.classLoader.IMethod m : javaClass.getDeclaredMethods()){
//					String mName = m.getName().toString();
//					if(apackage_s!= null)
//						System.out.println(apackage_s + " " + className + " " + mName);
//				}


				for (Iterator<ChangedItem> iterator = changedItems.iterator(); iterator.hasNext();) {
					ChangedItem changedItem = (ChangedItem) iterator.next();

					if((apackage==null&&changedItem.packageName.isEmpty())
							||apackage!=null&&apackage_s.equals(changedItem.packageName)){

						if(className.contains(changedItem.className)){

							for(com.ibm.wala.classLoader.IMethod m : javaClass.getDeclaredMethods()){
								String mName = m.getName().toString();
								if(mName.equals(changedItem.methodName)){
									TypeName typeName = m.getDeclaringClass().getName();
									IClass class_old = loader_old.lookupClass(typeName);

									if(class_old==null) {
										return;//TODO: other kinds of changes
									}

									com.ibm.wala.classLoader.IMethod m_old = class_old.getMethod(m.getSelector());// cha.resolveMethod(m.getReference());
									//save new method
									if(class_old instanceof AstClass){
										AstClass ast_old = (AstClass) class_old;
										updateMethod(ast_old, m.getSelector(), m);
									}

									IR ir_old = model.getCache().getSSACache().findOrCreateIR(m_old, Everywhere.EVERYWHERE, model.getOptions().getSSAOptions());
									IR ir = model.getCache().getSSACache().findOrCreateIR(m, Everywhere.EVERYWHERE, model.getOptions().getSSAOptions());
									DefUse du = model.getCache().getSSACache().findOrCreateDU(ir, Everywhere.EVERYWHERE);

									if(compareIRs(ir_old, ir, furItems)){//only compare the inner ir
										if(m.isSynchronized() == m_old.isSynchronized()){
											//changes are not in this ir, should be in hidden new body,
											//e.g. see sunflow/LigherServer/calculatePhotonse:  new thread/runnable.
											continue;
										}else{
											//only modifer changes
											onlyModifier = true;
										}
									}else if(m.isSynchronized() != m_old.isSynchronized()){
										//modifer may change and ir may also change
										modifier = true;
									}
									CGNode node = model.getOldCGNode(m_old);
									model.updatePointerAnalysis(node,ir_old,ir);
									node = model.updateCallGraph(m_old,m,ir,du);
									if(onlyModifier){
										changedModifiers.add(node);
									}else{
										changedNodes.add(node);
										if(modifier){
											changedModifiers.add(node);
										}
									}
									onlyModifier = false;
									modifier = false;
									System.out.println();
//									System.err.println("Changed Item: " + changedItem.packageName + " " + changedItem.className + " " + changedItem.methodName);
								}
							}
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		if(!furItems.isEmpty()){
			discoverChangedCGNodes(javaProject, file, furItems);
		}
	}

	private boolean compareIRs(IR ir_old, IR ir, ArrayList<ChangedItem> furItems) {
		SSAInstruction[] insts_old = ir_old.getInstructions();
		SSAInstruction[] insts = ir.getInstructions();
		if(insts.length == insts_old.length){
			int size = insts.length;
			for(int i=0; i<size; i++){
				SSAInstruction old = insts_old[i];
				SSAInstruction newI = insts[i];
				if(old != null && newI != null){
					if(old.toString().equals(newI.toString())){
						//changes are not in this ir, should be in hidden new body,
						if(newI instanceof SSAAbstractInvokeInstruction){
							SSAAbstractInvokeInstruction invoke = (SSAAbstractInvokeInstruction) newI;
							String cname = invoke.getCallSite().getDeclaredTarget().getDeclaringClass().getName().getClassName().toString();
							if(cname.contains("anonymous subclass of java.lang.Object")){
								ChangedItem item = new ChangedItem();
								item.packageName = invoke.getCallSite().getDeclaredTarget().getDeclaringClass().getName().getPackage().toString().replace('/', '.');
								item.methodName = "run";
								item.className = cname;
								if(!furItems.contains(item)) {
									furItems.add(item);
								}
							}
						}
						continue;
					}else{
						return false;
					}
				}else if(old == null && newI == null){
					continue;
				}else {
					return false;
				}
			}
			return true;
		}else {
			return false;
		}
	}



	private int num_of_detection = 0;

	private void letUsRock(IJavaProject javaProject, final IFile file, final TIDECGModel model){
		num_of_detection = 1;
		//TODO: fork a new Thread to do this
		new Thread(new Runnable(){
			@Override
			public void run() {
				//Detect Bugs
				if(num_of_detection == 1){
					//initial
					System.err.println("INITIAL DETECTION >>>");
					model.detectBug();
				}else{
					System.out.println("wrong call");
				}
				//update UI
				model.updateGUI(javaProject, file, true);
				FixHub.d4DetectionTime = System.currentTimeMillis() - start_time;
				System.err.println("Total Time: "+(System.currentTimeMillis()-start_time));
				long startFix = System.currentTimeMillis();
				FixHub fixHub = FixHub.getInstance();
				fixHub.clear();
				
				if(fixHub.handleFix()){
					fixHub.addMarker(javaProject, file);
//					fixHub.initialGUI();
					FixHub.policyConjectureTime = System.currentTimeMillis() - startFix;
					System.err.println("policy conjecture cost : " + FixHub.policyConjectureTime);
				}
				else{
					System.err.println("failed to handle fix");
				}
			}
		}).start();
	}

	private void letUsRockIncremental(IJavaProject javaProject, final IFile file, final TIDECGModel model, boolean ptachanges, boolean trigger){
		num_of_detection++;
		//TODO: fork a new Thread to do this
		new Thread(new Runnable(){
			@Override
			public void run() {
				start_time = System.currentTimeMillis();
				HashSet<ITIDEBug> bugs = new HashSet<ITIDEBug>();
				//Detect Bugs
				if(num_of_detection == 1){
					System.out.println("wrong call inc");
				}else{
					//incremental
					System.err.println("DETECTION AGAIN >>>");
					model.detectBugAgain(changedNodes, changedModifiers, updateIRNodes, ptachanges);
				}
				//clear pta changes
				model.clearChanges();
				//update UI
				model.updateGUI(javaProject, file, false);
				if(trigger){
//					Activator.getDefault().getDefaultCollector().resetCollectedChanges();
				}
				System.err.println("\nIncremental Time (exclude update Eclipse views): "+(System.currentTimeMillis()-start_time));
				System.out.println();
			}
		}).start();
	}

	private void letUsIgnore(IFile file, TIDECGModel model) {
		if(num_of_detection <= 0){
			return;
		}
		new Thread(new Runnable(){
			@Override
			public void run() {
				model.ignoreCGNodes(ignoreNodes);
				//update UI
				//remove the marker from editor
				model.removeBugMarkersForIgnore(model.getBugEngine().removedraces);
				//remove the bug from echoview
				model.updateEchoViewForIgnore();
				System.err.println("Total Time: "+(System.currentTimeMillis()-start_time));
			}
		}).start();
	}



	private void letUsConsider(IFile file, TIDECGModel model) {
		if(num_of_detection <= 0){
			return;
		}
		new Thread(new Runnable(){
			@Override
			public void run() {
				model.considerCGNodes(considerNodes);
				//update UI
				//remove the marker from editor
				try {
					model.addBugMarkersForConsider(null, file);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				//remove the bug from echoview
				model.updateEchoViewForConsider();
				System.err.println("Total Time: "+(System.currentTimeMillis()-start_time));
			}
		}).start();
	}


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
//		start_time = System.currentTimeMillis();
//		ISelection sel = HandlerUtil.getActiveMenuSelection(event);
//		IStructuredSelection selection = (IStructuredSelection) sel;
//		Object firstElement = selection.getFirstElement();
//		if (firstElement instanceof ICompilationUnit) {
//			ICompilationUnit cu = (ICompilationUnit) firstElement;
//			if(hasMain(cu)){
//				test(cu, selection);//initial
//			}
//		}
		return null;
	}


	public void test(ICompilationUnit cu){
		try{
			IJavaProject javaProject = cu.getJavaProject();
			String mainSig = getSignature(cu);
			//excluded in text file
//			TIDECGModel model = new TIDECGModel(javaProject, "EclipseDefaultExclusions.txt", mainSig);
			//excluded in String
//			String deafaultDefined = excludeView.getDefaultText();
//			String userDefined = excludeView.getChangedText();
//			String new_exclusions = null;
//			if(userDefined.length() > 0){
//	            //append new added in excludeview
//				StringBuilder stringBuilder = new StringBuilder();
//				stringBuilder.append(deafaultDefined);
//				stringBuilder.append(userDefined);
//				new_exclusions = stringBuilder.toString();//combined
//				//write back to EclipseDefaultExclusions.txt
//				java.io.File file = new java.io.File("~/Documents/Eclipse2/Test_both_copy/edu.tamu.cse.aser.echo/data/EclipseDefaultExclusions.txt");
//				FileWriter fileWriter = new FileWriter(file, false);
//				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
//				bufferedWriter.write(new_exclusions);
//				bufferedWriter.close();
//			}
			TIDECGModel model = new TIDECGModel(javaProject, "EclipseDefaultExclusions.txt", mainSig);
			model.buildGraph();
			System.err.println("Call Graph Construction Time: "+(System.currentTimeMillis()-start_time));
			modelMap.put(javaProject, model);
//			excludeView.setProgramInfo(cu, selection);
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(cu.getPath());
			//set echoview to menuhandler
//			echoRaceView = model.getEchoRaceView();
////			pFixView = model.getPFixView();
//			echoRWView = model.getEchoRWView();
//			echoDLView = model.getEchoDLView();
			//set current model
			currentModel = model;
			currentProject = javaProject;
			//concurrent
			letUsRock(javaProject, file, model);
//			Activator.getDefaultReporter().initialSubtree(cu, selection, javaProject);
			//for trigger button
//			MyJavaElementChangeCollector collector = Activator.getDefault().getDefaultCollector();
//			collector.resetCollectedChanges();
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	private boolean hasMain(ICompilationUnit cu){
		try{
			for(IJavaElement e: cu.getChildren()){
				if(e instanceof SourceType){
					SourceType st = (SourceType)e;
					for (IMethod m: st.getMethods()) {
						if((m.getFlags()&Flags.AccStatic)>0
								&&(m.getFlags()&Flags.AccPublic)>0
								&&m.getElementName().equals("main")
								&&m.getSignature().equals("([QString;)V")){
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}


	private String getSignature(ICompilationUnit cu){
		try {
			String name = cu.getElementName();
			int index = name.indexOf(".java");
			name = name.substring(0,index);
			for(IType t :cu.getTypes()){
				String tName = t.getElementName();
				if(name.equals(tName)){
					return t.getFullyQualifiedName()+".main"+DESC_MAIN;
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return "";
	}

	public void updateMethod(AstClass astClass, Selector selector, com.ibm.wala.classLoader.IMethod method){
		Map<Selector, com.ibm.wala.classLoader.IMethod> methods = (Map<Selector, com.ibm.wala.classLoader.IMethod>) astClass.getDeclaredMethods();
		if(methods.containsKey(selector)){
			methods.put(selector, method);
		}else if (astClass.getSuperclass() != null){
			if(astClass.getSuperclass() instanceof AstClass){
				updateMethod(((AstClass) astClass.getSuperclass()), selector, method);
			}
		}
	}
}
