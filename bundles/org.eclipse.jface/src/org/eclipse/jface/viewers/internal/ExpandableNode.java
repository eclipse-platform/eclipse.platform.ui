/*******************************************************************************
 * Copyright (c) 2023 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 				Raghunandana Murthappa
 *******************************************************************************/
package org.eclipse.jface.viewers.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;

/**
 * The expandable placeholder element to be used for viewer items that represent
 * an expandable tree or table element.
 * <p>
 * The idea of {@link ExpandableNode} is to allow viewers to show only some
 * subset of children that would otherwise all appear below parent element. The
 * main purpose of this is to prevent UI freezes with viewers that can offer lot
 * of elements but can't efficiently handle such amount using SWT.
 * <p>
 * The node consists of a parent element, list of all children of this parent
 * and the offset to which child elements are supposed to be created and shown
 * in the viewer.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ExpandableNode {

	/**
	 * This array must be sorted always because we will populate elements
	 * from offset onwards.
	 */
	private Object[] orginalArray;
	private final int startOffSet;
	private final int limit;
	/**
	 * These elements are added into expandable node. will be rendered in the next
	 * iterations.
	 */
	private final List<Object> addedElements;
	/**
	 * a cursor only used for labeling to point to starting point of next block.
	 */
	private final int start;

	private final StructuredViewer viewer;

	/**
	 * @param children    non null list of children
	 * @param startOffSet first not shown item index
	 * @param limit       current child limit of the viewer
	 * @param viewer      owner of the node
	 */
	public ExpandableNode(Object[] children, int startOffSet, int limit, StructuredViewer viewer) {
		Assert.isNotNull(children, "children of an ExpandableNode cannot be null"); //$NON-NLS-1$
		Assert.isTrue(startOffSet >= 0 && startOffSet < children.length,
				"startOffSet must be within the range of children: " + startOffSet); //$NON-NLS-1$
		this.orginalArray = children;
		this.startOffSet = startOffSet;
		this.start = startOffSet;
		this.limit = limit;
		this.viewer = viewer;
		this.addedElements = new ArrayList<>();
	}

	/**
	 * The viewer is supposed to show subset of original elements as children of
	 * this node up to this offset (but not including it). In other worlds, this is
	 * the index of first invisible element under this node.
	 *
	 * {@return current offset in the original list of elements}
	 */
	public int getOffset() {
		return startOffSet;
	}

	/**
	 * This method returns those children of the current node which are supposed to
	 * be not created / shown yet in the viewer.
	 *
	 * {@return all remaining elements from original array starting with the element
	 * at offset index}
	 */
	public Object[] getRemainingElements() {
		if (addedElements.size() > 0) {
			Object[] children = updateChildrenWithAddedElements();
			return children;
		}
		Object[] children = new Object[orginalArray.length - startOffSet];
		System.arraycopy(orginalArray, startOffSet, children, 0, children.length);
		return children;
	}

	/**
	 * This will grow the original array with added elements. And it clears the
	 * added elements. Also sort the newly added elements along with remaining
	 * elements from offset.
	 *
	 * @return Returns the remaining elements from offset. returned array is sorted
	 *         if viewer has comparator.
	 */
	private Object[] updateChildrenWithAddedElements() {
		// sub array from offset + newly added elements
		Object[] children = new Object[orginalArray.length - startOffSet + addedElements.size()];

		System.arraycopy(orginalArray, startOffSet, children, 0, orginalArray.length - startOffSet);
		System.arraycopy(addedElements.toArray(), 0, children, orginalArray.length - startOffSet, addedElements.size());

		if (viewer.getComparator() != null) {
			viewer.getComparator().sort(viewer, children);
		}

		// grow the original array with newly added elements.
		Object[] newOriginalArray = new Object[orginalArray.length + addedElements.size()];
		System.arraycopy(orginalArray, 0, newOriginalArray, 0, startOffSet);
		System.arraycopy(children, 0, newOriginalArray, startOffSet, children.length);
		this.orginalArray = newOriginalArray;
		addedElements.clear();
		return children;
	}

	/**
	 * {@return original list of elements of the parent}
	 */
	public Object[] getAllElements() {
		if (addedElements.size() > 0) {
			updateChildrenWithAddedElements();
			return this.orginalArray;
		}
		return orginalArray;
	}

	/**
	 * {@return label shown for the node in the viewer}
	 */
	public String getLabel() {
		Integer start = Integer.valueOf(this.start + 1);
		Integer length = Integer.valueOf(orginalArray.length + addedElements.size());
		int next = this.start + this.limit;
		if (next > orginalArray.length + addedElements.size()) {
			next = orginalArray.length + addedElements.size();
		}
		Integer nextBlock = Integer.valueOf(next);
		String label = JFaceResources.format("ExpandableNode.defaultLabel", start, nextBlock, length); //$NON-NLS-1$
		return label;
	}

	/**
	 * Client can use {@link TableViewer#add(Object[])} to add an element beyond
	 * visible range. It must be tracked to render when {@link ExpandableNode} node
	 * is clicked.
	 *
	 * @param element
	 */
	public void addElement(Object element) {
		addedElements.add(element);
	}
}
