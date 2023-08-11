/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - bug 153993, bug 167323, bug 175192
 *     Lasse Knudsen, bug 205700
 *     Micah Hainline, bug 210448
 *     Michael Schneider, bug 210747
 *     Bruce Sutton, bug 221768
 *     Matthew Hall, bug 221988
 *     Julien Desgats, bug 203950
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548314
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.internal.InternalPolicy;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.internal.ExpandableNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Abstract base implementation for tree-structure-oriented viewers (trees and
 * table trees).
 * <p>
 * Nodes in the tree can be in either an expanded or a collapsed state,
 * depending on whether the children on a node are visible. This class
 * introduces public methods for controlling the expanding and collapsing of
 * nodes.
 * </p>
 * <p>
 * As of 3.2, AbstractTreeViewer supports multiple equal elements (each with a
 * different parent chain) in the tree. This support requires that clients
 * enable the element map by calling <code>setUseHashlookup(true)</code>.
 * </p>
 * <p>
 * Content providers for abstract tree viewers must implement one of the
 * interfaces <code>ITreeContentProvider</code> or (as of 3.2, to support
 * multiple equal elements) <code>ITreePathContentProvider</code>.
 * </p>
 * <p>
 * <strong> This class is not intended to be subclassed outside of the JFace
 * viewers framework.</strong>
 * </p>
 *
 * @see TreeViewer
 */
public abstract class AbstractTreeViewer extends ColumnViewer {

	/**
	 * Constant indicating that all levels of the tree should be expanded or
	 * collapsed.
	 *
	 * @see #expandToLevel(int)
	 * @see #collapseToLevel(Object, int)
	 */
	public static final int ALL_LEVELS = -1;

	/**
	 * List of registered tree listeners (element type:
	 * <code>TreeListener</code>).
	 */
	private ListenerList<ITreeViewerListener> treeListeners = new ListenerList<>();

	/**
	 * The level to which the tree is automatically expanded each time the
	 * viewer's input is changed (that is, by <code>setInput</code>). A value
	 * of 0 means that auto-expand is off.
	 *
	 * @see #setAutoExpandLevel
	 */
	private int expandToLevel = 0;

	/**
	 * Indicates if filters should be checked to determine expandability of
	 * a tree node.
	 */
	private boolean isExpandableCheckFilters = false;

	/**
	 * Indicates if the viewer's content provider is an instance of ITreePathContentProvider.
	 */
	private boolean isTreePathContentProvider = false;

	/**
	 * Safe runnable used to update an item.
	 */
	class UpdateItemSafeRunnable extends SafeRunnable {
		private Object element;

		private Item item;

		UpdateItemSafeRunnable(Item item, Object element) {
			this.item = item;
			this.element = element;
		}

		@Override
		public void run() {
			doUpdateItem(item, element);
		}

	}

	/**
	 * Creates an abstract tree viewer. The viewer has no input, no content
	 * provider, a default label provider, no sorter, no filters, and has
	 * auto-expand turned off.
	 */
	protected AbstractTreeViewer() {
		// do nothing
	}

	/**
	 * Adds the given child elements to this viewer as children of the given parent
	 * element. If this viewer does not have a sorter, the elements are added at the
	 * end of the parent's list of children in the order given; otherwise, the
	 * elements are inserted at the appropriate positions. If a child already exists
	 * under the given parent, the child gets refreshed and not added twice.
	 * <p>
	 * This method should be called (by the content provider) when elements have
	 * been added to the model, in order to cause the viewer to accurately reflect
	 * the model. This method only affects the viewer, not the model.
	 * </p>
	 *
	 * @param parentElementOrTreePath the parent element
	 * @param childElements           the child elements to add
	 */
	public void add(Object parentElementOrTreePath, Object... childElements) {
		Assert.isNotNull(parentElementOrTreePath);
		assertElementsNotNull(childElements);
		if (checkBusy())
			return;
		Widget[] widgets = internalFindItems(parentElementOrTreePath);
		// If parent hasn't been realized yet, just ignore the add.
		if (widgets.length == 0) {
			return;
		}

		for (Widget widget : widgets) {
			internalAdd(widget, parentElementOrTreePath, childElements);
		}
	}

	/**
	 * Find the items for the given element of tree path
	 *
	 * @param parentElementOrTreePath
	 *            the element or tree path
	 * @return the items for that element
	 *
	 * @since 3.3
	 */
	final protected Widget[] internalFindItems(Object parentElementOrTreePath) {
		Widget[] widgets;
		if (parentElementOrTreePath instanceof TreePath) {
			TreePath path = (TreePath) parentElementOrTreePath;
			Widget w = internalFindItem(path);
			if (w == null) {
				widgets = new Widget[] {};
			} else {
				widgets = new Widget[] { w };
			}
		} else {
			widgets = findItems(parentElementOrTreePath);
		}
		return widgets;
	}

	/**
	 * Return the item at the given path or <code>null</code>
	 *
	 * @param path
	 *            the path
	 * @return {@link Widget} the item at that path
	 */
	private Widget internalFindItem(TreePath path) {
		Widget[] widgets = findItems(path.getLastSegment());
		for (Widget widget : widgets) {
			if (widget instanceof Item) {
				Item item = (Item) widget;
				TreePath p = getTreePathFromItem(item);
				if (p.equals(path)) {
					return widget;
				}
			}
		}
		return null;
	}

	/**
	 * Adds the given child elements to this viewer as children of the given
	 * parent element.
	 * <p>
	 * EXPERIMENTAL. Not to be used except by JDT. This method was added to
	 * support JDT's explorations into grouping by working sets. This method
	 * cannot be removed without breaking binary backwards compatibility, but
	 * should not be called by clients.
	 * </p>
	 *
	 * @param widget
	 *            the widget for the parent element
	 * @param parentElementOrTreePath
	 *            the parent element
	 * @param childElements
	 *            the child elements to add
	 * @since 3.1
	 */
	protected void internalAdd(Widget widget, Object parentElementOrTreePath,
			Object[] childElements) {
		Object parent;
		TreePath path;
		if (parentElementOrTreePath instanceof TreePath) {
			path = (TreePath) parentElementOrTreePath;
			parent = path.getLastSegment();
		} else {
			parent = parentElementOrTreePath;
			path = null;
		}

		// optimization!
		// if the widget is not expanded we just invalidate the subtree
		if (widget instanceof Item) {
			Item ti = (Item) widget;
			if (!getExpanded(ti)) {
				boolean needDummy = isExpandable(ti, path, parent);
				boolean haveDummy = false;
				// remove all children
				Item[] items = getItems(ti);
				for (Item item : items) {
					if (item.getData() != null) {
						disassociate(item);
						item.dispose();
					} else if (needDummy && !haveDummy) {
						haveDummy = true;
					} else {
						item.dispose();
					}
				}
				// append a dummy if necessary
				if (needDummy && !haveDummy) {
					newItem(ti, SWT.NULL, -1);
				}
				return;
			}
		}

		if (childElements.length > 0) {
			// TODO: Add filtering back?
			Object[] filtered = filter(parentElementOrTreePath, childElements);
			ViewerComparator comparator = getComparator();
			if (comparator != null) {
				if (comparator instanceof TreePathViewerSorter) {
					TreePathViewerSorter tpvs = (TreePathViewerSorter) comparator;
					if (path == null) {
						path = internalGetSorterParentPath(widget, comparator);
					}
					tpvs.sort(this, path, filtered);
				} else {
					comparator.sort(this, filtered);
				}
			}
			// there are elements to be shown and viewer is showing limited items.
			// newly added element can be inside expandable node or can be visible item.
			// Assumption that user has already updated the model and needs addition of
			// item.
			if (getItemsLimit() > 0 && hasLimitedChildrenItems(widget)) {
				internalRefreshStruct(widget, parent, false);
				return;
			}

			createAddedElements(widget, filtered);
			if (InternalPolicy.DEBUG_LOG_EQUAL_VIEWER_ELEMENTS) {
				Item[] children = getChildren(widget);
				Object[] elements = new Object[children.length];
				for (int i = 0; i < children.length; i++) {
					elements[i] = children[i].getData();
				}
				assertElementsNotNull(parent, elements);
			}
		}
	}

	/**
	 * Filter the children elements.
	 *
	 * @param parentElementOrTreePath
	 *            the parent element or path
	 * @param elements
	 *            the child elements
	 * @return the filter list of children
	 */
	private Object[] filter(Object parentElementOrTreePath, Object[] elements) {
		ViewerFilter[] filters = getFilters();
		if (filters != null) {
			List<Object> filtered = new ArrayList<>(elements.length);
			for (Object element : elements) {
				boolean add = true;
				for (ViewerFilter filter : filters) {
					add = filter.select(this, parentElementOrTreePath,
							element);
					if (!add) {
						break;
					}
				}
				if (add) {
					filtered.add(element);
				}
			}
			return filtered.toArray();
		}
		return elements;
	}

	/**
	 * Create the new elements in the parent widget. If the child already exists it
	 * will be refreshed to handle potential changes within its children.
	 *
	 * @param widget
	 * @param elements Sorted list of elements to add.
	 */
	private void createAddedElements(Widget widget, Object[] elements) {

		if (elements.length == 1) {
			if (equals(elements[0], widget.getData())) {
				return;
			}
		}

		ViewerComparator comparator = getComparator();
		TreePath parentPath = internalGetSorterParentPath(widget, comparator);
		Item[] items = getChildren(widget);

		// Optimize for the empty case
		if (items.length == 0) {
			for (Object element : elements) {
				createTreeItem(widget, element, -1);
			}
			return;
		}

		// Optimize for no comparator
		if (comparator == null) {
			for (Object element : elements) {
				if (itemExists(items, element)) {
					internalRefresh(element);
				} else {
					createTreeItem(widget, element, -1);
				}
			}
			return;
		}
		// As the items are sorted already we optimize for a
		// start position. This is the insertion position relative to the
		// original item array.
		int indexInItems = 0;

		// Count of elements we have added. See bug 205700 for why this is needed.
		int newItems = 0;

		elementloop: for (Object element : elements) {
			// update the index relative to the original item array
			indexInItems = insertionPosition(items, comparator,
					indexInItems, element, parentPath);
			if (indexInItems == items.length) {
				createTreeItem(widget, element, -1);
				newItems++;
			} else {
				// Search for an item for the element. The comparator might
				// regard elements as equal when they are not.

				// Use a separate index variable to search within the existing
				// elements that compare equally, see
				// TreeViewerTestBug205700.testAddEquallySortedElements.
				int insertionIndexInItems = indexInItems - 1;
				// Searching not only forward but also backward while trying to find an equal
				// element.
				// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=571844.
				int directionStep = -1;
				while (insertionIndexInItems < items.length) {
					if (insertionIndexInItems >= 0 && internalCompare(comparator, parentPath, element,
							items[insertionIndexInItems].getData()) == 0) {
						// As we cannot assume the sorter is consistent with
						// equals() - therefore we can
						// just check against the item prior to this index (if
						// any)
						if (items[insertionIndexInItems].getData().equals(element)) {
							// Found the item for the element.
							// Refresh the element in case it has new children.
							internalRefresh(element);
							// Do not create a new item - continue with the next element.
							continue elementloop;
						}
						insertionIndexInItems += directionStep;
					} else {
						if (directionStep < 0) {
							// Reached an non equal item or the first item in forwardDirection
							// change search direction
							directionStep = 1;
							insertionIndexInItems = indexInItems;
						} else {
							// Reached an non equal item in forwardDirection, break the while loop
							// The last item is detected by the while loop itself.
							break;
						}
					}
				}
				// Did we get to the end?
				if (insertionIndexInItems == items.length) {
					createTreeItem(widget, element, -1);
					newItems++;
				} else {
					// InsertionIndexInItems is the index in the original array. We
					// need to correct by the number of new items we have
					// created. See bug 205700.
					createTreeItem(widget, element, insertionIndexInItems + newItems);
					newItems++;
				}
			}
		}
	}

	/**
	 * See if element is the data of one of the elements in items.
	 *
	 * @param items
	 * @param element
	 * @return <code>true</code> if the element matches.
	 */
	private boolean itemExists(Item[] items, Object element) {
		if (usingElementMap()) {
			Widget[] existingItems = findItems(element);
			// optimization for two common cases
			if (existingItems.length == 0) {
				return false;
			} else if (existingItems.length == 1) {
				if (items.length > 0 && existingItems[0] instanceof Item) {
					Item existingItem = (Item) existingItems[0];
					return getParentItem(existingItem) == getParentItem(items[0]);
				}
			}
		}
		for (Item item : items) {
			if (item.getData().equals(element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the index where the item should be inserted. It uses sorter to
	 * determine the correct position, if sorter is not assigned, returns the
	 * index of the element after the last.
	 *
	 * @param items
	 *            the items to search
	 * @param comparator
	 *            The comparator to use.
	 * @param lastInsertion
	 *            the start index to start search for position from this allows
	 *            optimizing search for multiple elements that are sorted
	 *            themselves.
	 * @param element
	 *            element to find position for.
	 * @param parentPath
	 *            the tree path for the element's parent or <code>null</code>
	 *            if the element is a root element or the sorter is not a
	 *            {@link TreePathViewerSorter}
	 * @return the index to use when inserting the element.
	 *
	 */

	private int insertionPosition(Item[] items, ViewerComparator comparator,
			int lastInsertion, Object element, TreePath parentPath) {

		int size = items.length;
		if (comparator == null) {
			return size;
		}
		int min = lastInsertion, max = size - 1;

		while (min <= max) {
			int mid = (min + max) / 2;
			Object data = items[mid].getData();
			int compare = internalCompare(comparator, parentPath, data, element);
			if (compare == 0) {
				return mid;// Return if we already match
			}
			if (compare < 0) {
				min = mid + 1;
			} else {
				max = mid - 1;
			}
		}
		return min;

	}

	/**
	 * Returns the index where the item should be inserted. It uses sorter to
	 * determine the correct position, if sorter is not assigned, returns the
	 * index of the element after the last.
	 *
	 * @param parent
	 *            The parent widget
	 * @param sorter
	 *            The sorter to use.
	 * @param startIndex
	 *            the start index to start search for position from this allows
	 *            optimizing search for multiple elements that are sorted
	 *            themselves.
	 * @param element
	 *            element to find position for.
	 * @param currentSize
	 *            the current size of the collection
	 * @return the index to use when inserting the element.
	 *
	 */

	/**
	 * Returns the index where the item should be inserted.
	 *
	 * @param parent
	 *            The parent widget the element will be inserted into.
	 * @param element
	 *            The element to insert.
	 * @return the index of the element
	 */
	protected int indexForElement(Widget parent, Object element) {
		ViewerComparator comparator = getComparator();
		TreePath parentPath = internalGetSorterParentPath(parent, comparator);

		Item[] items = getChildren(parent);
		int count = items.length;

		if (comparator == null) {
			return count;
		}
		int min = 0, max = count - 1;

		while (min <= max) {
			int mid = (min + max) / 2;
			Object data = items[mid].getData();
			int compare = internalCompare(comparator, parentPath, data, element);
			if (compare == 0) {
				// find first item > element
				while (compare == 0) {
					++mid;
					if (mid >= count) {
						break;
					}
					data = items[mid].getData();
					compare = internalCompare(comparator, parentPath, data,
							element);
				}
				return mid;
			}
			if (compare < 0) {
				min = mid + 1;
			} else {
				max = mid - 1;
			}
		}
		return min;
	}

	/**
	 * Return the tree path that should be used as the parent path for the given
	 * widget and sorter. A <code>null</code> is returned if either the sorter
	 * is not a {@link TreePathViewerSorter} or if the parent widget is not an
	 * {@link Item} (i.e. is the root of the tree).
	 *
	 * @param parent
	 *            the parent widget
	 * @param comparator
	 *            the sorter
	 * @return the tree path that should be used as the parent path for the
	 *         given widget and sorter
	 */
	private TreePath internalGetSorterParentPath(Widget parent,
			ViewerComparator comparator) {
		TreePath path;
		if (comparator instanceof TreePathViewerSorter
				&& parent instanceof Item) {
			Item item = (Item) parent;
			path = getTreePathFromItem(item);
		} else {
			path = null;
		}
		return path;
	}

	/**
	 * Compare the two elements using the given sorter. If the sorter is a
	 * {@link TreePathViewerSorter}, the provided tree path will be used. If
	 * the tree path is null and the sorter is a tree path sorter, then the
	 * elements are root elements
	 *
	 * @param comparator
	 *            the sorter
	 * @param parentPath
	 *            the path of the elements' parent
	 * @param e1
	 *            the first element
	 * @param e2
	 *            the second element
	 * @return the result of comparing the two elements
	 */
	private int internalCompare(ViewerComparator comparator,
			TreePath parentPath, Object e1, Object e2) {
		if (comparator instanceof TreePathViewerSorter) {
			TreePathViewerSorter tpvs = (TreePathViewerSorter) comparator;
			return tpvs.compare(this, parentPath, e1, e2);
		}
		return comparator.compare(this, e1, e2);
	}

	@Override
	protected Object[] getSortedChildren(Object parentElementOrTreePath) {
		Object[] result = null;
		ViewerComparator comparator = getComparator();
		if (parentElementOrTreePath != null
				&& comparator instanceof TreePathViewerSorter) {
			result = getFilteredChildren(parentElementOrTreePath);
			TreePathViewerSorter tpvs = (TreePathViewerSorter) comparator;

			// be sure we're not modifying the original array from the model
			result = result.clone();

			TreePath path = null;
			if (parentElementOrTreePath instanceof TreePath) {
				path = (TreePath) parentElementOrTreePath;
			} else {
				Object parent = parentElementOrTreePath;
				Widget w = internalGetWidgetToSelect(parent);
				if (w != null) {
					path = internalGetSorterParentPath(w, comparator);
				}
			}
			tpvs.sort(this, path, result);
			result = applyItemsLimit(parentElementOrTreePath, result);
		} else {
			return super.getSortedChildren(parentElementOrTreePath);
		}
		return result;
	}

	/**
	 * Adds the given child element to this viewer as a child of the given parent
	 * element. If this viewer does not have a sorter, the element is added at the
	 * end of the parent's list of children; otherwise, the element is inserted at
	 * the appropriate position. If the child already exists under the given parent,
	 * the child gets refreshed and not added twice.
	 * <p>
	 * This method should be called (by the content provider) when a single element
	 * has been added to the model, in order to cause the viewer to accurately
	 * reflect the model. This method only affects the viewer, not the model. Note
	 * that there is another method for efficiently processing the simultaneous
	 * addition of multiple elements.
	 * </p>
	 *
	 * @param parentElementOrTreePath the parent element or path
	 * @param childElement            the child element
	 */
	public void add(Object parentElementOrTreePath, Object childElement) {
		add(parentElementOrTreePath, new Object[] { childElement });
	}

	/**
	 * Adds the given SWT selection listener to the given SWT control.
	 *
	 * @param control
	 *            the SWT control
	 * @param listener
	 *            the SWT selection listener
	 * @deprecated
	 */
	@Deprecated
	protected void addSelectionListener(Control control,
			SelectionListener listener) {
		// do nothing
	}

	/**
	 * Adds a listener for expand and collapse events in this viewer. Has no
	 * effect if an identical listener is already registered.
	 *
	 * @param listener
	 *            a tree viewer listener
	 */
	public void addTreeListener(ITreeViewerListener listener) {
		treeListeners.add(listener);
	}

	/**
	 * Adds the given SWT tree listener to the given SWT control.
	 *
	 * @param control
	 *            the SWT control
	 * @param listener
	 *            the SWT tree listener
	 */
	protected abstract void addTreeListener(Control control,
			TreeListener listener);

	@Override
	protected void associate(Object element, Item item) {
		Object data = item.getData();
		if (data != null && data != element && equals(data, element)) {
			// workaround for PR 1FV62BT
			// assumption: elements are equal but not identical
			// -> remove from map but don't touch children
			unmapElement(data, item);
			item.setData(element);
			mapElement(element, item);
		} else {
			// recursively disassociate all
			super.associate(element, item);
		}
	}

	/**
	 * Collapses all nodes of the viewer's tree, starting with the root. This method
	 * is equivalent to <code>collapseToLevel(ALL_LEVELS)</code>.
	 */
	public void collapseAll() {
		Object root = getRoot();
		if (root != null) {
			collapseToLevel(root, ALL_LEVELS);
		}
	}

	/**
	 * Collapses the subtree rooted at the given element or tree path to the given
	 * level.
	 * <p>
	 * Note that the default implementation of this method does turn redraw off via
	 * this operation via a call to <code>setRedraw</code>
	 * </p>
	 *
	 * @param elementOrTreePath the element or tree path
	 * @param level             non-negative level, or <code>ALL_LEVELS</code> to
	 *                          collapse all levels of the tree
	 */
	public void collapseToLevel(Object elementOrTreePath, int level) {
		Assert.isNotNull(elementOrTreePath);
		Control control = getControl();
		try {
			control.setRedraw(false);
			Widget w = internalGetWidgetToSelect(elementOrTreePath);
			if (w != null) {
				internalCollapseToLevel(w, level);
			}
		} finally {
			control.setRedraw(true);
		}
	}

	/**
	 * Creates all children for the given widget.
	 * <p>
	 * The default implementation of this framework method assumes that
	 * <code>widget.getData()</code> returns the element corresponding to the
	 * node. Note: the node is not visually expanded! You may have to call
	 * <code>parent.setExpanded(true)</code>.
	 * </p>
	 *
	 * @param widget
	 *            the widget
	 */
	protected void createChildren(final Widget widget) {
		createChildren(widget, true);
	}

	/**
	 * Creates all children for the given widget.
	 * <p>
	 * The default implementation of this framework method assumes that
	 * <code>widget.getData()</code> returns the element corresponding to the
	 * node. Note: the node is not visually expanded! You may have to call
	 * <code>parent.setExpanded(true)</code>.
	 * </p>
	 *
	 * @param widget
	 *            the widget
	 * @param materialize
	 * 			  true if children are expected to be fully materialized
	 */
	void createChildren(final Widget widget, boolean materialize) {
		boolean oldBusy = isBusy();
		setBusy(true);
		try {
			final Item[] items = getChildren(widget);
			if (items != null && items.length > 0) {
				Object data = items[0].getData();
				if (data != null) {
					return; // children already there!
				}
			}

			// fix for PR 1FW89L7:
			// don't complain and remove all "dummies" ...
			if (items != null) {
				for (Item item : items) {
					if (item.getData() != null) {
						disassociate(item);
						Assert.isTrue(item.getData() == null, "Second or later child is non -null");//$NON-NLS-1$

					}
					item.dispose();
				}
			}
			Object d = widget.getData();
			if (d != null) {
				Object parentElement = d;
				Object[] children;
				if (isTreePathContentProvider && widget instanceof Item) {
					TreePath path = getTreePathFromItem((Item) widget);
					children = getSortedChildren(path);
				} else {
					children = getSortedChildren(parentElement);
				}
				for (Object element : children) {
					createTreeItem(widget, element, -1);
				}
			}
		} finally {
			setBusy(oldBusy);
		}
	}

	/**
	 * Creates a single item for the given parent and synchronizes it with the
	 * given element.
	 *
	 * @param parent
	 *            the parent widget
	 * @param element
	 *            the element
	 * @param index
	 *            if non-negative, indicates the position to insert the item
	 *            into its parent
	 */
	protected void createTreeItem(Widget parent, Object element, int index) {
		Item item = newItem(parent, SWT.NULL, index);
		updateItem(item, element);
		updatePlus(item, element);
	}

	/**
	 * The <code>AbstractTreeViewer</code> implementation of this method also
	 * recurses over children of the corresponding element.
	 */
	@Override
	protected void disassociate(Item item) {
		super.disassociate(item);
		// recursively unmapping the items is only required when
		// the hash map is used. In the other case disposing
		// an item will recursively dispose its children.
		if (usingElementMap()) {
			disassociateChildren(item);
		}
	}

	/**
	 * Disassociates the children of the given SWT item from their corresponding
	 * elements.
	 *
	 * @param item
	 *            the widget
	 */
	private void disassociateChildren(Item item) {
		Item[] items = getChildren(item);
		for (Item child : items) {
			if (child.getData() != null) {
				disassociate(child);
			}
		}
	}

	@Override
	protected Widget doFindInputItem(Object element) {
		// compare with root
		Object root = getRoot();
		if (root == null) {
			return null;
		}

		if (equals(root, element)) {
			return getControl();
		}
		return null;
	}

	@Override
	protected Widget doFindItem(Object element) {
		// compare with root
		Object root = getRoot();
		if (root == null) {
			return null;
		}

		Item[] items = getChildren(getControl());
		if (items != null) {
			for (Item item : items) {
				Widget o = internalFindItem(item, element);
				if (o != null) {
					return o;
				}
			}
		}
		return null;
	}

	/**
	 * Copies the attributes of the given element into the given SWT item.
	 *
	 * @param item
	 *            the SWT item
	 * @param element
	 *            the element
	 */
	protected void doUpdateItem(final Item item, Object element) {
		if (item.isDisposed()) {
			unmapElement(element, item);
			return;
		}

		int columnCount = doGetColumnCount();
		if (columnCount == 0)// If no columns are created then fake one
			columnCount = 1;

		ViewerRow viewerRowFromItem = getViewerRowFromItem(item);

		boolean isVirtual = (getControl().getStyle() & SWT.VIRTUAL) != 0;

		// If the control is virtual, we cannot use the cached viewer row object. See bug 188663.
		if (isVirtual) {
			viewerRowFromItem = (ViewerRow) viewerRowFromItem.clone();
		}

		for (int column = 0; column < columnCount; column++) {

			// ExpandableNode is shown in first column only.
			if (element instanceof ExpandableNode && column != 0) {
				continue;
			}

			ViewerColumn columnViewer = getViewerColumn(column);
			ViewerCell cellToUpdate = updateCell(viewerRowFromItem, column,
					element);

			// If the control is virtual, we cannot use the cached cell object. See bug 188663.
			if (isVirtual) {
				cellToUpdate = new ViewerCell(cellToUpdate.getViewerRow(), cellToUpdate.getColumnIndex(), element);
			}

			columnViewer.refresh(cellToUpdate);

			// clear cell (see bug 201280)
			updateCell(null, 0, null);

			// As it is possible for user code to run the event
			// loop check here.
			if (item.isDisposed()) {
				unmapElement(element, item);
				return;
			}

		}
	}

	/**
	 * Returns <code>true</code> if the given list and array of items refer to
	 * the same model elements. Order is unimportant.
	 * <p>
	 * This method is not intended to be overridden by subclasses.
	 * </p>
	 *
	 * @param items
	 *            the list of items
	 * @param current
	 *            the array of items
	 * @return <code>true</code> if the refer to the same elements,
	 *         <code>false</code> otherwise
	 *
	 * @since 3.1 in TreeViewer, moved to AbstractTreeViewer in 3.3
	 */
	protected boolean isSameSelection(List<Item> items, Item[] current) {
		// If they are not the same size then they are not equivalent
		int n = items.size();
		if (n != current.length) {
			return false;
		}

		CustomHashtable itemSet = newHashtable(n * 2 + 1);
		for (Item item : items) {
			Object element = item.getData();
			itemSet.put(element, element);
		}

		// Go through the items of the current collection
		// If there is a mismatch return false
		for (Item c : current) {
			if (c.getData() == null || !itemSet.containsKey(c.getData())) {
				return false;
			}
		}

		return true;
	}



	@Override
	protected void doUpdateItem(Widget widget, Object element, boolean fullMap) {
		boolean oldBusy = isBusy();
		setBusy(true);
		try {
			if (widget instanceof Item) {
				Item item = (Item) widget;

				// ensure that back pointer is correct
				if (fullMap) {
					associate(element, item);
				} else {
					Object data = item.getData();
					if (data != null) {
						unmapElement(data, item);
					}
					item.setData(element);
					mapElement(element, item);
				}

				// update icon and label
				SafeRunnable.run(new UpdateItemSafeRunnable(item, element));
			}
		} finally {
			setBusy(oldBusy);
		}
	}

	/**
	 * Expands all nodes of the viewer's tree, starting with the root. This
	 * method is equivalent to <code>expandToLevel(ALL_LEVELS)</code>.
	 */
	public void expandAll() {
		expandToLevel(ALL_LEVELS, true);
	}

	/**
	 * Expands all nodes of the viewer's tree, starting with the root. This method
	 * is equivalent to <code>expandToLevel(ALL_LEVELS)</code>.
	 *
	 * @param disableRedraw
	 *            <code>true</code> when drawing operations should be disabled
	 *            during expansion.
	 * @since 3.14
	 */
	public void expandAll(boolean disableRedraw) {
		expandToLevel(ALL_LEVELS, disableRedraw);
	}

	/**
	 * Expands the root of the viewer's tree to the given level.
	 *
	 * @param level
	 *            non-negative level, or <code>ALL_LEVELS</code> to expand all
	 *            levels of the tree
	 */
	public void expandToLevel(int level) {
		expandToLevel(level, true);
	}

	/**
	 * Expands the root of the viewer's tree to the given level.
	 *
	 * @param level         non-negative level, or <code>ALL_LEVELS</code> to expand
	 *                      all levels of the tree
	 * @param disableRedraw <code>true</code> when drawing operations should be
	 *                      disabled during expansion. <code>true</code> when
	 *                      drawing operations should be enabled during expansion.
	 *                      Prefer using true as this results in a faster UI
	 * @since 3.14
	 */
	public void expandToLevel(int level, boolean disableRedraw) {
		BusyIndicator.showWhile(getControl().getDisplay(), () -> {
			expandToLevel(getRoot(), level, disableRedraw);
		});
	}

	/**
	 * Expands all ancestors of the given element or tree path so that the given
	 * element becomes visible in this viewer's tree control, and then expands
	 * the subtree rooted at the given element to the given level.
	 *
	 * @param elementOrTreePath
	 *            the element
	 * @param level
	 *            non-negative level, or <code>ALL_LEVELS</code> to expand all
	 *            levels of the tree
	 */
	public void expandToLevel(Object elementOrTreePath, int level) {
		expandToLevel(elementOrTreePath, level, true);
	}

	/**
	 * Expands all ancestors of the given element or tree path so that the given
	 * element becomes visible in this viewer's tree control, and then expands the
	 * subtree rooted at the given element to the given level.
	 *
	 * @param elementOrTreePath the element
	 * @param level             non-negative level, or <code>ALL_LEVELS</code> to
	 *                          expand all levels of the tree
	 * @param disableRedraw     <code>true</code> when drawing operations should be
	 *                          disabled during expansion. <code>false</code> when
	 *                          drawing operations should be enabled during
	 *                          expansion. Prefer true as this results in a faster
	 *                          UI.
	 * @since 3.14
	 */
	public void expandToLevel(Object elementOrTreePath, int level, boolean disableRedraw) {
		if (checkBusy())
			return;
		Control control = getControl();
		try {
			if (disableRedraw) {
				control.setRedraw(false);
			}
			Widget w = internalExpand(elementOrTreePath, true);
			if (w != null) {
				internalExpandToLevel(w, level);
			}
		} finally {
			if (disableRedraw) {
				control.setRedraw(true);
			}
		}
	}

	/**
	 * Fires a tree collapsed event. Only listeners registered at the time this
	 * method is called are notified.
	 *
	 * @param event
	 *            the tree expansion event
	 * @see ITreeViewerListener#treeCollapsed
	 */
	protected void fireTreeCollapsed(final TreeExpansionEvent event) {
		boolean oldBusy = isBusy();
		setBusy(true);
		try {
			for (ITreeViewerListener l : treeListeners) {
				SafeRunnable.run(new SafeRunnable() {
					@Override
					public void run() {
						l.treeCollapsed(event);
					}
				});
			}
		} finally {
			setBusy(oldBusy);
		}
	}

	/**
	 * Fires a tree expanded event. Only listeners registered at the time this
	 * method is called are notified.
	 *
	 * @param event
	 *            the tree expansion event
	 * @see ITreeViewerListener#treeExpanded
	 */
	protected void fireTreeExpanded(final TreeExpansionEvent event) {
		boolean oldBusy = isBusy();
		setBusy(true);
		try {
			for (ITreeViewerListener l : treeListeners) {
				SafeRunnable.run(new SafeRunnable() {
					@Override
					public void run() {
						l.treeExpanded(event);
					}
				});
			}
		} finally {
			setBusy(oldBusy);
		}
	}

	/**
	 * Returns the auto-expand level.
	 *
	 * @return non-negative level, or <code>ALL_LEVELS</code> if all levels of
	 *         the tree are expanded automatically
	 * @see #setAutoExpandLevel
	 */
	public int getAutoExpandLevel() {
		return expandToLevel;
	}

	/**
	 * Returns the SWT child items for the given SWT widget.
	 *
	 * @param widget
	 *            the widget
	 * @return the child items
	 */
	protected abstract Item[] getChildren(Widget widget);

	/**
	 * Get the child for the widget at index. Note that the default
	 * implementation is not very efficient and should be overridden if this
	 * class is implemented.
	 *
	 * @param widget
	 *            the widget to check
	 * @param index
	 *            the index of the widget
	 * @return Item or <code>null</code> if widget is not a type that can
	 *         contain items.
	 *
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the index is not valid.
	 * @since 3.1
	 */
	protected Item getChild(Widget widget, int index) {
		return getChildren(widget)[index];
	}

	/**
	 * Returns whether the given SWT item is expanded or collapsed.
	 *
	 * @param item
	 *            the item
	 * @return <code>true</code> if the item is considered expanded and
	 *         <code>false</code> if collapsed
	 */
	protected abstract boolean getExpanded(Item item);

	/**
	 * Returns a list of elements corresponding to expanded nodes in this
	 * viewer's tree, including currently hidden ones that are marked as
	 * expanded but are under a collapsed ancestor.
	 * <p>
	 * This method is typically used when preserving the interesting state of a
	 * viewer; <code>setExpandedElements</code> is used during the restore.
	 * </p>
	 *
	 * @return the array of expanded elements
	 * @see #setExpandedElements
	 */
	public Object[] getExpandedElements() {
		ArrayList<Item> items = new ArrayList<>();
		internalCollectExpandedItems(items, getControl());
		ArrayList<Object> result = new ArrayList<>(items.size());
		for (Item item : items) {
			Object data = item.getData();
			if (data != null) {
				result.add(data);
			}
		}
		return result.toArray();
	}

	/**
	 * Returns whether the node corresponding to the given element or tree path
	 * is expanded or collapsed.
	 *
	 * @param elementOrTreePath
	 *            the element
	 * @return <code>true</code> if the node is expanded, and
	 *         <code>false</code> if collapsed
	 */
	public boolean getExpandedState(Object elementOrTreePath) {
		Assert.isNotNull(elementOrTreePath);
		Widget item = internalGetWidgetToSelect(elementOrTreePath);
		if (item instanceof Item) {
			return getExpanded((Item) item);
		}
		return false;
	}

	/**
	 * Returns the number of child items of the given SWT control.
	 *
	 * @param control
	 *            the control
	 * @return the number of children
	 */
	protected abstract int getItemCount(Control control);

	/**
	 * Returns the number of child items of the given SWT item.
	 *
	 * @param item
	 *            the item
	 * @return the number of children
	 */
	protected abstract int getItemCount(Item item);

	/**
	 * Returns the child items of the given SWT item.
	 *
	 * @param item
	 *            the item
	 * @return the child items
	 */
	protected abstract Item[] getItems(Item item);

	/**
	 * Returns the item after the given item in the tree, or <code>null</code>
	 * if there is no next item.
	 *
	 * @param item
	 *            the item
	 * @param includeChildren
	 *            <code>true</code> if the children are considered in
	 *            determining which item is next, and <code>false</code> if
	 *            subtrees are ignored
	 * @return the next item, or <code>null</code> if none
	 */
	protected Item getNextItem(Item item, boolean includeChildren) {
		if (item == null) {
			return null;
		}
		if (includeChildren && getExpanded(item)) {
			Item[] children = getItems(item);
			if (children != null && children.length > 0) {
				return children[0];
			}
		}

		// next item is either next sibling or next sibling of first
		// parent that has a next sibling.
		Item parent = getParentItem(item);
		if (parent == null) {
			return null;
		}
		Item[] siblings = getItems(parent);
		if (siblings != null) {
			if (siblings.length <= 1) {
				return getNextItem(parent, false);
			}

			for (int i = 0; i < siblings.length; i++) {
				if (siblings[i] == item && i < (siblings.length - 1)) {
					return siblings[i + 1];
				}
			}
		}
		return getNextItem(parent, false);
	}

	/**
	 * Returns the parent item of the given item in the tree, or
	 * <code>null</code> if there is no parent item.
	 *
	 * @param item
	 *            the item
	 * @return the parent item, or <code>null</code> if none
	 */
	protected abstract Item getParentItem(Item item);

	/**
	 * Returns the item before the given item in the tree, or <code>null</code>
	 * if there is no previous item.
	 *
	 * @param item
	 *            the item
	 * @return the previous item, or <code>null</code> if none
	 */
	protected Item getPreviousItem(Item item) {
		// previous item is either right-most visible descendent of previous
		// sibling or parent
		Item parent = getParentItem(item);
		if (parent == null) {
			return null;
		}
		Item[] siblings = getItems(parent);
		if (siblings.length == 0 || siblings[0] == item) {
			return parent;
		}
		Item previous = siblings[0];
		for (int i = 1; i < siblings.length; i++) {
			if (siblings[i] == item) {
				return rightMostVisibleDescendent(previous);
			}
			previous = siblings[i];
		}
		return null;
	}

	@Override
	protected Object[] getRawChildren(Object parentElementOrTreePath) {
		boolean oldBusy = isBusy();
		setBusy(true);
		try {
			Object parent;
			TreePath path;
			if (parentElementOrTreePath instanceof TreePath) {
				path = (TreePath) parentElementOrTreePath;
				parent = path.getLastSegment();
			} else {
				parent = parentElementOrTreePath;
				path = null;
			}
			if (parent != null) {
				if (equals(parent, getRoot())) {
					return super.getRawChildren(parent);
				}
				IContentProvider cp = getContentProvider();
				if (getItemsLimit() > 0 && parent instanceof ExpandableNode expNode) {
					return expNode.getRemainingElements();
				}
				if (cp instanceof ITreePathContentProvider) {
					ITreePathContentProvider tpcp = (ITreePathContentProvider) cp;
					if (path == null) {
						// A path was not provided so try and find one
						Widget w = findItem(parent);
						if (w instanceof Item) {
							Item item = (Item) w;
							path = getTreePathFromItem(item);
						}
						if (path == null) {
							path = new TreePath(new Object[] { parent });
						}
					}
					Object[] result = tpcp.getChildren(path);
					if (result != null) {
						assertElementsNotNull(parent, result);
						return result;
					}
				} else if (cp instanceof ITreeContentProvider) {
					ITreeContentProvider tcp = (ITreeContentProvider) cp;
					Object[] result = tcp.getChildren(parent);
					if (result != null) {
						assertElementsNotNull(parent, result);
						return result;
					}
				}
			}
			return new Object[0];
		} finally {
			setBusy(oldBusy);
		}
	}

	/**
	 * Asserts that the given array of elements is itself non- <code>null</code>
	 * and contains no <code>null</code> elements.
	 *
	 * @param parent
	 *            the parent element
	 * @param elements
	 *            the array to check
	 *
	 * @see #assertElementsNotNull(Object[])
	 */
	private void assertElementsNotNull(Object parent, Object[] elements) {
		Assert.isNotNull(elements);
		for (Object element : elements) {
			Assert.isNotNull(element);
		}

		if (InternalPolicy.DEBUG_LOG_EQUAL_VIEWER_ELEMENTS
				&& elements.length > 1) {
			CustomHashtable elementSet = newHashtable(elements.length * 2);
			for (Object element : elements) {
				Object old = elementSet.put(element, element);
				if (old != null) {
					String message = "Sibling elements in viewer must not be equal:\n  " //$NON-NLS-1$
							+ old + ",\n  " + element + ",\n  parent: " + parent; //$NON-NLS-1$ //$NON-NLS-2$
					Policy.getLog().log(
							new Status(IStatus.WARNING, Policy.JFACE, message,
									new RuntimeException()));
					return;
				}
			}
		}
	}

	/**
	 * Returns all selected items for the given SWT control.
	 *
	 * @param control
	 *            the control
	 * @return the list of selected items
	 */
	protected abstract Item[] getSelection(Control control);

	@SuppressWarnings("rawtypes")
	@Override
	protected List getSelectionFromWidget() {
		Widget[] items = getSelection(getControl());
		List<Object> list = new ArrayList<>(items.length);
		for (Widget item : items) {
			Object e = item.getData();
			if (e != null) {
				list.add(e);
			}
		}
		return list;
	}

	/*
	 * Overridden in AbstractTreeViewer to fix bug 108102 (code copied from
	 * StructuredViewer to avoid introducing new API)
	 */
	@Override
	protected void handleDoubleSelect(SelectionEvent event) {
		// expand ExpandableNode for default selection.
		if (event.item != null && event.item.getData() instanceof ExpandableNode node) {
			handleExpandableNodeClicked(event.item);
			// do not notify client listeners for this item.
			return;
		}

		// handle case where an earlier selection listener disposed the control.
		Control control = getControl();
		if (control != null && !control.isDisposed()) {
			// If the double-clicked element can be obtained from the event, use
			// it
			// otherwise get it from the control. Some controls like List do
			// not have the notion of item.
			// For details, see bug 90161 [Navigator] DefaultSelecting folders
			// shouldn't always expand first one
			ISelection selection;
			if (event.item != null && event.item.getData() != null) {

				// changes to fix bug 108102 follow
				TreePath treePath = getTreePathFromItem((Item) event.item);
				selection = new TreeSelection(treePath);
				// end of changes

			} else {
				selection = getSelection();
				updateSelection(selection);
			}
			fireDoubleClick(new DoubleClickEvent(this, selection));
		}
	}

	/**
	 * Handles a tree collapse event from the SWT widget.
	 *
	 * @param event
	 *            the SWT tree event
	 */
	protected void handleTreeCollapse(TreeEvent event) {
		if (event.item.getData() != null) {
			fireTreeCollapsed(new TreeExpansionEvent(this, event.item.getData()));
		}
	}

	/**
	 * Handles a tree expand event from the SWT widget.
	 *
	 * @param event
	 *            the SWT tree event
	 */
	protected void handleTreeExpand(TreeEvent event) {
		createChildren(event.item);
		if (event.item.getData() != null) {
			fireTreeExpanded(new TreeExpansionEvent(this, event.item.getData()));
		}
	}

	@Override
	protected void hookControl(Control control) {
		super.hookControl(control);
		addTreeListener(control, new TreeListener() {
			@Override
			public void treeExpanded(TreeEvent event) {
				handleTreeExpand(event);
			}

			@Override
			public void treeCollapsed(TreeEvent event) {
				handleTreeCollapse(event);
			}
		});
	}

	@Override
	protected void inputChanged(Object input, Object oldInput) {
		preservingSelection(() -> {
			Control tree = getControl();
			tree.setRedraw(false);
			try {
				removeAll(tree);
				tree.setData(getRoot());
				internalInitializeTree(tree);
			} finally {
				tree.setRedraw(true);
			}
		});
	}

	/**
	 * Initializes the tree with root items, expanding to the appropriate
	 * level if necessary.
	 *
	 * @param tree the tree control
	 * @since 3.3
	 */
	protected void internalInitializeTree(Control tree) {
		createChildren(tree);
		internalExpandToLevel(tree, expandToLevel);
	}

	/**
	 * Recursively collapses the subtree rooted at the given widget to the given
	 * level.
	 *
	 * @param widget the widget
	 * @param level  non-negative level, or <code>ALL_LEVELS</code> to collapse all
	 *               levels of the tree
	 */
	protected void internalCollapseToLevel(Widget widget, int level) {
		if (level == ALL_LEVELS || level > 0) {

			if (widget instanceof Item) {
				Item item = (Item) widget;
				setExpanded(item, false);
				Object element = item.getData();
				if (element != null && level == ALL_LEVELS) {
					if (optionallyPruneChildren(item, element)) {
						return;
					}
				}
			}

			if (level == ALL_LEVELS || level > 1) {
				Item[] children = getChildren(widget);
				if (children != null) {
					int nextLevel = (level == ALL_LEVELS ? ALL_LEVELS
							: level - 1);
					for (Item element : children) {
						internalCollapseToLevel(element, nextLevel);
					}
				}
			}
		}
	}

	/**
	 * Recursively collects all expanded items from the given widget.
	 *
	 * @param result
	 *            a list (element type: <code>Item</code>) into which to
	 *            collect the elements
	 * @param widget
	 *            the widget
	 */
	private void internalCollectExpandedItems(List<Item> result, Widget widget) {
		Item[] items = getChildren(widget);
		for (Item item : items) {
			// Disregard dummy nodes (see bug 287765)
			if (item.getData() != null) {
				if (getExpanded(item)) {
					result.add(item);
				}
				internalCollectExpandedItems(result, item);
			}
		}
	}

	/**
	 * Tries to create a path of tree items for the given element or tree path.
	 * This method recursively walks up towards the root of the tree and in the
	 * case of an element (rather than a tree path) assumes that
	 * <code>getParent</code> returns the correct parent of an element.
	 *
	 * @param elementOrPath
	 *            the element
	 * @param expand
	 *            <code>true</code> if all nodes on the path should be
	 *            expanded, and <code>false</code> otherwise
	 * @return Widget
	 */
	protected Widget internalExpand(Object elementOrPath, boolean expand) {

		if (elementOrPath == null) {
			return null;
		}

		Widget w = internalGetWidgetToSelect(elementOrPath);
		if (w == null) {
			if (equals(elementOrPath, getRoot())) { // stop at root
				return null;
			}
			// my parent has to create me
			Object parent = getParentElement(elementOrPath);
			if (parent != null) {
				Widget pw = internalExpand(parent, false);
				if (pw != null) {
					// let my parent create me
					createChildren(pw);
					Object element = internalToElement(elementOrPath);
					w = internalFindChild(pw, element);
				}
			}
		}
		if (expand && w instanceof Item) {
			// expand parent items top-down
			Item item = getParentItem((Item) w);
			LinkedList<Item> toExpandList = new LinkedList<>();
			while (item != null) {
				if (!getExpanded(item)) {
					toExpandList.addFirst(item);
				}
				item = getParentItem(item);
			}
			for (Item toExpand : toExpandList) {
				setExpanded(toExpand, true);
			}
		}
		return w;
	}

	/**
	 * If the argument is a tree path, returns its last segment, otherwise
	 * return the argument
	 *
	 * @param elementOrPath
	 *            an element or a tree path
	 * @return the element, or the last segment of the tree path
	 */
	private Object internalToElement(Object elementOrPath) {
		if (elementOrPath instanceof TreePath) {
			return ((TreePath) elementOrPath).getLastSegment();
		}
		return elementOrPath;
	}

	/**
	 * This method takes a tree path or an element. If the argument is not a tree
	 * path, returns the parent of the given element or <code>null</code> if the
	 * parent is not known. If the argument is a tree path with more than one
	 * segment, returns its parent tree path, otherwise returns <code>null</code>.
	 *
	 * @param elementOrTreePath the element or path to find parent for
	 * @return the parent element, or parent path, or <code>null</code>
	 *
	 * @since 3.2
	 */
	protected Object getParentElement(Object elementOrTreePath) {
		if (elementOrTreePath instanceof TreePath) {
			TreePath treePath = (TreePath) elementOrTreePath;
			return (treePath).getParentPath();
		}
		IContentProvider cp = getContentProvider();
		if (cp instanceof ITreePathContentProvider) {
			ITreePathContentProvider tpcp = (ITreePathContentProvider) cp;
			TreePath[] paths = tpcp.getParents(elementOrTreePath);
			if (paths.length > 0) {
				if (paths[0].getSegmentCount() == 0) {
					return getRoot();
				}
				return paths[0].getLastSegment();
			}
		}
		if (cp instanceof ITreeContentProvider) {
			ITreeContentProvider tcp = (ITreeContentProvider) cp;
			return tcp.getParent(elementOrTreePath);
		}
		return null;
	}

	/**
	 * Returns the widget to be selected for the given element or tree path.
	 *
	 * @param elementOrTreePath
	 *            the element or tree path to select
	 * @return the widget to be selected, or <code>null</code> if not found
	 *
	 * @since 3.1
	 */
	protected Widget internalGetWidgetToSelect(Object elementOrTreePath) {
		if (elementOrTreePath instanceof TreePath) {
			TreePath treePath = (TreePath) elementOrTreePath;
			if (treePath.getSegmentCount() == 0) {
				return getControl();
			}
			Widget[] candidates = findItems(treePath.getLastSegment());
			for (Widget candidate : candidates) {
				if (!(candidate instanceof Item)) {
					continue;
				}
				if (treePath.equals(getTreePathFromItem((Item) candidate), getComparer())) {
					return candidate;
				}
			}
			return null;
		}
		return findItem(elementOrTreePath);
	}

	/**
	 * Recursively expands the subtree rooted at the given widget to the given
	 * level.
	 * <p>
	 * Note that the default implementation of this method does not call
	 * <code>setRedraw</code>.
	 * </p>
	 *
	 * @param widget the widget
	 * @param level  non-negative level, or <code>ALL_LEVELS</code> to collapse all
	 *               levels of the tree
	 */
	protected void internalExpandToLevel(Widget widget, int level) {
		if (level == ALL_LEVELS || level > 0) {
			Object data = widget.getData();
			if (widget instanceof Item && data != null
					&& !isExpandable((Item) widget, null, data)) {
				return;
			}
			createChildren(widget, false);
			if (widget instanceof Item) {
				setExpanded((Item) widget, true);
			}
			if (level == ALL_LEVELS || level > 1) {
				Item[] children = getChildren(widget);
				if (children != null) {
					int newLevel = (level == ALL_LEVELS ? ALL_LEVELS
							: level - 1);
					for (Item element : children) {
						internalExpandToLevel(element, newLevel);
					}
				}
			}
		}
	}

	/**
	 * Non-recursively tries to find the given element as a child of the given
	 * parent (item or tree).
	 *
	 * @param parent
	 *            the parent item
	 * @param element
	 *            the element
	 * @return Widget
	 */
	private Widget internalFindChild(Widget parent, Object element) {
		Item[] items = getChildren(parent);
		for (Item item : items) {
			Object data = item.getData();
			if (data != null && equals(data, element)) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Recursively tries to find the given element.
	 *
	 * @param parent
	 *            the parent item
	 * @param element
	 *            the element
	 * @return Widget
	 */
	private Widget internalFindItem(Item parent, Object element) {

		// compare with node
		Object data = parent.getData();
		if (data != null) {
			if (equals(data, element)) {
				return parent;
			}
		}
		// recurse over children
		Item[] items = getChildren(parent);
		for (Item item : items) {
			Widget o = internalFindItem(item, element);
			if (o != null) {
				return o;
			}
		}
		return null;
	}

	@Override
	protected void internalRefresh(Object element) {
		internalRefresh(element, true);
	}

	@Override
	protected void internalRefresh(Object element, boolean updateLabels) {
		// If element is null, do a full refresh.
		if (element == null) {
			internalRefresh(getControl(), getRoot(), true, updateLabels);
			return;
		}
		Widget[] items = findItems(element);
		if (items.length != 0) {
			for (Widget item : items) {
				// pick up structure changes too
				internalRefresh(item, element, true, updateLabels);
			}
		}
	}

	/**
	 * Refreshes the tree starting at the given widget.
	 * <p>
	 * EXPERIMENTAL. Not to be used except by JDT. This method was added to
	 * support JDT's explorations into grouping by working sets. This method
	 * cannot be removed without breaking binary backwards compatibility, but
	 * should not be called by clients.
	 * </p>
	 *
	 * @param widget
	 *            the widget
	 * @param element
	 *            the element
	 * @param doStruct
	 *            <code>true</code> if structural changes are to be picked up,
	 *            and <code>false</code> if only label provider changes are of
	 *            interest
	 * @param updateLabels
	 *            <code>true</code> to update labels for existing elements,
	 *            <code>false</code> to only update labels as needed, assuming
	 *            that labels for existing elements are unchanged.
	 * @since 3.1
	 */
	protected void internalRefresh(Widget widget, Object element,
			boolean doStruct, boolean updateLabels) {

		if (widget instanceof Item) {
			if (doStruct) {
				updatePlus((Item) widget, element);
			}
			if (updateLabels || !equals(element, widget.getData())) {
				doUpdateItem(widget, element, true);
			} else {
				associate(element, (Item) widget);
			}
		}

		if (doStruct) {
			internalRefreshStruct(widget, element, updateLabels);
		} else {
			Item[] children = getChildren(widget);
			if (children != null) {
				for (Item item : children) {
					Object data = item.getData();
					if (data != null) {
						internalRefresh(item, data, doStruct, updateLabels);
					}
				}
			}
		}
	}

	/**
	 * Update the structure and recurse. Items are updated in updateChildren, as
	 * needed.
	 *
	 * @param widget
	 * @param element
	 * @param updateLabels
	 */
	/* package */void internalRefreshStruct(Widget widget, Object element,
			boolean updateLabels) {

		// updateChildren will ask getSortedChildren for items to be populated.
		// getSortedChildren always returns the limited items doesn't matter if there
		// were any items expanded. We need to fetch exactly same number of
		// elements which were shown in the viewer.
		Object[] updatedChildren = getChildrenWithLimitApplied(element, getChildren(widget));

		updateChildren(widget, element, updatedChildren, updateLabels);
		Item[] children = getChildren(widget);
		if (children != null) {
			for (Item item : children) {
				Object data = item.getData();
				if (data != null) {
					internalRefreshStruct(item, data, updateLabels);
				}
			}
		}
	}

	/**
	 * Removes the given elements from this viewer.
	 * <p>
	 * EXPERIMENTAL. Not to be used except by JDT. This method was added to
	 * support JDT's explorations into grouping by working sets. This method
	 * cannot be removed without breaking binary backwards compatibility, but
	 * should not be called by clients.
	 * </p>
	 *
	 * @param elementsOrPaths
	 *            the elements or element paths to remove
	 * @since 3.1
	 */
	protected void internalRemove(Object[] elementsOrPaths) {
		Object input = getInput();
		for (Object element : elementsOrPaths) {
			if (equals(element, input)) {
				setInput(null);
				return;
			}

			boolean continueOuter = false;
			if (getItemsLimit() > 0) {
				Widget[] itemsOfElement = internalFindItems(element);
				for (Widget item : itemsOfElement) {
					if (item instanceof TreeItem) {
						TreeItem parentItem = ((TreeItem) item).getParentItem();
						if (parentItem == null) {
							internalRefreshStruct(((TreeItem) item).getParent(), getInput(), false);
							continueOuter = true;
							break;
						}
						// refresh parent item with the latest model.
						internalRefreshStruct(parentItem, parentItem.getData(), false);
						continueOuter = true;
					}
				}
			}
			if (continueOuter) {
				continue;
			}
			Widget[] childItems = internalFindItems(element);
			if (childItems.length > 0) {
				for (Widget childItem : childItems) {
					if (childItem instanceof Item) {
						disassociate((Item) childItem);
						childItem.dispose();
					}
				}
			} else {
				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=210747
				Object parent = getParentElement(element);
				if (parent != null
						&& !equals(parent, getRoot())
						&& !(parent instanceof TreePath && ((TreePath) parent)
								.getSegmentCount() == 0)) {
					Widget[] parentItems = internalFindItems(parent);
					for (Widget parentItem : parentItems) {
						if (parentItem instanceof Item) {
							updatePlus((Item) parentItem, parent);
						}
					}
				}
			}
		}
	}

	/**
	 * Removes the given elements from this viewer, whenever those elements
	 * appear as children of the given parent.
	 *
	 * @param parent the parent element
	 * @param elements
	 *            the elements to remove
	 * @since 3.1
	 */
	protected void internalRemove(Object parent, Object[] elements) {

		CustomHashtable toRemove = new CustomHashtable(getComparer());
		for (Object element : elements) {
			toRemove.put(element, element);
		}

		// Find each place the parent appears in the tree
		Widget[] parentItemArray = findItems(parent);
		for (Widget parentItem : parentItemArray) {
			// May happen if parent element is a descendent of of a previously
			// removed element
			if (parentItem.isDisposed())
				continue;

			// Iterate over the child items and remove each one
			Item[] children = getChildren(parentItem);

			// there are elements to be shown and viewer is showing limited items.
			// newly added element can be inside expandable node or can be visible item.
			// Assumption that user has already updated the model and needs removal of
			// an item.
			if (getItemsLimit() > 0 && hasLimitedChildrenItems(parentItem)) {
				internalRefreshStruct(parentItem, parentItem.getData(), false);
				continue;
			}

			if (children.length == 1 && children[0].getData() == null &&
					parentItem instanceof Item) { // dummy node
				// Remove plus if parent element has no children
				updatePlus((Item) parentItem, parent);
			} else {
				for (Item child : children) {
					Object data = child.getData();
					if (data != null && toRemove.containsKey(data)) {
						disassociate(child);
						child.dispose();
					}
				}
			}
		}
	}

	/**
	 * Sets the expanded state of all items to correspond to the given set of
	 * expanded elements.
	 *
	 * @param expandedElements
	 *            the set (element type: <code>Object</code>) of elements
	 *            which are expanded
	 * @param widget
	 *            the widget
	 */
	private void internalSetExpanded(CustomHashtable expandedElements,
			Widget widget) {
		Item[] items = getChildren(widget);
		for (Item item : items) {
			Object data = item.getData();
			if (data != null) {
				// remove the element to avoid an infinite loop
				// if the same element appears on a child item
				boolean expanded = expandedElements.remove(data) != null;
				if (expanded != getExpanded(item)) {
					if (expanded) {
						createChildren(item);
					}
					setExpanded(item, expanded);
				}
			}
			if (expandedElements.size() > 0) {
				internalSetExpanded(expandedElements, item);
			}
		}
	}

	/**
	 * Sets the expanded state of all items to correspond to the given set of
	 * expanded tree paths.
	 *
	 * @param expandedTreePaths
	 *            the set (element type: <code>TreePath</code>) of elements
	 *            which are expanded
	 * @param widget
	 *            the widget
	 */
	private void internalSetExpandedTreePaths(
			CustomHashtable expandedTreePaths, Widget widget,
			TreePath currentPath) {
		Item[] items = getChildren(widget);
		for (Item item : items) {
			Object data = item.getData();
			TreePath childPath = data == null ? null : currentPath
					.createChildPath(data);
			if (data != null && childPath != null) {
				// remove the element to avoid an infinite loop
				// if the same element appears on a child item
				boolean expanded = expandedTreePaths.remove(childPath) != null;
				if (expanded != getExpanded(item)) {
					if (expanded) {
						createChildren(item);
					}
					setExpanded(item, expanded);
				}
			}
			internalSetExpandedTreePaths(expandedTreePaths, item, childPath);
		}
	}

	/**
	 * Return whether the tree node representing the given element or path can
	 * be expanded. Clients should query expandability by path if the viewer's
	 * content provider is an {@link ITreePathContentProvider}.
	 * <p>
	 * The default implementation of this framework method calls
	 * <code>hasChildren</code> on this viewer's content provider. It may be
	 * overridden if necessary.
	 * </p>
	 * @see #setExpandPreCheckFilters(boolean)
	 * @param elementOrTreePath
	 *            the element or path
	 * @return <code>true</code> if the tree node representing the given
	 *         element can be expanded, or <code>false</code> if not
	 */
	public boolean isExpandable(Object elementOrTreePath) {
		Object element;
		TreePath path;
		if (elementOrTreePath instanceof TreePath) {
			path = (TreePath) elementOrTreePath;
			element = path.getLastSegment();
		} else {
			element = elementOrTreePath;
			path = null;
		}
		IContentProvider cp = getContentProvider();
		if (cp instanceof ITreePathContentProvider) {
			ITreePathContentProvider tpcp = (ITreePathContentProvider) cp;
			if (path == null) {
				// A path was not provided so try and find one
				Widget w = findItem(element);
				if (w instanceof Item) {
					Item item = (Item) w;
					path = getTreePathFromItem(item);
				}
				if (path == null) {
					path = new TreePath(new Object[] { element });
				}
			}
			boolean hasChildren = tpcp.hasChildren(path);
			if (hasChildren && isExpandableCheckFilters && hasFilters()) {
				return getFilteredChildren(path).length > 0;
			}
			return hasChildren;
		}
		if (cp instanceof ITreeContentProvider) {
			ITreeContentProvider tcp = (ITreeContentProvider) cp;
			boolean hasChildren = tcp.hasChildren(element);
			if (hasChildren && isExpandableCheckFilters && hasFilters()) {
				return getFilteredChildren(element).length > 0;
			}
			return hasChildren;
		}
		return false;
	}

	/**
	 * Return whether the given element is expandable.
	 *
	 * @param item
	 *            the tree item for the element
	 * @param parentPath
	 *            the parent path if it is known or <code>null</code> if it
	 *            needs to be determines
	 * @param element
	 *            the element
	 * @return whether the given element is expandable
	 */
	private boolean isExpandable(Item item, TreePath parentPath, Object element) {
		Object elementOrTreePath = element;
		if (isTreePathContentProvider) {
			if (parentPath != null) {
				elementOrTreePath = parentPath.createChildPath(element);
			} else {
				elementOrTreePath = getTreePathFromItem(item);
			}
		}
		return isExpandable(elementOrTreePath);
	}

	@Override
	protected void labelProviderChanged() {
		// we have to walk the (visible) tree and update every item
		Control tree = getControl();
		tree.setRedraw(false);
		// don't pick up structure changes, but do force label updates
		internalRefresh(tree, getRoot(), false, true);
		tree.setRedraw(true);
	}

	/**
	 * Creates a new item.
	 *
	 * @param parent
	 *            the parent widget
	 * @param style
	 *            SWT style bits
	 * @param index
	 *            if non-negative, indicates the position to insert the item
	 *            into its parent
	 * @return the newly-created item
	 */
	protected abstract Item newItem(Widget parent, int style, int index);

	/**
	 * Removes the given elements from this viewer. The selection is updated if
	 * required.
	 * <p>
	 * This method should be called (by the content provider) when elements have
	 * been removed from the model, in order to cause the viewer to accurately
	 * reflect the model. This method only affects the viewer, not the model.
	 * </p>
	 *
	 * @param elementsOrTreePaths the elements to remove
	 */
	public void remove(final Object... elementsOrTreePaths) {
		assertElementsNotNull(elementsOrTreePaths);
		if (elementsOrTreePaths.length == 0) {
			return;
		}
		if (checkBusy())
			return;
		preservingSelection(() -> internalRemove(elementsOrTreePaths));
	}

	/**
	 * Removes the given elements from this viewer whenever they appear as
	 * children of the given parent element. If the given elements also appear
	 * as children of some other parent, the other parent will remain unchanged.
	 * The selection is updated if required.
	 * <p>
	 * This method should be called (by the content provider) when elements have
	 * been removed from the model, in order to cause the viewer to accurately
	 * reflect the model. This method only affects the viewer, not the model.
	 * </p>
	 *
	 * @param parent
	 *            the parent of the elements to remove
	 * @param elements
	 *            the elements to remove
	 *
	 * @since 3.2
	 */
	public void remove(final Object parent, final Object... elements) {
		assertElementsNotNull(elements);
		if (elements.length == 0) {
			return;
		}
		if (checkBusy())
			return;
		preservingSelection(() -> internalRemove(parent, elements));
	}

	/**
	 * Removes the given element from the viewer. The selection is updated if
	 * necessary.
	 * <p>
	 * This method should be called (by the content provider) when a single
	 * element has been removed from the model, in order to cause the viewer to
	 * accurately reflect the model. This method only affects the viewer, not
	 * the model. Note that there is another method for efficiently processing
	 * the simultaneous removal of multiple elements.
	 * </p>
	 *
	 * @param elementsOrTreePaths
	 *            the element
	 */
	public void remove(Object elementsOrTreePaths) {
		remove(new Object[] { elementsOrTreePaths });
	}

	/**
	 * Removes all items from the given control.
	 *
	 * @param control
	 *            the control
	 */
	protected abstract void removeAll(Control control);

	/**
	 * Removes a listener for expand and collapse events in this viewer. Has no
	 * effect if an identical listener is not registered.
	 *
	 * @param listener
	 *            a tree viewer listener
	 */
	public void removeTreeListener(ITreeViewerListener listener) {
		treeListeners.remove(listener);
	}

	/**
	 * This implementation of reveal() reveals the given element or tree path.
	 */
	@Override
	public void reveal(Object elementOrTreePath) {
		Assert.isNotNull(elementOrTreePath);
		Widget w = internalExpand(elementOrTreePath, true);
		if (w instanceof Item) {
			showItem((Item) w);
		}
	}

	/**
	 * Returns the rightmost visible descendent of the given item. Returns the
	 * item itself if it has no children.
	 *
	 * @param item
	 *            the item to compute the descendent of
	 * @return the rightmost visible descendent or the item itself if it has no
	 *         children
	 */
	private Item rightMostVisibleDescendent(Item item) {
		Item[] children = getItems(item);
		if (getExpanded(item) && children != null && children.length > 0) {
			return rightMostVisibleDescendent(children[children.length - 1]);
		}
		return item;
	}

	@Override
	public Item scrollDown(int x, int y) {
		Item current = getItem(x, y);
		if (current != null) {
			Item next = getNextItem(current, true);
			showItem(next == null ? current : next);
			return next;
		}
		return null;
	}

	@Override
	public Item scrollUp(int x, int y) {
		Item current = getItem(x, y);
		if (current != null) {
			Item previous = getPreviousItem(current);
			showItem(previous == null ? current : previous);
			return previous;
		}
		return null;
	}

	/**
	 * Sets the auto-expand level to be used when the input of the viewer is set
	 * using {@link #setInput(Object)}. The value 0 means that there is no
	 * auto-expand; 1 means that the invisible root element is expanded (since
	 * most concrete subclasses do not show the root element, there is usually
	 * no practical difference between using the values 0 and 1); 2 means that
	 * top-level elements are expanded, but not their children; 3 means that
	 * top-level elements are expanded, and their children, but not
	 * grandchildren; and so on.
	 * <p>
	 * The value <code>ALL_LEVELS</code> means that all subtrees should be
	 * expanded.
	 * </p>
	 * <p>
	 * Note that in previous releases, the Javadoc for this method had an off-by
	 * one error. See bug 177669 for details.
	 * </p>
	 *
	 * @param level
	 *            non-negative level, or <code>ALL_LEVELS</code> to expand all
	 *            levels of the tree
	 */
	public void setAutoExpandLevel(int level) {
		expandToLevel = level;
	}

	/**
	 * Sets the content provider used by this <code>AbstractTreeViewer</code>.
	 * <p>
	 * Content providers for abstract tree viewers must implement either
	 * {@link ITreeContentProvider} or {@link ITreePathContentProvider}.
	 */
	@Override
	public void setContentProvider(IContentProvider provider) {
		// the actual check is in assertContentProviderType
		super.setContentProvider(provider);
		isTreePathContentProvider = provider instanceof ITreePathContentProvider;
	}

	@Override
	protected void assertContentProviderType(IContentProvider provider) {
		Assert.isTrue(provider instanceof ITreeContentProvider
				|| provider instanceof ITreePathContentProvider,
				"Instances of AbstractTreeViewer must have a content provider " //$NON-NLS-1$
						+ "of type ITreeContentProvider or ITreePathContentProvider"); //$NON-NLS-1$
	}

	/**
	 * Sets the expand state of the given item.
	 *
	 * @param item
	 *            the item
	 * @param expand
	 *            the expand state of the item
	 */
	protected abstract void setExpanded(Item item, boolean expand);

	/**
	 * Sets which nodes are expanded in this viewer's tree. The given list
	 * contains the elements that are to be expanded; all other nodes are to be
	 * collapsed.
	 * <p>
	 * This method is typically used when restoring the interesting state of a
	 * viewer captured by an earlier call to <code>getExpandedElements</code>.
	 * </p>
	 *
	 * @param elements
	 *            the array of expanded elements
	 * @see #getExpandedElements
	 */
	public void setExpandedElements(Object... elements) {
		assertElementsNotNull(elements);
		if (checkBusy()) {
			return;
		}
		CustomHashtable expandedElements = newHashtable(elements.length * 2 + 1);
		for (Object element : elements) {
			// Ensure item exists for element. This will materialize items for
			// each element and their parents, if possible. This is important
			// to support expanding of inner tree nodes without necessarily
			// expanding their parents.
			internalExpand(element, false);
			expandedElements.put(element, element);
		}
		// this will traverse all existing items, and create children for
		// elements that need to be expanded. If the tree contains multiple
		// equal elements, and those are in the set of elements to be expanded,
		// only the first item found for each element will be expanded.
		internalSetExpanded(expandedElements, getControl());
	}

	/**
	 * Sets which nodes are expanded in this viewer's tree. The given list
	 * contains the tree paths that are to be expanded; all other nodes are to
	 * be collapsed.
	 * <p>
	 * This method is typically used when restoring the interesting state of a
	 * viewer captured by an earlier call to <code>getExpandedTreePaths</code>.
	 * </p>
	 *
	 * @param treePaths
	 *            the array of expanded tree paths
	 * @see #getExpandedTreePaths()
	 *
	 * @since 3.2
	 */
	public void setExpandedTreePaths(TreePath... treePaths) {
		assertElementsNotNull((Object[]) treePaths);
		if (checkBusy())
			return;
		final IElementComparer comparer = getComparer();
		IElementComparer treePathComparer = new IElementComparer() {

			@Override
			public boolean equals(Object a, Object b) {
				return ((TreePath) a).equals(((TreePath) b), comparer);
			}


			@Override
			public int hashCode(Object element) {
				return ((TreePath) element).hashCode(comparer);
			}
		};
		CustomHashtable expandedTreePaths = new CustomHashtable(
				treePaths.length * 2 + 1, treePathComparer);
		for (TreePath treePath : treePaths) {
			// Ensure item exists for element. This will materialize items for
			// each element and their parents, if possible. This is important
			// to support expanding of inner tree nodes without necessarily
			// expanding their parents.
			internalExpand(treePath, false);
			expandedTreePaths.put(treePath, treePath);
		}
		// this will traverse all existing items, and create children for
		// elements that need to be expanded. If the tree contains multiple
		// equal elements, and those are in the set of elements to be expanded,
		// only the first item found for each element will be expanded.
		internalSetExpandedTreePaths(expandedTreePaths, getControl(),
				new TreePath(new Object[0]));
	}

	/**
	 * Sets whether the node corresponding to the given element or tree path is
	 * expanded or collapsed.
	 *
	 * @param elementOrTreePath
	 *            the element
	 * @param expanded
	 *            <code>true</code> if the node is expanded, and
	 *            <code>false</code> if collapsed
	 */
	public void setExpandedState(Object elementOrTreePath, boolean expanded) {
		Assert.isNotNull(elementOrTreePath);
		if (checkBusy())
			return;
		Widget item = internalExpand(elementOrTreePath, false);
		if (item instanceof Item) {
			if (expanded) {
				createChildren(item);
			}
			setExpanded((Item) item, expanded);
		}
	}

	/**
	 * Sets the selection to the given list of items.
	 *
	 * @param items
	 *            list of items (element type:
	 *            <code>org.eclipse.swt.widgets.Item</code>)
	 */
	protected abstract void setSelection(List<Item> items);

	/**
	 * This implementation of setSelectionToWidget accepts a list of elements or
	 * a list of tree paths.
	 */
	@Override
	protected void setSelectionToWidget(@SuppressWarnings("rawtypes") List v, boolean reveal) {
		if (v == null) {
			setSelection(new ArrayList<>(0));
			return;
		}
		int size = v.size();
		List<Item> newSelection = new ArrayList<>(size);
		for (int i = 0; i < size; ++i) {
			Object elementOrTreePath = v.get(i);
			// Use internalExpand since item may not yet be created. See
			// 1G6B1AR.
			Widget w = internalExpand(elementOrTreePath, false);
			if (w instanceof Item) {
				newSelection.add((Item) w);
			} else if (w == null && elementOrTreePath instanceof TreePath) {
				TreePath treePath = (TreePath) elementOrTreePath;
				Object element = treePath.getLastSegment();
				if (element != null) {
					w = internalExpand(element, false);
					if (w instanceof Item) {
						newSelection.add((Item) w);
					}
				}
			}
		}

		// there can be some items inside expandable node and not populated yet. In this
		// case try to find the item to select inside all the visible expandable nodes.
		if (newSelection.size() < v.size() && getItemsLimit() > 0) {
			// make out still not found items
			List<Object> notFound = new ArrayList<>();
			for (Object toSelect : v) {
				boolean bFound = false;
				for (Item found : newSelection) {
					if (equals(toSelect, found.getData())) {
						bFound = true;
						break;
					}
				}
				if (!bFound) {
					notFound.add(toSelect);
				}
			}

			// find out all visible expandable nodes
			Collection<ExpandableNode> expandItems = getExpandableNodes();

			// search for still missing items inside expandable nodes
			for (Object nFound : notFound) {
				for (ExpandableNode expNode : expandItems) {
					if (findElementInExpandableNode(expNode, nFound)) {
						Widget w = findItem(expNode);
						if (w instanceof Item item) {
							newSelection.add(item);
						}
					}
				}
			}
		}

		setSelection(newSelection);

		// Although setting the selection in the control should reveal it,
		// setSelection may be a no-op if the selection is unchanged,
		// so explicitly reveal items in the selection here.
		// See bug 100565 for more details.
		if (reveal && newSelection.size() > 0) {
			// Iterate backwards so the first item in the list
			// is the one guaranteed to be visible
			for (int i = (newSelection.size()-1); i >= 0; i--) {
				showItem(newSelection.get(i));
			}
		}
	}

	private boolean findElementInExpandableNode(ExpandableNode expNode, Object toFind) {
		Object[] remEles = getFilteredChildren(expNode);
		for (Object element : remEles) {
			if (equals(element, toFind)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Shows the given item.
	 *
	 * @param item
	 *            the item
	 */
	protected abstract void showItem(Item item);

	/**
	 * Updates the tree items to correspond to the child elements of the given
	 * parent element. If null is passed for the children, this method obtains
	 * them (only if needed).
	 *
	 * @param widget
	 *            the widget
	 * @param parent
	 *            the parent element
	 * @param elementChildren
	 *            the child elements, or null
	 * @deprecated this is no longer called by the framework
	 */
	@Deprecated
	protected void updateChildren(Widget widget, Object parent,
			Object[] elementChildren) {
		updateChildren(widget, parent, elementChildren, true);
	}

	/**
	 * Updates the tree items to correspond to the child elements of the given
	 * parent element. If null is passed for the children, this method obtains
	 * them (only if needed).
	 *
	 * @param widget
	 *            the widget
	 * @param parent
	 *            the parent element
	 * @param elementChildren
	 *            the child elements, or null
	 * @param updateLabels
	 *            <code>true</code> to update labels for existing elements,
	 *            <code>false</code> to only update labels as needed, assuming
	 *            that labels for existing elements are unchanged.
	 * @since 2.1
	 */
	private void updateChildren(Widget widget, Object parent,
			Object[] elementChildren, boolean updateLabels) {
		// optimization! prune collapsed subtrees
		if (widget instanceof Item) {
			Item ti = (Item) widget;
			if (!getExpanded(ti)) {
				if (optionallyPruneChildren(ti, parent)) {
					// children were pruned, nothing left to do
					return;
				}
				// The following code is being executed if children were not pruned.
				// This is (as of 3.5) only the case for CheckboxTreeViewer.
				Item[] its = getItems(ti);
				if (isExpandable(ti, null, parent)) {
					if (its.length == 0) {
						// need dummy node
						newItem(ti, SWT.NULL, -1);
						return;
					} else if (its.length == 1 && its[0].getData() == null) {
						// dummy node exists, nothing left to do
						return;
					}
					// else fall through to normal update code below
				} else {
					for (Item it : its) {
						if (it.getData() != null) {
							disassociate(it);
						}
						it.dispose();
					}
					// nothing left to do
					return;
				}
			}
		}

		// If the children weren't passed in, get them now since they're needed
		// below.
		if (elementChildren == null) {
			if (isTreePathContentProvider && widget instanceof Item) {
				TreePath path = getTreePathFromItem((Item) widget);
				elementChildren = getSortedChildren(path);
			} else {
				elementChildren = getSortedChildren(parent);
			}
		}

		Control tree = getControl();

		// WORKAROUND
		int oldCnt = -1;
		if (widget == tree) {
			oldCnt = getItemCount(tree);
		}

		Item[] items = getChildren(widget);

		// save the expanded elements
		CustomHashtable expanded = newHashtable(CustomHashtable.DEFAULT_CAPACITY); // assume
																					// num
																					// expanded
																					// is
																					// small
		for (Item item : items) {
			if (getExpanded(item)) {
				Object element = item.getData();
				if (element != null) {
					expanded.put(element, element);
				}
			}
		}

		int min = Math.min(elementChildren.length, items.length);

		// dispose of surplus items, optimizing for the case where elements have
		// been deleted but not reordered, or all elements have been removed.
		int numItemsToDispose = items.length - min;
		if (numItemsToDispose > 0) {
			CustomHashtable children = newHashtable(elementChildren.length * 2);
			for (Object elementChild : elementChildren) {
				children.put(elementChild, elementChild);
			}
			int i = 0;
			while (numItemsToDispose > 0 && i < items.length) {
				Object data = items[i].getData();
				if (data == null || !children.containsKey(data)) {
					if (data != null) {
						disassociate(items[i]);
					}
					items[i].dispose();
					if (i + 1 < items.length) {
						// The components at positions i+1 through
						// items.length-1 in the source array are copied into
						// positions i through items.length-2
						System.arraycopy(items, i + 1, items, i, items.length - (i+1));
					}
					numItemsToDispose--;
				} else {
					i++;
				}
			}
		}

		// compare first min items, and update item if necessary
		// need to do it in two passes:
		// 1: disassociate old items
		// 2: associate new items
		// because otherwise a later disassociate can remove a mapping made for
		// a previous associate,
		// making the map inconsistent
		for (int i = 0; i < min; ++i) {
			Item item = items[i];
			Object oldElement = item.getData();
			if (oldElement != null) {
				Object newElement = elementChildren[i];
				if (newElement != oldElement) {
					if (equals(newElement, oldElement)) {
						// update the data to be the new element, since
						// although the elements
						// may be equal, they may still have different labels
						// or children
						Object data = item.getData();
						if (data != null) {
							unmapElement(data, item);
						}
						item.setData(newElement);
						mapElement(newElement, item);
					} else {
						disassociate(item);
						// Clear the text and image to force a label update
						item.setImage(null);
						item.setText("");//$NON-NLS-1$

					}
				}
			}
		}

		for (int i = 0; i < min; ++i) {
			Item item = items[i];
			Object newElement = elementChildren[i];
			if (item.getData() == null) {
				// old and new elements are not equal
				associate(newElement, item);
				updatePlus(item, newElement);
				updateItem(item, newElement);
			} else {
				// old and new elements are equal
				updatePlus(item, newElement);
				if (updateLabels) {
					updateItem(item, newElement);
				} else {
					associate(newElement, item);
				}
			}
		}

		// Restore expanded state for items that changed position.
		// Make sure setExpanded is called after updatePlus, since
		// setExpanded(false) fails if item has no children.
		// Need to call setExpanded for both expanded and unexpanded
		// cases since the expanded state can change either way.
		// This needs to be done in a second loop, see bug 148025.
		for (int i = 0; i < min; ++i) {
			Item item = items[i];
			Object newElement = elementChildren[i];
			setExpanded(item, expanded.containsKey(newElement));
		}

		// add any remaining elements
		if (min < elementChildren.length) {
			for (int i = min; i < elementChildren.length; ++i) {
				createTreeItem(widget, elementChildren[i], -1);
			}

			// Need to restore expanded state in a separate pass
			// because createTreeItem does not return the new item.
			// Avoid doing this unless needed.
			if (expanded.size() > 0) {
				// get the items again, to include the new items
				items = getChildren(widget);
				for (int i = min; i < elementChildren.length; ++i) {
					// Restore expanded state for items that changed position.
					// Make sure setExpanded is called after updatePlus (called
					// in createTreeItem), since
					// setExpanded(false) fails if item has no children.
					// Only need to call setExpanded if element was expanded
					// since new items are initially unexpanded.
					if (expanded.containsKey(elementChildren[i])) {
						setExpanded(items[i], true);
					}
				}
			}
		}

		// WORKAROUND
		if (widget == tree && oldCnt == 0 && getItemCount(tree) != 0) {
			// System.out.println("WORKAROUND setRedraw");
			tree.setRedraw(false);
			tree.setRedraw(true);
		}
	}

	/** Returns true if children were pruned */
	/*package*/ boolean optionallyPruneChildren(Item item, Object element) {
		// need a dummy node if element is expandable;
		// but try to avoid recreating the dummy node
		boolean needDummy = isExpandable(item, null, element);
		boolean haveDummy = false;
		// remove all children
		Item[] items = getItems(item);
		for (Item child : items) {
			if (child.getData() != null) {
				disassociate(child);
				child.dispose();
			} else if (needDummy && !haveDummy) {
				haveDummy = true;
			} else {
				child.dispose();
			}
		}
		if (needDummy && !haveDummy) {
			newItem(item, SWT.NULL, -1);
		}
		return true;
	}

	/**
	 * Not to be called by clients. Return the items to be refreshed as part of
	 * an update. elementChildren are the new elements.
	 *
	 * @param widget widget to get children for
	 * @param elementChildren unused
	 * @since 3.4
	 * @return Item[]
	 *
	 * @deprecated This method was inadvertently released as API but is not
	 *             intended to be called by clients.
	 */
	@Deprecated
	public Item[] getChildren(Widget widget,  Object[] elementChildren) {
		return getChildren(widget);
	}

	/**
	 * Updates the "+"/"-" icon of the tree node from the given element. It
	 * calls <code>isExpandable</code> to determine whether an element is
	 * expandable.
	 *
	 * @param item
	 *            the item
	 * @param element
	 *            the element
	 */
	protected void updatePlus(Item item, Object element) {
		boolean hasPlus = getItemCount(item) > 0;
		boolean needsPlus = isExpandable(item, null, element);
		boolean removeAll = false;
		boolean addDummy = false;
		Object data = item.getData();
		if (data != null && equals(element, data)) {
			// item shows same element
			if (hasPlus != needsPlus) {
				if (needsPlus) {
					addDummy = true;
				} else {
					removeAll = true;
				}
			}
		} else {
			// item shows different element
			removeAll = true;
			addDummy = needsPlus;

			// we cannot maintain expand state so collapse it
			setExpanded(item, false);
		}
		if (removeAll) {
			// remove all children
			Item[] items = getItems(item);
			for (Item item2 : items) {
				if (item2.getData() != null) {
					disassociate(item2);
				}
				item2.dispose();
			}
		}
		if (addDummy) {
			newItem(item, SWT.NULL, -1); // append a dummy
		}
	}

	/**
	 * Gets the expanded elements that are visible to the user. An expanded
	 * element is only visible if the parent is expanded.
	 *
	 * @return the visible expanded elements
	 * @since 2.0
	 */
	public Object[] getVisibleExpandedElements() {
		ArrayList<Object> v = new ArrayList<>();
		internalCollectVisibleExpanded(v, getControl());
		return v.toArray();
	}

	private void internalCollectVisibleExpanded(ArrayList<Object> result, Widget widget) {
		Item[] items = getChildren(widget);
		for (Item item : items) {
			if (getExpanded(item)) {
				Object data = item.getData();
				if (data != null) {
					result.add(data);
				}
				// Only recurse if it is expanded - if
				// not then the children aren't visible
				internalCollectVisibleExpanded(result, item);
			}
		}
	}

	/**
	 * Returns the tree path for the given item.
	 *
	 * @param item item to get path for
	 * @return {@link TreePath}
	 *
	 * @since 3.2
	 */
	protected TreePath getTreePathFromItem(Item item) {
		LinkedList<Object> segments = new LinkedList<>();
		while (item != null) {
			Object segment = item.getData();
			Assert.isNotNull(segment);
			segments.addFirst(segment);
			item = getParentItem(item);
		}
		return new TreePath(segments.toArray());
	}

	/**
	 * The <code>AbstractTreeViewer</code> implementation of this method returns
	 * the result as an <code>ITreeSelection</code>.
	 * <p>
	 * Call {@link #getStructuredSelection()} instead to get an instance of
	 * <code>ITreeSelection</code> directly.
	 * </p>
	 * Subclasses do not typically override this method, but implement
	 * <code>getSelectionFromWidget(List)</code> instead. If they override this
	 * method, they should return an <code>ITreeSelection</code> as well.
	 *
	 * @since 3.2
	 */
	@Override
	public ISelection getSelection() {
		Control control = getControl();
		if (control == null || control.isDisposed()) {
			return TreeSelection.EMPTY;
		}
		Widget[] items = getSelection(getControl());
		ArrayList<TreePath> list = new ArrayList<>(items.length);
		for (Widget item : items) {
			if (item.getData() != null) {
				list.add(getTreePathFromItem((Item) item));
			}
		}
		return new TreeSelection(list.toArray(new TreePath[list.size()]), getComparer());
	}

	/**
	 * Returns the <code>ITreeSelection</code> of this viewer.
	 * <p>
	 * Subclasses whose {@link #getSelection()} specifies to return a more
	 * specific type should also override this method and return that type.
	 * </p>
	 *
	 * @return ITreeSelection
	 * @throws ClassCastException
	 *             if the selection of the viewer is not an instance of
	 *             ITreeSelection
	 * @since 3.11
	 */
	@Override
	public ITreeSelection getStructuredSelection() throws ClassCastException {
		ISelection selection = getSelection();
		if (selection instanceof ITreeSelection) {
			return (ITreeSelection) selection;
		}
		throw new ClassCastException(
				getClass().getName() + " should return an instance of ITreeSelection from its getSelection() method."); //$NON-NLS-1$
	}

	@Override
	protected void setSelectionToWidget(ISelection selection, boolean reveal) {
		if (selection instanceof ITreeSelection) {
			ITreeSelection treeSelection = (ITreeSelection) selection;
			setSelectionToWidget(Arrays.asList(treeSelection.getPaths()), reveal);
		} else {
			super.setSelectionToWidget(selection, reveal);
		}
	}

	/**
	 * Returns a list of tree paths corresponding to expanded nodes in this
	 * viewer's tree, including currently hidden ones that are marked as
	 * expanded but are under a collapsed ancestor.
	 * <p>
	 * This method is typically used when preserving the interesting state of a
	 * viewer; <code>setExpandedElements</code> is used during the restore.
	 * </p>
	 *
	 * @return the array of expanded tree paths
	 * @see #setExpandedElements
	 *
	 * @since 3.2
	 */
	public TreePath[] getExpandedTreePaths() {
		ArrayList<Item> items = new ArrayList<>();
		internalCollectExpandedItems(items, getControl());
		ArrayList<TreePath> result = new ArrayList<>(items.size());
		for (Item item : items) {
			TreePath treePath = getTreePathFromItem(item);
			if (treePath != null) {
				result.add(treePath);
			}
		}
		return result.toArray(new TreePath[items.size()]);
	}

	/**
	 * Inserts the given element as a new child element of the given parent
	 * element at the given position. If this viewer has a sorter, the position
	 * is ignored and the element is inserted at the correct position in the
	 * sort order.
	 * <p>
	 * This method should be called (by the content provider) when elements have
	 * been added to the model, in order to cause the viewer to accurately
	 * reflect the model. This method only affects the viewer, not the model.
	 * </p>
	 *
	 * @param parentElementOrTreePath
	 *            the parent element, or the tree path to the parent
	 * @param element
	 *            the element
	 * @param position
	 *            a 0-based position relative to the model, or -1 to indicate
	 *            the last position
	 *
	 * @since 3.2
	 */
	public void insert(Object parentElementOrTreePath, Object element,
			int position) {
		Assert.isNotNull(parentElementOrTreePath);
		Assert.isNotNull(element);
		if (checkBusy())
			return;
		if (getComparator() != null || hasFilters()) {
			add(parentElementOrTreePath, new Object[] { element });
			return;
		}
		Widget[] items;
		if (internalIsInputOrEmptyPath(parentElementOrTreePath)) {
			items = new Widget[] { getControl() };
		} else {
			items = internalFindItems(parentElementOrTreePath);
		}

		for (Widget widget : items) {
			if (widget instanceof Item) {
				Item item = (Item) widget;

				Item[] childItems = getChildren(item);
				if (getExpanded(item)
						|| (childItems.length > 0 && childItems[0].getData() != null)) {
					// item has real children, go ahead and add
					int insertionPosition = position;
					if (insertionPosition == -1) {
						insertionPosition = getItemCount(item);
					}

					createTreeItem(item, element, insertionPosition);
				} else {
					Object parentElement = parentElementOrTreePath;
					if (element instanceof TreePath)
						parentElement = ((TreePath) parentElement).getLastSegment();
					updatePlus(item, parentElement);
				}
			} else {
				int insertionPosition = position;
				if (insertionPosition == -1) {
					insertionPosition = getItemCount((Control) widget);
				}

				createTreeItem(widget, element, insertionPosition);
			}
		}
	}

	@Override
	protected Widget getColumnViewerOwner(int columnIndex) {
		// Return null by default
		return null;
	}

	/**
	 * This implementation of {@link #getItemAt(Point)} returns null to ensure
	 * API backwards compatibility. Subclasses should override.
	 *
	 * @since 3.3
	 */
	@Override
	protected Item getItemAt(Point point) {
		return null;
	}

	/**
	 * This implementation of {@link #createViewerEditor()} returns null to ensure
	 * API backwards compatibility. Subclasses should override.
	 *
	 * @since 3.3
	 */
	@Override
	protected ColumnViewerEditor createViewerEditor() {
		return null;
	}

	/**
	 * Returns the number of columns of this viewer.
	 * <p><b>Subclasses should overwrite this method, which has a default
	 * implementation (returning 0) for API backwards compatility reasons</b></p>
	 *
	 * @return the number of columns
	 *
	 * @since 3.3
	 */
	@Override
	protected int doGetColumnCount() {
		return 0;
	}


	/**
	 * This implementation of buildLabel handles tree paths as well as elements.
	 *
	 * @param updateLabel
	 *            the ViewerLabel to collect the result in
	 * @param elementOrPath
	 *            the element or tree path for which a label should be built
	 *
	 * @see org.eclipse.jface.viewers.StructuredViewer#buildLabel(org.eclipse.jface.viewers.ViewerLabel,
	 *      java.lang.Object)
	 */
	@Override
	protected void buildLabel(ViewerLabel updateLabel, Object elementOrPath) {
		Object element;
		if (elementOrPath instanceof TreePath) {
			TreePath path = (TreePath) elementOrPath;
			IBaseLabelProvider provider = getLabelProvider();
			if (provider instanceof ITreePathLabelProvider) {
				ITreePathLabelProvider pprov = (ITreePathLabelProvider) provider;
				buildLabel(updateLabel, path, pprov);
				return;
			}
			element = path.getLastSegment();
		} else {
			element = elementOrPath;
		}
		super.buildLabel(updateLabel, element);
	}

	/**
	 * Returns true if the given object is either the input or an empty tree path.
	 *
	 * @param elementOrTreePath an element which could either be the viewer's input, or a tree path
	 *
	 * @return <code>true</code> if the given object is either the input or an empty tree path,
	 * <code>false</code> otherwise.
	 * @since 3.3
	 */
	final protected boolean internalIsInputOrEmptyPath(final Object elementOrTreePath) {
		if (elementOrTreePath.equals(getRoot()))
			return true;
		if (!(elementOrTreePath instanceof TreePath))
			return false;
		return ((TreePath) elementOrTreePath).getSegmentCount() == 0;
	}

	/*
	 * Subclasses should implement
	 */
	@Override
	protected ViewerRow getViewerRowFromItem(Widget item) {
		return null;
	}

	/**
	 * Instructs {@link #isExpandable(Object)} to consult filters to more accurately
	 * determine if an item can be expanded.
	 * <p>
	 * Setting this value to <code>true</code> will affect performance of the tree
	 * viewer.
	 * </p><p>
	 * To improve performance, by default the tree viewer does not consult filters when
	 * determining if a tree node could be expanded.
	 * </p>
	 * @param checkFilters <code>true</code> to instruct tree viewer to consult filters
	 * @see #isExpandable(Object)
	 * @since 3.8
	 */
	public void setExpandPreCheckFilters(boolean checkFilters) {
		if (checkFilters != isExpandableCheckFilters) {
			this.isExpandableCheckFilters = checkFilters;
			refresh();
		}
	}

	/**
	 * @param widget
	 * @return if the given widget's children has an expandable node at the end.
	 */
	boolean hasLimitedChildrenItems(Widget widget) {
		Item[] items = getChildren(widget);
		if (items.length == 0) {
			return false;
		}
		return items[items.length - 1].getData() instanceof ExpandableNode;
	}

	/**
	 * Returns true if the element is present in the viewer. If the viewer has
	 * incremental display set then the element is searched inside expandable node
	 * also. i.e. it searches inside the remaining elements to be populated.
	 *
	 * @param parent  model element which corresponds to any visible widget on the
	 *                viewer
	 * @param element model element
	 * @return if given model element is contained in the viewer
	 * @since 3.31
	 */
	public boolean contains(Object parent, Object element) {
		if (findItem(element) != null) {
			return true;
		}

		if (getItemsLimit() <= 0) {
			return false;
		}

		Widget parentWideget = findItem(parent);
		if (parentWideget == null) {
			return false;
		}

		Item[] items = getChildren(parentWideget);
		if (items.length == 0) {
			return false;
		}
		if (items[items.length - 1].getData() instanceof ExpandableNode node) {
			return node.contains(element);
		}
		return false;
	}

}
