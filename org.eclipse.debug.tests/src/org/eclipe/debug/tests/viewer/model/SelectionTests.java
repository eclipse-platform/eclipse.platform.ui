/*******************************************************************************
 * Copyright (c) 2009, 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     IBM Corporation - bug fixing
 *******************************************************************************/
package org.eclipe.debug.tests.viewer.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
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
abstract public class SelectionTests extends TestCase implements ITestModelUpdatesListenerConstants {
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
    @Override
	protected void setUp() throws Exception {
        fDisplay = PlatformUI.getWorkbench().getDisplay();
        fShell = new Shell(fDisplay);
        fShell.setMaximized(true);
        fShell.setLayout(new FillLayout());

        fViewer = createViewer(fDisplay, fShell);

        fListener = new TestModelUpdatesListener(fViewer, false, false);

        fShell.open ();
    }

    abstract protected ITreeModelViewer createViewer(Display display, Shell shell);

    /**
     * @throws java.lang.Exception
     */
    @Override
	protected void tearDown() throws Exception {
        fListener.dispose();
        fViewer.getPresentationContext().dispose();

        // Close the shell and exit.
        fShell.close();
        while (!fShell.isDisposed()) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
    }

    private static class SelectionListener implements ISelectionChangedListener {
		private final List<SelectionChangedEvent> fEvents = new ArrayList<SelectionChangedEvent>(1);

        @Override
		public void selectionChanged(SelectionChangedEvent event) {
            fEvents.add(event);
        }
    }

    private TestModel makeMultiLevelModel() throws InterruptedException {
        TestModel model = TestModel.simpleMultiLevel();
        fViewer.setAutoExpandLevel(-1);
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false);
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
        model.validateData(fViewer, TreePath.EMPTY);
        return model;
    }

    /**
     * In this test:
     * - set selection to an element deep in the model
     * - verify that selection chagned listener is called
     * - verify that the selection is in the viewer is correct
     */
    public void testSimpleSetSelection() throws InterruptedException {
        // Create the model and populate the view.
        TestModel model = makeMultiLevelModel();

        // Create a selection object to the deepest part of the tree.
        SelectionListener listener = new SelectionListener();
        fViewer.addSelectionChangedListener(listener);

        // Set the selection and verify that the listener is called.
		TreeSelection selection = new TreeSelection(model.findElement("3.3.3")); //$NON-NLS-1$
        fViewer.setSelection(selection, true, false);
        assertTrue(listener.fEvents.size() == 1);

        // Check that the new selection is what was requested.
        ISelection viewerSelection = fViewer.getSelection();
        assertEquals(selection, viewerSelection);
    }

    /**
     * In this test verify that selection policy can prevent selection
     * from being set and verify that a FORCE flag can override the selection
     * policy.
     */
    public void testSelectionPolicy() throws InterruptedException {
        // Create the model and populate the view.
        final TestModel model = makeMultiLevelModel();

        // Set the selection and verify it.
		TreeSelection selection_3_3_3 = new TreeSelection(model.findElement("3.3.3")); //$NON-NLS-1$
        fViewer.setSelection(selection_3_3_3, true, false);
        assertEquals(selection_3_3_3, fViewer.getSelection());

        model.setSelectionPolicy(new IModelSelectionPolicy() {

            @Override
			public ISelection replaceInvalidSelection(ISelection invalidSelection, ISelection newSelection) {
                return null;
            }

            @Override
			public boolean overrides(ISelection existing, ISelection candidate, IPresentationContext context) {
                return false;
            }

            @Override
			public boolean isSticky(ISelection selection, IPresentationContext context) {
                return true;
            }

            @Override
			public boolean contains(ISelection selection, IPresentationContext context) {
                return true;
            }
        });

        // Attempt to change selection and verify that old selection is still valid.
		TreeSelection selection_3_3_1 = new TreeSelection(model.findElement("3.3.1")); //$NON-NLS-1$
        fViewer.setSelection(selection_3_3_1, true, false);
        assertEquals(selection_3_3_3, fViewer.getSelection());

        // Now attempt to *force* selection and verify that new selection was set.
        fViewer.setSelection(selection_3_3_1, true, true);
        assertEquals(selection_3_3_1, fViewer.getSelection());

        // Create the an update delta to attempt to change selection back to
        // 3.3.3 and verify that selection did not get overriden.
		TreePath path_3_3_3 = model.findElement("3.3.3"); //$NON-NLS-1$
        ModelDelta baseDelta = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        ModelDelta delta_3_3_3 = model.getElementDelta(baseDelta, path_3_3_3, false);
        delta_3_3_3.setFlags(IModelDelta.SELECT);
        fViewer.updateViewer(baseDelta);
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
        assertEquals(selection_3_3_1, fViewer.getSelection());

        // Add the *force* flag to the selection delta and update viewer again.
        // Verify that selection did change.
        delta_3_3_3.setFlags(IModelDelta.SELECT | IModelDelta.FORCE);
        fViewer.updateViewer(baseDelta);
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
        assertEquals(selection_3_3_3, fViewer.getSelection());
    }


    /**
     * In this test:
     * - set a seleciton to an element
     * - then remove that element
     * - update the view with remove delta
     * -> The selection should be re-set to empty.
     */
    public void testSelectRemove() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);

        // Create the model and populate the view.
        TestModel model = makeMultiLevelModel();

        // Create a selection object to the deepest part of the tree.
		TreePath elementPath = model.findElement("3.3.3"); //$NON-NLS-1$
        TreeSelection selection = new TreeSelection(elementPath);

        // Set the selection.
        fViewer.setSelection(selection, true, false);

        // Remove the element
		TreePath removePath = model.findElement("3"); //$NON-NLS-1$
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
        while (!fListener.isFinished(ITestModelUpdatesListenerConstants.MODEL_CHANGED_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}

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
    public void testSelectRemoveRefreshStruct() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);

        // Create the model and populate the view.
        TestModel model = makeMultiLevelModel();

        // Create a selection object to the deepest part of the tree.
		TreePath elementPath = model.findElement("3.3.3"); //$NON-NLS-1$
        TreeSelection selection = new TreeSelection(elementPath);

        // Set the selection.
        fViewer.setSelection(selection, true, false);

        // Remove the element
		TreePath removePath = model.findElement("3"); //$NON-NLS-1$
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
        while (!fListener.isFinished(ITestModelUpdatesListenerConstants.ALL_UPDATES_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}

        // Check to make sure the selection was made
        // Commented out until JFace bug 219887 is fixed.
        //assertTrue(listener.fEvents.size() == 1);

        // Check that the new selection is empty
        ISelection viewerSelection = fViewer.getSelection();
        assertTrue(viewerSelection.isEmpty());
    }
}
