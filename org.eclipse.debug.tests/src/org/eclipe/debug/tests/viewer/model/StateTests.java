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
import org.eclipse.jface.viewers.TreeSelection;
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
        fViewer.addStateUpdateListener(fListener);

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
        fViewer.addStateUpdateListener(fListener);
        
        // Close the shell and exit.
        fShell.close();
        while (!fShell.isDisposed()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
    }

    protected ITreeModelContentProviderTarget getCTargetViewer() {
        return (ITreeModelContentProviderTarget)fViewer;
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
        
        fListener.reset(false, false);

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
        
        fViewer.updateViewer(updateDelta);
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
        
        // Set a selection in view
        TreeSelection originalSelection = new TreeSelection(model.findElement("5.1.1"));
        fViewer.setSelection(originalSelection);

        // Update the model
        ModelDelta delta = model.removeElementChild(TreePath.EMPTY, 0);
        
        // Remove delta should not generate any new updates
        fListener.reset(); 
        model.postDelta(delta);
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        
        // Validate data
        model.validateData(fViewer, TreePath.EMPTY, true);
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("2")) == false);
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("3")) == true);
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("3.1")) == true);
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("4")) == false);
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("5")) == true);
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("5.1")) == true);
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("6")) == false);
        Assert.assertEquals(originalSelection, fViewer.getSelection());
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
        
        // Set a selection in view
        TreeSelection originalSelection = new TreeSelection(model.findElement("5.1.1"));
        fViewer.setSelection(originalSelection);
        
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
        
        // Validate data
        model.validateData(fViewer, TreePath.EMPTY, true);
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("1")) == true);
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("1.1")) == true);
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("2")) == false);
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("3")) == true);
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("3.1")) == true);
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("4")) == false);
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("5")) == true);
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("5.1")) == true);
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("6")) == false);
        Assert.assertEquals(originalSelection, fViewer.getSelection());
    }

    public void testPreserveExpandedOnContentStress() {
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
        
        // Set a selection in view
        TreeSelection originalSelection = new TreeSelection(model.findElement("5.1.1"));
        fViewer.setSelection(originalSelection);
        Assert.assertEquals(originalSelection, fViewer.getSelection());

        // Run this test ten times as we've seen intermittent failures related 
        // to timing in it.
        for (int i = 0; i < 10; i++) {
            // Update the model
            model.removeElementChild(TreePath.EMPTY, 0);
            
            // Note: Re-expanding nodes causes redundant updates.
            fListener.reset(false, false);
            fListener.addUpdates(getCTargetViewer(), TreePath.EMPTY, model.getRootElement(), -1, ALL_UPDATES_COMPLETE); 
            model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
            while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) 
                if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

            // Validate data
            model.validateData(fViewer, TreePath.EMPTY, true);
            Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("2")) == false);
            Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("3")) == true);
            Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("3.1")) == true);
            Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("4")) == false);
            Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("5")) == true);
            Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("5.1")) == true);
            Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("6")) == false);
            Assert.assertEquals(originalSelection, fViewer.getSelection());
            
            // Update the model again
            model.addElementChild(TreePath.EMPTY, 0, new TestElement(model, "1", new TestElement[0]));
            
            // Note: Re-expanding nodes causes redundant updates.
            fListener.reset(false, false);
            fListener.addUpdates(getCTargetViewer(), TreePath.EMPTY, model.getRootElement(), -1, ALL_UPDATES_COMPLETE); 
            model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
            while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) 
                if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

            // Validate data
            model.validateData(fViewer, TreePath.EMPTY, true);
            Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("2")) == false);
            Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("3")) == true);
            Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("3.1")) == true);
            Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("4")) == false);
            Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("5")) == true);
            Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("5.1")) == true);
            Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("6")) == false);
            Assert.assertEquals(originalSelection, fViewer.getSelection());
        }
    }

//    public void testPreserveSetSelectionAfterContent() {
//        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
//        TestModel model = TestModel.simpleMultiLevel();
//
//        // Expand all
//        fViewer.setAutoExpandLevel(-1);
//        
//        // Create the listener.
//        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 
//
//        // Set the input into the view and update the view.
//        fViewer.setInput(model.getRootElement());
//        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
//        model.validateData(fViewer, TreePath.EMPTY, true);
//
//        // Set a selection in view
//        fViewer.setSelection(new TreeSelection(model.findElement("3.1.1")));
//
//        // Reset the listener (ignore redundant updates)
//        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, false, false);
//        
//        // Refresh content.
//        // Note: Wait only for the processing of the delta, nto for all updates
//        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
//        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
//            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
//
//        // Change the selection to something new in the view
//        TreeSelection newSelection = new TreeSelection(model.findElement("2.1"));
//        fViewer.setSelection(newSelection);
//        
//        // Wait for all the updates to cimplete (note: we're not resetting the listener.
//        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
//        while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) 
//            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
//
//        // Check to make sure that the state restore didn't change the selection.
//        Assert.assertEquals(newSelection, fViewer.getSelection());
//    }

    
    public void testPreserveSelectionDeltaAfterContent() {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = TestModel.simpleMultiLevel();

        // Expand all
        fViewer.setAutoExpandLevel(-1);
        
        // Create the listener.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY, true);

        // Set a selection in view
        fViewer.setSelection(new TreeSelection(model.findElement("3.1.1")));

        // Reset the listener (ignore redundant updates)
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, false, false);
        
        // Refresh content.
        // Note: Wait only for the processing of the delta, nto for all updates
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Update the viewer with new selection delta to something new in the view
        ModelDelta selectDelta = model.makeElementDelta(model.findElement("2.1"), IModelDelta.SELECT);

        // Wait for the second model dleta to process
        fListener.resetModelChanged();
        model.postDelta(selectDelta);
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        
        // Wait for all the updates to cimplete (note: we're not resetting the listener.
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Check to make sure that the state restore didn't change the selection.
        Assert.assertEquals(new TreeSelection(model.findElement("2.1")), fViewer.getSelection());
    }

    public void testPreserveCollapseDeltaAfterContent() {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = TestModel.simpleMultiLevel();

        // Expand all
        fViewer.setAutoExpandLevel(-1);
        
        // Create the listener.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY, true);

        // Turn off auto-expand
        fViewer.setAutoExpandLevel(0);
        
        // Reset the listener (ignore redundant updates)
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, false, false);
        
        // Refresh content.
        // Note: Wait only for the processing of the delta, not for all updates
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Update the viewer to collapse an element
        ModelDelta collapseDelta = model.makeElementDelta(model.findElement("3.1"), IModelDelta.COLLAPSE);
        
        // Remove updates for the collapsed element from listener, because they 
        // will never happen if the element remains collapsed.
        fListener.resetModelChanged();
        fListener.removeLabelUpdate(model.findElement("3.1.1"));
        fListener.removeLabelUpdate(model.findElement("3.1.2"));
        fListener.removeLabelUpdate(model.findElement("3.1.3"));
        fListener.removeHasChildrenUpdate(model.findElement("3.1.1"));
        fListener.removeHasChildrenUpdate(model.findElement("3.1.2"));
        fListener.removeHasChildrenUpdate(model.findElement("3.1.3"));
        fListener.removeChildreCountUpdate(model.findElement("3.1"));
        fListener.removeChildrenUpdate(model.findElement("3.1"), 0);
        fListener.removeChildrenUpdate(model.findElement("3.1"), 1);
        fListener.removeChildrenUpdate(model.findElement("3.1"), 2);

        // Wait for the second model delta to process
        model.postDelta(collapseDelta);
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        
        // Wait for all the updates to complete (note: we're not resetting the listener.
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Check to make sure that the state restore didn't change the selection.
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("3.1")) == false);
    }

    public void testPreserveExpandDeltaAfterContent() {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = TestModel.simpleMultiLevel();

        // Note: Do not auto-expand!
        
        // Create the listener.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY, true);

        // Reset the listener (ignore redundant updates)
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, false, false);
        
        // Refresh content.
        // Note: Wait only for the processing of the delta, not for all updates
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Update the viewer to expand an element
        ModelDelta expandDelta = model.makeElementDelta(model.findElement("3.1"), IModelDelta.EXPAND);
        
        // Wait for the second model delta to process
        fListener.resetModelChanged();
        model.postDelta(expandDelta);
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        
        // Wait for all the updates to complete (note: we're not resetting the listener.
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Check to make sure that the state restore didn't change the selection.
        Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("3.1")) == true);
    }

    
    public void testSaveAndRstore1() {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = alternatingSubsreesModel();

        // NOTE: WE ARE NOT EXPANDING ANY CHILDREN
        
        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY, true);

        // Expand some, but not all elements
        expandAlternateElements(model);

        // Set a selection in view
        fViewer.setSelection(new TreeSelection(model.findElement("5.1.1")));
        
        // Extract the original state from viewer
        ModelDelta originalState = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(TreePath.EMPTY, originalState, IModelDelta.EXPAND | IModelDelta.SELECT);

        // Set the viewer input to null.  This will trigger the view to save the viewer state.
        fListener.reset(true, false);
        fListener.addStateUpdates(getCTargetViewer(), TreePath.EMPTY, model.getRootElement());
        fViewer.setInput(null);
        while (!fListener.isFinished(STATE_SAVE_COMPLETE)) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
                
        // Set the viewer input back to the model.  When view updates are complete
        // the viewer 
        // Note: disable redundant updates because the reveal delta triggers one.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, false, false);
        // TODO: add state updates somehow?
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Extract the restored state from viewer
        ModelDelta restoredState = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(TreePath.EMPTY, restoredState, IModelDelta.EXPAND | IModelDelta.SELECT);

        Assert.assertTrue( deltaMatches(originalState, restoredState) );
    }

    public void testSaveAndRstore2() {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = TestModel.simpleMultiLevel();

        // expand all elements
        fViewer.setAutoExpandLevel(-1);
        
        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        model.validateData(fViewer, TreePath.EMPTY);

        // Set a selection in view
        fViewer.setSelection(new TreeSelection(model.findElement("3.2.3")));
                
        // Turn off the auto-expand now since we want to text the auto-expand logic
        fViewer.setAutoExpandLevel(-1);

        // Extract the original state from viewer
        ModelDelta originalState = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(TreePath.EMPTY, originalState, IModelDelta.EXPAND | IModelDelta.SELECT);

        // Set the viewer input to null.  This will trigger the view to save the viewer state.
        fListener.reset(true, false);
        fListener.addStateUpdates(getCTargetViewer(), TreePath.EMPTY, model.getRootElement());

        fViewer.setInput(null);
        while (!fListener.isFinished(STATE_SAVE_COMPLETE)) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Set the viewer input back to the model.  When view updates are complete
        // the viewer 
        // Note: disable redundant updates because the reveal delta triggers one.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, false, false);
        // TODO: add state updates somehow?
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        // Extract the restored state from viewer
        ModelDelta restoredState = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(TreePath.EMPTY, restoredState, IModelDelta.EXPAND | IModelDelta.SELECT);

        Assert.assertTrue( deltaMatches(originalState, restoredState) );
    }

}
