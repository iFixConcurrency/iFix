package fix.iDebugger.handlers;


import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import java.io.IOException;
import java.util.List;

/**
 * @author ann
 */
public interface IDetction {
    void detect() throws IOException, ClassHierarchyException, CallGraphBuilderCancelException;
}
