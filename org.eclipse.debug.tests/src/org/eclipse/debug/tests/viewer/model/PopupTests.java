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
package org.eclipse.debug.tests.viewer.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.debug.internal.ui.viewers.model.IInternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.tests.viewer.model.TestModel.TestElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Tests to verify that the viewer property updates when created
 * with the SWT.POPUP style.
 *
 * @since 3.6
 */
abstract public class PopupTests extends TestCase implements ITestModelUpdatesListenerConstants {
    Display fDisplay;
    Shell fShell;
    ITreeModelViewer fViewer;
    TestModelUpdatesListener fListener;

    public PopupTests(String name) {
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

        fViewer = createViewer(fDisplay, fShell, SWT.POP_UP);

        fListener = new TestModelUpdatesListener(fViewer, false, false);

        fShell.open ();
    }

    protected IInternalTreeModelViewer getCTargetViewer() {
        return (IInternalTreeModelViewer)fViewer;
    }


    abstract protected ITreeModelViewer createViewer(Display display, Shell shell, int style);

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

    /**
     * This test verifies that content updates are still being performed.
     */
    public void testRefreshStruct() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);

        TestModel model = TestModel.simpleSingleLevel();
        fViewer.setAutoExpandLevel(-1);

        // Create the listener
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false);

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
        model.validateData(fViewer, TreePath.EMPTY);

        // Update the model
        TestElement element = model.getRootElement().getChildren()[0];
        TreePath elementPath = new TreePath(new Object[] { element });
        TestElement[] newChildren = new TestElement[] {
 new TestElement(model, "1.1 - new", new TestElement[0]), //$NON-NLS-1$
		new TestElement(model, "1.2 - new", new TestElement[0]), //$NON-NLS-1$
		new TestElement(model, "1.3 - new", new TestElement[0]), //$NON-NLS-1$
        };
        ModelDelta delta = model.setElementChildren(elementPath, newChildren);

        fListener.reset(elementPath, element, -1, true, false);
        model.postDelta(delta);
        while (!fListener.isFinished()) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
        model.validateData(fViewer, TreePath.EMPTY);
    }

    /**
     * This test verifies that expand and select updates are being ignored.
     */
    public void testExpandAndSelect() throws InterruptedException {
        TestModel model = TestModel.simpleMultiLevel();

        // Create the listener
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, true, false);

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
        model.validateData(fViewer, TreePath.EMPTY, true);

        // Create the delta
        fListener.reset();
        // TODO Investigate: there seem to be unnecessary updates being issued
        // by the viewer.  These include the updates that are commented out:
        // For now disable checking for extra updates.
        fListener.setFailOnRedundantUpdates(false);
        TestElement element = model.getRootElement();
        TreePath path_root = TreePath.EMPTY;
        ModelDelta delta= new ModelDelta(model.getRootElement(), -1, IModelDelta.EXPAND, element.getChildren().length);
        ModelDelta deltaRoot = delta;
        element = element.getChildren()[2];
        TreePath path_root_3 = path_root.createChildPath(element);
        delta.addNode(element, 2, IModelDelta.SELECT | IModelDelta.EXPAND, element.fChildren.length);

        // Validate the expansion state BEFORE posting the delta.

        IInternalTreeModelViewer contentProviderViewer = (IInternalTreeModelViewer)fViewer;
        assertFalse(contentProviderViewer.getExpandedState(path_root_3));

        model.postDelta(deltaRoot);
        while (true) {
            if (fListener.isFinished(MODEL_CHANGED_COMPLETE)) {
                if (fListener.isFinished(CONTENT_SEQUENCE_STARTED)) {
                    if (fListener.isFinished(CONTENT_SEQUENCE_COMPLETE)) {
                        break;
                    }
                } else {
                    break;
                }
            }
            if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
        }
        model.validateData(fViewer, TreePath.EMPTY, true);

        // Validate the expansion state AFTER posting the delta.
        assertFalse(contentProviderViewer.getExpandedState(path_root_3));

        // Verify selection
        ISelection selection = fViewer.getSelection();
        if (selection instanceof ITreeSelection) {
			List<TreePath> selectionPathsList = Arrays.asList(((ITreeSelection) selection).getPaths());
            assertFalse(selectionPathsList.contains(path_root_3));
        } else {
			fail("Not a tree selection"); //$NON-NLS-1$
        }
    }



    public void testPreserveExpandedOnSubTreeContent() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = TestModel.simpleMultiLevel();

        // Expand all
        fViewer.setAutoExpandLevel(-1);

        // Create the listener,
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false);

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
        model.validateData(fViewer, TreePath.EMPTY, true);

        // Turn off auto-expansion
        fViewer.setAutoExpandLevel(0);

        // Set a selection in view
		TreeSelection originalSelection = new TreeSelection(model.findElement("3.3.1")); //$NON-NLS-1$
        fViewer.setSelection(originalSelection);

        // Update the model
		model.addElementChild(model.findElement("3"), null, 0, new TestElement(model, "3.0 - new", new TestElement[0])); //$NON-NLS-1$ //$NON-NLS-2$

        // Create the delta for element "3" with content update.
		TreePath elementPath = model.findElement("3"); //$NON-NLS-1$
        ModelDelta rootDelta = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        ModelDelta elementDelta = model.getElementDelta(rootDelta, elementPath, true);
        elementDelta.setFlags(IModelDelta.CONTENT);

        // Note: Re-expanding nodes causes redundant updates.
        fListener.reset(false, false);
        fListener.addUpdates(getCTargetViewer(), elementPath, model.getElement(elementPath), -1, ALL_UPDATES_COMPLETE);

        // Post the sub-tree update
        model.postDelta(rootDelta);
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}

        // Validate data
        model.validateData(fViewer, TreePath.EMPTY, true);
		assertTrue(getCTargetViewer().getExpandedState(model.findElement("3")) == true); //$NON-NLS-1$
        // On windows, getExpandedState() may return true for an element with no children:
        // assertTrue(getCTargetViewer().getExpandedState(model.findElement("3.0 - new")) == false);
		assertTrue(getCTargetViewer().getExpandedState(model.findElement("3.1")) == true); //$NON-NLS-1$
		assertTrue(getCTargetViewer().getExpandedState(model.findElement("3.2")) == true); //$NON-NLS-1$
		assertTrue(getCTargetViewer().getExpandedState(model.findElement("3.3")) == true); //$NON-NLS-1$
        assertTrue( areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );
    }

    private boolean areTreeSelectionsEqual(ITreeSelection sel1, ITreeSelection sel2) {
		Set<TreePath> sel1Set = new HashSet<TreePath>();
        sel1Set.addAll( Arrays.asList(sel1.getPaths()) );

		Set<TreePath> sel2Set = new HashSet<TreePath>();
        sel2Set.addAll( Arrays.asList(sel2.getPaths()) );

        return sel1Set.equals(sel2Set);
    }


}
