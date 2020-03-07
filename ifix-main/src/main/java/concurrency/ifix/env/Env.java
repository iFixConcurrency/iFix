package concurrency.ifix.env;

import ch.qos.logback.classic.Level;
import org.eclipse.jdt.core.dom.AST;

/**
 * @author ann
 */
public class Env {
    static{
        int a = 1, b = 2;
        a = a + b;
    }

    public static String SOURCE_FOLDER = "";

    public static String MAIN_PATH = "";

    public static String[] CLASSPATH = {
    };

    public final static String TARGET_FOLDER = "";

    public final static Integer JAVA_VERSION = AST.JLS8;

    public static Level LOG_LEVEL = Level.OFF;
}
