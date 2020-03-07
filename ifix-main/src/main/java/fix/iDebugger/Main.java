package fix.iDebugger;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.SourceType;
import org.slf4j.LoggerFactory;
import concurrency.ifix.entity.Pair;
import concurrency.ifix.env.Env;
import concurrency.ifix.util.ASTUtil;

import java.io.File;
import java.util.List;

public class Main {
    public static Logger logger = (Logger) LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        long start_time = System.currentTimeMillis();
        File root = new File(Env.SOURCE_FOLDER);
        List<Pair<String, CompilationUnit>> cUnitList = ASTUtil.parseFiles(root);
        CompilationUnit cu = null;
        for(Pair<String, CompilationUnit> pair : cUnitList){
            if(pair.getV1().equals(Env.MAIN_PATH)){
                cu = pair.getV2();
            }
        }
        if(cu == null){
            logger.error("Main path is not found, program terminate.");
        }
        else{
//            test(cu, selection);
        }
    }

    private boolean hasMain(ICompilationUnit cu){
        try{
            for(IJavaElement e: cu.getChildren()){
                if(e instanceof SourceType){
                    SourceType st = (SourceType)e;
                    for (IMethod m: st.getMethods()) {
                        if ((m.getFlags() & Flags.AccStatic) > 0
                                && (m.getFlags() & Flags.AccPublic) > 0
                                && m.getElementName().equals("main")
                                && m.getSignature().equals("([QString;)V")) {
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
}
