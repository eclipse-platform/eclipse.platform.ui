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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.set.AbstractObservableSet;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.internal.databinding.observable.tree.IUnorderedTreeProvider;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableSetTreeContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreePathContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * NON-API - Generic tree content provider to be used with an AbstractTreeViewer based on a IUnorderedTreeProvider.
 * @since 1.1
 * @deprecated Use {@link ObservableSetTreeContentProvider} or
 * {@link ObservableListTreeContentProvider} instead.
 */
public class UnorderedTreeContentProvider implements ITreeContentProvider, ITreePathContentProvider {

    private HashMap mapElementToTreeNode = new HashMap();
    private LinkedList enqueuedPrefetches = new LinkedList();
    private IParentProvider rootParentProvider = null;
    private boolean useTreePaths = false;
    
    class KnownElementsSet extends AbstractObservableSet {
        
        protected KnownElementsSet() {
            super(); 
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.internal.databinding.provisional.observable.set.AbstractObservableSet#getWrappedSet()
         */
        protected Set getWrappedSet() {
            return mapElementToTreeNode.keySet();
        }
        
        void doFireDiff(Set added, Set removed) {
            fireSetChange(Diffs.createSetDiff(added, removed));
        }
        
        public void fireSetChange(SetDiff diff) {
        	super.fireSetChange(diff);
        }

        void doFireStale(boolean isStale) {
            if (isStale) {
                fireStale();
            } else {
                fireSetChange(Diffs.createSetDiff(Collections.EMPTY_SET, Collections.EMPTY_SET));
            }
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#getElementType()
         */
        public Object getElementType() {
            return new Object();
        }
    }
    
    KnownElementsSet elements = new KnownElementsSet();
    
    private ITreeViewerListener expandListener = new ITreeViewerListener() {
        public void treeCollapsed(TreeExpansionEvent event) {
        }

        public void treeExpanded(TreeExpansionEvent event) {
        }
    };

    private IUnorderedTreeProvider provider;
    private Object pendingNode;

    private int avoidViewerUpdates;

    private TreeViewer treeViewer;

    private int staleCount = 0;
    private boolean useRefresh;
    private int maxPrefetches = 0;

    /**
     * Constructs a content provider that will render the given tree in a TreeViewer. 
     * 
     * @param provider IObservableTree that provides the contents of the tree
     * @param pendingNode element to insert whenever a node is being fetched in the background
     * @param useRefresh true = notify the viewer of changes by calling refresh(...), false =
     *        notify the viewer of changes by calling add(...) and remove(...). Using false
     *        is more efficient, but may not work with TreeViewer subclasses. 
     */
    public UnorderedTreeContentProvider(IUnorderedTreeProvider provider, 
            Object pendingNode, boolean useRefresh) {
        this.provider = provider;
        this.pendingNode = pendingNode;
        this.useRefresh = useRefresh;
    }
 
    /**
     * Sets whether this content provider should add/remove elements using
     * TreePaths (true) or elements (false).
     * 
     * <p></p>
     * <p>When using elements:</p>
     * 
     * <ul>
     * <li>Cycles are permitted (elements can be their own ancestor)</li>
     * <li>Addition, removal, and refresh are slightly faster</li>
     * <li>It is not possible to have more than one content provider per tree</li>
     * <li>The setRootPath(...) method is ignored</li>
     * </ul>
     * 
     * <p></p>
     * <p>When using TreePaths:</p>
     * 
     * <ul>
     * <li>Cycles are not permitted (elements cannot be their own parent)</li>
     * <li>Addition, removal, and refresh are slightly slower</li>
     * <li>It is possible to use more than one content provider in the same tree</li>
     * <li>The setRootPath(...) method can be used to direct the output to a particular
     *     subtree</li>
     * </ul>
     * 
     * @param usePaths
     */
    public void useTreePaths(boolean usePaths) {
        this.useTreePaths = usePaths;
    }
    
    /**
     * @param rootParentProvider
     */
    public void setRootPath(IParentProvider rootParentProvider) {
    	this.rootParentProvider = rootParentProvider;
    }
    
    /**
     * @param maxPrefetches
     */
    public void setMaxPrefetches(int maxPrefetches) {
        this.maxPrefetches = maxPrefetches; 
    }
    
    /* package */ IObservableSet createChildSet(Object element) {
        return provider.createChildSet(element);
    }

    /* package */ void remove(Object element, Set removals, boolean lastElement) {
        if (removals.isEmpty()) {
            return;
        }
        if (avoidViewerUpdates == 0) {
            if (lastElement || useRefresh) {
                doRefresh(element);
            } else {
                if (useTreePaths) {
                    List toRemove = new ArrayList();
                    TreePath[] parents = getParents(element);
                    for (int i = 0; i < parents.length; i++) {
                        TreePath parent = parents[i];

                        for (Iterator iter = removals.iterator(); iter.hasNext();) {
                            Object elementToRemove = (Object) iter.next();
                            
                            toRemove.add(parent.createChildPath(element).createChildPath(elementToRemove));
                        }
                    }
                    
                    treeViewer.remove((TreePath[]) toRemove.toArray(new TreePath[toRemove.size()]));
                } else {
                    treeViewer.remove(element, removals.toArray());
                }
            }
            for (Iterator iter = removals.iterator(); iter.hasNext();) {
                Object next = (Object) iter.next();
                
                TreeNode nextNode = (TreeNode)mapElementToTreeNode.get(next);
                if (nextNode != null) {
                    nextNode.removeParent(element);
                    removeIfUnused(nextNode);
                }
            }
        }
    }

    /* package */ void add(Object element, Set additions) {
        if (additions.isEmpty()) {
            return;
        }
        if (avoidViewerUpdates == 0) {
            // Handle new parents
            addParent(element, additions);
            if (useRefresh) {
                doRefresh(element);
            } else {
                if (useTreePaths) {
                    TreePath[] parents = getParents(element);
                    for (int i = 0; i < parents.length; i++) {
                        TreePath parent = parents[i];
                        
                        treeViewer.add(parent.createChildPath(element), additions.toArray());
                    }
                } else {
                    treeViewer.add(element, additions.toArray());
                }
            }
        }
    }

    private void doRefresh(Object element) {
        treeViewer.refresh(element);
    }
    
    /**
     * Ensures that the given set of children have the given parent as 
     * one of their parents.
     *  
     * @param parent
     * @param children
     */
    private void addParent(Object parent, Set children) {
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            Object next = (Object) iter.next();
            
            TreeNode nextNode = getNode(next);
            nextNode.addParent(parent);
        }
    }

    /**
     * @return saouesnth
     */
    public final Object getPendingNode() {
        return pendingNode;
    }
    
    /**
     * @param parent
     * @return aueosnht
     */
    public IObservableSet getChildrenSet(Object parent) {
        IObservableSet result = getNode(parent).getChildrenSet();
        
        return result;
    }
    
    public void dispose() {
        if (treeViewer != null) {
            try {
                avoidViewerUpdates++;
                enqueuedPrefetches.clear();
                Object[] keys = mapElementToTreeNode.keySet().toArray();
    
                for (int i = 0; i < keys.length; i++) {
                    Object key = keys[i];
    
                    TreeNode result = (TreeNode)mapElementToTreeNode.get(key);
                    if (result != null) {
                        result.dispose();
                    }
                }
                setViewer(null);
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
        
        try {
            avoidViewerUpdates++;
            TreeNode oldNode = (TreeNode)mapElementToTreeNode.get(oldInput);
            if (oldNode != null) {
                removeIfUnused(oldNode);
            }
        } finally {
            avoidViewerUpdates--;
        }
    }
    
    private void removeIfUnused(TreeNode toRemove) {
        //TreeNode result = (TreeNode)mapElementToTreeNode.get(element);
        Object element = toRemove.getElement();
        if (toRemove.getParent() == null) {
            mapElementToTreeNode.remove(element);
            elements.doFireDiff(Collections.EMPTY_SET, Collections.singleton(element));
            toRemove.dispose();
        }
    }

    private void setViewer(Viewer viewer) {
        if (viewer != null && !(viewer instanceof TreeViewer)) {
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

    public Object[] getChildren(Object parentElement) {
        Set result = getNode(parentElement).getChildren();
        
        addParent(parentElement, result);
        
        return result.toArray();
    }

    private TreeNode getNode(Object parentElement) {
        TreeNode result = (TreeNode)mapElementToTreeNode.get(parentElement);
        if (result == null) {
            result = new TreeNode(parentElement, this);
            mapElementToTreeNode.put(parentElement, result);
            elements.fireSetChange(Diffs.createSetDiff(Collections.singleton(parentElement), 
                    Collections.EMPTY_SET));
        }
        return result;
    }

    public Object getParent(Object element) {
        Object result = getNode(element).getParent();
        if (result == null && rootParentProvider != null) {
            result = rootParentProvider.getParent(element);
        }
        return result;
    }

    public boolean hasChildren(Object element) {
        return getNode(element).shouldShowPlus();
    }

    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }
    
    /**
     * @return aouesnth
     */
    public IObservableSet getKnownElements() {
        return elements;
    }
    
    /* package */ void changeStale(int staleDelta) {
        staleCount += staleDelta;
        processPrefetches();
        elements.setStale(staleCount != 0);
    }

    /**
     * @return aoueesnth      
     */
    public TreeViewer getViewer() {
        return treeViewer;
    }

    /**
     * @param element
     * @return aoeusnth
     */
    public boolean isDirty(Object element) {
        return false;
    }

    /* package */ void enqueuePrefetch(TreeNode node) {
        if (maxPrefetches > 0 || maxPrefetches == -1) {
            if (staleCount == 0) {
                // Call node.getChildren()... this will cause us to start listening to the 
                // node and will trigger prefetching. Don't call prefetch since this method
                // is intended to be called inside getters (which will simply return the
                // fetched nodes) and prefetch() is intended to be called inside an asyncExec,
                // which will notify the viewer directly of the newly discovered nodes.
                node.getChildren();
            } else {
                enqueuedPrefetches.add(node);
                while (maxPrefetches >= 0 && enqueuedPrefetches.size() > maxPrefetches) {
                    enqueuedPrefetches.removeFirst();
                }
            }
        }
    }

    private void processPrefetches() {
        while (staleCount == 0 && !enqueuedPrefetches.isEmpty()) {
            TreeNode next = (TreeNode)enqueuedPrefetches.removeLast();
            
            // Note that we don't remove nodes from the prefetch queue when they are disposed,
            // so we may encounter disposed nodes at this time. 
            if (!next.isDisposed()) {
                next.prefetch();
            }
        }
    }

    public Object[] getChildren(TreePath parentPath) {
        return getChildren(parentPath.getLastSegment());
    }

    public TreePath[] getParents(Object element) {
        // Compute all paths that do not contain cycles
    	/**
    	 * List of Lists
    	 */
        List parentPaths = computeParents(element, new HashSet());
        
        /**
         * List of TreePath
         */
        List result = new ArrayList();
       
        for (Iterator iterator = parentPaths.iterator(); iterator.hasNext();) {
			List nextPath = (List) iterator.next();
			            
            LinkedList resultPath = new LinkedList();
            resultPath.addAll(nextPath);
        	Object nextParent = resultPath.isEmpty() ? element : resultPath.getFirst();
            for(;nextParent != null;) {
            	if (rootParentProvider != null) {
            		nextParent = rootParentProvider.getParent(nextParent);
                    if (nextParent != null) {
                        resultPath.addFirst(nextParent);
                    }
            	} else {
            		nextParent = null;
            	}
            }
            
            result.add(new TreePath(resultPath.toArray()));
        }
        
        if (result.isEmpty() && rootParentProvider != null) {
            Object nextParent = rootParentProvider.getParent(element);
            if (nextParent != null) {
                LinkedList resultPath = new LinkedList();
                while (nextParent != null) {
                    resultPath.addFirst(nextParent);
                    nextParent = rootParentProvider.getParent(nextParent);
                }
                
                result.add(new TreePath(resultPath.toArray()));
            }
            
        }
        
        return (TreePath[]) result.toArray(new TreePath[result.size()]);
    }
    
    /**
     * 
     * @param node
     * @param toIgnore
     * @return a list of Lists, indicating all known paths to the given node
     */
    private List computeParents(Object node, HashSet toIgnore) {
        List result = new ArrayList();
        boolean containedNode = toIgnore.add(node);
        
        TreeNode tn = getNode(node);
        
        HashSet parents = new HashSet();
        parents.addAll(tn.getParents());
        parents.removeAll(toIgnore);
        if (parents.isEmpty()) {
            ArrayList newPath = new ArrayList();
            result.add(newPath);
        } else {
        	for (Iterator iterator = parents.iterator(); iterator.hasNext();) {
				Object parent = iterator.next();
				
				List parentPaths = computeParents(parent, toIgnore);

				for (Iterator iterator2 = parentPaths.iterator(); iterator2
						.hasNext();) {
					List parentPath = (List) iterator2.next();
					
                    parentPath.add(parent);
                    result.add(parentPath);
                }
			}
        }
        
        if (containedNode) {
            toIgnore.remove(node);
        }
        return result;
    }

    public boolean hasChildren(TreePath path) {
        return hasChildren(path.getLastSegment());
    }
}
