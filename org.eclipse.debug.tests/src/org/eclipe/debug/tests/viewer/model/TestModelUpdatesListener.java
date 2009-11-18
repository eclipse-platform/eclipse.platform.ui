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
import java.util.TreeSet;

import junit.framework.Assert;

import org.eclipe.debug.tests.viewer.model.TestModel.TestElement;
import org.eclipse.debug.internal.ui.viewers.model.ILabelUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelContentProviderTarget;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IStateUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.jface.viewers.TreePath;

public class TestModelUpdatesListener 
    implements IViewerUpdateListener, ILabelUpdateListener, IModelChangedListener, ITestModelUpdatesListenerConstants,
        IStateUpdateListener
{
    private boolean fFailOnRedundantUpdates;
    private boolean fFailOnMultipleUpdateSequences;
    
    private Set fHasChildrenUpdates = new HashSet();
    private Map fChildrenUpdates = new HashMap();
    private Set fChildCountUpdates = new HashSet();
    private Set fLabelUpdates = new HashSet();
    private Set fProxyModels = new HashSet();
    private Set fStateUpdates = new HashSet();
    private boolean fViewerUpdatesComplete;
    private boolean fLabelUpdatesComplete;
    private boolean fModelChangedComplete;
    private boolean fStateSaveComplete;
    private boolean fStateRestoreComplete;
	private int fViewerUpdatesRunning;
	private int fLabelUpdatesRunning;
	private int fTimeoutInterval = 60000;
	private long fTimeoutTime;
	
	
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
    
    /**
     * Sets the the maximum amount of time (in milliseconds) that the update listener 
     * is going to wait. If set to -1, the listener will wait indefinitely. 
     */
    public void setTimeoutInterval(int milis) {
        fTimeoutInterval = milis;
    }
    
    public void reset(TreePath path, TestElement element, int levels, boolean failOnRedundantUpdates, boolean failOnMultipleUpdateSequences) {
        reset();
        addUpdates(path, element, levels);
        addProxies(element);
        setFailOnRedundantUpdates(failOnRedundantUpdates);
        setFailOnMultipleUpdateSequences(failOnMultipleUpdateSequences);
    }

    public void reset(boolean failOnRedundantUpdates, boolean failOnMultipleUpdateSequences) {
        reset();
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
        fStateSaveComplete = false;
        fStateRestoreComplete = false;
        fTimeoutTime = System.currentTimeMillis() + fTimeoutInterval;
        resetModelChanged();
    }
    
    public void resetModelChanged() {
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
            childrenIndexes = new TreeSet();
            fChildrenUpdates.put(path, childrenIndexes);
        }
        childrenIndexes.add(new Integer(index));
    }

    public void removeChildrenUpdate(TreePath path, int index) {
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
        addUpdates(path, element, levels, ALL_UPDATES_COMPLETE);
    }

    public void addStateUpdates(ITreeModelContentProviderTarget viewer, TreePath path, TestElement element) {
        addUpdates(viewer, path, element, -1, STATE_UPDATES);
    }
    
    public void addUpdates(TreePath path, TestElement element, int levels, int flags) {
        addUpdates(null, path, element, levels, flags);
    }

    public void addUpdates(ITreeModelContentProviderTarget viewer, TreePath path, TestElement element, int levels, int flags) {
        if (!path.equals(TreePath.EMPTY)) {
            if ((flags & LABEL_UPDATES) != 0) {
                fLabelUpdates.add(path);
            }
            if ((flags & HAS_CHILDREN_UPDATES) != 0) {
                fHasChildrenUpdates.add(path);
            }
        }

        if (levels-- != 0) {
            TestElement[] children = element.getChildren();
            if (children.length > 0 && (viewer == null || path.getSegmentCount() == 0 || viewer.getExpandedState(path))) {
                if ((flags & CHILDREN_COUNT_UPDATES) != 0) {
                    fChildCountUpdates.add(path);
                }
                if ((flags & CHILDREN_UPDATES) != 0) {
                    Set childrenIndexes = new HashSet();
                    for (int i = 0; i < children.length; i++) {
                        childrenIndexes.add(new Integer(i));
                    }
                    fChildrenUpdates.put(path, childrenIndexes);
                }

                if ((flags & STATE_UPDATES) != 0 && viewer != null) {
                    fStateUpdates.add(path);
                }

                for (int i = 0; i < children.length; i++) {
                    addUpdates(viewer, path.createChildPath(children[i]), children[i], levels, flags);
                }
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
        if (fTimeoutInterval > 0 && fTimeoutTime < System.currentTimeMillis()) {
            throw new RuntimeException("Timed Out: " + toString(flags));
        }
        
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
        if ( (flags & STATE_SAVE_COMPLETE) != 0) {
            if (!fStateSaveComplete) return false;
        }
        if ( (flags & STATE_RESTORE_COMPLETE) != 0) {
            if (!fStateRestoreComplete) return false;
        }
        if ( (flags & MODEL_PROXIES_INSTALLED) != 0) {
            if (fProxyModels.size() != 0) return false;
        }
        if ( (flags & VIEWER_UPDATES_RUNNING) != 0) {
            if (fViewerUpdatesRunning != 0) {
            	return false;
            }
        }
        if ( (flags & LABEL_UPDATES_RUNNING) != 0) {
            if (fLabelUpdatesRunning != 0) {
            	return false;
            }
        }
        
        return true;
    }
    
    public void updateStarted(IViewerUpdate update) {
        synchronized (this) {
        	fViewerUpdatesRunning++;
        }
    }
    
    public void updateComplete(IViewerUpdate update) {
        synchronized (this) {
        	fViewerUpdatesRunning--;
        }

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
        synchronized (this) {
        	fLabelUpdatesRunning--;
        }
        if (!fLabelUpdates.remove(update.getElementPath()) && fFailOnRedundantUpdates) {
            Assert.fail("Redundant update: " + update);
        }
    }

    public void labelUpdateStarted(ILabelUpdate update) {
        synchronized (this) {
        	fLabelUpdatesRunning++;
        }
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
    
    public void stateRestoreUpdatesBegin(Object input) {
    }
    
    public void stateRestoreUpdatesComplete(Object input) {
        fStateRestoreComplete = true;
    }
    
    public void stateSaveUpdatesBegin(Object input) {
    }

    public void stateSaveUpdatesComplete(Object input) {
        fStateSaveComplete = true;
    }
    
    public void stateUpdateComplete(Object input, IViewerUpdate update) {
    }
    
    public void stateUpdateStarted(Object input, IViewerUpdate update) {
    }
    
    private String toString(int flags) {
        StringBuffer buf = new StringBuffer("Viewer Update Listener");
        
        if ( (flags & LABEL_UPDATES_COMPLETE) != 0) {
            buf.append("\n\t");
            buf.append("fLabelUpdatesComplete = " + fLabelUpdatesComplete);
        }
        if ( (flags & LABEL_UPDATES_RUNNING) != 0) {
            buf.append("\n\t");
            buf.append("fLabelUpdatesRunning = " + fLabelUpdatesRunning);
        }
        if ( (flags & LABEL_UPDATES) != 0) {
            buf.append("\n\t");
            buf.append("fLabelUpdates = ");
            buf.append( toString(fLabelUpdates) );
        }
        if ( (flags & CONTENT_UPDATES_COMPLETE) != 0) {
            buf.append("\n\t");
            buf.append("fViewerUpdatesComplete = " + fViewerUpdatesComplete);
        }
        if ( (flags & VIEWER_UPDATES_RUNNING) != 0) {
            buf.append("\n\t");
            buf.append("fViewerUpdatesRunning = " + fViewerUpdatesRunning);
        }
        if ( (flags & HAS_CHILDREN_UPDATES) != 0) {
            buf.append("\n\t");
            buf.append("fHasChildrenUpdates = ");
            buf.append( toString(fHasChildrenUpdates) );
        }
        if ( (flags & CHILDREN_COUNT_UPDATES) != 0) {
            buf.append("\n\t");
            buf.append("fChildCountUpdates = ");
            buf.append( toString(fChildCountUpdates) );
        }
        if ( (flags & CHILDREN_UPDATES) != 0) {
            buf.append("\n\t");
            buf.append("fChildrenUpdates = ");
            buf.append( toString(fChildrenUpdates) );
        }
        if ( (flags & MODEL_CHANGED_COMPLETE) != 0) {
            buf.append("\n\t");
            buf.append("fModelChangedComplete = " + fModelChangedComplete);
        }
        if ( (flags & STATE_SAVE_COMPLETE) != 0) {
            buf.append("\n\t");
            buf.append("fStateSaveComplete = " + fStateSaveComplete);
        }
        if ( (flags & STATE_RESTORE_COMPLETE) != 0) {
            buf.append("\n\t");
            buf.append("fStateRestoreComplete = " + fStateRestoreComplete);
        }
        if ( (flags & MODEL_PROXIES_INSTALLED) != 0) {
            buf.append("\n\t");
            buf.append("fProxyModels = " + fProxyModels);
        }
        if (fTimeoutInterval > 0) {
            buf.append("\n\t");
            buf.append("fTimeoutInterval = " + fTimeoutInterval);
        }
        return buf.toString();
    }

    private String toString(Set set) {
        if (set.isEmpty()) {
            return "(EMPTY)";
        }
        StringBuffer buf = new StringBuffer();
        for (Iterator itr = set.iterator(); itr.hasNext(); ) {
            buf.append("\n\t\t");
            buf.append(toString((TreePath)itr.next()));
        }
        return buf.toString();
    }
    
    private String toString(Map map) {
        if (map.isEmpty()) {
            return "(EMPTY)";
        }
        StringBuffer buf = new StringBuffer();
        for (Iterator itr = map.keySet().iterator(); itr.hasNext(); ) {
            buf.append("\n\t\t");
            TreePath path = (TreePath)itr.next();
            buf.append(toString(path));
            Set updates = (Set)map.get(path);
            buf.append(" = ");
            buf.append(updates.toString());
        }
        return buf.toString();
    }
    
    private String toString(TreePath path) {
        if (path.getSegmentCount() == 0) {
            return "/";
        }
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < path.getSegmentCount(); i++) {
            buf.append("/");
            buf.append(path.getSegment(i));
        }
        return buf.toString();
    }
    
    public String toString() {
        return toString(ALL_UPDATES_COMPLETE | MODEL_CHANGED_COMPLETE | STATE_RESTORE_COMPLETE);
    }
}


