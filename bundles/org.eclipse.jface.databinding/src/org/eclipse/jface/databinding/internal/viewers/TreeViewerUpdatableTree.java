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
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.2
 *
 */
public class TreeViewerUpdatableTree extends Updatable implements IUpdatableTree {
			
	private TreeViewer viewer;
	
	private Map nodes = new HashMap();
	
	private Object rootKey=this;
	
	private Object viewerInput=null;
	
	private boolean updating=false;
	
	private List viewerRefreshList = new ArrayList();
	
	Class[] classTypes;
		
	private class TreeNode {
		Object parent;
		Class  type;
		List   children;
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
		}
		/**
		 * @return the Class for the TreeNode
		 */
		public Class getType() {
			return type;
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
				TreeNode node = getTreeNode(element);				
				boolean have = node.getChildren()!=null && node.getChildren().size()>0;
				if (!have) {												
						fireChangeEvent(ChangeEvent.VIRTUAL, null, null, element, -1);
						// VIRTUAL processing may or may not be on the same thread
						// In any case, the binder will come back with the right answer
						// eventually
						have = node.getChildren()!=null && node.getChildren().size()>0;
				}
				return have;
			}		
			public Object getParent(Object element) {
				// TODO Auto-generated method stub
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
		if (updating) {
			viewerRefreshList.add(runable);
			return ;
		}
		
		updating=true;
		try {			
			runable.run();
		}
		finally {
			updating=false;
		}
		//	Virtual events may need to update the viewer as well
		while (!viewerRefreshList.isEmpty()) {
		   Runnable work[] = (Runnable[])viewerRefreshList.toArray(new Runnable[viewerRefreshList.size()]);
		   viewerRefreshList.clear();
		   for (int i = 0; i < work.length; i++) 
			updateViewer(work[i]);		
		}
	}
		
	public int addElement(final Object parentElement, int index, final Object value) {
		return addElement(parentElement, index, value, true);
	}
	
	/**
	 * @param parentElement
	 * @param index
	 * @param value
	 * @param fire
	 * @return added index
	 */
	public int addElement(final Object parentElement, int index, final Object value, final boolean fire) {				
		TreeNode parentNode = getTreeNode(parentElement);
		if (parentNode == null)
			throw new IllegalArgumentException(
					"Invalid Parent" + parentElement); //$NON-NLS-1$

		if (parentNode.getChildren() == null)
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


	public void removeElement(Object parentElement, int index) {
		removeElement(parentElement, index, true);
	}
	
	/**
	 * @param parentElement
	 * @param index
	 * @param fire
	 */
	public void removeElement(final Object parentElement, final int index, final boolean fire) {		
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


	public void setElement(Object parentElement, int index, final Object value) {		
		TreeNode parentNode = getTreeNode(parentElement);
		List children = parentNode.getChildren();
		Object oldValue;
		if (children!=null) {
			oldValue = children.get(index);
			if (oldValue.equals(value)) {
				updateViewer(new Runnable() {			
					public void run() {
						viewer.update(value, null);
					}
				});
			} else {
				removeElement(parentElement,index, false);
				addElement(parentElement, index, value, false);
			}
			fireChangeEvent(ChangeEvent.CHANGE, oldValue, value, parentElement, index );
		}
	}

	public void setElements(Object parentElement, Object[] values) {		
		TreeNode parentNode = getTreeNode(parentElement);
		List children = parentNode.getChildren();
		if (children!=null) {
			Object[] elements = children.toArray();
			for (int i = 0; i < elements.length; i++) {
				removeElement(parentElement, i);
			}
		}
		if (values!=null)
			for (int i = 0; i < values.length; i++) {
				addElement(parentElement,i, values[i]);
			}				
	}


	public Object getElement(Object parentElement, int index) {		
		Object element = null;
		TreeNode parentNode = getTreeNode(parentElement);
		List children = parentNode.getChildren();
		if (children!=null) 
			if (index>=0&&index<children.size())
				element = children.get(index);
		return element;
	}


	public Object[] getElements(Object parentElement) {		
		TreeNode parentNode = getTreeNode(parentElement);
		List children = parentNode==null? null : parentNode.getChildren();
		return children==null? Collections.EMPTY_LIST.toArray():children.toArray();
	}




	public Object getParent(Object element) {
		TreeNode elementNode = (TreeNode)nodes.get(element);
		return elementNode.getParent();
	}

	public Class[] getTypes() {
		return classTypes;
	}	

}
