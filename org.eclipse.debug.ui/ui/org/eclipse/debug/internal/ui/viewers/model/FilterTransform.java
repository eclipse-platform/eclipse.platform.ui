/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.viewers.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.TreePath;


/**
 * Helper class to support filtering in virtual tree viewer.
 * Translates indexes from viewer to model coordinate space (and visa versa).
 * <p>
 * This filter transform maintains a tree representing filtered elements in the
 * viewer. The filtering is performed dynamically as elements are 'replaced' in the tree
 * by a lazy tree content provider.
 * </p>
 * <p>
 * This class not intended to be subclassed or instantiated. For internal use only.
 * </p>
 * @since 3.3
 */
class FilterTransform {

	private Node root = new Node();
	
	class Node {
		private int[] filteredChildren = null; // only set for leaves
		private Map children = null; // only set for parent nodes, indexed by child
		
		Node() {
		}
		
		boolean addFilter(TreePath path, int childIndex, int pathIndex) {
			if (pathIndex == path.getSegmentCount()) {
				if (filteredChildren == null) {
					filteredChildren = new int[]{childIndex};
					return true;
				}
				int location = Arrays.binarySearch(filteredChildren, childIndex);
				if (location >= 0) {
					return false;
				}
				location = 0 - (location + 1);
				int[] next = new int[filteredChildren.length + 1];
				if (location == 0) {
					next[0] = childIndex;
					System.arraycopy(filteredChildren, 0, next, 1, filteredChildren.length);
				} else if (location == filteredChildren.length) {
					next[filteredChildren.length] = childIndex;
					System.arraycopy(filteredChildren, 0, next, 0, filteredChildren.length);
				} else {
					System.arraycopy(filteredChildren, 0, next, 0, location);
					next[location] = childIndex;
					System.arraycopy(filteredChildren, location, next, location + 1, filteredChildren.length - location);
				}
				filteredChildren = next;
				return true;
			}
			
			if (children == null) {
				children = new HashMap();
			}
			Object element = path.getSegment(pathIndex);
			Node node = (Node) children.get(element);
			if (node == null) {
				node = new Node();
				children.put(element, node);
			}
			return node.addFilter(path, childIndex, pathIndex + 1);
		}
		
		boolean clear(TreePath path, int pathIndex) {
			if (pathIndex == path.getSegmentCount()) {
				return true;
			}
			if (children == null) {
				return false;
			}
			Object child = path.getSegment(pathIndex);
			Node node = (Node) children.get(child);
			if (node != null) {
				if (node.clear(path, pathIndex + 1)) {
					children.remove(child);
				}
			}
			return children.isEmpty() && (filteredChildren == null || filteredChildren.length == 0);
		}
		
		boolean clear(TreePath path, int childIndex, int pathIndex) {
			if (pathIndex == path.getSegmentCount()) {
				if (filteredChildren != null) {
					int location = Arrays.binarySearch(filteredChildren, childIndex);
					if (location >= 0) {
						// remove it
						if (location == 0) {
							if (filteredChildren.length == 1) {
								filteredChildren = null;
								return true;
							} else {
								int[] next = new int[filteredChildren.length - 1];
								System.arraycopy(filteredChildren, 1, next, 0, next.length);
								filteredChildren = next;
							}
						} else if (location == (filteredChildren.length - 1)) {
							int[] next = new int[filteredChildren.length - 1];
							System.arraycopy(filteredChildren, 0, next, 0, location);
							filteredChildren = next;
						} else {
							int[] next = new int[filteredChildren.length - 1];
							System.arraycopy(filteredChildren, 0, next, 0, location);
							System.arraycopy(filteredChildren, location + 1, next, location, next.length - location);
							filteredChildren = next;
						}
						return false;
					}
				} else {
					return false;
				}
			}
			if (children == null) {
				return false;
			}
			Object element = path.getSegment(pathIndex);
			Node node = (Node) children.get(element);
			if (node == null) {
				return false;
			}
			boolean remove = node.clear(path, childIndex, pathIndex + 1);
			if (remove) {
				children.remove(element);
				return filteredChildren == null && children.isEmpty();
			} else {
				return false;
			}
		}
		
		Node find(TreePath path, int pathIndex) {
			if (pathIndex == path.getSegmentCount()) 
				return this;
			if (children == null) {
				return null;
			}
			Object child = path.getSegment(pathIndex);
			Node node = (Node) children.get(child);
			if (node != null) {
				return node.find(path, pathIndex + 1);
			}
			return null;
		}
		
		int viewToModel(int childIndex) {
			if (filteredChildren == null) {
				return childIndex;
			}
			// If there are filtered children, then we want to find the
			// (n+1)th missing number in the list of filtered indexes (missing
			// entries are visible in the view). For example, if the request
			// has asked for the model index corresponding to the 4th viewer
			// index, then we want to find the 5th missing number in the
			// filtered index sequence.
			
			int count = -1; // count from 0, 1, 2...
			int missingNumbers = 0; // how many numbers missing from the filtered index
			int offset = 0; // offset into the filtered index
			
			while (missingNumbers < (childIndex + 1)) {
				count++;
				if (offset < filteredChildren.length) {
					if (filteredChildren[offset] == count) {
						// not missing
						offset++;
					} else {
						// missing
						missingNumbers++;
					}
				} else {
					missingNumbers++;
				}
			}
			return count;
		}
		
		int modelToView(int childIndex) {
			if (filteredChildren == null) {
				return childIndex;
			}
			int offset = 0;
			for (int i = 0; i < filteredChildren.length; i++) {
				if (childIndex == filteredChildren[i] ) {
					return -1;
				} else if (childIndex > filteredChildren[i]) {
					offset++;
				} else {
					break;
				}
			}
			return childIndex - offset;
		}	
		
		int modelToViewCount(int childCount) {
			if (filteredChildren == null) {
				return childCount;
			}
			return childCount - filteredChildren.length;
		}
		
		boolean isFiltered(int index) {
			if (filteredChildren != null) {
				int location = Arrays.binarySearch(filteredChildren, index);
				return location >= 0;
			}
			return false;
		}
		
		/**
		 * Sets the child count for this element, trimming any filtered elements
		 * that were above this count.
		 * 
		 * @param childCount new child count
		 */
		void setModelChildCount(int childCount) {
			if (filteredChildren != null) {
				for (int i = 0; i < filteredChildren.length; i++) {
					if (filteredChildren[i] >= childCount) {
						// trim
						if (i == 0) {
							filteredChildren = null;
							return;
						} else {
							int[] temp = new int[i + 1];
							System.arraycopy(filteredChildren, 0, temp, 0, temp.length);
							filteredChildren = temp;
						}
					}
				}
			}
		}
	}

	/**
	 * Filters the specified child of the given parent and returns
	 * whether the child was already filtered.
	 * 
	 * @param parentPath path to parent element
	 * @param childIndex index of filtered child relative to parent (in model coordinates)
	 * @return whether the child was already filtered
	 */
	public synchronized boolean addFilteredIndex(TreePath parentPath, int childIndex) {
		return root.addFilter(parentPath, childIndex, 0);
	}
	
	/**
	 * Clears all filtered elements.
	 */
	public synchronized void clear() {
		root = new Node();
	}
	
	/**
	 * Clears all filters in the subtree of the given element.
	 * 
	 * @param path element path
	 */
	public synchronized void clear(TreePath path) {
		root.clear(path, 0);
	}
	
	/**
	 * Clears the given filtered index of the specified parent.
	 * 
	 * @param path parent path
	 * @param index index to clear
	 */
	public synchronized void clear(TreePath parentPath, int index) {
		root.clear(parentPath, index, 0);
	}	
	
	/**
	 * Translates and returns the given model index (raw index) into
	 * a view index (filtered index), or -1 if filtered.
	 * 
	 * @param parentPath path to parent element
	 * @param childIndex index of child element in model space
	 * @return the given index in view coordinates, or -1 if filtered.
	 */
	public synchronized int modelToViewIndex(TreePath parentPath, int childIndex) {
		Node parentNode = root.find(parentPath, 0);
		if (parentNode == null) {
			return childIndex;
		}
		return parentNode.modelToView(childIndex);
	}
	
	/**
	 * Translates and returns the given view index (filtered) into
	 * a model index (raw index).
	 * 
	 * @param parentPath path to parent element
	 * @param childIndex index of child element in view space
	 * @return the given index in model coordinates
	 */
	public synchronized int viewToModelIndex(TreePath parentPath, int childIndex) {
		Node parentNode = root.find(parentPath, 0);
		if (parentNode == null) {
			return childIndex;
		}
		return parentNode.viewToModel(childIndex);
	}
	
	/**
	 * Returns the number of children for the given parent, in the model.
	 * 
	 * @param parentPath path to parent element
	 * @param viewCount number of children in the view
	 * @return number of children in the model
	 */
	public synchronized int viewToModelCount(TreePath parentPath, int viewCount) {
		Node parentNode = root.find(parentPath, 0);
		if (parentNode != null) {
			if (parentNode.filteredChildren != null) {
				return viewCount + parentNode.filteredChildren.length;
			}
		}
		return viewCount;
	}
	
	/**
	 * Translates and returns the given model child count (raw) into
	 * a view count (filtered).
	 * 
	 * @param parentPath path to parent element
	 * @param count child count in model space
	 * @return the given count in view coordinates
	 */
	public synchronized int modelToViewCount(TreePath parentPath, int count) {
		Node parentNode = root.find(parentPath, 0);
		if (parentNode == null) {
			return count;
		}
		return parentNode.modelToViewCount(count);
	}	
	
	/**
	 * Returns whether the given index of the specified parent is currently filtered.
	 * 
	 * @param parentPath path to parent element
	 * @param index index of child element
	 * @return whether the child is currently filtered
	 */
	public synchronized boolean isFiltered(TreePath parentPath, int index) {
		Node parentNode = root.find(parentPath, 0);
		if (parentNode == null) {
			return false;
		}
		return parentNode.isFiltered(index);
	}
	
	/**
	 * Returns filtered children of the given parent, or <code>null</code> if none.
	 * 
	 * @param parentPath
	 * @return filtered children or <code>null</code>
	 */
	public int[] getFilteredChildren(TreePath parentPath) {
		Node parentNode = root.find(parentPath, 0);
		if (parentNode == null) {
			return null;
		}
		return parentNode.filteredChildren;
	}
	
	/**
	 * Clears any filters for the given parent above the given count.
	 * 
	 * @param parentPath path to parent element
	 * @param childCount child count
	 */
	public synchronized void setModelChildCount(TreePath parentPath, int childCount) {
		Node parentNode = root.find(parentPath, 0);
		if (parentNode != null) {
			parentNode.setModelChildCount(childCount);
		}
	}
}
