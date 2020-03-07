package concurrency.ifix.ast;

import ch.qos.logback.classic.Logger;
import concurrency.ifix.entity.Pair;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ann
 */
public class CompilationUnitASTRequestor extends FileASTRequestor {
    private static Logger logger = (Logger) LoggerFactory.getLogger(CompilationUnitASTRequestor.class);
    private List<Pair<String, CompilationUnit>> fileList = new ArrayList<>();

    public List<Pair<String, CompilationUnit>> getFileList() {
        return fileList;
    }

    @Override
    public void acceptAST(String sourceFilePath, CompilationUnit ast) {
        fileList.add(Pair.make(sourceFilePath, ast));
        super.acceptAST(sourceFilePath, ast);
    }
}
