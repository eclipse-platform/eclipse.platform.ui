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

import junit.framework.TestCase;

import org.eclipe.debug.tests.viewer.model.TestModel.TestElement;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.internal.ui.viewers.model.IInternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Tests which verify the check box support.  This test is very similar to the 
 * content test except that the extending class should create a viewer with 
 * the SWT.CHECK style enabled. <br>  
 * Most of the  check box verification is performed in the test model.
 * 
 * @since 3.6
 */
abstract public class CheckTests extends TestCase {
    Display fDisplay;
    Shell fShell;
    ITreeModelViewer fViewer;
    TestModelUpdatesListener fListener;
    
    public CheckTests(String name) {
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

    public void testSimpleSingleLevel() throws InterruptedException {
        // Create the model with test data
        TestModel model = TestModel.simpleSingleLevel();

        // Make sure that all elements are expanded
        fViewer.setAutoExpandLevel(-1);
        
        // Create the agent which forces the tree to populate
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        
        // Create the listener which determines when the view is finished updating.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 
        
        // Set the viewer input (and trigger updates).
        fViewer.setInput(model.getRootElement());
        
        // Wait for the updates to complete.
        while (!fListener.isFinished()) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
        
        model.validateData(fViewer, TreePath.EMPTY);
    }

    public void testSimpleMultiLevel() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        
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
    }

    // TODO: no idea how to trigger a toggle event on an item
//    public void testCheckReceiver() {
//        // Initial setup
//        TestModel model = TestModel.simpleSingleLevel();
//        fViewer.setAutoExpandLevel(-1);
//        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
//        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 
//        fViewer.setInput(model.getRootElement());
//        
//        // Wait for the updates to complete and validate.
//        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
//        model.validateData(fViewer, TreePath.EMPTY);
//        
//        InternalTreeModelViewer treeViewer = ((InternalTreeModelViewer)fViewer); 
//        TreePath elementPath = model.findElement("1");
//        TestElement element = model.getElement(elementPath);
//        boolean initialCheckState = element.getChecked();
//        Event event = new Event();
//        event.item = treeViewer.findItem(elementPath);
//        event.detail = SWT.CHECK;
//        event.display = fDisplay;
//        event.type = SWT.Selection;
//        event.widget = treeViewer.getControl();
//        fDisplay.post(event);
//
//        while (fDisplay.readAndDispatch ());
//        
//        Assert.assertTrue(element.getChecked() != initialCheckState);
//    }

    public void testUpdateCheck() throws InterruptedException {
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
        ModelDelta delta = model.setElementChecked(elementPath, false, false);
        
        fListener.reset(elementPath, element, -1, true, false); 
        model.postDelta(delta);
        while (!fListener.isFinished(ITestModelUpdatesListenerConstants.LABEL_COMPLETE | ITestModelUpdatesListenerConstants.MODEL_CHANGED_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
        model.validateData(fViewer, TreePath.EMPTY);
    }

}
