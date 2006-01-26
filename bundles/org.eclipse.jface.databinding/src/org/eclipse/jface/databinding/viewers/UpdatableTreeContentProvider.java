/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2005-2006. All Rights Reserved. 
 * 
 * Note to U.S. Government Users Restricted Rights:  Use, 
 * duplication or disclosure restricted by GSA ADP Schedule 
 * Contract with IBM Corp.
 *******************************************************************************/
package org.eclipse.jface.databinding.viewers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.databinding.AbstractUpdatableSet;
import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IReadableSet;
import org.eclipse.jface.databinding.ITreeProvider;
import org.eclipse.jface.databinding.updatables.EmptyReadableSet;
import org.eclipse.jface.internal.databinding.swt.SWTUtil;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public final class UpdatableTreeContentProvider implements ITreeContentProvider {

    private ITreeProvider treeProvider;
    
    private Map mapElementToValueTreeNode = new HashMap();

    private TreeViewer treeViewer;

    private ArrayList dirtyListeners = new ArrayList();
    
    private int dirtyCount;

    // Workaround for known JFace/SWT bug. The SWT table nulls out its items
    // field
    // before sending the Disposed event. This means that if we try to touch the
    // viewer
    // in the dispose callback, we will throw an NPE. inputChanged and dispose()
    // both get
    // called from the dispose callback, so we disable updates in these two
    // methods.
    private int avoidViewerUpdates = 0;

    private ITreeViewerListener expandListener = new ITreeViewerListener() {
        public void treeCollapsed(TreeExpansionEvent event) {

        }

        public void treeExpanded(TreeExpansionEvent event) {
            ValueTreeNode node = (ValueTreeNode) mapElementToValueTreeNode.get(event.getElement());

            if (node == null) {
                return;
            }

            node.expanded();
        }
    };

    private class KnownElementsSet extends AbstractUpdatableSet {
        protected Collection computeElements() {
            return UpdatableTreeContentProvider.this.computeKnownElements();
        }

        public void doFireAdd(Collection added) {
            fireAdded(added);
        }

        public void doFireRemove(Collection removed) {
            fireRemoved(removed);
        }
        
        public void doFireStale(boolean isStale) {
        	fireStale(isStale);
        }
    };

    private KnownElementsSet elements = new KnownElementsSet();

	private Object inputElement;

    private class ValueTreeNode implements IChangeListener {
        private HashSet parents = new HashSet();
        private Object node;
        // Stores one representative parent (there may be more than one)
        private Object parent;

        // Stores the set of children for this node (null if unknown)
        private IReadableSet model;
        
        // True iff the children have already been added to the viewer
        private boolean childrenAddedToViewer = false;

        public ValueTreeNode(Object key) {
            node = key;
        }
        
        public void childrenAddedToViewer() {
        	this.childrenAddedToViewer = true;
        }
        
        public void addParent(Object parent) {
        	if (this.parent == null) {
        		this.parent = parent;
        	}
        	parents.add(parent);
        }
        
        public void removeParent(Object parent) {
        	parents.remove(parent);
        	if (parent == this.parent) {
        		if (parents.isEmpty()) {
        			this.parent = null;
        		} else {
        			this.parent = parents.iterator().next();
        		}
        	}
        }

        public final Collection getChildren() {
            if (model == null) {
                return Collections.EMPTY_SET;
            }
            return model.toCollection();
        }
        
        /**
         * Called when this node is expanded
         */
        public void expanded() {
            for (Iterator iter = getChildren().iterator(); iter.hasNext();) {
                Object next = (Object) iter.next();

                ValueTreeNode childProvider = (ValueTreeNode) mapElementToValueTreeNode.get(next);

                childProvider.startListening();
            }
        }

        /**
         * Instructs this node to start monitoring child nodes
         */
        public void startListening() {
            if (model == null) {
                model = treeProvider.createChildList(node);
                if (model == null) {
                    model = EmptyReadableSet.getInstance();
                }
                model.addChangeListener(this);
                if (!childrenAddedToViewer) {
                	childrenAddedToViewer = true;
                	doAdd(model.toCollection());
                }
                if (model.isStale()) {
                    setStale(node, true);
                }
            }
        }
        
        public void setStale(Object staleObject, boolean isStale) {
            if (isStale) {
                dirtyCount++;
                if (dirtyCount == 1) {
                	elements.doFireStale(true);
                }
            } else {
                dirtyCount--;
                if (dirtyCount == 0) {
                	elements.doFireStale(false);
                }
            }
            fireStale(node, isStale);
        }

        /**
         * Instructs this node to stop monitoring its child nodes
         */
        public void stopListening() {
            if (model != null) {

                if (childrenAddedToViewer) {
                	doRemove(model.toCollection());
                	childrenAddedToViewer = false;
                }
                if (model.isStale()) {
                    setStale(node, false);
                }
                model.removeChangeListener(this);
                model = null;
            }
        }

        public void dispose() {
            stopListening();
            mapElementToValueTreeNode.remove(node);
        }

        public void handleChange(ChangeEvent changeEvent) {
        	if (isDisposed() || model == null) {
        		return;
        	}
        	
        	switch (changeEvent.getChangeType()) {
        	case ChangeEvent.ADD_MANY:
        		doAdd((Collection)changeEvent.getNewValue());
        		break;
        		
        	case ChangeEvent.REMOVE_MANY:
        		doRemove((Collection)changeEvent.getNewValue());
        		break;
        	
        	case ChangeEvent.STALE:
        		setStale(node, ((Boolean)changeEvent.getNewValue()).booleanValue());
        		break;
        	}
        }


        /**
         * Adds child elements. Must be called from the UI thread.
         * 
         * @param added
         *            elements to add
         */
        private void doAdd(Collection added) {
            if (added.isEmpty()) {
                return;
            }

            for (Iterator iter = added.iterator(); iter.hasNext();) {
				Object object = iter.next();
				
                createSubtree(this.node, object, false);
            }

            if (mapElementToValueTreeNode.containsKey(node)) {
                treeViewer.add(node, added.toArray());
            }

            doFireAdd(added);
        }

        private void doRemove(Collection removed) {
            if (removed.isEmpty()) {
                return;
            }

            Collection actuallyRemoved = new ArrayList();
            
            for (Iterator iter = removed.iterator(); iter.hasNext();) {
				Object object = iter.next();
				
                if (pruneSubtree(node, object)) {
                	actuallyRemoved.add(node);
                }
            }

            if (!treeViewer.getControl().isDisposed()) {
                if (avoidViewerUpdates == 0) {
                    if (mapElementToValueTreeNode.containsKey(node)) {
                    	// FIXME: reflection code to maintain 3.1 compatibility.
                    	Method removeMethod = null;
                    	boolean haveRemoveMethod32 = false;
                    	try {
                    		removeMethod = treeViewer.getClass().getDeclaredMethod("remove", new Class[]{Object.class, Object[].class}); //$NON-NLS-1$
                    		if (removeMethod != null) 
                    			haveRemoveMethod32 = true;
                    	} catch (Exception e) {}
                    	if (haveRemoveMethod32) {
                    		try {
                    			removeMethod.invoke(treeViewer, new Object[] {removed.toArray()});
                    		} catch (Exception e) {
                    			// This should never happen, but it's better than crashing...
                            	treeViewer.remove(removed.toArray());
                    		}
                    	} else {
                        	treeViewer.remove(removed.toArray());
                    	}
                    }
                    
                    doFireRemove(actuallyRemoved);
                }
            }
        }

        public boolean isDirty() {
            return model != null && model.isStale();
        }

		public Set getParents() {
			return parents;
		}
    }

    public UpdatableTreeContentProvider(ITreeProvider provider) {
        this.treeProvider = provider;
    }

    private void fireStale(Object node, boolean isStale) {
    	ChangeEvent event = new ChangeEvent(node, ChangeEvent.STALE, null, isStale ? Boolean.TRUE : Boolean.FALSE);
    	for (Iterator iter = dirtyListeners.iterator(); iter.hasNext();) {
			IChangeListener next = (IChangeListener) iter.next();
			
			try {
				next.handleChange(event);
			} catch (Exception e) {
				SWTUtil.logException(e);
			}
		}
	}

	/**
     * Returns the number of nodes that are currently dirty (that are currently
     * being fetched in a background thread).
     * 
     * @return the number of nodes that are currently dirty
     */
    public int getDirtyCount() {
        return dirtyCount;
    }

    /**
     * Adds a dirty listener to this content provider. This listener will be notified
     * whenever background processing starts or stops anywhere in the tree. This
     * is commonly used by label providers to decorate nodes that are being recomputed
     * asynchronously. 
     * <p>
     * The listener will recieve a
     * ChangeEvent.STALE or ChangeEvent.NOT_STALE event with the source pointing
     * to an element in the tree whenever the stale state changes for any node.
     * </p>
     * 
     * @param toAdd listener to add
     */
    public void addDirtyListener(IChangeListener toAdd) {
    	dirtyListeners.add(toAdd);
    }

    /**
     * Removes a listener previously added via addDirtyListener
     * 
     * @param toRemove
     */
    public void removeDirtyListener(IChangeListener toRemove) {
    	dirtyListeners.remove(toRemove);
    }

    private void doFireAdd(Collection added) {
        elements.doFireAdd(added);
    }

    private boolean isDisposed() {
        return treeViewer.getControl().isDisposed();
    }

    private void doFireRemove(Collection removed) {
        elements.doFireRemove(removed);
    }

    private ValueTreeNode createSubtree(Object parent, Object object, boolean childrenAddedToViewer) {
        ValueTreeNode result = (ValueTreeNode) mapElementToValueTreeNode.get(object);

        if (result == null) {
            result = new ValueTreeNode(object);

            mapElementToValueTreeNode.put(object, result);
        }
        
        if (childrenAddedToViewer) {
        	result.childrenAddedToViewer();
        }
        
        if (parent != null) {
        	result.addParent(parent);
        }
        
        if (parent == null || parent == inputElement || treeViewer.getExpandedState(parent)) {
            result.startListening();
        }

        return result;
    }

    private boolean pruneSubtree(Object parent, Object child) {
        ValueTreeNode result = (ValueTreeNode) mapElementToValueTreeNode.get(child);

        if (result != null) {
        	result.removeParent(parent);
        	if (result.getParents().isEmpty()) {
        		result.dispose();
        		return true;
        	}
        }
        
        return false;
    }

    private boolean subtreeExists(Object element) {
        return mapElementToValueTreeNode.containsKey(element);
    }

    public Object[] getChildren(Object parentElement) {
        // Shouldn't happen -- inputChanged is supposed to be called before the viewer asks for
        // any elements, but we include this check so that we end up returning an empty subtree
        // rather than throwing an NPE.
        if (treeViewer == null) {
            return new Object[0];
        }
        
        ValueTreeNode node = (ValueTreeNode) mapElementToValueTreeNode.get(parentElement);

        if (node == null) {
            node = createSubtree(null, parentElement, true);
        }

        return node.getChildren().toArray();
    }

    public Object getParent(Object element) {
        // Shouldn't happen -- inputChanged is supposed to be called before the viewer asks for
        // any elements, but we include this check so that we end up returning an bogus result
        // rather than throwing an NPE.
        if (treeViewer == null) {
            return null;
        }
        
        ValueTreeNode node = (ValueTreeNode) mapElementToValueTreeNode.get(element);

        if (node == null) {
            return null;
        }

        // Returns one representative parent of the given node (there may me many)
        return node.parent;
    }

    public boolean hasChildren(Object element) {
        // Shouldn't happen -- inputChanged is supposed to be called before the viewer asks for
        // any elements, but we include this check so that we end up returning an empty subtree
        // rather than throwing an NPE.
        if (treeViewer == null) {
            return false;
        }
        
        return getChildren(element).length > 0;
    }

    public Object[] getElements(Object inputElement) {
    	return getChildren(inputElement);
    }

    public void dispose() {
        if (treeViewer != null) {
            try {
                avoidViewerUpdates++;
                Object[] keys = mapElementToValueTreeNode.keySet().toArray();
    
                for (int i = 0; i < keys.length; i++) {
                    Object key = keys[i];
    
                    ValueTreeNode result = (ValueTreeNode)mapElementToValueTreeNode.get(key);
                    result.dispose();
                }
            } finally {
                avoidViewerUpdates--;
            }
        }
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // This should only ever be called for a single viewer
        setViewer(viewer);
        
        if (oldInput != null && newInput != null && oldInput.equals(newInput)) {
            return;
        }
        
        inputElement = newInput;
        try {
            avoidViewerUpdates++;
            pruneSubtree(null, oldInput);
        } finally {
            avoidViewerUpdates--;
        }
    }

    private void setViewer(Viewer viewer) {
        if (!(viewer instanceof TreeViewer)) {
            throw new IllegalArgumentException("This content provider can only be used with TreeViewers"); //$NON-NLS-1$
        }
        TreeViewer newTreeViewer = (TreeViewer) viewer;
        
        if (newTreeViewer != treeViewer) {
            if (treeViewer != null) {
                treeViewer.removeTreeListener(expandListener);
            }
            
            this.treeViewer = newTreeViewer;
            if (newTreeViewer != null) {
                newTreeViewer.addTreeListener(expandListener);
            }
        }
    }

    public IReadableSet getKnownElements() {
        return elements;
    }

    private Collection computeKnownElements() {
        return Collections.unmodifiableCollection(mapElementToValueTreeNode.keySet());
    }

    public boolean isDirty(Object element) {
        ValueTreeNode node = (ValueTreeNode) mapElementToValueTreeNode.get(element);
        if (node == null) {
            return false;
        }
        return node.isDirty();
    }
    
    public TreeViewer getViewer() {
        return treeViewer;
    }

}
