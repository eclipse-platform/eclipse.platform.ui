/*******************************************************************************
 * Copyright (c) 2009, 2011 Wind River Systems and others.
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
import org.eclipse.debug.internal.ui.viewers.model.IInternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
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
        fShell = new Shell(fDisplay);
        fShell.setMaximized(true);
        fShell.setLayout(new FillLayout());

        fViewer = createViewer(fDisplay, fShell);
        
        fListener = new TestModelUpdatesListener(fViewer, false, false);

        fShell.open ();
    }

    abstract protected IInternalTreeModelViewer createViewer(Display display, Shell shell);
    
    /**
     * @throws java.lang.Exception
     */
    protected void tearDown() throws Exception {
        fListener.dispose();
        fViewer.getPresentationContext().dispose();
        
        // Close the shell and exit.
        fShell.close();
        while (!fShell.isDisposed()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
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
    
    public void testRefreshStruct() throws InterruptedException {
        TestModel model = new TestModel();
        model.setRoot( new TestElement(model, "root", new TestElement[0] ) ); 
        model.setElementChildren(TreePath.EMPTY, TestModel.makeMultiLevelElements(model, getTestModelDepth(), "model."));
        
        fViewer.setAutoExpandLevel(-1);

        // Create the listener
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY);

        Performance perf = Performance.getDefault();
        PerformanceMeter meter = perf.createPerformanceMeter(perf.getDefaultScenarioId(this));
        try {
            for (int i = 0; i < 100; i++) {
                // Update the model
                model.setAllAppendix(" - pass " + i);
                
                TestElement element = model.getRootElement();
                fListener.reset(TreePath.EMPTY, element, -1, false, false);
                
                meter.start();
                model.postDelta(new ModelDelta(element, IModelDelta.CONTENT));
                while (!fListener.isFinished(ALL_UPDATES_COMPLETE | MODEL_CHANGED_COMPLETE)) 
                    if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
                meter.stop();
                System.gc();
            }
            
            meter.commit();
            perf.assertPerformance(meter);
        } finally {
            meter.dispose();
        }
    }

    public void testRefreshStructReplaceElements() throws InterruptedException {
        TestModel model = new TestModel();
        model.setRoot( new TestElement(model, "root", new TestElement[0] ) ); 
        model.setElementChildren(TreePath.EMPTY, TestModel.makeMultiLevelElements(model, getTestModelDepth(), "model."));
        
        fViewer.setAutoExpandLevel(-1);

        // Create the listener
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY);

        Performance perf = Performance.getDefault();
        PerformanceMeter meter = perf.createPerformanceMeter(perf.getDefaultScenarioId(this));
        try {
            for (int i = 0; i < 100; i++) {
                // Update the model
                model.setElementChildren(TreePath.EMPTY, TestModel.makeMultiLevelElements(model, getTestModelDepth(), "pass " + i + "."));
                
                TestElement element = model.getRootElement();
                fListener.reset(TreePath.EMPTY, element, -1, false, false);
                
                meter.start();
                model.postDelta(new ModelDelta(element, IModelDelta.CONTENT));
                while (!fListener.isFinished(ALL_UPDATES_COMPLETE | MODEL_CHANGED_COMPLETE)) 
                    if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
                meter.stop();
                System.gc();
            }
            
            meter.commit();
            perf.assertPerformance(meter);
        } finally {
            meter.dispose();
        }
    }

    
    public void testRefreshList() throws InterruptedException {
        TestModel model = new TestModel();
        model.setRoot( new TestElement(model, "root", new TestElement[0] ) ); 
        int numElements = (int)Math.pow(2, getTestModelDepth());
        model.setElementChildren(TreePath.EMPTY, TestModel.makeSingleLevelModelElements(model, numElements, "model."));
        
        fViewer.setAutoExpandLevel(-1);

        // Create the listener
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY);

        Performance perf = Performance.getDefault();
        PerformanceMeter meter = perf.createPerformanceMeter(perf.getDefaultScenarioId(this));
        try {
            for (int i = 0; i < 100; i++) {
                // Update the model
                model.setAllAppendix(" - pass " + i);
                
                TestElement element = model.getRootElement();
                fListener.reset(TreePath.EMPTY, element, -1, false, false);
                
                meter.start();
                model.postDelta(new ModelDelta(element, IModelDelta.CONTENT));
                while (!fListener.isFinished(ALL_UPDATES_COMPLETE | MODEL_CHANGED_COMPLETE)) 
                    if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
                meter.stop();
                System.gc();
            }
            
            meter.commit();
            perf.assertPerformance(meter);
        } finally {
            meter.dispose();
        }
    }

    public void testSaveAndRestore() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = TestModel.simpleMultiLevel();

        // expand all elements
        fViewer.setAutoExpandLevel(-1);
        
        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY);

        // Set a selection in view
        fViewer.setSelection(new TreeSelection(model.findElement("3.2.3")));
                
        // Turn off the auto-expand now since we want to text the auto-expand logic
        fViewer.setAutoExpandLevel(-1);

        Performance perf = Performance.getDefault();
        PerformanceMeter meter = perf.createPerformanceMeter(perf.getDefaultScenarioId(this));
        try {
            for (int i = 0; i < 100; i++) {
                // Update the model
                model.setAllAppendix(" - pass " + i);

                // Set the viewer input to null.  This will trigger the view to save the viewer state.
                fListener.reset(true, false);
        
                meter.start();
                fViewer.setInput(null);
                while (!fListener.isFinished(STATE_SAVE_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        
                // Set the viewer input back to the model.  When view updates are complete
                // the viewer 
                // Note: disable redundant updates because the reveal delta triggers one.
                fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, false, false);
                // TODO: add state updates somehow?
                fViewer.setInput(model.getRootElement());
                while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
                meter.stop();
                System.gc();
            }
            
            meter.commit();
            perf.assertPerformance(meter);
        } finally {
            meter.dispose();
        }

    }

    public void testRefreshListFiltered() throws InterruptedException {
        TestModel model = new TestModel();
        model.setRoot( new TestElement(model, "root", new TestElement[0] ) ); 
        int numElements = (int)Math.pow(2, getTestModelDepth());
        model.setElementChildren(TreePath.EMPTY, TestModel.makeSingleLevelModelElements(model, 1000, "model."));
        
        fViewer.setAutoExpandLevel(-1);

        // Create the listener
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 

        fViewer.addFilter(new ViewerFilter() {
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof TestElement) {
                    String id = ((TestElement)element).getID();
                    if (id.startsWith("model.")) {
                        id = id.substring("model.".length());
                    }
                    if (id.length() >= 2 && (id.charAt(1) == '1' || id.charAt(1) == '3' || id.charAt(1) == '5' || id.charAt(1) == '7' || id.charAt(1) == '9')) {
                        return false;
                    }
                }
                return true;
            }
        });
        
        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY);

        Performance perf = Performance.getDefault();
        PerformanceMeter meter = perf.createPerformanceMeter(perf.getDefaultScenarioId(this));
        try {
            for (int i = 0; i < 100; i++) {
                // Update the model
                model.setAllAppendix(" - pass " + i);
                
                TestElement element = model.getRootElement();
                fListener.reset(TreePath.EMPTY, element, -1, false, false);
                
                meter.start();
                model.postDelta(new ModelDelta(element, IModelDelta.CONTENT));
                while (!fListener.isFinished(ALL_UPDATES_COMPLETE | MODEL_CHANGED_COMPLETE)) 
                    if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
                meter.stop();
                System.gc();
            }
            
            meter.commit();
            perf.assertPerformance(meter);
        } finally {
            meter.dispose();
        }
    }
    
}
