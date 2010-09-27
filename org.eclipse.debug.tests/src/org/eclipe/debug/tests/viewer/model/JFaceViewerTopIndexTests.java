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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipe.debug.tests.viewer.model.TestModel.TestElement;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelContentProviderTarget;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @since 3.6
 */
public class JFaceViewerTopIndexTests extends TestCase implements ITestModelUpdatesListenerConstants {
    
    Display fDisplay;
    Shell fShell;
    TreeModelViewer fViewer;
    TestModelUpdatesListener fListener;
    
    public JFaceViewerTopIndexTests(String name) {
        super(name);
    }

    /**
     * @throws java.lang.Exception
     */
    protected void setUp() throws Exception {
        fDisplay = PlatformUI.getWorkbench().getDisplay();
        fShell = new Shell(fDisplay/*, SWT.ON_TOP | SWT.SHELL_TRIM*/);
        fShell.setSize(300, 100);
        fShell.setLayout(new FillLayout());

        fViewer = createViewer(fDisplay, fShell);
        
        fListener = new TestModelUpdatesListener(fViewer, false, false);

        fShell.open ();
    }

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
    
    protected ITreeModelContentProviderTarget getCTargetViewer() {
        return (ITreeModelContentProviderTarget)fViewer;
    }

    protected TreeModelViewer createViewer(Display display, Shell shell) {
        return new TreeModelViewer(fShell, SWT.VIRTUAL | SWT.MULTI, new PresentationContext("TestViewer"));
    }
    
    /**
     * Restore REVEAL on simple model with elements without children.
     * 
     */
    public void testRestoreTopIndex() {
        TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        
    	TestModel model = new TestModel();
        
        TestElement[] elements = new TestElement[8];
        for (int i = 0; i < elements.length; i++) {
            String text = Integer.toString(i + 1);
        	// elements don't have children
        	elements[i] = 
                new TestElement(model, text, new TestElement[0] );
            
        }
        model.setRoot(new TestElement(model, "root", elements));

        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, false, false);
                
        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY, true);
        
        // Stop forcing view updates. 
        autopopulateAgent.dispose();
        
        // scroll to the 5th element
        int indexRevealElem = 4;
        getCTargetViewer().reveal(TreePath.EMPTY, indexRevealElem);       
        while(fDisplay.readAndDispatch()) {}
        final TreePath originalTopPath = getCTargetViewer().getTopElementPath();
        Assert.assertNotNull("Top item should not be null!", originalTopPath);
        Assert.assertEquals(elements[indexRevealElem], originalTopPath.getLastSegment());
        
        // Set the viewer input to null.  This will trigger the view to save the viewer state.
        fListener.reset(true, false);
        fListener.addStateUpdates(getCTargetViewer(), TreePath.EMPTY, model.getRootElement());

        fViewer.setInput(null);
        while (!fListener.isFinished(STATE_SAVE_COMPLETE)) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Set the viewer input back to the model to trigger RESTORE operation. 
        fListener.reset(false, false);
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        while (fDisplay.readAndDispatch ()) {}
        // check if REVEAL was restored OK
        final TreePath topPath = getCTargetViewer().getTopElementPath();
        Assert.assertNotNull("Top item should not be null!", topPath);
        Assert.assertEquals(elements[indexRevealElem], topPath.getLastSegment());
        Assert.assertEquals(originalTopPath, topPath);
        
    }
    
    /**
     * Restore REVEAL when having also to restore an expanded element 
     * that is just above the REVEAL element.
     * 
     * See bug 324100
     */
    public void testRestoreTopAndExpand() {
        TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        
        TestModel model = new TestModel();
        
        TestElement[] elements = new TestElement[10];
        for (int i = 0; i < elements.length; i++) {
            String text = Integer.toString(i + 1);
            // first element has 2 children
            if (i == 0) {
            	elements[i] = 
                    new TestElement(model, text, new TestElement[] { 
                        new TestElement(model, text + ".1", new TestElement[0] ),
                        new TestElement(model, text + ".2", new TestElement[0] )
                    });
            } else {
            	// rest of elements don't have children
            	elements[i] = 
                    new TestElement(model, text, new TestElement[0] );
            }
            
        }
        model.setRoot(new TestElement(model, "root", elements));

        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, false, false);
                
        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY, true);

        // Expand first element
        fListener.reset(); 
        fListener.setFailOnRedundantUpdates(false);
        int indexFirstElem = 0;
        TestElement firstElem = elements[indexFirstElem];
        ModelDelta rootDelta = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        ModelDelta delta = model.getBaseDelta(rootDelta);
        TreePath firstElemPath = model.findElement(firstElem.getLabel());
        fListener.addUpdates(
            firstElemPath, firstElem, 1, 
            CHILD_COUNT_UPDATES | CHILDREN_UPDATES );
        delta.addNode(firstElem, indexFirstElem, IModelDelta.EXPAND, firstElem.getChildren().length);
        
        model.postDelta(rootDelta);

        while (!fListener.isFinished(CONTENT_UPDATES_COMPLETE | MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Validate that the first node is expanded
        Assert.assertTrue(getCTargetViewer().getExpandedState(firstElemPath) == true);
        
        // Stop forcing view updates. 
        autopopulateAgent.dispose();
        
        // scroll to the 2nd element
        getCTargetViewer().reveal(TreePath.EMPTY, 1);       
        while(fDisplay.readAndDispatch()) {}
        final TreePath originalTopPath = getCTargetViewer().getTopElementPath();
        Assert.assertNotNull("Top item should not be null!", originalTopPath);
        Assert.assertEquals(elements[1], originalTopPath.getLastSegment());
        
        // Set the viewer input to null.  This will trigger the view to save the viewer state.
        fListener.reset(true, false);
        fListener.addStateUpdates(getCTargetViewer(), TreePath.EMPTY, model.getRootElement());
        fViewer.setInput(null);
        while (!fListener.isFinished(STATE_SAVE_COMPLETE)) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Set the viewer input back to the model
        fListener.reset(false, false);
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        while (fDisplay.readAndDispatch ()) {}
        // check if REVEAL was restored OK
        final TreePath topPath = getCTargetViewer().getTopElementPath();
        Assert.assertNotNull("Top item should not be null!", topPath);
        Assert.assertEquals(elements[1], topPath.getLastSegment());
        Assert.assertEquals(originalTopPath, topPath);
        
    }
    
    /**
     * Restore REVEAL when this operation triggers restoring of an expanded
     * element.
     * 
     * See bug 324100
     */
    public void testRestoreTopTriggersExpand() {
        TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);

        TestModel model = new TestModel();
        
        TestElement[] elements = new TestElement[10];
        for (int i = 0; i < elements.length; i++) {
            String text = Integer.toString(i + 1);
            // last element has 2 children
            if (i == elements.length - 1) {
            	elements[i] = 
                    new TestElement(model, text, new TestElement[] { 
                        new TestElement(model, text + ".1", new TestElement[0] ),
                        new TestElement(model, text + ".2", new TestElement[0] )
                    });
            } else {
            	// rest of elements don't have children
            	elements[i] = 
                    new TestElement(model, text, new TestElement[0] );
            }
            
        }
        
        fViewer.setAutoExpandLevel(-1);
        model.setRoot(new TestElement(model, "root", elements));

        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, false, false);
                
        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY, true);

        int indexLastElem = elements.length-1;
        TestElement lastElem = elements[indexLastElem];
        TreePath lastElemePath = model.findElement(lastElem.getLabel());

        // Validate that the last node is expanded
        Assert.assertTrue(getCTargetViewer().getExpandedState(lastElemePath) == true);
        
        // Stop forcing view updates. 
        fViewer.setAutoExpandLevel(0);
        autopopulateAgent.dispose();
        
        // scroll to the element before last element
        getCTargetViewer().reveal(TreePath.EMPTY, indexLastElem-1);       
        while(fDisplay.readAndDispatch()) {}
        final TreePath originalTopPath = getCTargetViewer().getTopElementPath();
        Assert.assertNotNull("Top item should not be null!", originalTopPath);
        Assert.assertEquals(elements[indexLastElem-1], originalTopPath.getLastSegment());
        
        // Set the viewer input to null.  This will trigger the view to save the viewer state.
        fListener.reset(true, false);
        fListener.addStateUpdates(getCTargetViewer(), TreePath.EMPTY, model.getRootElement());

        fViewer.setInput(null);
        while (!fListener.isFinished(STATE_SAVE_COMPLETE)) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Set the viewer input back to the model.
        fListener.reset(false, false);
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        while (fDisplay.readAndDispatch ()) {}
        // check if REVEAL was restored OK
        final TreePath topPath = getCTargetViewer().getTopElementPath();
        Assert.assertNotNull("Top item should not be null!", topPath);
        Assert.assertEquals(elements[indexLastElem-1], topPath.getLastSegment());
        Assert.assertEquals(originalTopPath, topPath);
        
    }
}
