/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.eclipse.cg.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.summaries.BypassClassTargetSelector;
import com.ibm.wala.ipa.summaries.BypassMethodTargetSelector;
import com.ibm.wala.ipa.summaries.XMLMethodSummaryReader;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.strings.Atom;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;


import com.ibm.wala.cast.java.client.d4.D4JDTJavaSourceAnalysisEngine;
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.ide.util.EclipseProjectPath;
import com.ibm.wala.ide.util.JavaEclipseProjectPath;


import edu.tamu.wala.increpta.ipa.callgraph.propagation.IPASSAPropagationCallGraphBuilder;

abstract public class WalaProjectCGModel implements WalaCGModel {

  protected D4JDTJavaSourceAnalysisEngine engine;

  protected CallGraph callGraph;

  protected Collection roots;

  protected WalaProjectCGModel(IJavaProject project, final String exclusionsFile) throws IOException, CoreException{

    final EclipseProjectPath ep = JavaEclipseProjectPath.make(project, EclipseProjectPath.AnalysisScopeType.SOURCE_FOR_PROJ);

    this.engine = new D4JDTJavaSourceAnalysisEngine(project) {
      @Override
      public void buildAnalysisScope() {
        try {
          scope = ep.toAnalysisScope(new JavaSourceAnalysisScope());
          setExclusionsFile(exclusionsFile);

          if(getExclusionsFile()!=null) {
            InputStream is = WalaProjectCGModel.class.getClassLoader().getResourceAsStream(getExclusionsFile());
            scope.setExclusions(new FileOfClasses(is));
          }
        } catch (IOException e) {
          Assertions.UNREACHABLE(e.toString());
        }
      }
      @Override
      protected Iterable<Entrypoint> makeDefaultEntrypoints(AnalysisScope scope, IClassHierarchy cha) {
        return getEntrypoints(scope, cha);
      }

      @SuppressWarnings("unused")
      private void addCustomBypassLogic(IClassHierarchy classHierarchy, AnalysisOptions analysisOptions) throws IllegalArgumentException {
        ClassLoader classLoader = Util.class.getClassLoader();
        if (classLoader == null) {
          throw new IllegalArgumentException("classLoader is null");
        }

        Util.addDefaultSelectors(analysisOptions, classHierarchy);

        InputStream inputStream = classLoader.getResourceAsStream(Util.nativeSpec);
        XMLMethodSummaryReader methodSummaryReader = new XMLMethodSummaryReader(inputStream, scope);
        MethodTargetSelector customMethodTargetSelector = new BypassMethodTargetSelector( analysisOptions.getMethodTargetSelector(), methodSummaryReader.getSummaries(), methodSummaryReader.getIgnoredPackages(), classHierarchy);
        analysisOptions.setSelector(customMethodTargetSelector);

        ClassTargetSelector customClassTargetSelector = new BypassClassTargetSelector(analysisOptions.getClassTargetSelector(), methodSummaryReader.getAllocatableClasses(), classHierarchy,
                classHierarchy.getLoader(scope.getLoader(Atom.findOrCreateUnicodeAtom("Synthetic"))));
        analysisOptions.setSelector(customClassTargetSelector);
      }
    };
  }

  @Override
  public void buildGraph() throws WalaException, CancelException {
    try {
      //AstJavaIPAZeroXCFABuilder
      callGraph = ((IPASSAPropagationCallGraphBuilder) engine.defaultCallGraphBuilder()).getCallGraph();
      roots = inferRoots(callGraph);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public CallGraphBuilder getCallGraphBuilder() {
    return engine.getBuilder();
  }

  @Override
  public CallGraph getGraph() {
    return callGraph;
  }

  @Override
  public Collection getRoots() {
    return roots;
  }

  public D4JDTJavaSourceAnalysisEngine getEngine() {
    return engine;
  }

  abstract protected Iterable<Entrypoint> getEntrypoints(AnalysisScope scope, IClassHierarchy cha);

  abstract protected Collection inferRoots(CallGraph cg) throws WalaException;


}
