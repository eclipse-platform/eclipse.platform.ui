/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Xenos, IBM - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.provisional.viewers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableSetTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Control;

/**
 * 
 * @since 1.0
 * @deprecated Use {@link ObservableSetTreeContentProvider} or
 * {@link ObservableListTreeContentProvider} instead.
 */
/* package */ class TreeNode implements ISetChangeListener, IStaleListener {
    private UnorderedTreeContentProvider contentProvider;
    private Object element;
    
    // Stores the set of parents (null if there are less than 2)
    private HashSet parents = null;
    
    // Stores one representative parent. If there is more than one parent,
    // the complete set of parents can be found in the parents set.
    Object parent;
    
    /**
     * Set of child elements.
     */
    private IObservableSet children;
    
    private boolean hasPendingNode = false;
    private boolean isStale;
    private boolean listeningToChildren = false;
    private boolean prefetchEnqueued = false;
    
    /**
     * @param element
     * @param cp
     */
    public TreeNode(Object element, UnorderedTreeContentProvider cp) {
        this.element = element;
        this.contentProvider = cp;
        children = contentProvider.createChildSet(element);
        if (children == null) {
            children = Observables.emptyObservableSet();
            listeningToChildren = true;
        }
        hasPendingNode = children.isStale();
    }
    
    /**
     * @param parent
     */
    public void addParent(Object parent) {
        if (this.parent == null) {
            this.parent = parent;
        } else {
            if (parent.equals(this.parent)) {
                return;
            }
            if (parents == null) {
                parents = new HashSet();
                parents.add(this.parent);
            }
            parents.add(parent);
        }
    }
    
    /**
     * @param parent
     */
    public void removeParent(Object parent) {
        if (this.parents != null) {
            parents.remove(parent);
        } 
        
        if (parent == this.parent) {
            if (parents == null || parents.isEmpty()) {
                this.parent = null;
            } else {
                this.parent = parents.iterator().next();
            }
        }
        
        if (this.parents != null && this.parents.size() <= 1) {
            this.parents = null;
        }
    }
    
    /**
     * Returns the set of children for this node. If new children are discovered later, they
     * will be added directly to the viewer.
     *  
     * @return TODO
     */
    public Set getChildren() {
        if (!listeningToChildren) {
            listeningToChildren = true;
            children.addSetChangeListener(this);
            hasPendingNode = children.isEmpty() && children.isStale();
            children.addStaleListener(this);
            updateStale();
        }
        
        // If the child set is stale and empty, show the "pending" node
        if (hasPendingNode) {
            Object pendingNode = contentProvider.getPendingNode();
            return Collections.singleton(pendingNode);
        }
        return children;
    }
    
    /**
     * @return TODO
     */
    public IObservableSet getChildrenSet() {
        return children;
    }
    
    private void updateStale() {
        boolean willBeStale = children.isStale();
        if (willBeStale != isStale) {
            isStale = willBeStale;
            
            contentProvider.changeStale(isStale? 1 : -1);
        }
    }
    
    /**
     * @return TODO
     */
    public boolean isStale() {
        return isStale;
    }

    /**
     * Returns true if the viewer should show a plus sign for expanding this 
     * node. 
     * 
     * @return TODO
     */
    public boolean shouldShowPlus() {
        if (children == null) {
//            if (!hasPendingNode) {
//                hasPendingNode = true;
//                contentProvider.add(element, Collections.singleton(contentProvider.getPendingNode()));
//            }
            return true;
        }
        if (!listeningToChildren && !prefetchEnqueued) {
            prefetchEnqueued = true;
            contentProvider.enqueuePrefetch(this);
        }
        return !listeningToChildren || hasPendingNode || !children.isEmpty();
    }
    
    /**
     * Disposes this node and removes all remaining children.
     */
    public void dispose() {
        if (children != null) {
            if (listeningToChildren) {
                contentProvider.remove(element, children, true);
                children.removeSetChangeListener(this);
                children.removeStaleListener(this);
            }
            children.dispose();
            children = null;
            
            if (listeningToChildren && isStale) {
                contentProvider.changeStale(-1);
            }
        }
    }
    
    /**
     * @return TODO
     */
    public boolean isDisposed() {
        return children == null;
    }
    
    /**
     * Returns one representative parent, or null if this node is unparented. Use
     * getParents() to get the complete set of known parents.
     * 
     * @return TODO
     */
    public Object getParent() {
        return parent;
    }
    
    /**
     * 
     * @return the set of all known parents for this node
     */
    public Set getParents() {
        if (parents == null) {
            if (parent == null) {
                return Collections.EMPTY_SET;
            }
			return Collections.singleton(parent);
        }
		return parents;
    }
    
    /**
     * Called when the child set changes. Should not be called directly by the viewer.
     */
    public void handleSetChange(SetChangeEvent event) {
        SetDiff diff = event.diff;
        TreeViewer viewer = this.contentProvider.getViewer();
        if (viewer != null) {
            Control control = viewer.getControl();
            if (control != null) {
                if (control.isDisposed()) {
                    // If the widgetry was disposed without notifying the content provider, then
                    // dispose the content provider now and stop processing events.
                    contentProvider.dispose();
                    return;
                }
            }
        }
        
        boolean shouldHavePendingNode = children.isEmpty() && children.isStale();
        
        Set additions = diff.getAdditions();
        // Check if we should add the pending node
        if (shouldHavePendingNode && !hasPendingNode) {
            HashSet newAdditions = new HashSet();
            newAdditions.addAll(additions);
            newAdditions.add(contentProvider.getPendingNode());
            additions = newAdditions;
            hasPendingNode = true;
        }

        Set removals = diff.getRemovals();
        // Check if we should remove the pending node
        if (!shouldHavePendingNode && hasPendingNode) {
            HashSet newRemovals = new HashSet();
            newRemovals.addAll(removals);
            newRemovals.add(contentProvider.getPendingNode());
            removals = newRemovals;
            hasPendingNode = false;
        }
        if (!additions.isEmpty()) {
            contentProvider.add(element, additions);
        }
        if (!removals.isEmpty()) {
            contentProvider.remove(element, removals, children.isEmpty() && !hasPendingNode);
        }
        
        updateStale();
    }

    public void handleStale(StaleEvent staleEvent) {
        TreeViewer viewer = this.contentProvider.getViewer();
        if (viewer != null) {
            Control control = viewer.getControl();
            if (control != null) {
                if (control.isDisposed()) {
                    // If the widgetry was disposed without notifying the content provider, then
                    // dispose the content provider now and stop processing events.
                    contentProvider.dispose();
                    return;
                }
            }
        }
        
        boolean shouldHavePendingNode = children.isEmpty() && children.isStale();
        
        // Check if we should add the pending node
        if (shouldHavePendingNode && !hasPendingNode) {
            hasPendingNode = shouldHavePendingNode;
            contentProvider.add(element, Collections.singleton(contentProvider.getPendingNode()));
        }
        
        // Check if we should remove the pending node
        if (!shouldHavePendingNode && hasPendingNode) {
            hasPendingNode = shouldHavePendingNode;
            contentProvider.remove(element, Collections.singleton(contentProvider.getPendingNode()), true);
        }
        
        updateStale();
    }

    /**
     * @return TODO
     */
    public Object getElement() {
        return element;
    }

    /**
     * 
     */
    public void prefetch() {
        TreeViewer viewer = this.contentProvider.getViewer();
        if (viewer != null) {
            Control control = viewer.getControl();
            if (control != null) {
                if (control.isDisposed()) {
                    // If the widgetry has been disposed, then avoid sending anything
                    // to the viewer.
                    return;
                }
            }
        }
        
        Set children = getChildren();
        if (!children.isEmpty()) {
            contentProvider.add(element, children);
        } else {
            // We need to remove the + sign, and adding/removing elements won't do the trick
            contentProvider.getViewer().refresh(element);
        }
    }
}
