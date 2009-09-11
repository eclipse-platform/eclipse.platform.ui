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
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualTreeModelViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Tests which verify the operation of the virtual viewer in the lazy mode.
 * 
 *  @since 3.6
 */
public class VirtualViewerLazyModeTests extends TestCase {
    Display fDisplay;
    Shell fShell;
    ITreeModelViewer fViewer;
    TestModelUpdatesListener fListener;
    
    public VirtualViewerLazyModeTests(String name) {
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

    protected ITreeModelViewer createViewer(Display display, Shell shell) {
        return new VirtualTreeModelViewer(fDisplay, SWT.VIRTUAL, new PresentationContext("TestViewer"));
    }
    
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

    public void testUpdateViewer() {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        
        TestModel model = TestModel.simpleMultiLevel();
        
        // Initialize the listener.  In lazy mode, until a selection 
        // is made, only the root element count should be updated.
        fListener.reset();
        fListener.addChildreCountUpdate(TreePath.EMPTY);

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished(TestModelUpdatesListener.CONTENT_UPDATES_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Do not try to validate data, because the virtual viewer contains only 
        // a subset of model.
        
        // Create the update delta 
        TestElement element = model.getRootElement();
        TreePath path0 = TreePath.EMPTY;
        ModelDelta delta = new ModelDelta(model.getRootElement(), -1, IModelDelta.EXPAND, element.getChildren().length);
        ModelDelta updateDelta = delta;
        element = element.getChildren()[2];
        TreePath path1 = path0.createChildPath(element);
        delta = delta.addNode(element, 2, IModelDelta.EXPAND, element.fChildren.length);
        element = element.getChildren()[1];
        TreePath path2 = path1.createChildPath(element);
        delta = delta.addNode(element, 1, IModelDelta.EXPAND, element.fChildren.length);
        element = element.getChildren()[1];
        TreePath path3 = path2.createChildPath(element);
        delta = delta.addNode(element, 1, IModelDelta.SELECT);
        
        fViewer.updateViewer(updateDelta);

        // Viewer should update data and labels of items in selection
        fListener.reset();
        fListener.addChildreUpdate(path0, 2);
        fListener.addHasChildrenUpdate(path1);
        fListener.addChildreCountUpdate(path1);
        fListener.addLabelUpdate(path1);
        fListener.addChildreUpdate(path1, 1);
        fListener.addHasChildrenUpdate(path2);
        fListener.addChildreCountUpdate(path2);
        fListener.addLabelUpdate(path2);
        fListener.addHasChildrenUpdate(path2);
        fListener.addChildreCountUpdate(path2);
        fListener.addChildreUpdate(path2, 1);
        fListener.addHasChildrenUpdate(path3);
        fListener.addLabelUpdate(path3);

        // When processing expand and select deltas, the label may be requested
        // multiple times for the same element.  So we have to set the fail for 
        // redundant updates to false.
        fListener.setFailOnRedundantUpdates(false);

        while (!fListener.isFinished(TestModelUpdatesListener.CONTENT_UPDATES_COMPLETE | TestModelUpdatesListener.LABEL_UPDATES)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Extract the new state from viewer
        ModelDelta savedDelta = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(path0, savedDelta);
        
        Assert.assertTrue( deltaMatches(updateDelta, savedDelta) );
    }

    boolean deltaMatches(ModelDelta requested, ModelDelta received) {
        if ( requested.getElement().equals(received.getElement()) &&
            requested.getFlags() == received.getFlags() &&
            ( requested.getChildCount() == -1 || requested.getChildCount() == received.getChildCount() )&&
            ( requested.getIndex() == -1 || requested.getIndex() == received.getIndex()) &&
             ((requested.getReplacementElement() != null && requested.getReplacementElement().equals(received.getReplacementElement())) || 
              (requested.getReplacementElement() == null && received.getReplacementElement() == null)) && 
              requested.getChildDeltas().length == received.getChildDeltas().length)   
        {
            for (int i = 0; i < requested.getChildDeltas().length; i++) {
                ModelDelta requestedChildDelta = (ModelDelta)requested.getChildDeltas()[i];
                ModelDelta receivedChildDelta = received.getChildDelta(requestedChildDelta.getElement());
                if ( receivedChildDelta == null || !deltaMatches(requestedChildDelta, receivedChildDelta) ) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
}
