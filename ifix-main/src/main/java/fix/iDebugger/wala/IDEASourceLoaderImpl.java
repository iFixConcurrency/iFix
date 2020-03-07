package fix.iDebugger.wala;

import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl;
import com.ibm.wala.cast.java.translator.SourceModuleTranslator;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;

public class IDEASourceLoaderImpl extends JavaSourceLoaderImpl {

    private boolean dump;

    public IDEASourceLoaderImpl(ClassLoaderReference loaderRef, IClassLoader parent, IClassHierarchy cha) {
        this(loaderRef, parent, cha, false);
    }

    public IDEASourceLoaderImpl(ClassLoaderReference loaderRef, IClassLoader parent, IClassHierarchy cha, boolean dump) {
        super(loaderRef, parent, cha);
        this.dump = dump;
    }

    @Override
    protected SourceModuleTranslator getTranslator() {
        return new IDEASourceModuleTranslator(this.cha.getScope(), this, this.dump);
    }
}
