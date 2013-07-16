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

import junit.framework.TestCase;

import org.eclipe.debug.tests.viewer.model.TestModel.TestElement;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.internal.ui.viewers.model.IInternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
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
abstract public class LazyTests extends TestCase implements ITestModelUpdatesListenerConstants {
    
    Display fDisplay;
    Shell fShell;
    IInternalTreeModelViewer fViewer;
    TestModelUpdatesListener fListener;
    
    public LazyTests(String name) {
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
        while (!fShell.isDisposed()) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
    }

    protected void runTest() throws Throwable {
        try {
            super.runTest();
        } catch (Throwable t) {
			throw new ExecutionException("Test failed: " + t.getMessage() + "\n fListener = " + fListener.toString(), t); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    /**
     * Creates a model in the pattern of:
     * 
     * root
     *   1
     *     1.1
     *     1.2
     *     1.3
     *     ...
     *     1.(size)
     */
    private TestModel largeSubtreeModel(int size) {
        TestModel model = new TestModel();
        TestElement[] children = new TestElement[size];
        for (int i = 0; i < size; i++) {
			children[i] = new TestElement(model, "1." + i, new TestElement[0]); //$NON-NLS-1$
        }
		TestElement element = new TestElement(model, "1", children); //$NON-NLS-1$
		model.setRoot(new TestElement(model, "root", new TestElement[] { element })); //$NON-NLS-1$
        
        return model;
    }

    /**
     * Test to make sure that if an element is expanded its children are 
     * not automatically materialized.
     * (bug 305739 and bug 304277)
     */
    public void testExpandLargeSubTree() throws InterruptedException {
        // Create test model with lots of children.
        TestModel model = largeSubtreeModel(1000); 
        
        // NOTE: WE ARE NOT EXPANDING ANY CHILDREN

        // Populate initial view content
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, true, true); 
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}

        // Create delta to expand the "1" element.
        TestElement rootElement = model.getRootElement();
        ModelDelta rootDelta = new ModelDelta(rootElement, IModelDelta.NO_CHANGE);
        ModelDelta expandDelta = model.getBaseDelta(rootDelta);
        TestElement expandElement = rootElement.getChildren()[0];
        expandDelta.addNode(expandElement, 0, IModelDelta.EXPAND, expandElement.getChildren().length);

        // Add first 250 elements as acceptable to materialize
        fListener.reset(); 
        fListener.setFailOnRedundantUpdates(true);
		TreePath expandElementPath = model.findElement("1"); //$NON-NLS-1$
        fListener.addChildreCountUpdate(expandElementPath);
        fListener.addLabelUpdate(expandElementPath); // TODO: not sure why label is updated upon expand?
        for (int i = 0; i < 250; i++) {
            fListener.addChildreUpdate(expandElementPath, i);
            TreePath childPath = expandElementPath.createChildPath(expandElement.getChildren()[i]);
            fListener.addLabelUpdate(childPath);
            fListener.addHasChildrenUpdate(childPath);
        }
        model.postDelta(rootDelta);

        while (!fListener.isFinished(CONTENT_SEQUENCE_COMPLETE | MODEL_CHANGED_COMPLETE | LABEL_SEQUENCE_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
    }

    /**
     * Test to make sure that if an element that is previously selected, is 
     * then selected and replaced, that no extra elements are retrieved.
     * (bug 304277 comment #24, and bug 305739 comment #9).
     */
    public void testReplaceAndSelectInSubTreeTree() throws InterruptedException {
        // Create test model with lots of children.
        TestModel model = largeSubtreeModel(1000); 
        
        // Expand all children
        fViewer.setAutoExpandLevel(-1);
        
        // Populate initial view content, watch for all updates but only wait 
        // for the content update sequence to finish (elements off screen will
        // not be updated).
        // TODO: child count for element 1 is updated multiple times.
        fListener.reset();
        fListener.setFailOnMultipleModelUpdateSequences(true); 
        fListener.setFailOnRedundantUpdates(false);
        fViewer.setInput(model.getRootElement());
		fListener.addLabelUpdate(model.findElement("1.0")); //$NON-NLS-1$
        while (!fListener.isFinished(CONTENT_SEQUENCE_COMPLETE | LABEL_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}

        // Set selection so that the initial selection is not empty
		fViewer.setSelection(new TreeSelection(new TreePath[] { model.findElement("1.0") })); //$NON-NLS-1$
        
        // Create delta to select the "1" element.
        TestElement rootElement = model.getRootElement();
        ModelDelta rootDelta = new ModelDelta(rootElement, IModelDelta.NO_CHANGE);
        ModelDelta baseDelta = model.getBaseDelta(rootDelta);
        TestElement _1Element = rootElement.getChildren()[0];
        ModelDelta _1Delta = baseDelta.addNode(_1Element, 0, IModelDelta.NO_CHANGE, _1Element.getChildren().length);

        // Add the delta to select the "1.1" element.
		TestElement _1_0_newElement = new TestElement(model, "1.0 - new", new TestElement[0]); //$NON-NLS-1$
		TreePath _1ElementPath = model.findElement("1"); //$NON-NLS-1$
        model.replaceElementChild(_1ElementPath, 0, _1_0_newElement);
        _1Delta.addNode(_1_0_newElement, 0, IModelDelta.SELECT);

        // Add element label update and post the delta
        fListener.reset(); 
        fListener.setFailOnRedundantUpdates(true);
		TreePath _1_0_newElementPath = model.findElement("1.0 - new"); //$NON-NLS-1$
        fListener.addLabelUpdate(_1_0_newElementPath);
        model.postDelta(rootDelta);

        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE |  LABEL_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}


        assertEquals(((IStructuredSelection)fViewer.getSelection()).getFirstElement(), _1_0_newElement);
    }

    /**
     */
    public void testContentRefresh() throws InterruptedException {
        // Create test model with lots of children.
        TestModel model = largeSubtreeModel(1000); 
        
        // Expand children all
        fViewer.setAutoExpandLevel(-1);

        // Populate initial view content
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, true); 
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished(CONTENT_SEQUENCE_COMPLETE | LABEL_SEQUENCE_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}

        // Turn off autoexpand
        fViewer.setAutoExpandLevel(0);

        // Reposition the viewer to middle of list
        fListener.reset();
        fListener.setFailOnRedundantUpdates(false);
		fViewer.reveal(model.findElement("1"), 500); //$NON-NLS-1$
        while (!fListener.isFinished(CONTENT_SEQUENCE_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
        
        // Create delta to refresh the "1" element.
        TestElement rootElement = model.getRootElement();
        ModelDelta rootDelta = new ModelDelta(rootElement, IModelDelta.NO_CHANGE);
        ModelDelta expandDelta = model.getBaseDelta(rootDelta);
        TestElement expandElement = rootElement.getChildren()[0];
        expandDelta.addNode(expandElement, 0, IModelDelta.CONTENT, expandElement.getChildren().length);

        // Rinse and repeast.  The refresh in bug 335734 is only triggered 
        // only on the second time.
        for (int repeatCount = 0; repeatCount < 3; repeatCount++) {
            // Add first 250 elements (after element 500) as acceptable to materialize
            fListener.reset(); 
            fListener.setFailOnRedundantUpdates(true);
			TreePath refreshElementPath = model.findElement("1"); //$NON-NLS-1$
            fListener.addRedundantExceptionChildCount(refreshElementPath);
            fListener.addRedundantExceptionLabel(refreshElementPath);
            fListener.addChildreUpdate(TreePath.EMPTY, 0);
            fListener.addHasChildrenUpdate(refreshElementPath);
            fListener.addChildreCountUpdate(refreshElementPath);
            fListener.addLabelUpdate(refreshElementPath); // TODO: not sure why label is updated upon expand?
            for (int i = 499; i < 750; i++) {
                fListener.addChildreUpdate(refreshElementPath, i);
                TreePath childPath = refreshElementPath.createChildPath(expandElement.getChildren()[i]);
                fListener.addLabelUpdate(childPath);
                fListener.addHasChildrenUpdate(childPath);
            }
            model.postDelta(rootDelta);
    
            while (!fListener.isFinished(CONTENT_SEQUENCE_COMPLETE | MODEL_CHANGED_COMPLETE)) {
				if (!fDisplay.readAndDispatch ()) {
					Thread.sleep(0);
				}
			}
        }
    }

}
