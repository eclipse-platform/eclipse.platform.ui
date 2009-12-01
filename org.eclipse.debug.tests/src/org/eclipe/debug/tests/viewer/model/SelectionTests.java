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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/** 
 * Tests to verify that the viewer properly handles selection changes.
 */
abstract public class SelectionTests extends TestCase {
    Display fDisplay;
    Shell fShell;
    ITreeModelViewer fViewer;
    TestModelUpdatesListener fListener;
    
    public SelectionTests(String name) {
        super(name);
    }

    /**
     * @throws java.lang.Exception
     */
    protected void setUp() throws Exception {
        fDisplay = PlatformUI.getWorkbench().getDisplay();
        fShell = new Shell(fDisplay/*, SWT.ON_TOP | SWT.SHELL_TRIM*/);
        fShell.setMaximized(true);
        fShell.setLayout(new FillLayout());

        fViewer = createViewer(fDisplay, fShell);
        
        fListener = new TestModelUpdatesListener(false, false);
        fViewer.addViewerUpdateListener(fListener);
        fViewer.addLabelUpdateListener(fListener);
        fViewer.addModelChangedListener(fListener);

        fShell.open ();
    }

    abstract protected ITreeModelViewer createViewer(Display display, Shell shell);
    
    /**
     * @throws java.lang.Exception
     */
    protected void tearDown() throws Exception {
        fViewer.removeLabelUpdateListener(fListener);
        fViewer.removeViewerUpdateListener(fListener);
        fViewer.removeModelChangedListener(fListener);
        fViewer.getPresentationContext().dispose();
        
        // Close the shell and exit.
        fShell.close();
        while (!fShell.isDisposed()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
    }

    private static class SelectionListener implements ISelectionChangedListener {
        private List fEvents = new ArrayList(1);
        
        public void selectionChanged(SelectionChangedEvent event) {
            fEvents.add(event);
        }
    }

    private TestModel makeMultiLevelModel() {
        TestModel model = TestModel.simpleMultiLevel();
        fViewer.setAutoExpandLevel(-1);
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY);
        return model;
    }
    
    /**
     * In this test:
     * - set selection to an element deep in the model
     * - verify that selection chagned listener is called
     * - verify that the selection is in the viewer is correct 
     */
    public void testSimpleSetSelection() {
        // Create the model and populate the view.
        TestModel model = makeMultiLevelModel();
        
        // Create a selection object to the deepest part of the tree.
        SelectionListener listener = new SelectionListener();
        fViewer.addSelectionChangedListener(listener);

        // Set the selection and verify that the listener is called.
        TreeSelection selection = new TreeSelection(model.findElement("3.3.3"));
        fViewer.setSelection(selection, true, false);
        assertTrue(listener.fEvents.size() == 1);

        // Check that the new selection is what was requested.
        ISelection viewerSelection = fViewer.getSelection();        
        assertEquals(selection, viewerSelection);
    }

    /**
     * In this test:
     * - set a seleciton to an element 
     * - then remove that element 
     * - update the view with remove delta
     * -> The selection should be re-set to empty.
     */
    public void testSelectRemove() {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);

        // Create the model and populate the view.
        TestModel model = makeMultiLevelModel();
        
        // Create a selection object to the deepest part of the tree.
        TreePath elementPath = model.findElement("3.3.3");
        TreeSelection selection = new TreeSelection(elementPath);

        // Set the selection.
        fViewer.setSelection(selection, true, false);

        // Remove the element
        TreePath removePath = model.findElement("3");
        TreePath parentPath = removePath.getParentPath();
        int removeIndex = model.getElement(parentPath).indexOf( model.getElement(removePath) );
        ModelDelta delta = model.removeElementChild(removePath.getParentPath(), removeIndex);

        // Configure a selection listener
        SelectionListener listener = new SelectionListener();
        fViewer.addSelectionChangedListener(listener);

        // Reset the listener and update the viewer.  With a remove
        // delta only wait for the delta to be processed.
        fListener.reset(); 
        model.postDelta(delta);
        while (!fListener.isFinished(TestModelUpdatesListener.MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Check to make sure the selection was made
        //assertTrue(listener.fEvents.size() == 1);

        // Check that the new selection is empty
        ISelection viewerSelection = fViewer.getSelection();        
        assertTrue(viewerSelection.isEmpty());
    }

    
    /**
     * In this test:
     * - set a selection to an element 
     * - then remove that element 
     * - then refresh the view.
     * -> The selection should be re-set to empty.
     */
    public void testSelectRemoveRefreshStruct() {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);

        // Create the model and populate the view.
        TestModel model = makeMultiLevelModel();
        
        // Create a selection object to the deepest part of the tree.
        TreePath elementPath = model.findElement("3.3.3");
        TreeSelection selection = new TreeSelection(elementPath);

        // Set the selection.
        fViewer.setSelection(selection, true, false);

        // Remove the element
        TreePath removePath = model.findElement("3");
        TreePath parentPath = removePath.getParentPath();
        int removeIndex = model.getElement(parentPath).indexOf( model.getElement(removePath) );
        model.removeElementChild(removePath.getParentPath(), removeIndex);

        // Configure a selection listener
        SelectionListener listener = new SelectionListener();
        fViewer.addSelectionChangedListener(listener);

        // Reset the listener to ignore redundant updates.  When elements are removed
        // the viewer may still request updates for those elements. 
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, false, false); 

        // Refresh the viewer
        model.postDelta( new ModelDelta(model.getRootElement(), IModelDelta.CONTENT) );
        while (!fListener.isFinished(TestModelUpdatesListener.ALL_UPDATES_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Check to make sure the selection was made
        // Commented out until JFace bug 219887 is fixed.
        //assertTrue(listener.fEvents.size() == 1);

        // Check that the new selection is empty
        ISelection viewerSelection = fViewer.getSelection();        
        assertTrue(viewerSelection.isEmpty());
    }
}
