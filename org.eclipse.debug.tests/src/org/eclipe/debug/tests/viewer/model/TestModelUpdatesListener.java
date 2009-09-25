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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.eclipe.debug.tests.viewer.model.TestModel.TestElement;
import org.eclipse.debug.internal.ui.viewers.model.ILabelUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.jface.viewers.TreePath;

public class TestModelUpdatesListener implements IViewerUpdateListener, ILabelUpdateListener, IModelChangedListener {

    public static final int LABEL_UPDATES_COMPLETE = 0X0001;
    public static final int CONTENT_UPDATES_COMPLETE = 0X0002;
    public static final int LABEL_UPDATES = 0X0004;
    public static final int HAS_CHILDREN_UPDATES = 0X0008;
    public static final int CHILDREN_COUNT_UPDATES = 0X0010;
    public static final int CHILDREN_UPDATES = 0X0020;
    public static final int MODEL_CHANGED_COMPLETE = 0X0040; 
    public static final int MODEL_PROXIES_INSTALLED = 0X0080; 
    
    public static final int LABEL_COMPLETE = LABEL_UPDATES_COMPLETE | LABEL_UPDATES;
    public static final int CONTENT_COMPLETE = 
        CONTENT_UPDATES_COMPLETE | HAS_CHILDREN_UPDATES | CHILDREN_COUNT_UPDATES | CHILDREN_UPDATES;
    
    
    public static final int ALL_UPDATES_COMPLETE = LABEL_COMPLETE | CONTENT_COMPLETE | MODEL_PROXIES_INSTALLED;
    
    private boolean fFailOnRedundantUpdates;
    private boolean fFailOnMultipleUpdateSequences;
    
    private Set fHasChildrenUpdates = new HashSet();
    private Map fChildrenUpdates = new HashMap();
    private Set fChildCountUpdates = new HashSet();
    private Set fLabelUpdates = new HashSet();
    private Set fProxyModels = new HashSet();
    private boolean fViewerUpdatesComplete;
    private boolean fLabelUpdatesComplete;
    private boolean fModelChangedComplete;
    
    public TestModelUpdatesListener(boolean failOnRedundantUpdates, boolean failOnMultipleUpdateSequences) {
        setFailOnRedundantUpdates(failOnRedundantUpdates);
        setFailOnMultipleUpdateSequences(failOnMultipleUpdateSequences);
    }
    
    public void setFailOnRedundantUpdates(boolean failOnRedundantUpdates) {
        fFailOnRedundantUpdates = failOnRedundantUpdates;
    }

    public void setFailOnMultipleUpdateSequences(boolean failOnMultipleUpdateSequences) {
        fFailOnMultipleUpdateSequences = failOnMultipleUpdateSequences;
    }


    public void reset(TreePath path, TestElement element, int levels, boolean failOnRedundantUpdates, boolean failOnMultipleUpdateSequences) {
        reset();
        addUpdates(path, element, levels);
        addProxies(element);
        setFailOnRedundantUpdates(failOnRedundantUpdates);
        setFailOnMultipleUpdateSequences(failOnMultipleUpdateSequences);
    }
    
    public void reset() {
        fHasChildrenUpdates.clear();
        fChildrenUpdates.clear();
        fChildCountUpdates.clear();
        fLabelUpdates.clear();
        fProxyModels.clear();
        fViewerUpdatesComplete = false;
        fLabelUpdatesComplete = false;
        fModelChangedComplete = false;
    }
    
    public void addHasChildrenUpdate(TreePath path) {
        fHasChildrenUpdates.add(path);
    }

    public void removeHasChildrenUpdate(TreePath path) {
        fHasChildrenUpdates.remove(path);
    }

    public void addChildreCountUpdate(TreePath path) {
        fChildCountUpdates.add(path);
    }

    public void removeChildreCountUpdate(TreePath path) {
        fChildCountUpdates.remove(path);
    }

    public void addChildreUpdate(TreePath path, int index) {
        Set childrenIndexes = (Set)fChildrenUpdates.get(path);
        if (childrenIndexes == null) {
            childrenIndexes = new HashSet(1);
            fChildrenUpdates.put(path, childrenIndexes);
        }
        childrenIndexes.add(new Integer(index));
    }

    public void recurseremoveChildreUpdate(TreePath path, int index) {
        Set childrenIndexes = (Set)fChildrenUpdates.get(path);
        if (childrenIndexes != null) {
            childrenIndexes.remove(new Integer(index));
            if (childrenIndexes.isEmpty()) {
                fChildrenUpdates.remove(path);
            }
        }
    }

    public void addLabelUpdate(TreePath path) {
        fLabelUpdates.add(path);
    }

    public void removeLabelUpdate(TreePath path) {
        fLabelUpdates.remove(path);
    }

    public void addUpdates(TreePath path, TestElement element, int levels) {
        TestElement[] children = element.getChildren();
        
        if (!path.equals(TreePath.EMPTY)) {
            fLabelUpdates.add(path);
            fHasChildrenUpdates.add(path);
        }

        if (levels != 0) {
            levels--;
            if (children.length > 0) {
                fChildCountUpdates.add(path);
                Set childrenIndexes = new HashSet();
                for (int i = 0; i < children.length; i++) {
                    childrenIndexes.add(new Integer(i));
                }
                fChildrenUpdates.put(path, childrenIndexes);
            }
        
            for (int i = 0; i < children.length; i++) {
                addUpdates(path.createChildPath(children[i]), children[i], levels);
            }
        }
    }

    private void addProxies(TestElement element) {
        TestModel model = element.getModel();
        if (model.getModelProxy() == null) {
            fProxyModels.add(element.getModel());
        }
        TestElement[] children = element.getChildren();
        for (int i = 0; i < children.length; i++) {
            addProxies(children[i]);
        }
    }
    
    public boolean isFinished() {
        return isFinished(ALL_UPDATES_COMPLETE);
    }
    
    public boolean isFinished(int flags) {
        if ( (flags & LABEL_UPDATES_COMPLETE) != 0) {
            if (!fLabelUpdatesComplete) return false;
        }
        if ( (flags & LABEL_UPDATES) != 0) {
            if (!fLabelUpdates.isEmpty()) return false;
        }
        if ( (flags & CONTENT_UPDATES_COMPLETE) != 0) {
            if (!fViewerUpdatesComplete) return false;
        }
        if ( (flags & HAS_CHILDREN_UPDATES) != 0) {
            if (!fHasChildrenUpdates.isEmpty()) return false;
        }
        if ( (flags & CHILDREN_COUNT_UPDATES) != 0) {
            if (!fChildCountUpdates.isEmpty()) return false;
        }
        if ( (flags & CHILDREN_UPDATES) != 0) {
            if (!fChildrenUpdates.isEmpty()) return false;
        }
        if ( (flags & MODEL_CHANGED_COMPLETE) != 0) {
            if (!fModelChangedComplete) return false;
        }
        if ( (flags & MODEL_PROXIES_INSTALLED) != 0) {
            if (fProxyModels.size() != 0) return false;
        }
        return true;
    }
    
    public void updateStarted(IViewerUpdate update) {
        System.out.println("started: " + update);
        
    }
    
    public void updateComplete(IViewerUpdate update) {
        if (!update.isCanceled()) {
            if (update instanceof IHasChildrenUpdate) {
                if (!fHasChildrenUpdates.remove(update.getElementPath()) && fFailOnRedundantUpdates) {
                    Assert.fail("Redundant update: " + update);
                }
            } if (update instanceof IChildrenCountUpdate) {
                if (!fChildCountUpdates.remove(update.getElementPath()) && fFailOnRedundantUpdates) {
                    Assert.fail("Redundant update: " + update);
                }
            } else if (update instanceof IChildrenUpdate) {
                int start = ((IChildrenUpdate)update).getOffset();
                int end = start + ((IChildrenUpdate)update).getLength();
                
                Set childrenIndexes = (Set)fChildrenUpdates.get(update.getElementPath());
                if (childrenIndexes != null) {
                    for (int i = start; i < end; i++) {
                        childrenIndexes.remove(new Integer(i));
                    }
                    if (childrenIndexes.isEmpty()) {
                        fChildrenUpdates.remove(update.getElementPath());
                    }
                } else if (fFailOnRedundantUpdates) {
                    Assert.fail("Redundant update: " + update);                    
                }
            } 
        }
    }
    
    public void viewerUpdatesBegin() {
        
    }
    
    public void viewerUpdatesComplete() {
        if (fFailOnMultipleUpdateSequences && fViewerUpdatesComplete) {
            Assert.fail("Multiple viewer update sequences detected");
        }
        fViewerUpdatesComplete = true;
    }

    public void labelUpdateComplete(ILabelUpdate update) {
        if (!fLabelUpdates.remove(update.getElementPath()) && fFailOnRedundantUpdates) {
            Assert.fail("Redundant update: " + update);
        }
    }

    public void labelUpdateStarted(ILabelUpdate update) {
        System.out.println("started: " + update);
    }

    public void labelUpdatesBegin() {
    }

    public void labelUpdatesComplete() {
        if (fFailOnMultipleUpdateSequences && fLabelUpdatesComplete) {
            Assert.fail("Multiple label update sequences detected");
        }
        fLabelUpdatesComplete = true;
    }
    
    public void modelChanged(IModelDelta delta, IModelProxy proxy) {
        if (fFailOnMultipleUpdateSequences && fModelChangedComplete) {
            Assert.fail("Multiple model changed sequences detected");
        }
        fModelChangedComplete = true;

        for (Iterator itr = fProxyModels.iterator(); itr.hasNext();) {
            TestModel model = (TestModel)itr.next();
            if (model.getModelProxy() == proxy) {
                itr.remove();
                break;
            }
        }
    }
}


