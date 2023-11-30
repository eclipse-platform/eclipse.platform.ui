/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.viewers.deferred;

import java.util.Comparator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.AcceptAllFilter;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;

/**
 * Content provider that performs sorting and filtering in a background thread.
 * Requires a <code>TableViewer</code> created with the <code>SWT.VIRTUAL</code>
 * flag and an <code>IConcurrentModel</code> as input.
 * <p>
 * The sorter and filter must be set directly on the content provider.
 * Any sorter or filter on the TableViewer will be ignored.
 * </p>
 *
 * <p>
 * The real implementation is in <code>BackgroundContentProvider</code>. This
 * object is a lightweight wrapper that adapts the algorithm to work with
 * <code>TableViewer</code>.
 * </p>
 *
 * @since 3.1
 */
public class DeferredContentProvider implements ILazyContentProvider {

	private int limit = -1;
	private BackgroundContentProvider provider;
	private Comparator sortOrder;
	private IFilter filter = AcceptAllFilter.getInstance();
	private AbstractVirtualTable table;

	private static final class TableViewerAdapter extends AbstractVirtualTable {

		private TableViewer viewer;

		public TableViewerAdapter(TableViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void clear(int index) {
			viewer.clear(index);
		}

		@Override
		public void replace(Object element, int itemIndex) {
			viewer.replace(element, itemIndex);
		}

		@Override
		public void setItemCount(int total) {
			viewer.setItemCount(total);
		}

		@Override
		public int getItemCount() {
			return viewer.getTable().getItemCount();
		}

		@Override
		public int getTopIndex() {
			return Math.max(viewer.getTable().getTopIndex() - 1, 0);
		}

		@Override
		public int getVisibleItemCount() {
			Table table = viewer.getTable();
			Rectangle rect = table.getClientArea ();
			int itemHeight = table.getItemHeight ();
			int headerHeight = table.getHeaderHeight ();
			return (rect.height - headerHeight + itemHeight - 1) / (itemHeight + table.getGridLineWidth());
		}

		@Override
		public Control getControl() {
			return viewer.getControl();
		}

	}

	/**
	 * Create a DeferredContentProvider with the given sort order.
	 * @param sortOrder a comparator that sorts the content.
	 */
	public DeferredContentProvider(Comparator sortOrder) {
		this.sortOrder = sortOrder;
	}

	@Override
	public void dispose() {
		setProvider(null);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput == null) {
			setProvider(null);
			return;
		}

		Assert.isTrue(newInput instanceof IConcurrentModel);
		Assert.isTrue(viewer instanceof TableViewer);
		IConcurrentModel model = (IConcurrentModel)newInput;

		this.table = new TableViewerAdapter((TableViewer)viewer);

		BackgroundContentProvider newProvider = new BackgroundContentProvider(
				table,
				model, sortOrder);

		setProvider(newProvider);

		newProvider.setLimit(limit);
		newProvider.setFilter(filter);
	}

	/**
	 * Sets the sort order for this content provider. This sort order takes priority
	 * over anything that was supplied to the <code>TableViewer</code>.
	 *
	 * @param sortOrder new sort order. The comparator must be able to support being
	 * used in a background thread.
	 */
	public void setSortOrder(Comparator sortOrder) {
		Assert.isNotNull(sortOrder);
		this.sortOrder = sortOrder;
		if (provider != null) {
			provider.setSortOrder(sortOrder);
		}
	}

	/**
	 * Sets the filter for this content provider. This filter takes priority over
	 * anything that was supplied to the <code>TableViewer</code>. The filter
	 * must be capable of being used in a background thread.
	 *
	 * @param toSet filter to set
	 */
	public void setFilter(IFilter toSet) {
		this.filter = toSet;
		if (provider != null) {
			provider.setFilter(toSet);
		}
	}

	/**
	 * Sets the maximum number of rows in the table. If the model contains more
	 * than this number of elements, only the top elements will be shown based on
	 * the current sort order.
	 *
	 * @param limit maximum number of rows to show or -1 if unbounded
	 */
	public void setLimit(int limit) {
		this.limit = limit;
		if (provider != null) {
			provider.setLimit(limit);
		}
	}

	/**
	 * Returns the current maximum number of rows or -1 if unbounded
	 *
	 * @return the current maximum number of rows or -1 if unbounded
	 */
	public int getLimit() {
		return limit;
	}

	@Override
	public void updateElement(int element) {
		if (provider != null) {
			provider.checkVisibleRange(element);
		}
	}

	private void setProvider(BackgroundContentProvider newProvider) {
		if (provider != null) {
			provider.dispose();
		}

		provider = newProvider;
	}

}
