package concurrency.ifix.wala;

import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.SetOfClasses;

public class MyECJClassLoaderFactory extends ECJClassLoaderFactory {

    public MyECJClassLoaderFactory(SetOfClasses exclusions) {
        super(exclusions);
    }

    @Override
    protected JavaSourceLoaderImpl makeSourceLoader(
            ClassLoaderReference classLoaderReference, IClassHierarchy cha, IClassLoader parent) {
        return new MyECJSourceLoaderImpl(classLoaderReference, parent, cha, false);
    }
}
