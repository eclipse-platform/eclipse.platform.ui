/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Ziegler - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.internal.text.TextMatcher;
import org.eclipse.jface.text.AbstractFilteredStructuredViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * A simple control that provides a text widget and a table viewer. The contents
 * of the text widget are used to drive a TextMatcher that is on the viewer.
 *
 * @since 3.135
 */
public class FilteredTable extends AbstractFilteredStructuredViewer {

	/**
	 * Default time for refresh job delay in ms
	 */
	private static final long DEFAULT_REFRESH_TIME = 200;

	private TableViewer tableViewer;
	private TextMatcher matcher;

	public FilteredTable(Composite parent, int style) {
		this(parent, style, DEFAULT_REFRESH_TIME);
	}

	public FilteredTable(Composite parent, int style, long refreshTime) {
		super(parent, style, refreshTime);
		init(style);
	}

	@Override
	protected void createControl(Composite parent, int treeStyle) {
		super.createControl(parent, treeStyle);

		Composite tableComposite = new Composite(this, SWT.NONE);
		GridLayout treeCompositeLayout = new GridLayout();
		treeCompositeLayout.marginHeight = 0;
		treeCompositeLayout.marginWidth = 0;
		tableComposite.setLayout(treeCompositeLayout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		tableComposite.setLayoutData(data);
		createTableControl(tableComposite, treeStyle);
	}

	@Override
	protected void createFilterText(Composite parent) {
		super.createFilterText(parent);
		filterText.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				String filterTextString = filterText.getText();
				if (filterTextString.isEmpty() || filterTextString.equals(initialText)) {
					e.result = initialText;
				} else {
					e.result = NLS.bind(WorkbenchMessages.FilteredTree_AccessibleListenerFiltered,
							new String[] { filterTextString, String.valueOf(getFilteredItemsCount()) });
				}
			}

			private int getFilteredItemsCount() {
				return getViewer().getTable().getItemCount();
			}
		});

		filterText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// on a CR we want to transfer focus to the list
				boolean hasItems = getViewer().getTable().getItemCount() > 0;
				if (hasItems && e.keyCode == SWT.ARROW_DOWN) {
					getViewer().getTable().setFocus();
					return;
				}
			}
		});

		// enter key set focus to tree
		filterText.addTraverseListener(e -> {
			if (isQuickSelectionMode()) {
				return;
			}
			if (e.detail == SWT.TRAVERSE_RETURN) {
				e.doit = false;
				updateTableSelection(true);
			}
		});
	}

	/**
	 * Updates the selection in the tree, based on the filter text.
	 *
	 * @param setFocus {@code true} if the focus should be set on the tree,
	 *                 {@code false} otherwise
	 */
	private void updateTableSelection(boolean setFocus) {
		Table table = tableViewer.getTable();
		if (table.getItemCount() != 0) {
			// if the initial filter text hasn't changed, do not try
			// to match
			boolean hasFocus = setFocus ? table.setFocus() : true;
			boolean textChanged = !getInitialText().equals(filterText.getText().trim());
			if (hasFocus && textChanged && filterText.getText().trim().length() > 0) {
				TableItem item;
				if (table.getSelectionCount() > 0) {
					item = getFirstMatchingItem(table.getSelection());
				} else {
					item = getFirstMatchingItem(table.getItems());
				}
				if (item != null) {
					table.setSelection(new TableItem[] { item });
					tableViewer.setSelection(tableViewer.getSelection(), true);
				}
			}
		}
	}

	/**
	 * Return the first item in the tree that matches the filter pattern.
	 *
	 * @return the first matching TreeItem
	 */
	private TableItem getFirstMatchingItem(TableItem[] items) {
		for (TableItem item : items) {
			if (matcher == null) {
				return item;
			}

			ILabelProvider labelProvider = (ILabelProvider) getViewer().getLabelProvider();
			if (matcher.match(labelProvider.getText(item.getData()))) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Creates and set up the table and table viewer. This method calls
	 * {@link #doCreateTableViewer(Composite, int)} to create the table viewer.
	 * Subclasses should override {@link #doCreateTableViewer(Composite, int)}
	 * instead of overriding this method.
	 *
	 * @param parent parent <code>Composite</code>
	 * @param style  SWT style bits used to create the table
	 * @return the table
	 */
	protected Table createTableControl(Composite parent, int style) {
		tableViewer = doCreateTableViewer(parent, style);
		tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (matcher == null) {
					return true;
				}
				ILabelProvider labelProvider = (ILabelProvider) tableViewer.getLabelProvider();
				return matcher.match(labelProvider.getText(element));
			}
		});
		return tableViewer.getTable();
	}

	/**
	 * Creates the table viewer. Subclasses may override.
	 *
	 * @param parent the parent composite
	 * @param style  SWT style bits used to create the table viewer
	 * @return the table viewer
	 */
	protected TableViewer doCreateTableViewer(Composite parent, int style) {
		return new TableViewer(parent, style);
	}

	@Override
	protected WorkbenchJob doCreateRefreshJob() {
		return new WorkbenchJob("Refresh Filter") {//$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (getViewer().getControl().isDisposed()) {
					return Status.CANCEL_STATUS;
				}

				String text = getFilterString();
				if (text == null) {
					return Status.OK_STATUS;
				}

				boolean initial = initialText != null && initialText.equals(text);
				if (initial) {
					matcher = null;
				} else if (text != null) {
					matcher = new TextMatcher(text + '*', true, false);
				}

				tableViewer.refresh(true);

				if (isQuickSelectionMode()) {
					updateTableSelection(false);
				}
				return Status.OK_STATUS;
			}
		};
	}

	@Override
	protected boolean isShowFilterControls() {
		return PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.SHOW_FILTERED_TEXTS);
	}

	@Override
	public TableViewer getViewer() {
		return tableViewer;
	}
}
