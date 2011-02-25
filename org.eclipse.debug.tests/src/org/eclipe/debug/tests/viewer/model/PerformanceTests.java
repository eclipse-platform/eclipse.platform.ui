/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipe.debug.tests.viewer.model;

import junit.framework.TestCase;

import org.eclipe.debug.tests.viewer.model.TestModel.TestElement;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelContentProviderTarget;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.ui.PlatformUI;

/**
 * Tests to measure the performance of the viewer updates.  
 */
abstract public class PerformanceTests extends TestCase implements ITestModelUpdatesListenerConstants {
    Display fDisplay;
    Shell fShell;
    ITreeModelViewer fViewer;
    TestModelUpdatesListener fListener;
    
    public PerformanceTests(String name) {
        super(name);
    }

    /**
     * @throws java.lang.Exception
     */
    protected void setUp() throws Exception {
        fDisplay = PlatformUI.getWorkbench().getDisplay();
        // Tests end in DNF on Mac and Windows (bug 325465)
        if (Platform.getOS().equals(Platform.OS_MACOSX) || Platform.getOS().equals(Platform.OS_WIN32)) {
            fShell = new Shell(fDisplay);
            fShell.setMaximized(true);
        } else {
            fShell = new Shell(fDisplay, SWT.ON_TOP | SWT.SHELL_TRIM);
            // Maximizing a shell with SWT.ON_TOP doesn't work on Linux (bug 325465)
            fShell.setSize(800, 600);
        }
        fShell.setLayout(new FillLayout());

        fViewer = createViewer(fDisplay, fShell);
        
        fListener = new TestModelUpdatesListener(fViewer, false, false);

        fShell.open ();
    }

    abstract protected ITreeModelContentProviderTarget createViewer(Display display, Shell shell);
    
    /**
     * @throws java.lang.Exception
     */
    protected void tearDown() throws Exception {
        fListener.dispose();
        fViewer.getPresentationContext().dispose();
        
        // Close the shell and exit.
        fShell.close();
        while (!fShell.isDisposed()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
    }
    
    protected void runTest() throws Throwable {
        try {
            super.runTest();
        } catch (Throwable t) {
            throw new ExecutionException("Test failed: " + t.getMessage() + "\n fListener = " + fListener.toString(), t);
        }
    }

    /**
     * Depth (size) of the test model to be used in the tests.  This number allows
     * the jface based tests to use a small enough model to fit on the screen, and 
     * for the virtual viewer to exercise the content provider to a greater extent.
     */
    abstract protected int getTestModelDepth();
    
    public void testRefreshStruct() {
        TestModel model = new TestModel();
        model.setRoot( new TestElement(model, "root", new TestElement[0] ) ); 
        model.setElementChildren(TreePath.EMPTY, makeModelElements(model, getTestModelDepth(), "model"));
        
        fViewer.setAutoExpandLevel(-1);

        // Create the listener
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY);

        Performance perf = Performance.getDefault();
        PerformanceMeter meter = perf.createPerformanceMeter(perf.getDefaultScenarioId(this));
        try {
            for (int i = 0; i < 100; i++) {
                // Update the model
                model.setAllAppendix(" - pass " + i);
                //model.setElementChildren(TreePath.EMPTY, makeModelElements(model, getTestModelDepth(), "pass " + i));
                
                TestElement element = model.getRootElement();
                fListener.reset(TreePath.EMPTY, element, -1, false, false);
                
                meter.start();
                model.postDelta(new ModelDelta(element, IModelDelta.CONTENT));
                while (!fListener.isFinished(ALL_UPDATES_COMPLETE | MODEL_CHANGED_COMPLETE)) 
                    if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
                model.validateData(fViewer, TreePath.EMPTY);
                meter.stop();
                System.gc();
            }
            
            meter.commit();
            perf.assertPerformance(meter);
        } finally {
            meter.dispose();
        }
    }

    public void testRefreshStructReplaceElements() {
        TestModel model = new TestModel();
        model.setRoot( new TestElement(model, "root", new TestElement[0] ) ); 
        model.setElementChildren(TreePath.EMPTY, makeModelElements(model, getTestModelDepth(), "model"));
        
        fViewer.setAutoExpandLevel(-1);

        // Create the listener
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY);

        Performance perf = Performance.getDefault();
        PerformanceMeter meter = perf.createPerformanceMeter(perf.getDefaultScenarioId(this));
        try {
            for (int i = 0; i < 2000; i++) {
                // Update the model
                model.setElementChildren(TreePath.EMPTY, makeModelElements(model, getTestModelDepth(), "pass " + i));
                
                TestElement element = model.getRootElement();
                fListener.reset(TreePath.EMPTY, element, -1, false, false);
                
                meter.start();
                model.postDelta(new ModelDelta(element, IModelDelta.CONTENT));
                while (!fListener.isFinished(ALL_UPDATES_COMPLETE | MODEL_CHANGED_COMPLETE)) 
                    if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
                model.validateData(fViewer, TreePath.EMPTY);
                meter.stop();
                System.gc();
            }
            
            meter.commit();
            perf.assertPerformance(meter);
        } finally {
            meter.dispose();
        }
    }

    private TestElement[] makeModelElements(TestModel model, int depth, String prefix) {
        TestElement[] elements = new TestElement[depth];
        for (int i = 0; i < depth; i++) {
            String name = prefix + "." + i;
            elements[i] = new TestElement(model, name, makeModelElements(model, i, name));
        }
        return elements;
    }
}
