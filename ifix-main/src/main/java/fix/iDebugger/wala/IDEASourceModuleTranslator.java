package fix.iDebugger.wala;

import com.ibm.wala.cast.java.translator.Java2IRTranslator;
import com.ibm.wala.cast.java.translator.SourceModuleTranslator;
import com.ibm.wala.cast.java.translator.jdt.JDTJava2CAstTranslator;
import com.ibm.wala.classLoader.DirectoryTreeModule;
import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.classLoader.ModuleEntry;
import com.ibm.wala.ide.classloader.EclipseSourceFileModule;
import com.ibm.wala.ide.util.JdtPosition;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.SetOfClasses;
import com.ibm.wala.util.debug.Assertions;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IDEASourceModuleTranslator implements SourceModuleTranslator {
    private final class JdtAstToIR extends ASTRequestor {
        private final Map.Entry<IProject, Map<ICompilationUnit, EclipseSourceFileModule>> proj;

        private JdtAstToIR(Map.Entry<IProject, Map<ICompilationUnit, EclipseSourceFileModule>> proj) {
            this.proj = proj;
        }

        @Override
        public void acceptAST(ICompilationUnit source, CompilationUnit ast) {

            try {
                JDTJava2CAstTranslator<JdtPosition> jdt2cast =
                        makeCAstTranslator(
                                ast,
                                proj.getValue().get(source).getIFile(),
                                source.getUnderlyingResource().getLocation().toOSString());
                final Java2IRTranslator java2ir = makeIRTranslator();
                java2ir.translate(proj.getValue().get(source), jdt2cast.translateToCAst());
            } catch (JavaModelException e) {
                e.printStackTrace();
            }

            if (!"true".equals(System.getProperty("wala.jdt.quiet"))) {
                IProblem[] problems = ast.getProblems();
                int length = problems.length;
                if (length > 0) {
                    StringBuilder buffer = new StringBuilder();
                    for (IProblem problem : problems) {
                        buffer.append(problem.getMessage());
                        buffer.append('\n');
                    }
                    if (length != 0) {
                        System.err.println("Unexpected problems in " + source.getElementName() + buffer);
                    }
                }
            }
        }
    }

    protected boolean dump;
    protected IDEASourceLoaderImpl sourceLoader;
    private SetOfClasses exclusions;

    public IDEASourceModuleTranslator(AnalysisScope scope, IDEASourceLoaderImpl sourceLoader) {
        this(scope, sourceLoader, false);
    }

    public IDEASourceModuleTranslator(
            AnalysisScope scope, IDEASourceLoaderImpl sourceLoader, boolean dump) {
        computeClassPath(scope);
        this.sourceLoader = sourceLoader;
        this.dump = dump;
        this.exclusions = scope.getExclusions();
    }

    private static void computeClassPath(AnalysisScope scope) {
        StringBuilder buf = new StringBuilder();

        ClassLoaderReference cl = scope.getApplicationLoader();

        while (cl != null) {
            List<Module> modules = scope.getModules(cl);

            for (Module m : modules) {
                if (buf.length() > 0) {
                    buf.append(File.pathSeparator);
                }
                if (m instanceof JarFileModule) {
                    JarFileModule jarFileModule = (JarFileModule) m;

                    buf.append(jarFileModule.getAbsolutePath());
                } else if (m instanceof DirectoryTreeModule) {
                    DirectoryTreeModule directoryTreeModule = (DirectoryTreeModule) m;

                    buf.append(directoryTreeModule.getPath());
                } else {
                    Assertions.UNREACHABLE("Module entry is neither jar file nor directory");
                }
            }
            cl = cl.getParent();
        }
    }

    /*
     * Project -> AST code from org.eclipse.jdt.core.tests.performance
     */

    @Override
    public void loadAllSources(Set<ModuleEntry> modules) {
        // TODO: we might need one AST (-> "Object" class) for all files.
        // TODO: group by project and send 'em in

        // sort files into projects
        Map<IProject, Map<ICompilationUnit, EclipseSourceFileModule>> projectsFiles = new HashMap<>();
        for (ModuleEntry m : modules) {
            assert m instanceof EclipseSourceFileModule
                    : "Expecing EclipseSourceFileModule, not " + m.getClass();
            EclipseSourceFileModule entry = (EclipseSourceFileModule) m;
            IProject proj = entry.getIFile().getProject();
            if (!projectsFiles.containsKey(proj)) {
                projectsFiles.put(proj, new HashMap<>());
            }
            projectsFiles.get(proj).put(JavaCore.createCompilationUnitFrom(entry.getIFile()), entry);
        }

        @SuppressWarnings("deprecation")
        final ASTParser parser = ASTParser.newParser(AST.JLS8);

        for (final Map.Entry<IProject, Map<ICompilationUnit, EclipseSourceFileModule>> proj :
                projectsFiles.entrySet()) {
            parser.setProject(JavaCore.create(proj.getKey()));
            parser.setResolveBindings(true);

            Set<ICompilationUnit> units = proj.getValue().keySet();
            parser.createASTs(
                    units.toArray(new ICompilationUnit[0]), new String[0], new IDEASourceModuleTranslator.JdtAstToIR(proj), null);
        }
    }

    protected Java2IRTranslator makeIRTranslator() {
        return new Java2IRTranslator(sourceLoader, exclusions);
    }

    protected JDTJava2CAstTranslator<JdtPosition> makeCAstTranslator(
            CompilationUnit cu, final IFile sourceFile, String fullPath) {
        return new JDTJava2CAstTranslator<JdtPosition>(sourceLoader, cu, fullPath, false, dump) {
            @Override
            public JdtPosition makePosition(int start, int end) {
                return new JdtPosition(
                        start,
                        end,
                        this.cu.getLineNumber(start),
                        this.cu.getLineNumber(end),
                        sourceFile,
                        this.fullPath);
            }
        };
    }
}
