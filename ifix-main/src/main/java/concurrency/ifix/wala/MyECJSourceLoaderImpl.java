package concurrency.ifix.wala;

import com.ibm.wala.cast.java.translator.SourceModuleTranslator;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJSourceLoaderImpl;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;

public class MyECJSourceLoaderImpl extends ECJSourceLoaderImpl {
    private final boolean dump;

    public MyECJSourceLoaderImpl(
            ClassLoaderReference loaderRef, IClassLoader parent, IClassHierarchy cha) {
        this(loaderRef, parent, cha, false);
    }

    public MyECJSourceLoaderImpl(
            ClassLoaderReference loaderRef, IClassLoader parent, IClassHierarchy cha, boolean dump) {
        super(loaderRef, parent, cha, dump);
        this.dump = false;
    }

    @Override
    protected SourceModuleTranslator getTranslator() {
        return new MyECJSourceModuleTranslator(cha.getScope(), this, dump);
    }
}

