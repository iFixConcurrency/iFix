package concurrency.ifix.util;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.LoggerFactory;
import concurrency.ifix.ast.CompilationUnitASTRequestor;
import concurrency.ifix.entity.Pair;
import concurrency.ifix.env.Env;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * @author ann
 */
public class ASTUtil {
    private static Logger logger = (Logger) LoggerFactory.getLogger(ASTUtil.class);
    static {
        logger.setLevel(Env.LOG_LEVEL);
    }
    public static Map<String, CompilationUnit> cpMap = new HashMap<>();

    /**
     * Parse a file to compilation unit.
     *
     * @param file
     * @return
     */
    public static CompilationUnit getCompilationUnit(File file, ASTParser tParser){
        if(cpMap.containsKey(file.getAbsolutePath())){
            return cpMap.get(file.getAbsolutePath());
        }
        ASTParser parser;
        if(tParser == null){
            parser = ASTParser.newParser(Env.JAVA_VERSION);
            parser.setResolveBindings(true);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            parser.setBindingsRecovery(true);

            parser.setUnitName(FileUtil.removeSuffix(file.getName()));

            String[] sources = { Env.SOURCE_FOLDER };
            String[] classpath = Env.CLASSPATH;

            parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);

        }
        else{
            parser = tParser;
        }
        parser.setSource(FileUtil.getFileContents(file));
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
//        parser.createASTs();
        return cu;
    }

    public static List<Pair<String, CompilationUnit>> parseFiles(File root){
        ASTParser parser;
        parser = ASTParser.newParser(Env.JAVA_VERSION);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);

        String[] sources = { Env.SOURCE_FOLDER };
        String[] classpath = Env.CLASSPATH;

        parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
        List<File> files = FileUtil.getFiles(root, new String[]{"java"});
        String[] paths = new String[files.size()];
        for(int i = 0; i < files.size(); i ++){
            paths[i] = files.get(i).getAbsolutePath();
        }
        String[] srcEncodings = new String[paths.length];
        Charset charset = Charset.defaultCharset();
        for (int i = 0; i < srcEncodings.length; i++) {
            srcEncodings[i] = charset.name();
        }
        CompilationUnitASTRequestor requestor = new CompilationUnitASTRequestor();
        parser.createASTs(paths, srcEncodings, new String[]{}, requestor, null);
        for(Pair<String, CompilationUnit> pair : requestor.getFileList()){
            cpMap.put(pair.getV1(), pair.getV2());
        }
        return requestor.getFileList();
    }

    public static void clearMap(){
        cpMap.clear();
    }

    /**
     * get AST node, expression statement, of read write lock
     * example :
     *      lockName.readLock().lock();
     *      lockName.writeLock().unlock;
     * @param lockName name of lock variable
     * @param isRead true for readLock, false for writeLock
     * @param isLock true for lock operation, false for unLock operation
     * @return write lock expression statement, AST node
     */
    public static ExpressionStatement getReadWriteLockExpression(String lockName, boolean isRead, boolean isLock){
        ASTParser parser = ASTParser.newParser(Env.JAVA_VERSION);
        String lockType = isRead ? "readLock()" : "writeLock()";
        String lockProperty = isLock ? "lock()" : "unlock()";
        parser.setSource((lockName + "." + lockType + "." + lockProperty + ";").toCharArray());
        parser.setKind(ASTParser.K_STATEMENTS);
        ExpressionStatement statement = (ExpressionStatement) ((Block)parser.createAST(null)).statements().get(0);
        return statement;
    }

    public static ExpressionStatement getExclusiveLockExpression(String lockName, boolean isLock){
        ASTParser parser = ASTParser.newParser(Env.JAVA_VERSION);
        String lockProperty = isLock ? "lock()" : "unlock()";
        parser.setSource((lockName + "." + lockProperty + ";").toCharArray());
        parser.setKind(ASTParser.K_STATEMENTS);
        ExpressionStatement statement = (ExpressionStatement) ((Block)parser.createAST(null)).statements().get(0);
        return statement;
    }

    /**
     * get field declaration, ast node
     * example :
     *          public Object a = new Object();
     * @param varType object type
     * @param varName variable name
     * @return ast node
     */
    public static FieldDeclaration getVarDeclaration(String varType, String varName, boolean isStatic){
        ASTParser parser = ASTParser.newParser(Env.JAVA_VERSION);
        parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
        String modifier = isStatic ? " static " : " ";
        parser.setSource(("public" + modifier + varType + " " + varName + " = new " + varType + "();").toCharArray());
        TypeDeclaration newLockBlock = (TypeDeclaration) parser.createAST(null);
        return newLockBlock.getFields()[0];
    }

    public static boolean addLockDeclaration(ASTNode node, String lockName, boolean isStatic){
        ASTNode parent = node;
        while(parent != null && parent.getNodeType() != ASTNode.TYPE_DECLARATION){
            parent = parent.getParent();
        }
        if(parent == null){
            logger.debug("node has no ancestor typed type declaration");
            return false;
        }

        TypeDeclaration typeDeclaration = (TypeDeclaration) parent;
        FieldDeclaration fieldDeclaration = getVarDeclaration("ReentrantReadWriteLock", lockName, isStatic);
        fieldDeclaration = (FieldDeclaration) (ASTNode.copySubtree(typeDeclaration.getAST(), fieldDeclaration));
        typeDeclaration.bodyDeclarations().add(0, fieldDeclaration);
        return true;
    }

    public static String format(String code) {
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put( JavaCore.COMPILER_SOURCE, "1.5");
        hashMap.put( JavaCore.COMPILER_COMPLIANCE, "1.5");
        hashMap.put( JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, "1.5");
        CodeFormatter formatter = ToolFactory.createCodeFormatter(hashMap);

        TextEdit edit = formatter.format(CodeFormatter.K_COMPILATION_UNIT, code, 0, code.length(), 0, null);
        if (edit == null) {
            return code;
        }
        IDocument doc = new Document();
        doc.set(code);
        try {
            edit.apply(doc);
        } catch (Exception e) {
            e.printStackTrace();
            return code;
        }
        return doc.get();
    }

    public static boolean addPermissionAnnotation(MethodDeclaration node, String prePermission, String postPermission){
        if("".equals(prePermission.trim())){
            prePermission = "no permission";
        }

        if ("".equals(postPermission.trim())) {
            postPermission = "no permission";
        }

        ASTParser parser = ASTParser.newParser(Env.JAVA_VERSION);
        parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);

        parser.setSource(("@Perm(requires=\"" + prePermission  +" in alive\", \n" +
                "ensures=\"" + postPermission +" in alive\")\n" +
                "void func() {}\n").toCharArray());

        TypeDeclaration typeDeclaration = (TypeDeclaration) parser.createAST(null);

        NormalAnnotation annotation = (NormalAnnotation) typeDeclaration.getMethods()[0].modifiers().get(0);
        annotation = (NormalAnnotation) ASTNode.copySubtree(node.getAST(), annotation);
        node.modifiers().add(0, annotation);
        return true;
    }

    /**
     *
     * @param block block to add tmp var
     * @param index index of return statement
     * @param varName tmp var name
     * @return true if replace, false if return type is void
     */
    public static boolean addTmpVarForReturnStatement(Block block, int index, String varName){
        ReturnStatement returnStatement = (ReturnStatement) block.statements().get(index);
        // 1 get return type
        ASTNode clazz = returnStatement.getParent();
        while (clazz != null && clazz.getNodeType() != ASTNode.METHOD_DECLARATION){
            clazz = clazz.getParent();
        }
        MethodDeclaration tmp = (MethodDeclaration) clazz;
        Type returnType = (Type) tmp.getReturnType2();
        if(returnType instanceof PrimitiveType && ((PrimitiveType)returnType).getPrimitiveTypeCode() == PrimitiveType.VOID){
            return false;
        }
        else{
            ASTParser parser = ASTParser.newParser(Env.JAVA_VERSION);
            parser.setSource(("boolean " + varName + " = a;").toCharArray());
            parser.setKind(ASTParser.K_STATEMENTS);
            VariableDeclarationStatement statement = (VariableDeclarationStatement) ((Block)parser.createAST(null)).statements().get(0);
            statement.setType((Type)ASTNode.copySubtree(statement.getAST(), returnType));
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
            fragment.setInitializer((Expression) ASTNode.copySubtree(fragment.getAST(), returnStatement.getExpression()) );
            returnStatement.setExpression(returnStatement.getAST().newSimpleName("tmpVar"));
            block.statements().add(index, ASTNode.copySubtree(block.getAST(), statement));
        }
        return true;
    }

    public static ImportDeclaration getImportDeclaration(String importName){
        ASTParser parser = ASTParser.newParser(Env.JAVA_VERSION);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(("import " + importName + ";").toCharArray());
        CompilationUnit node = (CompilationUnit)parser.createAST(null);
        return (ImportDeclaration) node.imports().get(0);
    }

    public static PackageDeclaration getPackageDeclaration(String packageName){
        ASTParser parser = ASTParser.newParser(Env.JAVA_VERSION);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(("package " + packageName + ";").toCharArray());
        CompilationUnit node = (CompilationUnit)parser.createAST(null);
        return node.getPackage();
    }

    public static String[] getParametersTypeString(IMethodBinding binding){
        if(binding == null || binding.getParameterTypes() == null){
            return new String[0];
        }
        String[] res = new String[binding.getParameterTypes().length];
        for(int i = 0; i < binding.getParameterTypes().length; i ++){
            res[i] = binding.getParameterTypes()[i].getName();
        }
        return res;
    }

    public static void changeSync(File sourceFile) throws IOException {
        if(sourceFile.isDirectory()) {
            for (File tFile : sourceFile.listFiles()) {
                changeSync(tFile);
            }
            return;
        }
        String s = new String(Files.readAllBytes(sourceFile.toPath()), StandardCharsets.UTF_8);
        Document document = new Document(s);
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(s.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        cu.accept(new ASTVisitor() {
            ASTRewrite rewrite = null;
            ListRewrite listRewrite = null;
            List<String> infoMsg = new ArrayList<>();

            String className = "";

            @Override
            public boolean visit(TypeDeclaration node) {
                className = cu.getPackage().getName().getFullyQualifiedName() + "." + node.getName().toString();
                return super.visit(node);
            }

            @Override
            public boolean visit(MethodDeclaration node) {
                if(Modifier.isSynchronized(node.getModifiers())){
                    if(rewrite == null){
                        rewrite = ASTRewrite.create(cu.getAST());
                    }
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
                    infoMsg.add(className + "." + node.getName().toString());
                }
                return super.visit(node);
            }

            @Override
            public void endVisit(CompilationUnit node) {
                if(rewrite != null){
                    try{
                        TextEdit edit = rewrite.rewriteAST(document, null);
                        edit.apply(document);
                        FileUtil.writeToFile(sourceFile, document.get());
                        for(String s : infoMsg){
                            logger.info("sync changed : " + s);
                        }
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
