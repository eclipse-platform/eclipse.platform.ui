/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.internal.viewers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IUpdatableTree;
import org.eclipse.jface.databinding.Updatable;
import org.eclipse.jface.databinding.internal.swt.AsyncRunnable;
import org.eclipse.jface.databinding.internal.swt.SyncRunnable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.2
 * 
 */
//TODO Thread safe
public class TreeViewerUpdatableTree extends Updatable implements IUpdatableTree {
			
	protected TreeViewer viewer;
	
	// TODO use identityWrapper
	private Map nodes = new HashMap();
	
	private Object rootKey=this;
	
	private Object viewerInput=null;
	
	private boolean updatingViewer=false;
	
	private boolean updatingVirtual=false;
	
	private List viewerRefreshList = new ArrayList();
	
	Class[] classTypes;
		
	private class TreeNode {
		Object parent;
		List   children;
		boolean requestedChildren=false;
		private TreeNode(Object parent, List children) {
			this.parent=parent;
			this.children=children;			
		}
		private TreeNode(Object parent) {
			this(parent, null);
		}
		/**
		 * @return a List, the children of this node
		 */
		public List getChildren() {
			return children;
		}
		/**
		 * @return an Object, the parent of this Node
		 */
		public Object getParent() {
			return parent;
		}
		/**
		 * @param children
		 */
		public void setChildren(List children) {
			this.children = children;
			requestedChildren=false;
		}
		/**
		 * @return true if VIRTUAL request has been made already
		 */
		public boolean isRequestedChildren() {
			return requestedChildren;
		}
		/**
		 * @param requestedChildren
		 */
		public void setRequestedChildren(boolean requestedChildren) {
			this.requestedChildren = requestedChildren;
		}
	}

	
	/**
	 * @param viewer
	 */
	public TreeViewerUpdatableTree(final TreeViewer viewer) {	
		this (viewer, new Class[] { Object.class } );
	}
	/**
	 * @param viewer 
	 * @param classTypes 
	 */
	public TreeViewerUpdatableTree(final TreeViewer viewer, Class[] classTypes) {		
		this.viewer = viewer;
		this.classTypes=classTypes;
		
		viewer.setContentProvider(new ITreeContentProvider() {		
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				viewerInput=newInput;
			}		
			public void dispose() {}		
			public Object[] getElements(Object inputElement) {
				return TreeViewerUpdatableTree.this.getElements(inputElement);
			}		
			public boolean hasChildren(final Object element) {
				return TreeViewerUpdatableTree.this.hasChildren(element);
			}		
			public Object getParent(Object element) {
				TreeNode node = getTreeNode(element);
				if (node!=null)
					return node.getParent();
				return null;
			}		
			public Object[] getChildren(Object parentElement) {
				return TreeViewerUpdatableTree.this.getElements(parentElement);
			}		
		});
		
		this.viewer.setInput(this);
	}
		
	protected TreeNode getTreeNode(Object element) {		
		if (element==null || element==rootKey || element==viewerInput) {
			TreeNode result = (TreeNode) nodes.get(rootKey);
			if (result==null) {
				result = new TreeNode(null);
				nodes.put(rootKey, result);
			}			
			return result;
		}
		return (TreeNode)nodes.get(element);
	}
	
	private int primAddChildElement(List list, Object element, int index) {
		int position;
		if (index < 0 || index > list.size()) {
			position = list.size();
			list.add(element);
		} else {
			list.add(index, element);
			position = index;
		}
		return position;
	}
	
	protected void updateViewer(Runnable runable) {
		if (updatingViewer) {
			viewerRefreshList.add(runable);
			return ;
		}
		
		updatingViewer=true;
		try {			
			runable.run();
		}
		finally {
			updatingViewer=false;
		}
		//	Virtual events may need to update the viewer as well
		while (!viewerRefreshList.isEmpty()) {
		   Runnable work[] = (Runnable[])viewerRefreshList.toArray(new Runnable[viewerRefreshList.size()]);
		   viewerRefreshList.clear();
		   for (int i = 0; i < work.length; i++) 
			updateViewer(work[i]);		
		}
	}
		
	public int addElement(final Object parentElement, final int index, final Object value) {
		SyncRunnable runnable = new SyncRunnable() {
			public Object run() {
				return new Integer(addElement(parentElement, index, value, true));
			}
		};
		return ((Integer)runnable.run()).intValue();
	}
	
	
	protected int addElement(final Object parentElement, int index, final Object value, final boolean fire) {				
		TreeNode parentNode = getTreeNode(parentElement);
		if (parentNode == null) return -1;
			

		if (!hasChildren(parentElement) && parentNode.getChildren()==null)
			parentNode.setChildren(new ArrayList());
		List list = parentNode.getChildren();
		int addedIndex = -1;
		if (!list.contains(value)) {
			addedIndex = primAddChildElement(list, value, index);
			TreeNode valueNode = new TreeNode(parentElement);
			nodes.put(value, valueNode);
			final int fireIndex=addedIndex;
			updateViewer(new Runnable() {			
				public void run() {
					viewer.add(parentElement == null ? viewerInput : parentElement,
							value);			
					if (fire) 
						fireChangeEvent(ChangeEvent.ADD, null, value, parentElement, fireIndex);						
				}			
			});			
		}
		return addedIndex;
	}


	public void removeElement(final Object parentElement, final int index) {
		SyncRunnable runnable = new SyncRunnable() {
			public Object run() {
				removeElement(parentElement, index, true);
				return null;
			}
		};
		runnable.run();
	}
	
	
	protected void removeElement(final Object parentElement, final int index, final boolean fire) {
		if (hasChildren(parentElement)) {
			TreeNode parentNode = getTreeNode(parentElement);
			List children = parentNode.getChildren();
			if (children!=null) {
				final Object element = children.remove(index);			
				updateViewer(new Runnable() {			
					public void run() {
							viewer.remove(element);		
							if (fire)
								fireChangeEvent(ChangeEvent.REMOVE, element, null, parentElement, index);
					}
				});
			}
			if (children.size()==0)
				parentNode.setChildren(null);
			}
	}


	public void setElement(final Object parentElement, final int index, final Object value) {	
		AsyncRunnable runnable = new AsyncRunnable() {
			public void run() {
				if (hasChildren(parentElement)) {
					TreeNode parentNode = getTreeNode(parentElement);
					List children = parentNode.getChildren();
					Object oldValue = children.get(index);
					if (oldValue.equals(value)) {
						updateViewer(new Runnable() {
							public void run() {
								viewer.update(value, null);
							}
						});
					} else {
						removeElement(parentElement, index, false);
						addElement(parentElement, index, value, false);
					}
					fireChangeEvent(ChangeEvent.CHANGE, oldValue, value,
							parentElement, index);
				}
				
			}
		};
		runnable.runOn(viewer.getTree().getDisplay());
	}

	public void setElements(final Object parentElement, final Object[] values) {
		SyncRunnable runnable = new SyncRunnable() {
			public Object run() {
				hasChildren(parentElement); // prime Children if needed
				TreeNode parentNode = getTreeNode(parentElement);
				if (parentNode == null)
					return null;

				List children = parentNode.getChildren();
				if (children != null) {
					while (children.size() > 0)
						removeElement(parentElement, 0, true);
				}					
				parentNode.setRequestedChildren(true);
				if (values != null)
					for (int i = 0; i < values.length; i++) {
						addElement(parentElement, i, values[i], true);
					}
				return null;
			}
		};
		runnable.runOn(viewer.getTree().getDisplay());
	}


	public Object getElement(Object parentElement, int index) {		
		Object element = null;
		if (hasChildren(parentElement)) {
			TreeNode parentNode = getTreeNode(parentElement);
			if (parentNode!=null) {
				List children = parentNode.getChildren();
				if (children!=null) 
					if (index>=0&&index<children.size())
						element = children.get(index);
			}
		}
		return element;
	}


	public Object[] getElements(Object parentElement) {
		if (hasChildren(parentElement)) {
			TreeNode parentNode = getTreeNode(parentElement);
			
			List children = parentNode==null? null : parentNode.getChildren();
			return children==null? Collections.EMPTY_LIST.toArray():children.toArray();
		}
		return Collections.EMPTY_LIST.toArray();
	}


	/**
	 * This method will drive <code>ChangeElement.VIRTUAL</code> if no
	 * childrent are set.
	 *  
	 * @param element
	 * @return true if element has children, false if not
	 */
	public boolean hasChildren(final Object element) {
		TreeNode node = getTreeNode(element);	
		if (node==null) return false;
		boolean have = node.getChildren()!=null && node.getChildren().size()>0;
		if (!have && !updatingVirtual && !node.isRequestedChildren()) {	
			    updatingVirtual=true;
				try {					
					fireChangeEvent(ChangeEvent.VIRTUAL, null, null, element, -1);
					// Once asked, the assumption is that the model will listen, and update
					// if a new child is added
					node.setRequestedChildren(true);
					// VIRTUAL processing may or may not be on the same thread
					// In any case, the binder will come back with the right answer
					// eventually
					have = node.getChildren()!=null && node.getChildren().size()>0;
			    }
			    finally {			    	
			    	updatingVirtual=false;
			    }
		}
		return have;
	}

	/**
	 * @param element
	 * @return element's parent
	 */
	public Object getParent(Object element) {
		TreeNode elementNode = (TreeNode)nodes.get(element);
		return elementNode.getParent();
	}

	public Class[] getTypes() {
		return classTypes;
	}	

}
