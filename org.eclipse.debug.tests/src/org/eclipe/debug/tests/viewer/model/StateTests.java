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
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelContentProviderTarget;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Tests to verify that the viewer can save and restore correctly the expansion 
 * state of elements.
 * 
 * @since 3.6
 */
abstract public class StateTests extends TestCase implements ITestModelUpdatesListenerConstants {
    Display fDisplay;
    Shell fShell;
    ITreeModelViewer fViewer;
    TestModelUpdatesListener fListener;
    
    public StateTests(String name) {
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
        
        // Close the shell and exit.
        fShell.close();
        while (!fShell.isDisposed()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
    }

    public void testUpdateViewer() {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        
        TestModel model = TestModel.simpleMultiLevel();
        
        // Create the listener
        fListener.reset();
        fListener.addChildreUpdate(TreePath.EMPTY, 0);
        fListener.addChildreUpdate(TreePath.EMPTY, 1);
        fListener.addChildreUpdate(TreePath.EMPTY, 2);

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY, true);

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
        
        fListener.reset();
        fViewer.updateViewer(updateDelta);

        fListener.reset();
        fListener.setFailOnRedundantUpdates(false);
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
        
        while (!fListener.isFinished(CONTENT_UPDATES_COMPLETE | LABEL_UPDATES)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Extract the new state from viewer
        ModelDelta savedDelta = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(path0, savedDelta, IModelDelta.EXPAND | IModelDelta.SELECT);
        
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
    
    private TestModel alternatingSubsreesModel() {
        TestModel model = new TestModel();
        model.setRoot( new TestElement(model, "root", new TestElement[] {
            new TestElement(model, "1", new TestElement[] {
                new TestElement(model, "1.1", new TestElement[] {
                    new TestElement(model, "1.1.1", new TestElement[0]),
                }),
            }),
            new TestElement(model, "2", new TestElement[] {
                new TestElement(model, "2.1", new TestElement[] {
                    new TestElement(model, "2.1.1", new TestElement[0]),
                }),
            }),
            new TestElement(model, "3", new TestElement[] {
                new TestElement(model, "3.1", new TestElement[] {
                    new TestElement(model, "3.1.1", new TestElement[0]),
                }),
            }),
            new TestElement(model, "4", new TestElement[] {
                new TestElement(model, "4.1", new TestElement[] {
                    new TestElement(model, "4.1.1", new TestElement[0]),
                }),
            }),
            new TestElement(model, "5", new TestElement[] {
                new TestElement(model, "5.1", new TestElement[] {
                    new TestElement(model, "5.1.1", new TestElement[0]),
                }),
            }),
            new TestElement(model, "6", new TestElement[] {
                new TestElement(model, "6.1", new TestElement[] {
                    new TestElement(model, "6.1.1", new TestElement[0]),
                }),
            })
        }) );
        return model;
    }

    private void expandAlternateElements(TestModel model) {
        // Expand every other child
        fListener.reset(); 
        fListener.setFailOnRedundantUpdates(false);
        ITreeModelContentProviderTarget viewer = (ITreeModelContentProviderTarget)fViewer; 
        TreePath path;
        fListener.addUpdates(path = model.findElement("1"), (TestElement)path.getLastSegment(), 1, CHILDREN_COUNT_UPDATES | CHILDREN_UPDATES);
        viewer.setExpandedState(path, true);
        fListener.addUpdates(path = model.findElement("3"), (TestElement)path.getLastSegment(), 1, CHILDREN_COUNT_UPDATES | CHILDREN_UPDATES);
        viewer.setExpandedState(path, true);
        fListener.addUpdates(path = model.findElement("5"), (TestElement)path.getLastSegment(), 1, CHILDREN_COUNT_UPDATES | CHILDREN_UPDATES);
        viewer.setExpandedState(path, true);
             
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY, true);

        // Expand the sub-children as well (so that the expanded nodes go 2 levels down.
        fListener.reset(); 
        fListener.addUpdates(path = model.findElement("1.1"), (TestElement)path.getLastSegment(), 1, CHILDREN_COUNT_UPDATES | CHILDREN_UPDATES);
        viewer.setExpandedState(path, true);
        fListener.addUpdates(path = model.findElement("3.1"), (TestElement)path.getLastSegment(), 1, CHILDREN_COUNT_UPDATES | CHILDREN_UPDATES);
        viewer.setExpandedState(path, true);
        fListener.addUpdates(path = model.findElement("5.1"), (TestElement)path.getLastSegment(), 1, CHILDREN_COUNT_UPDATES | CHILDREN_UPDATES);
        viewer.setExpandedState(path, true);
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY, true);

    }
    
    public void testPreserveExpandedOnRemove() {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = alternatingSubsreesModel();

        // NOTE: WE ARE NOT EXPANDING ANY CHILDREN
        
        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY, true);

        expandAlternateElements(model);
        
        // Update the model
        ModelDelta delta = model.removeElementChild(TreePath.EMPTY, 0);
        
        // Remove delta should not generate any new updates
        fListener.reset(); 
        model.postDelta(delta);
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY, true);
        ITreeModelContentProviderTarget viewer = (ITreeModelContentProviderTarget)fViewer; 
        Assert.assertTrue(viewer.getExpandedState(model.findElement("2")) == false);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("3")) == true);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("3.1")) == true);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("4")) == false);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("5")) == true);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("5.1")) == true);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("6")) == false);
    }

    public void testPreserveExpandedOnInsert() {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = alternatingSubsreesModel();

        // NOTE: WE ARE NOT EXPANDING ANY CHILDREN
        
        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY, true);

        expandAlternateElements(model);
        
        // Update the model
        ModelDelta delta = model.insertElementChild(TreePath.EMPTY, 0, new TestElement(model, "0 - new", new TestElement[0]));
        
        // Insert delta should generate updates only for the new element
        TreePath path = model.findElement("0 - new");
        // Note: redundant label updates on insert.
        fListener.reset(path, (TestElement)path.getLastSegment(), 0, false, false); 
        fListener.addChildreUpdate(TreePath.EMPTY, 0);
        model.postDelta(delta);
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE | ALL_UPDATES_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY, true);
        ITreeModelContentProviderTarget viewer = (ITreeModelContentProviderTarget)fViewer; 
        Assert.assertTrue(viewer.getExpandedState(model.findElement("1")) == true);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("1.1")) == true);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("2")) == false);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("3")) == true);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("3.1")) == true);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("4")) == false);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("5")) == true);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("5.1")) == true);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("6")) == false);
    }


    public void testPreserveExpandedOnContent() {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = alternatingSubsreesModel();

        // NOTE: WE ARE NOT EXPANDING ANY CHILDREN
        
        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY, true);

        expandAlternateElements(model);
        
        // Update the model
        model.removeElementChild(TreePath.EMPTY, 0);
        
        // Content delta should generate updates for the elements being re-expanded
        ITreeModelContentProviderTarget viewer = (ITreeModelContentProviderTarget)fViewer;
        // Note: Re-expanding nodes causes redundant updates.
        fListener.reset(false, false);
        fListener.addUpdates(viewer, TreePath.EMPTY, model.getRootElement(), -1, ALL_UPDATES_COMPLETE); 
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY, true);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("2")) == false);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("3")) == true);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("3.1")) == true);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("4")) == false);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("5")) == true);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("5.1")) == true);
        Assert.assertTrue(viewer.getExpandedState(model.findElement("6")) == false);
    }

}
