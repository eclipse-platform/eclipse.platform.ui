/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools;

import java.util.Iterator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

/**
 * Abstract class representing a view made of two tables.
 * The first one is used to display data and the second to display totals of the
 * data showed in the first table.
 */
public abstract class TableWithTotalView extends ViewPart implements ISelectionProvider {
	protected TableTreeViewer viewer; // Table tree viewer used to contain all the data but the total
	protected TableTree tableTree; // The table tree that will populate the viewer
	protected Table totalTable; // The table used to display the totals
	protected boolean flat; // Flag indicating the view mode 
	protected Clipboard clipboard;
	protected Action copyAction;
	protected Action selectAllAction;

	abstract protected String[] getColumnHeaders();

	abstract protected ColumnLayoutData[] getColumnLayout();

	abstract protected void createActions();

	abstract protected void createToolbar();

	abstract protected void createContextMenu();

	private SelectionListener getColumnListener() {
		return new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent event) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent event) {
				// column selected - need to sort
				int column = viewer.getTableTree().getTable().indexOf((TableColumn) event.widget);

				ISorter oldSorter = (ISorter) viewer.getSorter();
				boolean threeState = oldSorter.states() == 3;
				ISelection selection = viewer.getSelection();

				// first check to see if we are changing sort columns.
				// If so, set flatness and get a new sorter
				if (oldSorter == null || !threeState || column != oldSorter.getColumnNumber()) {
					flat = column != 0; // default for column 0 is NOT flat
					viewer.setSorter(getSorter(column));
				} else {
					// Not changing sorters so we have to cycle through states for the columns
					// Three state sort for column 0.  !flat/!reverse -> flat/!reverse -> flat/reverse
					if (column == 0) {
						if (flat) {
							if (oldSorter.isReversed())
								flat = false;
							oldSorter.setReversed(!oldSorter.isReversed());
						} else {
							flat = true;
							oldSorter.setReversed(false);
						}
					} else { // for all other columns flatten and simply reverse
						flat = true;
						oldSorter.setReversed(!oldSorter.isReversed());
					}
				}
				if (viewer.getContentProvider() instanceof IFlattable)
					((IFlattable) viewer.getContentProvider()).setFlat(flat);
				viewer.refresh();
				viewer.setSelection(selection);
			}
		};
	}

	protected ControlListener getColumnResizeListener() {
		return new ControlListener() {
			public void controlMoved(ControlEvent event) {
				// do nothing
			}

			public void controlResized(ControlEvent event) {
				TableColumn column = (TableColumn) event.widget;
				int columnNumber = viewer.getTableTree().getTable().indexOf(column);
				totalTable.getColumn(columnNumber).setWidth(column.getWidth());
			}
		};
	}

	private void createTables(Composite parent) {
		// create a first table, that will display all the data
		tableTree = new TableTree(parent, SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		tableTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		Table table = tableTree.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);

		SelectionListener headerListener = getColumnListener();

		// create a second table for totals
		totalTable = new Table(parent, 0);
		GridData gridInfo = new GridData(GridData.FILL_HORIZONTAL);
		gridInfo.heightHint = totalTable.getItemHeight();
		totalTable.setLayoutData(gridInfo);
		totalTable.setHeaderVisible(false);
		totalTable.setLinesVisible(true);
		TableLayout totalLayout = new TableLayout();
		totalTable.setLayout(totalLayout);

		ControlListener columnResizeListener = getColumnResizeListener();
		//create the columns for the two tables
		ColumnLayoutData[] columnLayout = getColumnLayout();
		String[] headers = getColumnHeaders();
		for (int i = 0; i < getColumnHeaders().length; i++) {
			// column for the first table
			tableLayout.addColumnData(columnLayout[i]);
			TableColumn column = new TableColumn(table, SWT.NONE, i);
			column.setResizable(true);
			column.setText(headers[i]);
			column.addSelectionListener(headerListener);
			// "connect" the two tables so the width of their column evolve simultaneously
			// more precisely here, only the resize of the first table will trigger a resize of the second one
			column.addControlListener(columnResizeListener);

			// column for the second table
			totalLayout.addColumnData(columnLayout[i]);
			column = new TableColumn(totalTable, SWT.NONE, i);
			column.setResizable(true);
		}
	}

	protected abstract ITreeContentProvider getContentProvider();

	protected abstract ITableLabelProvider getLabelProvider();

	protected abstract ViewerSorter getSorter(int column);

	protected abstract String getStatusLineMessage(Object element);

	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, 0);
		// crete a grid layout of one column
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);

		createTables(composite);

		clipboard = new Clipboard(parent.getDisplay());
		//create the viewer
		viewer = new TableTreeViewer(tableTree);
		viewer.setContentProvider(getContentProvider());
		viewer.setLabelProvider(getLabelProvider());
		viewer.setSorter(getSorter(0));
		viewer.addSelectionChangedListener(getTableListener());

		createCommonActions();
		createActions();
		createToolbar();
		createContextMenu();
	}

	abstract protected String[] computeTotalLine(Iterator elements);

	private void createCommonActions() {
		copyAction = new Action() {
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				String result = ""; //$NON-NLS-1$
				String[] columnHeaders = getColumnHeaders();
				for (int i = 0; i < columnHeaders.length; i++)
					result += columnHeaders[i] + ","; //$NON-NLS-1$
				result += "\n\n"; //$NON-NLS-1$

				ITableLabelProvider labelProvider = (ITableLabelProvider) viewer.getLabelProvider();
				for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
					Object selectedItem = iterator.next();
					for (int i = 0; i < columnHeaders.length; i++)
						result += labelProvider.getColumnText(selectedItem, i) + ","; //$NON-NLS-1$
					result += "\n"; //$NON-NLS-1$
				}
				clipboard.setContents(new Object[] {result}, new Transfer[] {TextTransfer.getInstance()});
			}
		};
		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, copyAction);

		selectAllAction = new Action() {
			public void run() {
				tableTree.selectAll();
				// force viewer selection change
				viewer.setSelection(viewer.getSelection());
			}
		};
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.SELECT_ALL, selectAllAction);
	}

	protected ISelectionChangedListener getTableListener() {
		return new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				IStructuredSelection selection = (IStructuredSelection) e.getSelection();
				copyAction.setEnabled(!selection.isEmpty());
				if (selection.size() == 1) {
					String message = getStatusLineMessage(selection.getFirstElement());
					getViewSite().getActionBars().getStatusLineManager().setMessage(message);
				}
				totalTable.removeAll();
				updateTotals();
			}
		};
	}

	public void updateTotals() {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		TableItem item = new TableItem(totalTable, 0);
		item.setText(computeTotalLine(selection.iterator()));
	}

	public ISelection getSelection() {
		return viewer.getSelection();
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		// do nothing
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		// do nothing
	}

	public void setSelection(ISelection selection) {
		// do nothing
	}

	public void setFocus() {
		if (tableTree != null)
			tableTree.setFocus();
	}
}