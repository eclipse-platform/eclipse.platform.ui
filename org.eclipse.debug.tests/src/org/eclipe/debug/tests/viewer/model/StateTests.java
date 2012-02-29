/*******************************************************************************
 * Copyright (c) 2009, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipe.debug.tests.viewer.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipe.debug.tests.viewer.model.TestModel.TestElement;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.internal.ui.viewers.model.IInternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.ITreeSelection;
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
    protected void tearDown() throws Exception, InterruptedException {
        fListener.dispose();
        fViewer.getPresentationContext().dispose();
        
        // Close the shell and exit.
        fShell.close();
        while (!fShell.isDisposed()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
    }

    protected void runTest() throws Throwable {
        try {
            super.runTest();
        } catch (Throwable t) {
            throw new ExecutionException("Test failed: " + t.getMessage() + "\n fListener = " + fListener.toString(), t);
        }
    }
    
    protected IInternalTreeModelViewer getInternalViewer() {
        return (IInternalTreeModelViewer)fViewer;
    }
    
    public void testUpdateViewer() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        
        TestModel model = TestModel.simpleMultiLevel();
        
        // Create the listener
        fListener.reset();
        fListener.addChildreUpdate(TreePath.EMPTY, 0);
        fListener.addChildreUpdate(TreePath.EMPTY, 1);
        fListener.addChildreUpdate(TreePath.EMPTY, 2);

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
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
        while (!fListener.isFinished(CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Extract the new state from viewer
        ModelDelta savedDelta = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(path0, savedDelta, IModelDelta.EXPAND | IModelDelta.SELECT);
        
        if (!deltaMatches(updateDelta, savedDelta) ) {
            Assert.fail("Expected:\n" + updateDelta.toString() + "\nGot:\n" + savedDelta); 
        }
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

    /**
     * Creates a model in the pattern of:
     * 
     * root
     *   1
     *     1.1
     *       1.1.1
     *   2
     *     2.1
     *       2.1.1
     *   3
     *     3.1
     *       3.1.1
     * ...
     *   (size)
     *     (size).1
     *       (size).1.1
     */
    static TestModel alternatingSubsreesModel(int size) {
        TestModel model = new TestModel();
        
        TestElement[] elements = new TestElement[size];
        for (int i = 0; i < size; i++) {
            String text = Integer.toString(i + 1);
            elements[i] = 
                new TestElement(model, text, new TestElement[] { 
                    new TestElement(model, text + ".1", new TestElement[] {
                        new TestElement(model, text + ".1.1", new TestElement[0])
                    })
                }); 
        }
        model.setRoot(new TestElement(model, "root", elements));
        
        return model;
    }

    static boolean areTreeSelectionsEqual(ITreeSelection sel1, ITreeSelection sel2) {
        Set sel1Set = new HashSet();
        sel1Set.addAll( Arrays.asList(sel1.getPaths()) );
        
        Set sel2Set = new HashSet();
        sel2Set.addAll( Arrays.asList(sel2.getPaths()) );
        
        return sel1Set.equals(sel2Set);
    }
    
    static void expandAlternateElements(TestModelUpdatesListener listener, TestModel model, boolean waitForAllUpdates) throws InterruptedException {
        listener.reset(); 
        listener.setFailOnRedundantUpdates(false);
        
        TestElement rootElement = model.getRootElement();
        TestElement[] children = rootElement.getChildren();
        ModelDelta rootDelta = new ModelDelta(rootElement, IModelDelta.NO_CHANGE);
        ModelDelta expandDelta = model.getBaseDelta(rootDelta);
        for (int i = 0; i < children.length; i++) {
            // Expand only odd children
            if (i % 2 == 1) {
                continue;
            }
            
            // Expand the element and the first child of each sub-element
            TestElement element = children[i];
            ModelDelta delta = expandDelta;
            int index = i;
            while (element.getChildren().length != 0) {
                TreePath elementPath = model.findElement(element.getLabel());
                listener.addUpdates(
                    elementPath, element, 1, 
                    CHILD_COUNT_UPDATES | (waitForAllUpdates ? CHILDREN_UPDATES : 0) );
                delta = delta.addNode(element, index, IModelDelta.EXPAND, element.getChildren().length);
                element = element.getChildren()[0];
                index = 0;
            }
        }
        model.postDelta(rootDelta);

        while (!listener.isFinished(CONTENT_SEQUENCE_COMPLETE | MODEL_CHANGED_COMPLETE)) 
            if (!Display.getDefault().readAndDispatch ()) Thread.sleep(0);
    }
    
    public void testPreserveExpandedOnRemove() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = alternatingSubsreesModel(6);

        // NOTE: WE ARE NOT EXPANDING ANY CHILDREN
        
        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY, true);

        expandAlternateElements(fListener, model, true);
        
        // Set a selection in view
        TreeSelection originalSelection = new TreeSelection(model.findElement("5.1"));
        fViewer.setSelection(originalSelection);

        // Update the model
        ModelDelta delta = model.removeElementChild(TreePath.EMPTY, 0);
        
        // Remove delta should not generate any new updates
        fListener.reset(); 
        model.postDelta(delta);
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        
        // Validate data
        model.validateData(fViewer, TreePath.EMPTY, true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("2")) == false);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("4")) == false);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("6")) == false);
        Assert.assertTrue( areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );
    }

    public void testPreserveExpandedOnInsert() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = alternatingSubsreesModel(6);

        // NOTE: WE ARE NOT EXPANDING ANY CHILDREN
        
        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY, true);

        expandAlternateElements(fListener, model, true);
        
        // Set a selection in view
        TreeSelection originalSelection = new TreeSelection(model.findElement("5.1"));
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
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        
        // Validate data
        model.validateData(fViewer, TreePath.EMPTY, true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("1.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("2")) == false);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("4")) == false);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("6")) == false);
        Assert.assertTrue( areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );
    }

    public void testPreserveExpandedOnMultLevelContent() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = alternatingSubsreesModel(6);

        // NOTE: WE ARE NOT EXPANDING ANY CHILDREN
        
        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY, true);

        expandAlternateElements(fListener, model, true);
        
        // Set a selection in view
        // Set a selection in view
        TreeSelection originalSelection = new TreeSelection(
            new TreePath[] { model.findElement("5"), model.findElement("5.1"), model.findElement("6") });
        fViewer.setSelection(originalSelection);
        Assert.assertTrue( areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );

        // Update the model
        model.removeElementChild(TreePath.EMPTY, 0);
        
        // Note: Re-expanding nodes causes redundant updates.
        fListener.reset(false, false);
        fListener.addUpdates(getInternalViewer(), TreePath.EMPTY, model.getRootElement(), -1, ALL_UPDATES_COMPLETE);
        
        // Create the delta which has nodes with CONTENT flag set at multiple levels. 
        ModelDelta rootDelta = new ModelDelta(model.getRootElement(), IModelDelta.CONTENT);
        ModelDelta elementDelta = model.getElementDelta(rootDelta, model.findElement("3.1.1"), true);
        elementDelta.setFlags(IModelDelta.CONTENT);
        
        // Post the multi-content update delta
        model.postDelta(rootDelta);
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Validate data
        model.validateData(fViewer, TreePath.EMPTY, true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("2")) == false);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("4")) == false);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("6")) == false);
        Assert.assertTrue( areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );
        
        // Note: in past it was observed sub-optimal coalescing in this test due 
        // to scattered update requests from viewer.
        Assert.assertTrue( fListener.checkCoalesced(TreePath.EMPTY, 0, 6) );
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
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY, true);

        // Turn off auto-expansion
        fViewer.setAutoExpandLevel(0);
        
        // Set a selection in view
        TreeSelection originalSelection = new TreeSelection(model.findElement("3.3.1"));
        fViewer.setSelection(originalSelection);

        // Update the model
        model.addElementChild(model.findElement("3"), null, 0, new TestElement(model, "3.0 - new", new TestElement[0]));
        
        // Create the delta for element "3" with content update.
        TreePath elementPath = model.findElement("3");
        ModelDelta rootDelta = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        ModelDelta elementDelta = model.getElementDelta(rootDelta, elementPath, true);
        elementDelta.setFlags(IModelDelta.CONTENT);

        // Note: Re-expanding nodes causes redundant updates.
        fListener.reset(false, false);
        fListener.addUpdates(getInternalViewer(), elementPath, model.getElement(elementPath), -1, ALL_UPDATES_COMPLETE);
        
        // Post the sub-tree update
        model.postDelta(rootDelta);
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Validate data
        model.validateData(fViewer, TreePath.EMPTY, true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3")) == true);
        // On windows, getExpandedState() may return true for an element with no children:
        // Assert.assertTrue(getCTargetViewer().getExpandedState(model.findElement("3.0 - new")) == false);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3.2")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3.3")) == true);
        Assert.assertTrue( areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );
    }

    public void testPreserveExpandedOnContentStress() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = alternatingSubsreesModel(6);

        // NOTE: WE ARE NOT EXPANDING ANY CHILDREN
        
        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY, true);

        expandAlternateElements(fListener, model, true);
        
        // Set a selection in view
//        TreeSelection originalSelection = new TreeSelection(
//            new TreePath[] { model.findElement("5"), model.findElement("5.1"), model.findElement("6") });
        TreeSelection originalSelection = new TreeSelection(model.findElement("5.1.1"));
        fViewer.setSelection(originalSelection);
        Assert.assertTrue( areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );

        // Run this test ten times as we've seen intermittent failures related 
        // to timing in it.
        for (int i = 0; i < 10; i++) {
            // Update the model
            model.removeElementChild(TreePath.EMPTY, 0);
            
            // Note: Re-expanding nodes causes redundant updates.
            fListener.reset(false, false);
            fListener.addUpdates(getInternalViewer(), TreePath.EMPTY, model.getRootElement(), -1, ALL_UPDATES_COMPLETE); 
            model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
            while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) 
                if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

            // Validate data
            model.validateData(fViewer, TreePath.EMPTY, true);
            Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("2")) == false);
            Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3")) == true);
            Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3.1")) == true);
            Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("4")) == false);
            Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5")) == true);
            Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5.1")) == true);
            Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("6")) == false);
            Assert.assertTrue( areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );
            
            // Update the model again
            model.addElementChild(TreePath.EMPTY, null, 0, new TestElement(model, "1", new TestElement[0]));
            
            // Note: Re-expanding nodes causes redundant updates.
            fListener.reset(false, false);
            fListener.addUpdates(getInternalViewer(), TreePath.EMPTY, model.getRootElement(), -1, ALL_UPDATES_COMPLETE); 
            model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
            while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) 
                if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

            // Validate data
            model.validateData(fViewer, TreePath.EMPTY, true);
            Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("2")) == false);
            Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3")) == true);
            Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3.1")) == true);
            Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("4")) == false);
            Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5")) == true);
            Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5.1")) == true);
            Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("6")) == false);
            Assert.assertTrue( areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );
        }
    }

    public void testPreserveLargeModelOnContent() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = alternatingSubsreesModel(100);

        // NOTE: WE ARE NOT EXPANDING ANY CHILDREN
        
        // Create the listener, only check the first level
        fListener.reset();

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished(CONTENT_SEQUENCE_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
//        model.validateData(fViewer, TreePath.EMPTY, true);

        expandAlternateElements(fListener, model, false);
        
        // Set a selection in view
        TreeSelection originalSelection = new TreeSelection(model.findElement("5.1.1"));
        fViewer.setSelection(originalSelection);
        Assert.assertTrue( areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );

        // Update the model
        model.removeElementChild(TreePath.EMPTY, 0);
        
        // Note: Re-expanding nodes causes redundant updates.
        fListener.reset(false, false);
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(CONTENT_SEQUENCE_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Validate data
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("2")) == false);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("4")) == false);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("6")) == false);
        Assert.assertTrue( areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );
        
        // Update the model again
        model.addElementChild(TreePath.EMPTY, null, 0, new TestElement(model, "1", new TestElement[0]));
        
        // Note: Re-expanding nodes causes redundant updates.
        fListener.reset(false, false);
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(CONTENT_SEQUENCE_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Validate data
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("2")) == false);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("4")) == false);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("6")) == false);
        Assert.assertTrue( areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );
    }
    
    /**
     * This test verifies that if the model selects a new element 
     * following a content refresh, the state restore logic will
     * not override the selection requested by the model.
     */
    public void testPreserveSelectionDeltaAfterContent() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = TestModel.simpleMultiLevel();

        // Expand all
        fViewer.setAutoExpandLevel(-1);
        
        // Create the listener.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY, true);

        // Set a selection in view
        fViewer.setSelection(new TreeSelection(model.findElement("3.1.1")));

        // Reset the listener (ignore redundant updates)
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, false, false);
        
        // Refresh content.
        // Note: Wait only for the processing of the delta, not for all updates
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Update the viewer with new selection delta to something new in the view
        ModelDelta selectDelta = model.makeElementDelta(model.findElement("2.1"), IModelDelta.SELECT);

        // Wait for the second model delta to process
        fListener.resetModelChanged();
        model.postDelta(selectDelta);
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        
        // Wait for all the updates to complete (note: we're not resetting the listener.
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Check to make sure that the state restore didn't change the selection.
        Assert.assertEquals(new TreeSelection(model.findElement("2.1")), fViewer.getSelection());
    }

    public void testPreserveCollapseDeltaAfterContent() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = TestModel.simpleMultiLevel();

        // Expand all
        fViewer.setAutoExpandLevel(-1);
        
        // Create the listener.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY, true);

        // Turn off auto-expand
        fViewer.setAutoExpandLevel(0);
        
        // Reset the listener (ignore redundant updates)
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, false, false);
        
        // Refresh content.
        // Note: Wait only for the processing of the delta, not for all updates
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

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
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        
        // Wait for all the updates to complete (note: we're not resetting the listener.
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Check to make sure that the state restore didn't change the selection.
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3.1")) == false);
    }

    public void testPreserveExpandDeltaAfterContent() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = TestModel.simpleMultiLevel();

        // Note: Do not auto-expand!
        
        // Create the listener.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY, true);

        // Reset the listener (ignore redundant updates)
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, false, false);
        
        // Refresh content.
        // Note: Wait only for the processing of the delta, not for all updates
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Update the viewer to expand an element
        ModelDelta expandDelta = model.makeElementDelta(model.findElement("3.1"), IModelDelta.EXPAND);
        
        // Wait for the second model delta to process
        fListener.resetModelChanged();
        model.postDelta(expandDelta);
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        
        // Wait for all the updates to complete (note: we're not resetting the listener.
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Check to make sure that the state restore didn't change the selection.
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3.1")) == true);
    }

    
    public void testSaveAndRestore1() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = alternatingSubsreesModel(6);

        // NOTE: WE ARE NOT EXPANDING ANY CHILDREN
        
        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY, true);

        // Expand some, but not all elements
        expandAlternateElements(fListener, model, true);

        // Set a selection in view
        fViewer.setSelection(new TreeSelection(new TreePath[] { model.findElement("5.1"), model.findElement("5.1.1"), model.findElement("6.1.1") } ));
        
        // Extract the original state from viewer
        ModelDelta originalState = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(TreePath.EMPTY, originalState, IModelDelta.EXPAND | IModelDelta.SELECT);

        // Set the viewer input to null.  This will trigger the view to save the viewer state.
        fListener.reset(false, false);
        fListener.addStateUpdates(getInternalViewer(), originalState, IModelDelta.EXPAND | IModelDelta.SELECT | IModelDelta.REVEAL);
        fViewer.setInput(null);
        while (!fListener.isFinished(STATE_SAVE_COMPLETE | STATE_UPDATES)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
                
        // Set the viewer input back to the model.  When view updates are complete
        // the viewer 
        // Note: disable redundant updates because the reveal delta triggers one.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, false, false);
        // TODO: add state updates somehow?
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Extract the restored state from viewer
        ModelDelta restoredState = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(TreePath.EMPTY, restoredState, IModelDelta.EXPAND | IModelDelta.SELECT);

        if (!deltaMatches(originalState, restoredState)) {
            Assert.fail("Expected:\n" + originalState.toString() + "\nGot:\n" + restoredState);
        }
    }
    
    public void testSaveAndRestore2() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = TestModel.simpleMultiLevel();

        // expand all elements
        fViewer.setAutoExpandLevel(-1);
        
        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY);

        // Set a selection in view
        fViewer.setSelection(new TreeSelection(new TreePath[] { model.findElement("3.2"), model.findElement("3.2.1"), model.findElement("2") } ));
        fViewer.setSelection(new TreeSelection(model.findElement("3.2.3")));
                
        // Turn off the auto-expand now since we want to text the auto-expand logic
        fViewer.setAutoExpandLevel(-1);

        // Extract the original state from viewer
        ModelDelta originalState = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(TreePath.EMPTY, originalState, IModelDelta.EXPAND | IModelDelta.SELECT);

        // Set the viewer input to null.  This will trigger the view to save the viewer state.
        fListener.reset(true, false);
        fListener.addStateUpdates(getInternalViewer(), originalState, IModelDelta.EXPAND | IModelDelta.SELECT | IModelDelta.REVEAL);

        fViewer.setInput(null);
        while (!fListener.isFinished(STATE_SAVE_COMPLETE | STATE_UPDATES)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Set the viewer input back to the model.  When view updates are complete
        // the viewer 
        // Note: disable redundant updates because the reveal delta triggers one.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, false, false);
        // TODO: add state updates somehow?
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Extract the restored state from viewer
        ModelDelta restoredState = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(TreePath.EMPTY, restoredState, IModelDelta.EXPAND | IModelDelta.SELECT);

        if (!deltaMatches(originalState, restoredState)) {
            Assert.fail("Expected:\n" + originalState.toString() + "\nGot:\n" + restoredState);
        }
    }

    public void testSaveAndRestoreInputInstance() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = alternatingSubsreesModel(6);

        // NOTE: WE ARE NOT EXPANDING ANY CHILDREN
        
        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY, true);

        // Expand some, but not all elements
        expandAlternateElements(fListener, model, true);

        // Set a selection in view
        fViewer.setSelection(new TreeSelection(new TreePath[] { model.findElement("5.1"), model.findElement("5.1.1"), model.findElement("6.1.1") } ));
        
        // Extract the original state from viewer
        ModelDelta originalState = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(TreePath.EMPTY, originalState, IModelDelta.EXPAND | IModelDelta.SELECT);

        // Do not reset to null, just reset input to the same object.
        
        // Set the viewer input back to the model.  When view updates are complete
        // the viewer 
        // Note: disable redundant updates because the reveal delta triggers one.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, false, false);
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Extract the restored state from viewer
        ModelDelta restoredState = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(TreePath.EMPTY, restoredState, IModelDelta.EXPAND | IModelDelta.SELECT);

        if (!deltaMatches(originalState, restoredState)) {
            Assert.fail("Expected:\n" + originalState.toString() + "\nGot:\n" + restoredState);
        }
    }

    public void testSaveAndRestoreInputInstanceEquals() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = alternatingSubsreesModel(6);

        // NOTE: WE ARE NOT EXPANDING ANY CHILDREN
        
        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY, true);

        // Expand some, but not all elements
        expandAlternateElements(fListener, model, true);

        // Set a selection in view
        fViewer.setSelection(new TreeSelection(new TreePath[] { model.findElement("5.1"), model.findElement("5.1.1"), model.findElement("6.1.1") } ));
        
        // Extract the original state from viewer
        ModelDelta originalState = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(TreePath.EMPTY, originalState, IModelDelta.EXPAND | IModelDelta.SELECT);

        // Create a copy of the input object and set it to model. 
        TestElement newRoot = new TestElement(model, model.getRootElement().getID(), model.getRootElement().getChildren());
        model.setRoot(newRoot);
        
        // Set the viewer input back to the model.  When view updates are complete
        // the viewer 
        // Note: disable redundant updates because the reveal delta triggers one.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, false, false);
        
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Extract the restored state from viewer
        ModelDelta restoredState = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(TreePath.EMPTY, restoredState, IModelDelta.EXPAND | IModelDelta.SELECT);

        if (!deltaMatches(originalState, restoredState)) {
            Assert.fail("Expected:\n" + originalState.toString() + "\nGot:\n" + restoredState);
        }
    }

    
    public void testSaveAndRestoreLarge() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = alternatingSubsreesModel(100);

        // NOTE: WE ARE NOT EXPANDING ANY CHILDREN
        
        // Create the listener, only check the first level
        fListener.reset();

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished(CONTENT_SEQUENCE_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        expandAlternateElements(fListener, model, false);
        
        // Set a selection in view
        TreeSelection originalSelection = new TreeSelection(model.findElement("5.1.1"));
        fViewer.setSelection(originalSelection);
        Assert.assertTrue( areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );

        // Extract the original state from viewer
        ModelDelta originalState = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(TreePath.EMPTY, originalState, IModelDelta.EXPAND | IModelDelta.SELECT);

        // Set the viewer input to null.  This will trigger the view to save the viewer state.
        fListener.reset();
        fListener.addStateUpdates(getInternalViewer(), originalState, IModelDelta.EXPAND | IModelDelta.SELECT | IModelDelta.REVEAL);

        fViewer.setInput(null);
        while (!fListener.isFinished(STATE_SAVE_COMPLETE | STATE_UPDATES)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Set the viewer input back to the model.  When view updates are complete
        // the viewer 
        // Note: disable redundant updates because the reveal delta triggers one.
        fListener.reset();
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished(CONTENT_SEQUENCE_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Validate data (only select visible elements).
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("1.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("2")) == false);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("4")) == false);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("6")) == false);
        Assert.assertTrue( areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );
    }

    /**
     * This test saves state of a large tree.  Then the tree is modified 
     * to contain much fewer elements.  The restore logic should discard the
     * rest of the saved state delta once all the elements are visible. 
     */
    public void testSaveAndRestorePartialStateLarge() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = alternatingSubsreesModel(100);

        // NOTE: WE ARE NOT EXPANDING ANY CHILDREN
        
        // Create the listener, only check the first level
        fListener.reset();

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished(CONTENT_SEQUENCE_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        expandAlternateElements(fListener, model, false);
        
        // Set a selection in view
        TreeSelection originalSelection = new TreeSelection(model.findElement("5.1.1"));
        fViewer.setSelection(originalSelection);
        Assert.assertTrue( areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );

        // Extract the original state from viewer
        ModelDelta originalState = new ModelDelta(model.getRootElement(), IModelDelta.NO_CHANGE);
        fViewer.saveElementState(TreePath.EMPTY, originalState, IModelDelta.EXPAND | IModelDelta.SELECT);

        // Set the viewer input to null.  This will trigger the view to save the viewer state.
        fListener.reset();
        fListener.addStateUpdates(getInternalViewer(), originalState, IModelDelta.EXPAND | IModelDelta.SELECT | IModelDelta.REVEAL);

        fViewer.setInput(null);
        while (!fListener.isFinished(STATE_SAVE_COMPLETE | STATE_UPDATES)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        
        TestElement[] elements = model.getRootElement().getChildren();
        TestElement[] newElements = new TestElement[10];
        System.arraycopy(elements, 0, newElements, 0, newElements.length);
        model.setElementChildren(TreePath.EMPTY, newElements);
        
        // Set the viewer input back to the model.  When view updates are complete
        // the viewer 
        // Note: disable redundant updates because the reveal delta triggers one.
        fListener.reset();
        fViewer.setInput(model.getRootElement());
        
        // MONITOR FOR THE STATE RESTORE TO COMPLETE   
        while (!fListener.isFinished(CONTENT_SEQUENCE_COMPLETE| STATE_RESTORE_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Validate data
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("1.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("2")) == false);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("4")) == false);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("5.1")) == true);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("6")) == false);
        Assert.assertTrue( areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );
    }
    
    public void testPreserveCollapseAndSelectDeltaAfterSaveAndRestore() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = TestModel.simpleMultiLevel();

        // Expand all
        fViewer.setAutoExpandLevel(-1);
        
        // Create the listener.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY, true);

        fViewer.setSelection(new TreeSelection(model.findElement("3")));

        // Turn off auto-expand
        fViewer.setAutoExpandLevel(0);
               
        // Set the viewer input to null.  This will trigger the view to save the viewer state.
        fListener.reset(false, false);
        fViewer.setInput(null);
        while (!fListener.isFinished(STATE_SAVE_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
                
        // Set the viewer input back to the model.  When view updates are complete
        // the viewer 
        // Note: disable redundant updates because the reveal delta triggers one.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, false, false);
        fViewer.setInput(model.getRootElement());
        TreePath path = model.findElement("2");
        fListener.addUpdates(null, path, (TestElement)path.getLastSegment(), 0, STATE_UPDATES);
        path = model.findElement("3");
        fListener.addUpdates(null, path, (TestElement)path.getLastSegment(), 0, STATE_UPDATES);

        // Wait till we restore state of elements we want to collapse and select
        while (!fListener.isFinished(STATE_RESTORE_STARTED | STATE_UPDATES | CHILDREN_UPDATES | MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
                
        // Post first collapse delta
        fListener.resetModelChanged();
        model.postDelta(model.makeElementDelta(model.findElement("2"), IModelDelta.COLLAPSE));
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Post second collapse delta
        fListener.resetModelChanged();
        model.postDelta(model.makeElementDelta(model.findElement("3"), IModelDelta.COLLAPSE));
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Post select delta
        model.postDelta(model.makeElementDelta(model.findElement("1"), IModelDelta.SELECT));
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Wait for all the updates to complete (note: we're not resetting the listener).
        while (!fListener.isFinished(STATE_RESTORE_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Check to make sure that the state restore didn't change the selection.
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("2")) == false);
        Assert.assertTrue(getInternalViewer().getExpandedState(model.findElement("3")) == false);
        Assert.assertEquals(new TreeSelection(model.findElement("1")), fViewer.getSelection());
    }

    /**
     * Test for bug 359859.<br>
     * This test verifies that RESTORE state is handled after SAVE previous state was completed
     */
    public void testSaveRestoreOrder() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = TestModel.simpleMultiLevel();

        // Expand all
        fViewer.setAutoExpandLevel(-1);
        
        // Create the listener.
        fListener.reset(TreePath.EMPTY, model.getRootElement(), -1, false, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        model.validateData(fViewer, TreePath.EMPTY, true);
        
        // a new similar model
        TestModel copyModel = TestModel.simpleMultiLevel();
        
        // Trigger save - restore sequence.
        fListener.reset();
        fListener.expectRestoreAfterSaveComplete();
        fViewer.setInput(copyModel.getRootElement());
        while (!fListener.isFinished(STATE_RESTORE_STARTED)) Thread.sleep(0);
        Assert.assertTrue("RESTORE started before SAVE to complete", fListener.isFinished(STATE_SAVE_COMPLETE));        
    }    

}
