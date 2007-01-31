/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - concept of ViewerRow,
 *                                                 fix for 159597, refactoring (bug 153993), 
 *                                                 widget-independency (bug 154329)
 *******************************************************************************/

package org.eclipse.jface.viewers;


import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

/**
 * A concrete viewer based on a SWT <code>Table</code> control.
 * <p>
 * This class is not intended to be subclassed outside the viewer framework. It
 * is designed to be instantiated with a pre-existing SWT table control and
 * configured with a domain-specific content provider, table label provider,
 * element filter (optional), and element sorter (optional).
 * </p>
 * <p>
 * Label providers for table viewers must implement either the
 * <code>ITableLabelProvider</code> or the <code>ILabelProvider</code>
 * interface (see <code>TableViewer.setLabelProvider</code> for more details).
 * </p>
 * <p>
 * As of 3.1 the TableViewer now supports the SWT.VIRTUAL flag. If the
 * underlying table is SWT.VIRTUAL, the content provider may implement
 * {@link ILazyContentProvider} instead of {@link IStructuredContentProvider}.
 * Note that in this case, the viewer does not support sorting or filtering.
 * Also note that in this case, the Widget based APIs may return null if the
 * element is not specified or not created yet.
 * </p>
 * <p>
 * Users of SWT.VIRTUAL should also avoid using getItems() from the Table within
 * the TreeViewer as this does not necessarily generate a callback for the
 * TreeViewer to populate the items. It also has the side effect of creating all
 * of the items thereby eliminating the performance improvements of SWT.VIRTUAL.
 * </p>
 * 
 * @see SWT#VIRTUAL
 * @see #doFindItem(Object)
 * @see #internalRefresh(Object, boolean)
 */
public class TableViewer extends AbstractTableViewer {
	/**
	 * This viewer's table control.
	 */
	private Table table;

	/**
	 * This viewer's table editor.
	 */
	private TableEditor tableEditor;

	/**
	 * Creates a table viewer on a newly-created table control under the given
	 * parent. The table control is created using the SWT style bits
	 * <code>MULTI, H_SCROLL, V_SCROLL,</code> and <code>BORDER</code>. The
	 * viewer has no input, no content provider, a default label provider, no
	 * sorter, and no filters. The table has no columns.
	 * 
	 * @param parent
	 *            the parent control
	 */
	public TableViewer(Composite parent) {
		this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
	}

	/**
	 * Creates a table viewer on a newly-created table control under the given
	 * parent. The table control is created using the given style bits. The
	 * viewer has no input, no content provider, a default label provider, no
	 * sorter, and no filters. The table has no columns.
	 * 
	 * @param parent
	 *            the parent control
	 * @param style
	 *            SWT style bits
	 */
	public TableViewer(Composite parent, int style) {
		this(new Table(parent, style));
	}

	/**
	 * Creates a table viewer on the given table control. The viewer has no
	 * input, no content provider, a default label provider, no sorter, and no
	 * filters.
	 * 
	 * @param table
	 *            the table control
	 */
	public TableViewer(Table table) {
		this.table = table;
		tableEditor = new TableEditor(table);
		hookControl(table);
	}

	public Control getControl() {
		return table;
	}

	/**
	 * Returns this table viewer's table control.
	 * 
	 * @return the table control
	 */
	public Table getTable() {
		return table;
	}
	
	protected AbstractViewerEditor createViewerEditor() {
		return new AbstractViewerEditor(this) {

			protected StructuredSelection createSelection(Object element) {
				return new StructuredSelection(element);
			}

			protected Item[] getSelection() {
				return table.getSelection();
			}

			protected void setEditor(Control w, Item item, int fColumnNumber) {
				tableEditor.setEditor(w, (TableItem) item, fColumnNumber);
			}

			protected void setLayoutData(LayoutData layoutData) {
				tableEditor.grabHorizontal = layoutData.grabHorizontal;
				tableEditor.horizontalAlignment = layoutData.horizontalAlignment;
				tableEditor.minimumWidth = layoutData.minimumWidth;
			}

			protected void showSelection() {
				table.showSelection();
			}

		};
	}
	
	/**
	 * <p>
	 * Sets a new selection for this viewer and optionally makes it visible. The
	 * TableViewer implmentation of this method is ineffecient for the
	 * ILazyContentProvider as lookup is done by indices rather than elements
	 * and may require population of the entire table in worse case.
	 * </p>
	 * <p>
	 * Use Table#setSelection(int[] indices) and Table#showSelection() if you
	 * wish to set selection more effeciently when using a ILazyContentProvider.
	 * </p>
	 * 
	 * @param selection
	 *            the new selection
	 * @param reveal
	 *            <code>true</code> if the selection is to be made visible,
	 *            and <code>false</code> otherwise
	 * @see Table#setSelection(int[])
	 * @see Table#showSelection()
	 */
	public void setSelection(ISelection selection, boolean reveal) {
		super.setSelection(selection, reveal);
	}
	
	protected ViewerRow getViewerRowFromItem(Widget item) {
		ViewerRow part = (ViewerRow) item.getData(ViewerRow.ROWPART_KEY);

		if (part == null) {
			part = new TableViewerRow(((TableItem) item));
		}

		return part;
	}
	
	/**
	 * Create a new row with style at index
	 * 
	 * @param style
	 * @param rowIndex
	 * @return ViewerRow
	 */
	protected ViewerRow internalCreateNewRowPart(int style, int rowIndex) {
		TableItem item;

		if (rowIndex >= 0) {
			item = new TableItem(table, style, rowIndex);
		} else {
			item = new TableItem(table, style);
		}

		return getViewerRowFromItem(item);
	}
	
	protected Item getItemAt(Point p) {
		return table.getItem(p);
	}

	// Methods to provide widget independency
	
	protected int doGetItemCount() {
		return table.getItemCount();
	}

	protected int doIndexOf(Item item) {
		return table.indexOf((TableItem)item);
	}

	protected void doSetItemCount(int count) {
		table.setItemCount(count);
	}

	protected Item[] doGetItems() {
		return table.getItems();
	}

	protected int doGetColumnCount() {
		return table.getColumnCount();
	}

	protected Widget doGetColumn(int index) {
		return table.getColumn(index);
	}
	
	protected Item doGetItem(int index) {
		return table.getItem(index);
	}

	protected Item[] doGetSelection() {
		return table.getSelection();
	}

	protected int[] doGetSelectionIndices() {
		return table.getSelectionIndices();
	}

	protected void doClearAll() {
		table.clearAll();
	}
	
	protected void doResetItem(Item item) {
		TableItem tableItem = (TableItem) item;
		int columnCount = Math.max(1, table.getColumnCount());
		for (int i = 0; i < columnCount; i++) {
			tableItem.setText(i, ""); //$NON-NLS-1$
		}
		tableItem.setImage(new Image[columnCount]);// Clear all images
	}

	protected void doRemove(int start, int end) {
		table.remove(start, end);
	}
	
	protected void doRemoveAll() {
		table.removeAll();
	}

	protected void doRemove(int[] indices) {
		table.remove(indices);
	}

	protected void doShowItem(Item item) {
		table.showItem((TableItem)item);
	}

	protected void doDeselectAll() {
		table.deselectAll();
	}

	protected void doSetSelection(Item[] items) {
		Assert.isNotNull(items, "Items-Array can not be null"); //$NON-NLS-1$
		
		TableItem[] t = new TableItem[items.length];
		System.arraycopy(items, 0, t, 0, t.length);
		
		table.setSelection(t);
	}

	protected void doShowSelection() {
		table.showSelection();
	}

	protected void doSetSelection(int[] indices) {
		table.setSelection(indices);
	}

	protected void doClear(int index) {
		table.clear(index);
	}
}
