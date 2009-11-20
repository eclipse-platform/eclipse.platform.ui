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

import org.eclipse.debug.internal.ui.viewers.model.ITreeModelContentProviderTarget;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer;
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
abstract public class ContentTests extends TestCase {
    
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
        fShell = new Shell(fDisplay/*, SWT.ON_TOP | SWT.SHELL_TRIM*/);
        fShell.setMaximized(true);
        fShell.setLayout(new FillLayout());

        fViewer = createViewer(fDisplay, fShell);
        
        fListener = new TestModelUpdatesListener(true, true);
        fViewer.addViewerUpdateListener(fListener);
        fViewer.addLabelUpdateListener(fListener);
        fViewer.addModelChangedListener(fListener);

        fShell.open ();
    }

    abstract protected ITreeModelContentProviderTarget createViewer(Display display, Shell shell);
    
    /**
     * @throws java.lang.Exception
     */
    protected void tearDown() throws Exception {
        fViewer.removeLabelUpdateListener(fListener);
        fViewer.removeViewerUpdateListener(fListener);
        fViewer.removeModelChangedListener(fListener);
        
        // Close the shell and exit.
        fShell.close();
        while (!fShell.isDisposed()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
    }

    public void testSimpleSingleLevel() {
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
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        
        model.validateData(fViewer, TreePath.EMPTY);
    }

    public void testSimpleMultiLevel() {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        
        TestModel model = TestModel.simpleMultiLevel();
        fViewer.setAutoExpandLevel(-1);
        
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, true); 
        
        fViewer.setInput(model.getRootElement());

        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        model.validateData(fViewer, TreePath.EMPTY);
    }
    
}
