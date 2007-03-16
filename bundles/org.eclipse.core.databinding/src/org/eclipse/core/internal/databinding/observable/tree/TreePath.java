/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.databinding.observable.tree;

import org.eclipse.core.runtime.Assert;

/**
 * A tree path denotes a model element in a tree viewer. Tree path objects have
 * value semantics. A model element is represented by a path of elements in the
 * tree from the root element to the leaf element.
 * <p>
 * Clients may instantiate this class. Not intended to be subclassed.
 * </p>
 * 
 * @since 3.2
 */
public final class TreePath {
	
	/**
	 * Constant for representing an empty tree path.
	 */
	public static final TreePath EMPTY = new TreePath(new Object[0]);
	
	private Object[] segments;

	private int hash;

	/**
	 * Constructs a path identifying a leaf node in a tree.
	 * 
	 * @param segments
	 *            path of elements to a leaf node in a tree, starting with the
	 *            root element
	 */
	public TreePath(Object[] segments) {
		Assert.isNotNull(segments);
		for (int i = 0; i < segments.length; i++) {
			Assert.isNotNull(segments[i]);
		}
		this.segments = segments;
	}

	/**
	 * Returns the element at the specified index in this path.
	 * 
	 * @param index
	 *            index of element to return
	 * @return element at the specified index
	 */
	public Object getSegment(int index) {
		return segments[index];
	}

	/**
	 * Returns the number of elements in this path.
	 * 
	 * @return the number of elements in this path
	 */
	public int getSegmentCount() {
		return segments.length;
	}

	/**
	 * Returns the first element in this path.
	 * 
	 * @return the first element in this path
	 */
	public Object getFirstSegment() {
		if (segments.length == 0) {
			return null;
		}
		return segments[0];
	}

	/**
	 * Returns the last element in this path.
	 * 
	 * @return the last element in this path
	 */
	public Object getLastSegment() {
		if (segments.length == 0) {
			return null;
		}
		return segments[segments.length - 1];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (!(other instanceof TreePath)) {
			return false;
		}
		TreePath otherPath = (TreePath) other;
		if (segments.length != otherPath.segments.length) {
			return false;
		}
		for (int i = 0; i < segments.length; i++) {
				if (!segments[i].equals(otherPath.segments[i])) {
					return false;
				}
		}
		return true;
	}

	public int hashCode() {
		if (hash == 0) {
			for (int i = 0; i < segments.length; i++) {
					hash += segments[i].hashCode();
			}
		}
		return hash;
	}

	/**
	 * Returns whether this path starts with the same segments as the given
	 * path, using the given comparer to compare segments.
	 * 
	 * @param treePath
	 *            path to compare to
	 * @return whether the given path is a prefix of this path, or the same as
	 *         this path
	 */
	public boolean startsWith(TreePath treePath) {
		int thisSegmentCount = getSegmentCount();
		int otherSegmentCount = treePath.getSegmentCount();
		if (otherSegmentCount == thisSegmentCount) {
			return equals(treePath);
		}
		if (otherSegmentCount > thisSegmentCount) {
			return false;
		}
		for (int i = 0; i < otherSegmentCount; i++) {
			Object otherSegment = treePath.getSegment(i);
				if (!otherSegment.equals(segments[i])) {
					return false;
				}
		}
		return true;
	}

	/**
	 * Returns a copy of this tree path with one segment removed from the end,
	 * or <code>null</code> if this tree path has no segments.
	 * @return a tree path
	 */
	public TreePath getParentPath() {
		int segmentCount = getSegmentCount();
		if (segmentCount <= 1) {
			return null;
		}
		Object[] parentSegments = new Object[segmentCount - 1];
		System.arraycopy(segments, 0, parentSegments, 0, segmentCount - 1);
		return new TreePath(parentSegments);
	}

	/**
	 * Returns a copy of this tree path with the given segment added at the end.
	 * @param newSegment 
	 * @return a tree path
	 */
	public TreePath createChildPath(Object newSegment) {
		int segmentCount = getSegmentCount();
		Object[] childSegments = new Object[segmentCount + 1];
		if(segmentCount>0) {
			System.arraycopy(segments, 0, childSegments, 0, segmentCount);
		}
		childSegments[segmentCount] = newSegment;
		return new TreePath(childSegments);
	}
}
