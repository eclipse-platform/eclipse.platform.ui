/*******************************************************************************
 * Copyright (c) 2009, 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     IBM Corporation - clean-up
 *******************************************************************************/
package org.eclipe.debug.tests.viewer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.eclipe.debug.tests.viewer.model.TestModel.TestElement;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.internal.ui.viewers.model.IInternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ICheckUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Tests that verify that the viewer property retrieves all the content 
 * from the model.
 * 
 * @since 3.6
 */
abstract public class ContentTests extends TestCase implements ITestModelUpdatesListenerConstants {
    
    Display fDisplay;
    Shell fShell;
    ITreeModelViewer fViewer;
    TestModelUpdatesListener fListener;
    
    public ContentTests(String name) {
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
        
        fListener = new TestModelUpdatesListener(fViewer, true, true);

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
    
    public void testSimpleSingleLevel() throws InterruptedException {
        // Create the model with test data
        TestModel model = TestModel.simpleSingleLevel();

        // Make sure that all elements are expanded
        fViewer.setAutoExpandLevel(-1);
        
        // Create the agent which forces the tree to populate
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        
        // Create the listener which determines when the view is finished updating.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, true); 
        
        // Set the viewer input (and trigger updates).
        fViewer.setInput(model.getRootElement());
        
        // Wait for the updates to complete.
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        
        model.validateData(fViewer, TreePath.EMPTY);
        
        assertTrue( fListener.checkCoalesced(TreePath.EMPTY, 0, 6) );
    }

    public void testSimpleMultiLevel() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        
        TestModel model = TestModel.simpleMultiLevel();
        fViewer.setAutoExpandLevel(-1);
        
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, true); 
        
        fViewer.setInput(model.getRootElement());

        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        model.validateData(fViewer, TreePath.EMPTY);
        
        assertTrue( fListener.checkCoalesced(TreePath.EMPTY, 0, 3) );
    }
    
    /**
     * Modified test model that optionally captures (i.e. doesn't compete) 
     * udpates after filling in their data.   
     */
    class TestModelWithCapturedUpdates extends TestModel {
        
        boolean fCaptureLabelUpdates = false;
        boolean fCaptureChildrenUpdates = false;
        
        List fCapturedUpdates = Collections.synchronizedList(new ArrayList());
        
        public void update(IChildrenUpdate[] updates) {
            for (int i = 0; i < updates.length; i++) {
                TestElement element = (TestElement)updates[i].getElement();
                int endOffset = updates[i].getOffset() + updates[i].getLength();
                for (int j = updates[i].getOffset(); j < endOffset; j++) {
                    if (j < element.getChildren().length) {
                        updates[i].setChild(element.getChildren()[j], j);
                    }
                }
                if (fCaptureChildrenUpdates) {
                    fCapturedUpdates.add(updates[i]);
                } else {
                    updates[i].done();
                }
            }
        }
        
        public void update(ILabelUpdate[] updates) {
            for (int i = 0; i < updates.length; i++) {
                TestElement element = (TestElement)updates[i].getElement();
                updates[i].setLabel(element.getLabel(), 0);
                if (updates[i] instanceof ICheckUpdate && 
                    Boolean.TRUE.equals(updates[i].getPresentationContext().getProperty(ICheckUpdate.PROP_CHECK))) 
                {
                    ((ICheckUpdate)updates[i]).setChecked(element.getChecked(), element.getGrayed());
                }
                if (fCaptureLabelUpdates) {
                    fCapturedUpdates.add(updates[i]);
                } else {
                    updates[i].done();
                }
            }        
        } 
    }
    
    /**
     * Test to make sure that label provider cancels stale updates and doesn't 
     * use data from stale updates to populate the viewer.<br>
     * See bug 210027
     */
    public void testLabelUpdatesCompletedOutOfSequence1() throws InterruptedException {
        TestModelWithCapturedUpdates model = new TestModelWithCapturedUpdates();
        model.fCaptureLabelUpdates = true;
        
        model.setRoot( new TestElement(model, "root", new TestElement[] {
            new TestElement(model, "1", new TestElement[0]),
            new TestElement(model, "2", new TestElement[0]),
        }) );

        // Set input into the view to update it, but block children updates.
        // Wait for view to start retrieving content.
        fViewer.setInput(model.getRootElement());
        while (model.fCapturedUpdates.size() < model.getRootElement().fChildren.length) {
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        }
        List firstUpdates = model.fCapturedUpdates;
        model.fCapturedUpdates = new ArrayList(2);
        
//      // Change the model and run another update set. 
        model.getElement(model.findElement("1")).setLabelAppendix(" - changed");
        model.getElement(model.findElement("2")).setLabelAppendix(" - changed");
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, false, false); 
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (model.fCapturedUpdates.size() < model.getRootElement().fChildren.length) {
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        }
        
        // Complete the second set of children updates
        for (int i = 0; i < model.fCapturedUpdates.size(); i++) {
            ((ILabelUpdate)model.fCapturedUpdates.get(i)).done();
        }
        
        // Then complete the first set.
        for (int i = 0; i < firstUpdates.size(); i++) {
            ILabelUpdate capturedUpdate = (ILabelUpdate)firstUpdates.get(i); 
            assertTrue(capturedUpdate.isCanceled());
            capturedUpdate.done();
        }

        while (!fListener.isFinished(CHILDREN_UPDATES)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        
        // Check viewer data
        model.validateData(fViewer, TreePath.EMPTY);
    }

    /**
     * Test to make sure that label provider cancels stale updates and doesn't 
     * use data from stale updates to populate the viewer.<br>
     * This version of the test changes the elements in the view, and not just 
     * the elements' labels.  In this case, the view should still cancel stale 
     * updates.<br> 
     * See bug 210027
     */
    public void testLabelUpdatesCompletedOutOfSequence2() throws InterruptedException {
        TestModelWithCapturedUpdates model = new TestModelWithCapturedUpdates();
        model.fCaptureLabelUpdates = true;
        
        model.setRoot( new TestElement(model, "root", new TestElement[] {
            new TestElement(model, "1", new TestElement[0]),
            new TestElement(model, "2", new TestElement[0]),
        }) );

        // Set input into the view to update it, but block children updates.
        // Wait for view to start retrieving content.
        fViewer.setInput(model.getRootElement());
        while (model.fCapturedUpdates.size() < model.getRootElement().fChildren.length) {
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        }
        List firstUpdates = model.fCapturedUpdates;
        model.fCapturedUpdates = new ArrayList(2);
        
        // Change the model and run another update set. 
        model.setElementChildren(TreePath.EMPTY, new TestElement[] {
            new TestElement(model, "1-new", new TestElement[0]),
            new TestElement(model, "2-new", new TestElement[0]),
        });
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, false, false); 
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (model.fCapturedUpdates.size() < model.getRootElement().fChildren.length) {
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        }
        
        // Complete the second set of children updates
        for (int i = 0; i < model.fCapturedUpdates.size(); i++) {
            ((ILabelUpdate)model.fCapturedUpdates.get(i)).done();
        }
        
        // Then complete the first set.
        for (int i = 0; i < firstUpdates.size(); i++) {
            ILabelUpdate capturedUpdate = (ILabelUpdate)firstUpdates.get(i); 
            assertTrue(capturedUpdate.isCanceled());
            capturedUpdate.done();
        }

        while (!fListener.isFinished(CHILDREN_UPDATES)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        
        // Check viewer data
        model.validateData(fViewer, TreePath.EMPTY);
    }

    /**
     * Test to make sure that content provider cancels stale updates and doesn't 
     * use data from stale updates to populate the viewer.<br>
     * Note: this test is disabled because currently the viewer will not issue 
     * a new update for an until the previous update is completed.  This is even
     * if the previous update is canceled.  If this behavior is changed at some 
     * point, then this test should be re-enabled.<br>
     * See bug 210027
     */
    public void _x_testChildrenUpdatesCompletedOutOfSequence() throws InterruptedException {
        TestModelWithCapturedUpdates model = new TestModelWithCapturedUpdates();
        model.fCaptureChildrenUpdates = true;
        
        model.setRoot( new TestElement(model, "root", new TestElement[] {
            new TestElement(model, "1", new TestElement[0]),
            new TestElement(model, "2", new TestElement[0]),
        }) );

        // Set input into the view to update it, but block children updates.
        // Wait for view to start retrieving content.
        fViewer.setInput(model.getRootElement());
        while (!areCapturedChildrenUpdatesComplete(model.fCapturedUpdates, model.getRootElement().fChildren.length)) {
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        }
        IChildrenUpdate[] firstUpdates = (IChildrenUpdate[])model.fCapturedUpdates.toArray(new IChildrenUpdate[0]);
        model.fCapturedUpdates.clear();
        
        // Change the model and run another update set. 
        model.setElementChildren(TreePath.EMPTY, new TestElement[] {
            new TestElement(model, "1-new", new TestElement[0]),
            new TestElement(model, "2-new", new TestElement[0]),
        });
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, false, false); 
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!areCapturedChildrenUpdatesComplete(model.fCapturedUpdates, model.getRootElement().fChildren.length)) {
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        }
        
        // Complete the second set of children updates
        for (int i = 0; i < model.fCapturedUpdates.size(); i++) {
            ((IChildrenUpdate)model.fCapturedUpdates.get(i)).done();
        }
        
        // Then complete the first set.
        for (int i = 0; i < firstUpdates.length; i++) {
            firstUpdates[i].done();
        }

        while (!fListener.isFinished(CHILDREN_UPDATES)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        
        // Check viewer data
        model.validateData(fViewer, TreePath.EMPTY);
    }
    
    private boolean areCapturedChildrenUpdatesComplete(List capturedUpdates, int childCount) {
        List expectedChildren = new ArrayList();
        for (int i = 0; i < childCount; i++) {
            expectedChildren.add(new Integer(i));
        }
        IChildrenUpdate[] updates = (IChildrenUpdate[])capturedUpdates.toArray(new IChildrenUpdate[0]);
        for (int i = 0; i < updates.length; i++) {
            for (int j = 0; j < updates[i].getLength(); j++) {
                expectedChildren.remove( new Integer(updates[i].getOffset() + j) );
            }
        }
        return expectedChildren.isEmpty();
    }
}
