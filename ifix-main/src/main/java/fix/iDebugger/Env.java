package fix.iDebugger;

import ch.qos.logback.classic.Level;
import org.eclipse.jdt.core.dom.AST;

/**
 * @author ann
 */
public class Env {
    public static String SOURCE_FOLDER =
            "";

    public static String[] CLASSPATH = {
            "/Library/Java/JavaVirtualMachines/jdk1.8.0_192.jdk/Contents/Home/jre/lib/rt.jar",
            ".",
            "/Library/Java/JavaVirtualMachines/jdk1.8.0_192.jdk/Contents/Home/lib/dt.jar",
            "/Library/Java/JavaVirtualMachines/jdk1.8.0_192.jdk/Contents/Home/lib/tools.jar",
    };

    public final static String TARGET_FOLDER =
            "";

    public final static Integer JAVA_VERSION = AST.JLS8;

    public static Level LOG_LEVEL = Level.OFF;
}
