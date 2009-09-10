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

import java.util.Arrays;

import junit.framework.Assert;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelContentProviderTarget;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.jface.viewers.TreePath;

/**
 * Test model for the use in unit tests.  This test model contains a set of 
 * elements in a tree structure.  It contains utility methods for modifying the 
 * model and for verifying that the viewer content matches the model.
 * 
 * @since 3.6
 */
public class TestModel implements IElementContentProvider, IElementLabelProvider, IModelProxyFactory /*, IElementCheckReceiver */ {
    
    public class TestElement extends PlatformObject {
        private final String fID;
        TestElement[] fChildren;
        String fLabelAppendix = "";
        boolean fExpanded;
        boolean fChecked;
        boolean fGrayed;
        
        public TestElement(String text, TestElement[] children) {
            this (text, false, false, children);
        }

        public TestElement(String text, boolean checked, boolean grayed, TestElement[] children) {
            fID = text;
            fChildren = children;
            fChecked = checked;
            fGrayed = grayed;
        }
        
        public Object getAdapter(Class adapter) {
            if (adapter.isInstance(TestModel.this)) {
                return TestModel.this;
            }
            return null;
        }
        
        public void setLabelAppendix(String appendix) {
            fLabelAppendix = appendix;
        }
        
        public String getLabel() {
            return fID + fLabelAppendix;
        }
        
        public TestElement[] getChildren() {
            return fChildren;
        }
        
        public boolean isExpanded() {
            return fExpanded;
        }
        
        public boolean getGrayed() {
            return fGrayed;
        }
        
        public boolean getChecked() {
            return fChecked;
        }

        public void setChecked(boolean checked, boolean grayed) {
            fChecked = checked;
            fGrayed = grayed;
        }
        
        public boolean equals(Object obj) {
            return obj instanceof TestElement && fID.equals(((TestElement)obj).fID);
        }
        
        public int hashCode() {
            return fID.hashCode();
        }
        
        public String toString() {
            return getLabel();
        }
        
        public int indexOf(TestElement child) {
            return Arrays.asList(fChildren).indexOf(child);
        }
    }

    private class ModelProxy extends AbstractModelProxy {}

    private TestElement fRoot;
    private ModelProxy fModelProxy;
    
    /**
     * Constructor private.  Use static factory methods instead. 
     */
    private TestModel() {}
    
    public TestElement getRootElement() {
        return fRoot;
    }
    
    public int getModelDepth() {
        return getDepth(getRootElement(), 0);
    }
    
    private int getDepth(TestElement element, int atDepth) {
        TestElement[] children = element.getChildren(); 
        if (children.length == 0) {
            return atDepth;
        }
        int depth = atDepth + 1;
        for (int i = 0; i < children.length; i++) {
            depth = Math.max(depth, getDepth(children[i], atDepth + 1));
        }

        return depth;
    }
    
    public void update(IHasChildrenUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            TestElement element = (TestElement)updates[i].getElement();
            updates[i].setHasChilren(element.getChildren().length > 0);
            updates[i].done();
        }
    }
    
    public void update(IChildrenCountUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            TestElement element = (TestElement)updates[i].getElement();
            updates[i].setChildCount(element.getChildren().length);
            updates[i].done();
        }
    }
    
    public void update(IChildrenUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            TestElement element = (TestElement)updates[i].getElement();
            int endOffset = updates[i].getOffset() + updates[i].getLength();
            for (int j = updates[i].getOffset(); j < endOffset; j++) {
                if (j < element.getChildren().length) {
                    updates[i].setChild(element.getChildren()[j], j);
                }
            }
            updates[i].done();
        }
    }
    
    public void update(ILabelUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            TestElement element = (TestElement)updates[i].getElement();
            updates[i].setLabel(element.fID, 0);
// TODO: wait for bug 286310
//            if (updates[i] instanceof ICheckUpdate && 
//                Boolean.TRUE.equals(updates[i].getPresentationContext().getProperty(ICheckUpdate.PROP_CHECK))) 
//            {
//                ((ICheckUpdate)updates[i]).setChecked(element.getChecked(), element.getGrayed());
//            }
            updates[i].done();
        }        
    }
    
    public void elementChecked(IPresentationContext context, Object viewerInput, TreePath path, boolean checked) {
        TestElement element = getElement(path); 
        Assert.assertFalse(element.getGrayed());
        element.setChecked(checked, false);
    }
    
    public IModelProxy createModelProxy(Object element, IPresentationContext context) {
        fModelProxy = new ModelProxy();
        return fModelProxy;
    }
    
    public TestElement getElement(TreePath path) {
        if (path.getSegmentCount() == 0) {
            return getRootElement();
        } else {
            return (TestElement)path.getLastSegment();
        }
    }

    public void setAllExpanded() {
        doSetExpanded(fRoot);
    }
    
    private void doSetExpanded(TestElement element) {
        element.fExpanded = true;
        for (int i = 0; i < element.fChildren.length; i++) {
            doSetExpanded(element.fChildren[i]);
        }
    }
    
    public void validateData(ITreeModelViewer viewer, TreePath path) {
        
        validateData(viewer, path, false);
    }

    public void validateData(ITreeModelViewer _viewer, TreePath path, boolean expandedElementsOnly) {
        ITreeModelContentProviderTarget viewer = (ITreeModelContentProviderTarget)_viewer;
        TestElement element = getElement(path);
// TODO: wait for bug 286310
//        if ( Boolean.TRUE.equals(_viewer.getPresentationContext().getProperty(ICheckUpdate.PROP_CHECK)) ) {
//            ITreeModelCheckProviderTarget checkTarget = (ITreeModelCheckProviderTarget)_viewer;  
//            Assert.assertEquals(element.getChecked(), checkTarget.getElementChecked(path));
//            Assert.assertEquals(element.getGrayed(), checkTarget.getElementGrayed(path));
//        }
        
        if (!expandedElementsOnly || path.getSegmentCount() == 0 || viewer.getExpandedState(path) ) {
            TestElement[] children = element.getChildren();
            Assert.assertEquals(children.length, viewer.getChildCount(path));

            for (int i = 0; i < children.length; i++) {
                Assert.assertEquals(children[i], viewer.getChildElement(path, i));
                validateData(viewer, path.createChildPath(children[i]), expandedElementsOnly);
            }
        }
    }

    private void setRoot(TestElement root) {
        fRoot = root;
    }
    
    public void postDelta(IModelDelta delta) {
        fModelProxy.fireModelChanged(delta);
    }
    
    private ModelDelta getElementDelta(ModelDelta baseDelta, TreePath path) {
        TestElement element = getRootElement();
        ModelDelta delta = baseDelta;
        
        for (int i = 0; i < path.getSegmentCount(); i++) {
            TestElement[] children = element.getChildren(); 
            delta.setChildCount(children.length);
            Object segment = path.getSegment(i);
            int j = 0;
            for (j = 0; j < element.getChildren().length; j++) {
                if (segment.equals(element.getChildren()[j])) {
                    element = element.getChildren()[j];
                    delta = delta.addNode(element, j, IModelDelta.NO_CHANGE);
                }
            }
            if (j == element.getChildren().length) {
                throw new IllegalArgumentException("Invalid path");
            }
        }
        return delta;
        
    }
    
    public ModelDelta appendElementLabel(TreePath path, String labelAppendix) {
        ModelDelta baseDelta = new ModelDelta(getRootElement(), IModelDelta.NO_CHANGE);
        TestElement element = getElement(path);
        ModelDelta delta = getElementDelta(baseDelta, path);
        element.setLabelAppendix(labelAppendix);
        delta.setFlags(delta.getFlags() | IModelDelta.STATE);

        return baseDelta;
    }

    public ModelDelta setElementChecked(TreePath path, boolean checked, boolean grayed) {
        ModelDelta baseDelta = new ModelDelta(getRootElement(), IModelDelta.NO_CHANGE);
        TestElement element = getElement(path);
        ModelDelta delta = getElementDelta(baseDelta, path);
        element.setChecked(checked, grayed);
        delta.setFlags(delta.getFlags() | IModelDelta.STATE);

        return baseDelta;
    }

    public ModelDelta setElementChildren(TreePath path, TestElement[] children) {
        ModelDelta baseDelta = new ModelDelta(getRootElement(), IModelDelta.NO_CHANGE);

        // Find the parent element and generate the delta node for it.
        TestElement element = getElement(path);
        ModelDelta delta = getElementDelta(baseDelta, path);
        
        // Set the new children array
        element.fChildren = children;
        
        // Add the delta flag and update the child count in the parent delta.        
        delta.setFlags(delta.getFlags() | IModelDelta.CONTENT);
        delta.setChildCount(children.length);
        
        return baseDelta;
    }
    
    public ModelDelta replaceElementChild(TreePath parentPath, int index, TestElement child) {
        ModelDelta baseDelta = new ModelDelta(getRootElement(), IModelDelta.NO_CHANGE);
        
        TestElement element = getElement(parentPath);
        ModelDelta delta= getElementDelta(baseDelta, parentPath);
        TestElement oldChild = element.fChildren[index]; 
        element.fChildren[index] = child;
        delta.addNode(oldChild, child, IModelDelta.REPLACED);
        // TODO: set replacement index!?!
        
        return baseDelta;
    }    

    public ModelDelta addElementChild(TreePath parentPath, int index, TestElement newChild) {
        ModelDelta baseDelta = new ModelDelta(getRootElement(), IModelDelta.NO_CHANGE);

        // Find the parent element and generate the delta node for it.
        TestElement element = getElement(parentPath);
        ModelDelta delta= getElementDelta(baseDelta, parentPath);

        // Add the new element
        element.fChildren = doInsertElementInArray(element.fChildren, index, newChild);
        
        // Add the delta flag and update the child count in the parent delta.
        delta.setChildCount(element.getChildren().length);
        delta.addNode(newChild, IModelDelta.ADDED);
        
        return baseDelta;
    }    

    public ModelDelta insertElementChild(TreePath parentPath, int index, TestElement newChild) {
        ModelDelta baseDelta = new ModelDelta(getRootElement(), IModelDelta.NO_CHANGE);

        // Find the parent element and generate the delta node for it.
        TestElement element = getElement(parentPath);
        ModelDelta delta= getElementDelta(baseDelta, parentPath);
        
        // Add the new element
        element.fChildren = doInsertElementInArray(element.fChildren, index, newChild);
        
        // Add the delta flag and update the child count in the parent delta.
        delta.setChildCount(element.getChildren().length);
        delta.addNode(newChild, index, IModelDelta.INSERTED);
        
        return baseDelta;
    }    

    private TestElement[] doInsertElementInArray(TestElement[] children, int index, TestElement newChild) {
        // Create the new children array add the element to it and set it to 
        // the parent.
        TestElement[] newChildren = new TestElement[children.length + 1];
        System.arraycopy(children, 0, newChildren, 0, index);
        newChildren[index] = newChild;
        System.arraycopy(children, index, newChildren, index + 1, children.length - index);
        return newChildren;
    }
    
    public ModelDelta removeElementChild(TreePath parentPath, int index) {
        ModelDelta baseDelta = new ModelDelta(getRootElement(), IModelDelta.NO_CHANGE);

        // Find the parent element and generate the delta node for it.
        TestElement element = getElement(parentPath);
        ModelDelta delta= getElementDelta(baseDelta, parentPath);
        
        // Create a new child array with the element removed
        TestElement[] children = element.getChildren();
        TestElement childToRemove = children[index];
        TestElement[] newChildren = new TestElement[children.length - 1];
        System.arraycopy(children, 0, newChildren, 0, index);
        System.arraycopy(children, index + 1, newChildren, index, children.length - index - 1);
        element.fChildren = newChildren;

        // Add the delta flag and update the child count in the parent delta.
        delta.setChildCount(element.getChildren().length);
        delta.addNode(childToRemove, index, IModelDelta.REMOVED);
        
        return baseDelta;
    }        

    public TreePath findElement(String label) {
        return findElement(TreePath.EMPTY, label);
    }

    public TreePath findElement(TreePath startPath, String label) {
        TestElement element = getElement(startPath);
        for (int i = 0; i < element.getChildren().length; i++) {
            TestElement child = element.getChildren()[i];
            TreePath path = startPath.createChildPath(child);
            if ( label.equals(child.getLabel()) ) {
                return path;
            } else {
                TreePath subPath = findElement(path, label);
                if (subPath != null) {
                    return subPath;
                }
            }
        }
        return null;
    }
    
    public static TestModel simpleSingleLevel() {
        TestModel model = new TestModel();
        model.setRoot( model.new TestElement("root", new TestElement[] {
            model.new TestElement("1", true, true, new TestElement[0]),
            model.new TestElement("2", true, false, new TestElement[0]),
            model.new TestElement("3", false, true, new TestElement[0]),
            model.new TestElement("4", false, false, new TestElement[0]),
            model.new TestElement("5", new TestElement[0]),
            model.new TestElement("6", new TestElement[0])
        }) );
        return model;
    }
    
    public static TestModel simpleMultiLevel() {
        TestModel model = new TestModel();
        model.setRoot( model.new TestElement("root", new TestElement[] {
            model.new TestElement("1", new TestElement[0]),
            model.new TestElement("2", true, false, new TestElement[] {
                model.new TestElement("2.1", true, true, new TestElement[0]),
                model.new TestElement("2.2", false, true, new TestElement[0]),
                model.new TestElement("2.3", true, false, new TestElement[0]),
            }),
            model.new TestElement("3", new TestElement[] {
                model.new TestElement("3.1", new TestElement[] {
                    model.new TestElement("3.1.1", new TestElement[0]),
                    model.new TestElement("3.1.2", new TestElement[0]),
                    model.new TestElement("3.1.3", new TestElement[0]),
                }),
                model.new TestElement("3.2", new TestElement[] {
                    model.new TestElement("3.2.1", new TestElement[0]),
                    model.new TestElement("3.2.2", new TestElement[0]),
                    model.new TestElement("3.2.3", new TestElement[0]),
                }),
                model.new TestElement("3.3", new TestElement[] {
                    model.new TestElement("3.3.1", new TestElement[0]),
                    model.new TestElement("3.3.2", new TestElement[0]),
                    model.new TestElement("3.3.3", new TestElement[0]),
                }),
            })
        }) );
        return model;
    }
}
