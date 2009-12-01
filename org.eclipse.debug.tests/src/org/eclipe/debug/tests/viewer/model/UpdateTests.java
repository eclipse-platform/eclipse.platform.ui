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
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Tests to verify that the viewer property updates following changes in the 
 * model, following simple content update deltas from the model.  
 * 
 * @since 3.6
 */
abstract public class UpdateTests extends TestCase {
    Display fDisplay;
    Shell fShell;
    ITreeModelViewer fViewer;
    TestModelUpdatesListener fListener;
    
    public UpdateTests(String name) {
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

    /**
      * This test:
     * - creates a simple model
     * - replaces the list of elements with a shorter list of elements
     * - refreshes the viewer
     */
    public void testRemoveElements() {
        TestModel model = TestModel.simpleSingleLevel();
        fViewer.setAutoExpandLevel(-1);

        // Create the listener
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY);
        
        // Update the model
        TestElement root = model.getRootElement();
        TreePath rootPath = new TreePath(new Object[] {});
        TestElement[] newElements = new TestElement[] {
            new TestElement(model, "1", new TestElement[0]),
            new TestElement(model, "2", new TestElement[0]),
            new TestElement(model, "3", new TestElement[0]),
        };
        model.setElementChildren(rootPath, newElements);
        
        // Reset the listener to NOT fail on redundant updates.
        // When elements are remvoed from the model and the model is 
        // refreshed the viewer will issue an IChildrenUpdate for the 
        // missing elements as an optimization.
        fListener.reset(rootPath, root, -1, false, false);
        
        model.postDelta(new ModelDelta(root, IModelDelta.CONTENT));
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY);
    }

    /**
     * This test: 
     * - creates a simple model
     * - sets a list of children to one of the elements
     * - refreshes the viewer
     */
    public void testAddNewChildren() {
        TestModel model = TestModel.simpleSingleLevel();
        fViewer.setAutoExpandLevel(-1);

        // Create the listener
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY);
        
        // Update the model
        TestElement element = model.getRootElement().getChildren()[0];
        TreePath elementPath = new TreePath(new Object[] { element });
        TestElement[] newChildren = new TestElement[] {
            new TestElement(model, "1.1", new TestElement[0]),
            new TestElement(model, "1.2", new TestElement[0]),
            new TestElement(model, "1.3", new TestElement[0]),
        };
        model.setElementChildren(elementPath, newChildren);

        // Reset the viewer to ignore redundant updates.  The '1' element 
        // will be updated for "hasChildren" before it is expanded, which is 
        // expected.
        TreePath rootPath = TreePath.EMPTY;
        TestElement rootElement = model.getRootElement();
        fListener.reset(rootPath, rootElement, -1, false, false); 

        // Refresh the viewer
        model.postDelta(new ModelDelta(rootElement, IModelDelta.CONTENT));
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY);
    }

    
    private void removeElement(TestModel model, int index) {
        ModelDelta delta = model.removeElementChild(TreePath.EMPTY, index);
        
        // Remove delta should generate no new updates, but we still need to wait for the event to
        // be processed.
        fListener.reset(); 
        model.postDelta(delta);
        while (!fListener.isFinished(TestModelUpdatesListener.MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY);        
    }
    
    private void addElement(TestModel model, String label, int position) {
        ModelDelta delta = model.addElementChild(TreePath.EMPTY, position, new TestElement(model, label, new TestElement[0]));
        
        // Remove delta should generate no new updates, but we still need to wait for the event to
        // be processed.
        fListener.reset(); 
        model.postDelta(delta);
        while (!fListener.isFinished(TestModelUpdatesListener.MODEL_CHANGED_COMPLETE | TestModelUpdatesListener.CONTENT_UPDATES_COMPLETE | TestModelUpdatesListener.LABEL_UPDATES_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY);                
    }
    
    public void testRepeatedAddRemoveElement() {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);

        TestModel model = TestModel.simpleSingleLevel();
        fViewer.setAutoExpandLevel(-1);

        // Create the listener
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, false, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY);
        
        // Update the model
        removeElement(model, 2);
        addElement(model, "3-new", 3);
        removeElement(model, 4);
        addElement(model, "5-new", 5);
        removeElement(model, 1);
        addElement(model, "1-new", 1);
        removeElement(model, 3);
        addElement(model, "4-new", 4);
    }

}
