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
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation bug 154329
 *                                               - fixes in bug 170381, 198665, 200731
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548314
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.internal.ExpandableNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

/**
 * This is a widget independent class implementors of
 * {@link org.eclipse.swt.widgets.Table} like widgets can use to provide a
 * viewer on top of their widget implementations.
 * <p>
 * <strong> This class is not intended to be subclassed outside of the JFace
 * viewers framework.</strong>
 * </p>
 *
 * @since 3.3
 */
public abstract class AbstractTableViewer extends ColumnViewer {

	private class VirtualManager {

		/**
		 * The currently invisible elements as provided by the content provider
		 * or by addition. This will not be populated by an
		 * ILazyStructuredContentProvider as an ILazyStructuredContentProvider
		 * is only queried on the virtual callback.
		 */
		private Object[] cachedElements = new Object[0];

		/**
		 * Create a new instance of the receiver.
		 *
		 */
		public VirtualManager() {
			addTableListener();
		}

		/**
		 * Add the listener for SetData on the table
		 */
		private void addTableListener() {
			getControl().addListener(SWT.SetData, event -> {
				Item item = (Item) event.item;
				final int index = doIndexOf(item);

				if (index == -1) {
					// Should not happen, but the spec for doIndexOf allows returning -1.
					// See bug 241117.
					return;
				}

				Object element = resolveElement(index);
				if (element == null) {
					// Didn't find it so make a request
					// Keep looking if it is not in the cache.
					IContentProvider contentProvider = getContentProvider();
					// If we are building lazily then request lookup now
					if (contentProvider instanceof ILazyContentProvider) {
						ILazyContentProvider lazyProvider = (ILazyContentProvider) contentProvider;
						if (!isBusy()) {
							lazyProvider.updateElement(index);
						} else {
							// In case event is sent during doUpdateItem() we
							// should run async update to avoid RuntimeException
							// from ColumnViewer.checkBusy(), see bug 488484
							Control control = getControl();
							control.getDisplay().asyncExec(() -> {
								if (!control.isDisposed()) {
									lazyProvider.updateElement(index);
								}
							});
						}
						return;
					}
				}

				associate(element, item);
				updateItem(item, element);
			});
		}

		/**
		 * Get the element at index.Resolve it lazily if this is available.
		 *
		 * @param index
		 * @return Object or <code>null</code> if it could not be found
		 */
		protected Object resolveElement(int index) {

			Object element = null;
			if (index < cachedElements.length) {
				element = cachedElements[index];
			}

			return element;
		}

		/**
		 * A non visible item has been added.
		 *
		 * @param element
		 * @param index
		 */
		public void notVisibleAdded(Object element, int index) {

			int requiredCount = doGetItemCount() + 1;

			Object[] newCache = new Object[requiredCount];
			System.arraycopy(cachedElements, 0, newCache, 0, index);
			if (index < cachedElements.length) {
				System.arraycopy(cachedElements, index, newCache, index + 1,
						cachedElements.length - index);
			}
			newCache[index] = element;
			cachedElements = newCache;

			doSetItemCount(requiredCount);
		}

		/**
		 * The elements with the given indices need to be removed from the
		 * cache.
		 *
		 * @param indices
		 */
		public void removeIndices(int[] indices) {
			if (indices.length == 1) {
				removeIndicesFromTo(indices[0], indices[0]);
			}
			int requiredCount = doGetItemCount() - indices.length;

			Arrays.sort(indices);
			Object[] newCache = new Object[requiredCount];
			int indexInNewCache = 0;
			int nextToSkip = 0;
			for (int i = 0; i < cachedElements.length; i++) {
				if (nextToSkip < indices.length && i == indices[nextToSkip]) {
					nextToSkip++;
				} else {
					newCache[indexInNewCache++] = cachedElements[i];
				}
			}
			cachedElements = newCache;
		}

		/**
		 * The elements between the given indices (inclusive) need to be removed
		 * from the cache.
		 *
		 * @param from
		 * @param to
		 */
		public void removeIndicesFromTo(int from, int to) {
			int indexAfterTo = to + 1;
			Object[] newCache = new Object[cachedElements.length
					- (indexAfterTo - from)];
			System.arraycopy(cachedElements, 0, newCache, 0, from);
			if (indexAfterTo < cachedElements.length) {
				System.arraycopy(cachedElements, indexAfterTo, newCache, from,
						cachedElements.length - indexAfterTo);
			}
		}

		/**
		 * @param element
		 * @return the index of the element in the cache, or null
		 */
		public int find(Object element) {
			return Arrays.asList(cachedElements).indexOf(element);
		}

		/**
		 * @param count
		 */
		public void adjustCacheSize(int count) {
			if (count == cachedElements.length) {
				return;
			} else if (count < cachedElements.length) {
				Object[] newCache = new Object[count];
				System.arraycopy(cachedElements, 0, newCache, 0, count);
				cachedElements = newCache;
			} else {
				Object[] newCache = new Object[count];
				System.arraycopy(cachedElements, 0, newCache, 0,
						cachedElements.length);
				cachedElements = newCache;
			}
		}

	}

	private VirtualManager virtualManager;

	/**
	 * Create the new viewer for table like widgets
	 */
	public AbstractTableViewer() {
		super();
	}

	@Override
	protected void hookControl(Control control) {
		super.hookControl(control);
		initializeVirtualManager(getControl().getStyle());
	}

	@Override
	protected void handleDispose(DisposeEvent event) {
		super.handleDispose(event);
		virtualManager = null;
	}

	/**
	 * Initialize the virtual manager to manage the virtual state if the table
	 * is VIRTUAL. If not use the default no-op version.
	 *
	 * @param style
	 */
	private void initializeVirtualManager(int style) {
		if ((style & SWT.VIRTUAL) == 0) {
			return;
		}

		virtualManager = new VirtualManager();
	}

	/**
	 * Adds the given elements to this table viewer. If this viewer does not
	 * have a sorter, the elements are added at the end in the order given;
	 * otherwise the elements are inserted at appropriate positions.
	 * <p>
	 * This method should be called (by the content provider) when elements have
	 * been added to the model, in order to cause the viewer to accurately
	 * reflect the model. This method only affects the viewer, not the model.
	 * </p>
	 *
	 * @param elements
	 *            the elements to add
	 */
	public void add(Object[] elements) {
		assertElementsNotNull(elements);
		if (checkBusy())
			return;
		Object[] filtered = filter(elements);

		final int itemsLimit = getItemsLimit();
		for (Object element : filtered) {
			int index = indexForElement(element);
			// 1. Cost is negligible if you don't set limit.
			// 2. Fast way to check if items have reached the limit.
			if (itemsLimit > 0) {
				if (doGetItemCount() == itemsLimit) {
					createExpandableNode(element, index);
					continue;
				}
				if (getLastElement() instanceof ExpandableNode expNode) {
					addElementAtIndex(element, index, expNode);
					continue;
				}
			}
			createItem(element, index);
		}
	}

	/**
	 * Create a new TableItem at index if required.
	 *
	 * @param element
	 * @param index
	 *
	 * @since 3.1
	 */
	void createItem(Object element, int index) {
		if (virtualManager == null) {
			updateItem(internalCreateNewRowPart(SWT.NONE, index).getItem(),
					element);
		} else {
			virtualManager.notVisibleAdded(element, index);

		}
	}

	/**
	 * Create a new row.  Callers can only use the returned object locally and before
	 * making the next call on the viewer since it may be re-used for subsequent method
	 * calls.
	 *
	 * @param style
	 *            the style for the new row
	 * @param rowIndex
	 *            the index of the row or -1 if the row is appended at the end
	 * @return the newly created row
	 */
	protected abstract ViewerRow internalCreateNewRowPart(int style,
			int rowIndex);

	/**
	 * Adds the given element to this table viewer. If this viewer does not have
	 * a sorter, the element is added at the end; otherwise the element is
	 * inserted at the appropriate position.
	 * <p>
	 * This method should be called (by the content provider) when a single
	 * element has been added to the model, in order to cause the viewer to
	 * accurately reflect the model. This method only affects the viewer, not
	 * the model. Note that there is another method for efficiently processing
	 * the simultaneous addition of multiple elements.
	 * </p>
	 *
	 * @param element
	 *            the element to add
	 */
	public void add(Object element) {
		add(new Object[] { element });
	}

	@Override
	protected Widget doFindInputItem(Object element) {
		if (equals(element, getRoot())) {
			return getControl();
		}
		return null;
	}

	@Override
	protected Widget doFindItem(Object element) {

		Item[] children = doGetItems();
		for (Item item : children) {
			Object data = item.getData();
			if (data != null && equals(data, element)) {
				return item;
			}
		}

		return null;
	}

	@Override
	protected void doUpdateItem(Widget widget, Object element, boolean fullMap) {
		boolean oldBusy = isBusy();
		setBusy(true);
		try {
			if (widget instanceof Item) {
				final Item item = (Item) widget;

				// remember element we are showing
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

				int columnCount = doGetColumnCount();
				if (columnCount == 0)
					columnCount = 1;// If there are no columns do the first one

				ViewerRow viewerRowFromItem = getViewerRowFromItem(item);

				boolean isVirtual = (getControl().getStyle() & SWT.VIRTUAL) != 0;

				// If the control is virtual, we cannot use the cached viewer row object. See bug 188663.
				if (isVirtual) {
					viewerRowFromItem = (ViewerRow) viewerRowFromItem.clone();
				}

				// Also enter loop if no columns added. See 1G9WWGZ: JFUIF:WINNT -
				// TableViewer with 0 columns does not work
				for (int column = 0; column < columnCount || column == 0; column++) {

					// ExpandableNode is shown in first column only.
					if (element instanceof ExpandableNode && column != 0) {
						continue;
					}

					ViewerColumn columnViewer = getViewerColumn(column);
					ViewerCell cellToUpdate = updateCell(viewerRowFromItem,
							column, element);

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
		} finally {
			setBusy(oldBusy);
		}
	}

	@Override
	protected Widget getColumnViewerOwner(int columnIndex) {
		int columnCount = doGetColumnCount();

		if (columnIndex < 0
				|| (columnIndex > 0 && columnIndex >= columnCount)) {
			return null;
		}

		if (columnCount == 0)// Hang it off the table if it
			return getControl();

		return doGetColumn(columnIndex);
	}

	/**
	 * Returns the element with the given index from this table viewer. Returns
	 * <code>null</code> if the index is out of range.
	 * <p>
	 * This method is internal to the framework.
	 * </p>
	 *
	 * @param index
	 *            the zero-based index
	 * @return the element at the given index, or <code>null</code> if the
	 *         index is out of range
	 */
	public Object getElementAt(int index) {
		if (index >= 0 && index < doGetItemCount()) {
			Item i = doGetItem(index);
			if (i != null) {
				return i.getData();
			}
		}
		return null;
	}

	/**
	 * The table viewer implementation of this <code>Viewer</code> framework
	 * method returns the label provider, which in the case of table viewers
	 * will be an instance of either <code>ITableLabelProvider</code> or
	 * <code>ILabelProvider</code>. If it is an
	 * <code>ITableLabelProvider</code>, then it provides a separate label
	 * text and image for each column. If it is an <code>ILabelProvider</code>,
	 * then it provides only the label text and image for the first column, and
	 * any remaining columns are blank.
	 */
	@Override
	public IBaseLabelProvider getLabelProvider() {
		return super.getLabelProvider();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected List getSelectionFromWidget() {
		if (virtualManager != null) {
			return getVirtualSelection();
		}
		Widget[] items = doGetSelection();
		List<Object> list = new ArrayList<>(items.length);
		for (Widget item : items) {
			Object e = item.getData();
			if (e != null) {
				list.add(e);
			}
		}
		return list;
	}

	/**
	 * Get the virtual selection. Avoid calling SWT whenever possible to prevent
	 * extra widget creation.
	 *
	 * @return List of Object
	 */

	private List<Object> getVirtualSelection() {

		List<Object> result = new ArrayList<>();
		int[] selectionIndices = doGetSelectionIndices();
		if (getContentProvider() instanceof ILazyContentProvider) {
			ILazyContentProvider lazy = (ILazyContentProvider) getContentProvider();
			for (int selectionIndex : selectionIndices) {
				lazy.updateElement(selectionIndex);// Start the update
				// check for the case where the content provider changed the number of items
				if (selectionIndex < doGetItemCount()) {
					Object element = doGetItem(selectionIndex).getData();
					// Only add the element if it got updated.
					// If this is done deferred the selection will
					// be incomplete until selection is finished.
					if (element != null) {
						result.add(element);
					}
				}
			}
		} else {
			for (int selectionIndex : selectionIndices) {
				Object element = null;
				if (selectionIndex < virtualManager.cachedElements.length) {
					element = virtualManager.cachedElements[selectionIndex];
				}
				if (element == null) {
					// Not cached so try the item's data
					Item item = doGetItem(selectionIndex);
					element = item.getData();
				}
				if (element != null) {
					result.add(element);
				}
			}

		}
		return result;
	}

	/**
	 * @param element
	 *            the element to insert
	 * @return the index where the item should be inserted.
	 */
	protected int indexForElement(Object element) {
		ViewerComparator comparator = getComparator();
		if (comparator == null) {
			return doGetItemCount();
		}
		int count = doGetItemCount();
		int min = 0, max = count - 1;
		while (min <= max) {
			int mid = (min + max) / 2;
			Object data = doGetItem(mid).getData();
			int compare = comparator.compare(this, data, element);
			if (compare == 0) {
				// find first item > element
				while (compare == 0) {
					++mid;
					if (mid >= count) {
						break;
					}
					data = doGetItem(mid).getData();
					compare = comparator.compare(this, data, element);
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

	@Override
	protected void inputChanged(Object input, Object oldInput) {
		getControl().setRedraw(false);
		try {
			preservingSelection(() -> internalRefresh(getRoot()));
		} finally {
			getControl().setRedraw(true);
		}
	}

	/**
	 * Inserts the given element into this table viewer at the given position.
	 * If this viewer has a sorter, the position is ignored and the element is
	 * inserted at the correct position in the sort order.
	 * <p>
	 * This method should be called (by the content provider) when elements have
	 * been added to the model, in order to cause the viewer to accurately
	 * reflect the model. This method only affects the viewer, not the model.
	 * </p>
	 *
	 * @param element
	 *            the element
	 * @param position
	 *            a 0-based position relative to the model, or -1 to indicate
	 *            the last position
	 */
	public void insert(Object element, int position) {
		applyEditorValue();
		if (getComparator() != null || hasFilters()) {
			add(element);
			return;
		}

		// if limit is set and still some elements are not populated.Assumption that
		// user has already updated the model and needs addition of item.
		if (getItemsLimit() > 0 && getLastElement() instanceof ExpandableNode) {
			internalRefreshAll(false);
			return;
		}

		if (position == -1) {
			position = doGetItemCount();
		}
		if (checkBusy())
			return;
		createItem(element, position);
	}

	@Override
	protected void internalRefresh(Object element) {
		internalRefresh(element, true);
	}

	@Override
	protected void internalRefresh(Object element, boolean updateLabels) {
		applyEditorValue();
		if (element == null || equals(element, getRoot())) {
			if (virtualManager == null) {
				internalRefreshAll(updateLabels);
			} else {
				internalVirtualRefreshAll();
			}
		} else {
			Widget w = findItem(element);
			if (w != null) {
				updateItem(w, element);
			}
		}
	}

	/**
	 * Refresh all with virtual elements.
	 *
	 * @since 3.1
	 */
	private void internalVirtualRefreshAll() {

		Object root = getRoot();
		IContentProvider contentProvider = getContentProvider();

		// Invalidate for lazy
		if (!(contentProvider instanceof ILazyContentProvider)
				&& (contentProvider instanceof IStructuredContentProvider)) {
			// Don't cache if the root is null but cache if it is not lazy.
			if (root != null) {
				virtualManager.cachedElements = getSortedChildren(root);
				doSetItemCount(virtualManager.cachedElements.length);
			}
		}
		doClearAll();
	}

	/**
	 * Refresh all of the elements of the table. update the labels if
	 * updatLabels is true;
	 *
	 * @param updateLabels
	 *
	 * @since 3.1
	 */
	private void internalRefreshAll(boolean updateLabels) {
		// the parent

		// in the code below, it is important to do all disassociates
		// before any associates, since a later disassociate can undo an
		// earlier associate
		// e.g. if (a, b) is replaced by (b, a), the disassociate of b to
		// item 1 could undo
		// the associate of b to item 0.
		Item[] items = doGetItems();
		Object[] children = getChildrenWithLimitApplied(getRoot(), items);
		if (children == null) {
			children = getSortedChildren(getRoot());
		}
		int min = Math.min(children.length, items.length);
		for (int i = 0; i < min; ++i) {

			Item item = items[i];

			// if the element is unchanged, update its label if appropriate
			if (equals(children[i], item.getData())) {
				if (updateLabels) {
					updateItem(item, children[i]);
				} else {
					// associate the new element, even if equal to the old
					// one,
					// to remove stale references (see bug 31314)
					associate(children[i], item);
				}
			} else {
				// updateItem does an associate(...), which can mess up
				// the associations if the order of elements has changed.
				// E.g. (a, b) -> (b, a) first replaces a->0 with b->0, then
				// replaces b->1 with a->1, but this actually removes b->0.
				// So, if the object associated with this item has changed,
				// just disassociate it for now, and update it below.
				// we also need to reset the item (set its text,images etc. to
				// default values) because the label decorators rely on this
				disassociate(item);
				doClear(i);
			}
		}
		// dispose of all items beyond the end of the current elements
		if (min < items.length) {
			for (int i = items.length; --i >= min;) {

				disassociate(items[i]);
			}
			if (virtualManager != null) {
				virtualManager.removeIndicesFromTo(min, items.length - 1);
			}
			doRemove(min, items.length - 1);
		}
		// Workaround for 1GDGN4Q: ITPUI:WIN2000 - TableViewer icons get
		// scrunched
		if (doGetItemCount() == 0) {
			doRemoveAll();
		}
		// Update items which were disassociated above
		for (int i = 0; i < min; ++i) {

			Item item = items[i];
			if (item.getData() == null) {
				updateItem(item, children[i]);
			}
		}
		// add any remaining elements
		for (int i = min; i < children.length; ++i) {
			createItem(children[i], i);
		}
	}

	/**
	 * Removes the given elements from this table viewer.
	 *
	 * @param elements
	 *            the elements to remove
	 */
	private void internalRemove(final Object[] elements) {
		Object input = getInput();
		for (Object element : elements) {
			if (equals(element, input)) {
				boolean oldBusy = isBusy();
				setBusy(false);
				try {
					setInput(null);
				} finally {
					setBusy(oldBusy);
				}
				return;
			}
		}

		// if limit is set and still some elements are not populated.Assumption that
		// user has already updated the model and needs removal of an item.
		if (getItemsLimit() > 0 && getLastElement() instanceof ExpandableNode) {
			internalRefreshAll(false);
			return;
		}

		// use remove(int[]) rather than repeated TableItem.dispose() calls
		// to allow SWT to optimize multiple removals
		int[] indices = new int[elements.length];
		int count = 0;
		for (Object element : elements) {
			Widget w = findItem(element);
			if (w == null && virtualManager != null) {
				int index = virtualManager.find(element);
				if (index != -1) {
					indices[count++] = index;
				}
			} else if (w instanceof Item) {
				Item item = (Item) w;
				disassociate(item);
				indices[count++] = doIndexOf(item);
			}
		}
		if (count < indices.length) {
			System.arraycopy(indices, 0, indices = new int[count], 0, count);
		}
		if (virtualManager != null) {
			virtualManager.removeIndices(indices);
		}
		doRemove(indices);

		// Workaround for 1GDGN4Q: ITPUI:WIN2000 - TableViewer icons get
		// scrunched
		if (doGetItemCount() == 0) {
			doRemoveAll();
		}
	}

	/**
	 * Returns the data of the last item on the viewer.
	 *
	 * @return may return null
	 */
	Object getLastElement() {
		Item lastItem;
		try {
			// fast path first
			int itemCount = doGetItemCount();
			if (itemCount == 0) {
				return null;
			}
			lastItem = doGetItem(itemCount - 1);
		} catch (Exception e) {
			// something went wrong with indexes
			Item[] items = doGetItems();
			int length = items.length;
			if (length == 0) {
				return null;
			}
			lastItem = items[length - 1];
		}
		return lastItem == null ? null : lastItem.getData();
	}

	/**
	 * Removes the given elements from this table viewer. The selection is
	 * updated if required.
	 * <p>
	 * This method should be called (by the content provider) when elements have
	 * been removed from the model, in order to cause the viewer to accurately
	 * reflect the model. This method only affects the viewer, not the model.
	 * </p>
	 *
	 * @param elements
	 *            the elements to remove
	 */
	public void remove(final Object[] elements) {
		assertElementsNotNull(elements);
		if (checkBusy())
			return;
		if (elements.length == 0) {
			return;
		}
		preservingSelection(() -> internalRemove(elements));
	}

	/**
	 * Removes the given element from this table viewer. The selection is
	 * updated if necessary.
	 * <p>
	 * This method should be called (by the content provider) when a single
	 * element has been removed from the model, in order to cause the viewer to
	 * accurately reflect the model. This method only affects the viewer, not
	 * the model. Note that there is another method for efficiently processing
	 * the simultaneous removal of multiple elements.
	 * </p>
	 * <strong>NOTE:</strong> removing an object from a virtual table will
	 * decrement the itemCount.
	 *
	 * @param element
	 *            the element
	 */
	public void remove(Object element) {
		remove(new Object[] { element });
	}

	@Override
	public void reveal(Object element) {
		Assert.isNotNull(element);
		Widget w = findItem(element);

		if (w == null && getItemsLimit() > 0) {
			// if limited elements are populated, element to reveal may be inside
			// ExpandableNode.
			if (getLastElement() instanceof ExpandableNode node) {
				Item[] items = doGetItems();
				Object[] remEles = node.getRemainingElements();
				for (Object object : remEles) {
					if (equals(object, element)) {
						w = items[items.length - 1];
						break;
					}
				}
			}
		}

		if (w instanceof Item) {
			doShowItem((Item) w);
		}
	}

	@Override
	protected void setSelectionToWidget(@SuppressWarnings("rawtypes") List list, boolean reveal) {
		if (list == null) {
			doDeselectAll();
			return;
		}

		if (virtualManager != null) {
			virtualSetSelectionToWidget(list, reveal);
			return;
		}

		// This is vital to use doSetSelection because on SWT-Table on Win32 this will also
		// move the focus to this row (See bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=198665)
		if (reveal) {
			int size = list.size();
			Item[] items = new Item[size];
			int count = 0;
			for (int i = 0; i < size; ++i) {
				Object o = list.get(i);
				Widget w = findItem(o);
				if (w instanceof Item) {
					Item item = (Item) w;
					items[count++] = item;
				}
			}
			if (count < size) {
				System.arraycopy(items, 0, items = new Item[count], 0, count);
			}
			doSetSelection(items);
		} else {
			doDeselectAll(); // Clear the selection
			if( ! list.isEmpty() ) {
				int[] indices = new int[list.size()];

				Iterator<?> it = list.iterator();
				Item[] items = doGetItems();
				Object modelElement;

				int count = 0;
				while( it.hasNext() ) {
					modelElement = it.next();
					boolean found = false;
					for (int i = 0; i < items.length && !found; i++) {
						if (equals(modelElement, items[i].getData())) {
							indices[count++] = i;
							found = true;
						}
					}
				}

				if (count < indices.length) {
					System.arraycopy(indices, 0, indices = new int[count], 0, count);
				}

				// item to select may be hidden inside expandable node.
				if (getItemsLimit() > 0 && indices.length < list.size()
						&& getLastElement() instanceof ExpandableNode expNode) {

					// extract only non found items already.
					List<Object> notFoundItems = new ArrayList<Object>(list);
					for (int index : indices) {
						notFoundItems.remove(doGetItem(index).getData());
					}

					Object[] remEles = expNode.getRemainingElements();
					OuterLoop : for (Object searchItem : notFoundItems) {
						for (Object remEle : remEles) {
							if (equals(remEle, searchItem)) {
								int[] placeHolder = new int[indices.length + 1];
								System.arraycopy(indices, 0, placeHolder, 0, indices.length);
								placeHolder[placeHolder.length - 1] = items.length - 1;
								indices = placeHolder;
								break OuterLoop;
							}
						}
					}
				}

				doSelect(indices);
			}
		}
	}

	/**
	 * Set the selection on a virtual table
	 *
	 * @param list
	 *            The elements to set
	 * @param reveal
	 *            Whether or not reveal the first item.
	 */
	private void virtualSetSelectionToWidget(@SuppressWarnings("rawtypes") List list, boolean reveal) {
		int size = list.size();
		int[] indices = new int[list.size()];

		Item firstItem = null;
		int count = 0;
		HashSet<Object> virtualElements = new HashSet<>();
		for (int i = 0; i < size; ++i) {
			Object o = list.get(i);
			Widget w = findItem(o);
			if (w instanceof Item) {
				Item item = (Item) w;
				indices[count++] = doIndexOf(item);
				if (firstItem == null) {
					firstItem = item;
				}
			} else {
				virtualElements.add(o);
			}
		}

		if (getContentProvider() instanceof ILazyContentProvider) {
			ILazyContentProvider provider = (ILazyContentProvider) getContentProvider();

			// Now go through it again until all is done or we are no longer
			// virtual
			// This may create all items so it is not a good
			// idea in general.
			// Use #setSelection (int [] indices,boolean reveal) instead
			for (int i = 0; virtualElements.size() > 0 && i < doGetItemCount(); i++) {
				provider.updateElement(i);
				Item item = doGetItem(i);
				if (virtualElements.contains(item.getData())) {
					indices[count++] = i;
					virtualElements.remove(item.getData());
					if (firstItem == null) {
						firstItem = item;
					}
				}
			}
		} else if (count != list.size()) {// As this is expensive skip it if all
			// have been found
			// If it is not lazy we can use the cache
			for (int i = 0; i < virtualManager.cachedElements.length; i++) {
				Object element = virtualManager.cachedElements[i];
				if (virtualElements.contains(element)) {
					Item item = doGetItem(i);
					item.getText();// Be sure to fire the update
					indices[count++] = i;
					virtualElements.remove(element);
					if (firstItem == null) {
						firstItem = item;
					}
				}
			}
		}

		if (count < size) {
			System.arraycopy(indices, 0, indices = new int[count], 0, count);
		}

		if (reveal) {
			doSetSelection(indices);
		} else {
			doDeselectAll();
			doSelect(indices);
		}

	}

	/**
	 * Set the item count of the receiver.
	 *
	 * @param count
	 *            the new table size.
	 *
	 * @since 3.1
	 */
	public void setItemCount(int count) {
		if (checkBusy())
			return;
		int oldCount = doGetItemCount();
		if (count < oldCount) {
			// need to disassociate elements that are being disposed
			for (int i = count; i < oldCount; i++) {
				Item item = doGetItem(i);
				if (item.getData() != null) {
					disassociate(item);
				}
			}
		}
		doSetItemCount(count);
		if (virtualManager != null) {
			virtualManager.adjustCacheSize(count);
		}
		getControl().redraw();
	}

	/**
	 * Replace the element at the given index with the given element. This
	 * method will not call the content provider to verify. <strong>Note that
	 * this method will materialize a TableItem the given index.</strong>.
	 *
	 * @param element the new element
	 * @param index the index of the table item to replace
	 * @see ILazyContentProvider
	 *
	 * @since 3.1
	 */
	public void replace(Object element, int index) {
		if (checkBusy())
			return;
		Item item = doGetItem(index);
		refreshItem(item, element);
	}

	/**
	 * Clear the table item at the specified index
	 *
	 * @param index
	 *            the index of the table item to be cleared
	 *
	 * @since 3.1
	 */
	public void clear(int index) {
		Item item = doGetItem(index);
		if (item.getData() != null) {
			disassociate(item);
		}
		doClear(index);
	}

	@Override
	protected Object[] getRawChildren(Object parent) {

		Assert.isTrue(!(getContentProvider() instanceof ILazyContentProvider),
				"Cannot get raw children with an ILazyContentProvider");//$NON-NLS-1$
		return super.getRawChildren(parent);

	}

	/**
	 * Sets the content provider used by this <code>AbstractTableViewer</code>.
	 * <p>
	 * Content providers for abstract table viewers must implement either
	 * {@link IStructuredContentProvider} or {@link ILazyContentProvider}.
	 */
	@Override
	public void setContentProvider(IContentProvider provider) {
		// the actual check is in assertContentProviderType
		super.setContentProvider(provider);
	}

	@Override
	protected void assertContentProviderType(IContentProvider provider) {
		Assert.isTrue(provider instanceof IStructuredContentProvider
				|| provider instanceof ILazyContentProvider);
	}

	private void createExpandableNode(Object element, int index) {
		// case2 : if ExpNode absent. Create an ExpNode
		Item[] items = doGetItems();
		// there is an extra element being added.
		Object[] allEles = new Object[items.length + 1];

		// case2.1 : new element goes inside ExpNode and a new ExpNode is created
		if (index >= doGetItemCount()) {
			for (int i = 0; i < items.length; i++) {
				allEles[i] = items[i].getData();
			}
			allEles[allEles.length - 1] = element;
			ExpandableNode expNode = new ExpandableNode(allEles, items.length, getItemsLimit(), this);
			createItem(expNode, -1);
		} else {
			// case2.2 : insert the element at it's appropriate position. update all
			// elements until last element and create an ExpNode.

			// copy data until index
			for (int i = 0; i < index; i++) {
				allEles[i] = items[i].getData();

			}
			// copy new element at it's index
			allEles[index] = element;

			Object temp = items[index].getData();
			// update the index with new element.
			doUpdateItem(items[index], element, true);

			Object temp1 = null;

			// move all elements one position ahead from index(including)
			// after the loop 'temp' will contain last element which will go inside ExpNode.
			for (int i = index; i < items.length - 1; i++) {
				temp1 = items[i + 1].getData();
				doUpdateItem(items[i + 1], temp, true);
				allEles[i + 1] = temp;
				temp = temp1;
			}

			allEles[allEles.length - 1] = temp;

			ExpandableNode expNode = new ExpandableNode(allEles, items.length, getItemsLimit(), this);
			createItem(expNode, -1);
		}
	}

	private void addElementAtIndex(Object element, int index, ExpandableNode expNode) {
		final int itemCount = doGetItemCount();
		if (index >= itemCount - 1) {
			// case 1.2 : this element belongs to hidden elements in the expandable node
			expNode.addElement(element);
			doUpdateItem(doGetItem(itemCount - 1), expNode, true);
		} else {
			// case 1.1 : element need to be inserted in the visible items.
			Item[] items = doGetItems();
			// we know last element is ExpNode and last but one now must go into this node.
			Item visibleLast = items[items.length - 2];
			expNode.addElement(visibleLast.getData());
			doUpdateItem(doGetItem(itemCount - 1), expNode, true);

			// backup index element and update it with new element being added.
			Object temp = items[index].getData();
			doUpdateItem(items[index], element, true);

			Object temp1 = items[index + 1].getData();

			// update all the items to one position next from the index.
			for (int i = index + 1; i < items.length - 1; i++) {
				temp1 = items[i].getData();
				doUpdateItem(items[i], temp, true);
				temp = temp1;
			}
		}
	}

	/**
	 * Searches the receiver's list starting at the first item (index 0) until
	 * an item is found that is equal to the argument, and returns the index of
	 * that item. If no item is found, returns -1.
	 *
	 * @param item
	 *            the search item
	 * @return the index of the item
	 *
	 * @since 3.3
	 */
	protected abstract int doIndexOf(Item item);

	/**
	 * Returns the number of items contained in the receiver.
	 *
	 * @return the number of items
	 *
	 * @since 3.3
	 */
	protected abstract int doGetItemCount();

	/**
	 * Sets the number of items contained in the receiver.
	 *
	 * @param count
	 *            the number of items
	 *
	 * @since 3.3
	 */
	protected abstract void doSetItemCount(int count);

	/**
	 * Returns a (possibly empty) array of TableItems which are the items in the
	 * receiver.
	 *
	 * @return the items in the receiver
	 *
	 * @since 3.3
	 */
	protected abstract Item[] doGetItems();

	/**
	 * Returns the column at the given, zero-relative index in the receiver.
	 * Throws an exception if the index is out of range. Columns are returned in
	 * the order that they were created. If no TableColumns were created by the
	 * programmer, this method will throw ERROR_INVALID_RANGE despite the fact
	 * that a single column of data may be visible in the table. This occurs
	 * when the programmer uses the table like a list, adding items but never
	 * creating a column.
	 *
	 * @param index
	 *            the index of the column to return
	 * @return the column at the given index
	 * @exception IllegalArgumentException -
	 *                if the index is not between 0 and the number of elements
	 *                in the list minus 1 (inclusive)
	 *
	 * @since 3.3
	 */
	protected abstract Widget doGetColumn(int index);

	/**
	 * Returns the item at the given, zero-relative index in the receiver.
	 * Throws an exception if the index is out of range.
	 *
	 * @param index
	 *            the index of the item to return
	 * @return the item at the given index
	 * @exception IllegalArgumentException -
	 *                if the index is not between 0 and the number of elements
	 *                in the list minus 1 (inclusive)
	 *
	 * @since 3.3
	 */
	protected abstract Item doGetItem(int index);

	/**
	 * Returns an array of {@link Item} that are currently selected in the
	 * receiver. The order of the items is unspecified. An empty array indicates
	 * that no items are selected.
	 *
	 * @return an array representing the selection
	 *
	 * @since 3.3
	 */
	protected abstract Item[] doGetSelection();

	/**
	 * Returns the zero-relative indices of the items which are currently
	 * selected in the receiver. The order of the indices is unspecified. The
	 * array is empty if no items are selected.
	 *
	 * @return an array representing the selection
	 *
	 * @since 3.3
	 */
	protected abstract int[] doGetSelectionIndices();

	/**
	 * Clears all the items in the receiver. The text, icon and other attributes
	 * of the items are set to their default values. If the table was created
	 * with the <code>SWT.VIRTUAL</code> style, these attributes are requested
	 * again as needed.
	 *
	 * @since 3.3
	 */
	protected abstract void doClearAll();

	/**
	 * Resets the given item in the receiver. The text, icon and other attributes
	 * of the item are set to their default values.
	 *
	 * @param item the item to reset
	 *
	 * @since 3.3
	 */
	protected abstract void doResetItem(Item item);

	/**
	 * Removes the items from the receiver which are between the given
	 * zero-relative start and end indices (inclusive).
	 *
	 * @param start
	 *            the start of the range
	 * @param end
	 *            the end of the range
	 *
	 * @exception IllegalArgumentException -
	 *                if either the start or end are not between 0 and the
	 *                number of elements in the list minus 1 (inclusive)
	 *
	 * @since 3.3
	 */
	protected abstract void doRemove(int start, int end);

	/**
	 * Removes all of the items from the receiver.
	 *
	 * @since 3.3
	 */
	protected abstract void doRemoveAll();

	/**
	 * Removes the items from the receiver's list at the given zero-relative
	 * indices.
	 *
	 * @param indices
	 *            the array of indices of the items
	 *
	 * @exception IllegalArgumentException -
	 *                if the array is null, or if any of the indices is not
	 *                between 0 and the number of elements in the list minus 1
	 *                (inclusive)
	 *
	 * @since 3.3
	 */
	protected abstract void doRemove(int[] indices);

	/**
	 * Shows the item. If the item is already showing in the receiver, this
	 * method simply returns. Otherwise, the items are scrolled until the item
	 * is visible.
	 *
	 * @param item
	 *            the item to be shown
	 *
	 * @exception IllegalArgumentException -
	 *                if the item is null
	 *
	 * @since 3.3
	 */
	protected abstract void doShowItem(Item item);

	/**
	 * Deselects all selected items in the receiver.
	 *
	 * @since 3.3
	 */
	protected abstract void doDeselectAll();

	/**
	 * Sets the receiver's selection to be the given array of items. The current selection is
	 * cleared before the new items are selected, and if necessary the receiver is scrolled to make
	 * the new selection visible.
	 * <p>
	 * Items that are not in the receiver are ignored. If the receiver is single-select and multiple
	 * items are specified, then all items are ignored.
	 * </p>
	 *
	 * @param items the array of items
	 *
	 * @exception IllegalArgumentException - if the array of items is null
	 *
	 * @since 3.3
	 */
	protected abstract void doSetSelection(Item[] items);

	/**
	 * Shows the selection. If the selection is already showing in the receiver,
	 * this method simply returns. Otherwise, the items are scrolled until the
	 * selection is visible.
	 *
	 * @since 3.3
	 */
	protected abstract void doShowSelection();

	/**
	 * Selects the items at the given zero-relative indices in the receiver. The current selection
	 * is cleared before the new items are selected, and if necessary the receiver is scrolled to
	 * make the new selection visible.
	 * <p>
	 * Indices that are out of range and duplicate indices are ignored. If the receiver is
	 * single-select and multiple indices are specified, then all indices are ignored.
	 * </p>
	 *
	 * @param indices the indices of the items to select
	 *
	 * @exception IllegalArgumentException - if the array of indices is null
	 *
	 * @since 3.3
	 */
	protected abstract void doSetSelection(int[] indices);

	/**
	 * Clears the item at the given zero-relative index in the receiver. The
	 * text, icon and other attributes of the item are set to the default value.
	 * If the table was created with the <code>SWT.VIRTUAL</code> style, these
	 * attributes are requested again as needed.
	 *
	 * @param index
	 *            the index of the item to clear
	 *
	 * @exception IllegalArgumentException -
	 *                if the index is not between 0 and the number of elements
	 *                in the list minus 1 (inclusive)
	 *
	 * @see SWT#VIRTUAL
	 * @see SWT#SetData
	 *
	 * @since 3.3
	 */
	protected abstract void doClear(int index);



	/**
	 * Selects the items at the given zero-relative indices in the receiver.
	 * The current selection is not cleared before the new items are selected.
	 * <p>
	 * If the item at a given index is not selected, it is selected.
	 * If the item at a given index was already selected, it remains selected.
	 * Indices that are out of range and duplicate indices are ignored.
	 * If the receiver is single-select and multiple indices are specified,
	 * then all indices are ignored.
	 * </p>
	 *
	 * @param indices the array of indices for the items to select
	 *
	 * @exception IllegalArgumentException - if the array of indices is null
	 *
	 */
	protected abstract void doSelect(int[] indices);

	/**
	 * Returns true if the element is present in the viewer. If the viewer has
	 * incremental display set then the element is searched inside expandable node
	 * also. i.e. it searches inside the remaining elements to be populated.
	 *
	 * @param element model element
	 * @return if given model element is contained in the viewer
	 * @since 3.31
	 */
	public boolean contains(Object element) {
		if (findItem(element) != null) {
			return true;
		}

		if (getItemsLimit() <= 0) {
			return false;
		}

		if (getLastElement() instanceof ExpandableNode node) {
			return node.contains(element);
		}
		return false;
	}

}
