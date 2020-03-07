package fix.iDebugger.wala;

import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl;
import com.ibm.wala.classLoader.ClassLoaderFactoryImpl;
import com.ibm.wala.classLoader.ClassLoaderImpl;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.SetOfClasses;

import java.io.IOException;

public class IDEAClassLoaderFactory extends ClassLoaderFactoryImpl {
    protected boolean dump;

    public IDEAClassLoaderFactory(SetOfClasses exclusions) {
        this(exclusions, false);
    }

    public IDEAClassLoaderFactory(SetOfClasses exclusions, boolean dump) {
        super(exclusions);
        this.dump = dump;
    }

    @Override
    protected IClassLoader makeNewClassLoader(ClassLoaderReference classLoaderReference, IClassHierarchy cha, IClassLoader parent, AnalysisScope scope) throws IOException {
        if (classLoaderReference.equals(JavaSourceAnalysisScope.SOURCE)) {
            ClassLoaderImpl cl = this.makeSourceLoader(classLoaderReference, cha, parent);
            cl.init(scope.getModules(classLoaderReference));
            return cl;
        } else {
            return super.makeNewClassLoader(classLoaderReference, cha, parent, scope);
        }
    }

    protected JavaSourceLoaderImpl makeSourceLoader(ClassLoaderReference classLoaderReference, IClassHierarchy cha, IClassLoader parent) {
        return new IDEASourceLoaderImpl(classLoaderReference, parent, cha, this.dump);
    }
}
