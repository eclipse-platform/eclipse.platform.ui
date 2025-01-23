/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
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
package org.eclipse.e4.ui.dialogs.filteredtree;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.ui.dialogs.textbundles.E4DialogMessages;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.AbstractFilteredViewerComposite;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * A simple control that provides a text widget and a table viewer. The contents
 * of the text widget are used to drive a TextMatcher that is on the viewer.
 *
 * @since 1.6
 */
public class FilteredTable extends AbstractFilteredViewerComposite<PatternFilter> {

	/**
	 * The viewer for the filtered table. This value should never be {@code null}
	 * after the widget creation methods are complete.
	 */
	private TableViewer tableViewer;

	/**
	 * The job used to refresh the table.
	 */
	private Job refreshJob;

	private Composite tableComposite;

	/**
	 * Default time for refresh job delay in ms
	 */
	private static final long DEFAULT_REFRESH_TIME = 200;

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param parent           the parent {@code Composite}
	 * @param tableStyle       the style bits for the {@code table}
	 * @param filter           the filter to be used
	 * @param refreshDelayTime refresh delay in ms, the time to expand the table
	 *                         after debounce
	 */
	public FilteredTable(Composite parent, int tableStyle, PatternFilter filter, long refreshDelayTime) {
		super(parent, SWT.NONE, refreshDelayTime);
		this.parent = getParent();
		init(tableStyle, filter);
	}

	/**
	 * Calls {@link #FilteredTable(Composite, int, PatternFilter, long)} with a
	 * default refresh time
	 */
	public FilteredTable(Composite parent, int tableStyle, PatternFilter filter) {
		this(parent, tableStyle, filter, DEFAULT_REFRESH_TIME);
	}

	/**
	 * Create a new instance of the receiver. Subclasses that wish to override
	 * the default creation behavior may use this constructor, but must ensure
	 * that the <code>init(composite, int, PatternFilter)</code> method is
	 * called in the overriding constructor.
	 *
	 * @param parent
	 *            the parent <code>Composite</code>
	 * @see #init(int, PatternFilter)
	 */
	protected FilteredTable(Composite parent) {
		super(parent, SWT.NONE, DEFAULT_REFRESH_TIME);
	}

	@Override
	protected void init(int tableStyle, PatternFilter filter) {
		setShowFilterControls(true);
		super.init(tableStyle, filter);
		createRefreshJob();
		setInitialText(E4DialogMessages.FilteredTable_FilterMessage);
	}

	@Override
	protected void createControl(Composite parent, int tableStyle) {
		super.createControl(parent, tableStyle);

		tableComposite = new Composite(this, SWT.NONE);
		GridLayout tableCompositeLayout = new GridLayout();
		tableCompositeLayout.marginHeight = 0;
		tableCompositeLayout.marginWidth = 0;
		tableComposite.setLayout(tableCompositeLayout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		tableComposite.setLayoutData(data);
		createTableControl(tableComposite, tableStyle);
	}

	/**
	 * Creates and set up the table and table viewer. This method calls
	 * {@link #doCreateTableViewer(Composite, int)} to create the table viewer.
	 * Subclasses should override {@link #doCreateTableViewer(Composite, int)}
	 * instead of overriding this method.
	 *
	 * @param parent parent {@code Composite}
	 * @param style  SWT style bits used to create the table
	 * @return the table
	 */
	protected Control createTableControl(Composite parent, int style) {
		tableViewer = doCreateTableViewer(parent, style);
		tableViewer.setUseHashlookup(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		tableViewer.getControl().setLayoutData(data);
		tableViewer.getControl().addDisposeListener(e -> refreshJob.cancel());
		if (tableViewer instanceof NotifyingTableViewer) {
			getPatternFilter().setUseCache(true);
		}
		tableViewer.addFilter(getPatternFilter());
		return tableViewer.getControl();
	}

	/**
	 * Creates the table viewer. Subclasses may override.
	 *
	 * @param parent the parent composite
	 * @param style  SWT style bits used to create the table viewer
	 * @return the table viewer
	 */
	protected TableViewer doCreateTableViewer(Composite parent, int style) {
		return new NotifyingTableViewer(parent, style);
	}

	/**
	 * Return the first item in the table that matches the filter pattern.
	 *
	 * @return the first matching TableItem
	 */
	private TableItem getFirstMatchingItem(TableItem[] items) {
		for (TableItem item : items) {
			if (getPatternFilter().isLeafMatch(tableViewer, item.getData())
					&& getPatternFilter().isElementSelectable(item.getData())) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Create the refresh job for the receiver.
	 */
	private void createRefreshJob() {
		refreshJob = doCreateRefreshJob();
		refreshJob.setSystem(true);
	}

	/**
	 * Creates a workbench job that will refresh the table based on the current
	 * filter text. Subclasses may override.
	 *
	 * @return a job that can be scheduled to refresh the table
	 */
	protected Job doCreateRefreshJob() {
		return new BasicUIJob("Refresh Filter", getDisplay()) {//$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (tableViewer.getControl().isDisposed()) {
					return Status.CANCEL_STATUS;
				}

				String text = getFilterString();
				if (text == null) {
					return Status.OK_STATUS;
				}

				boolean initial = initialText != null && initialText.equals(text);
				if (initial) {
					getPatternFilter().setPattern(null);
				} else if (text != null) {
					getPatternFilter().setPattern(text);
				}

				tableViewer.refresh(true);

				return Status.OK_STATUS;
			}
		};
	}

	/**
	 * Creates the filter text and adds listeners. This method calls
	 * {@link #doCreateFilterText(Composite)} to create the text control. Subclasses
	 * should override {@link #doCreateFilterText(Composite)} instead of overriding
	 * this method.
	 *
	 * @param parent <code>Composite</code> of the filter text
	 */
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
					e.result = NLS.bind(E4DialogMessages.FilteredTable_AccessibleListenerFiltered,
							new String[] { filterTextString, String.valueOf(getFilteredItemsCount()) });
				}
			}

			/**
			 * Return the number of filtered items
			 *
			 * @return int
			 */
			private int getFilteredItemsCount() {
				return getViewer().getTable().getItems().length;
			}
		});

		filterText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// on a CR we want to transfer focus to the list
				boolean hasItems = getViewer().getTable().getItemCount() > 0;
				if (hasItems && e.keyCode == SWT.ARROW_DOWN) {
					tableViewer.getTable().setFocus();
					return;
				}
			}
		});

		// enter key set focus to table
		filterText.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				e.doit = false;
				if (getViewer().getTable().getItemCount() == 0) {
					Display.getCurrent().beep();
				} else {
					// if the initial filter text hasn't changed, do not try
					// to match
					boolean hasFocus = getViewer().getTable().setFocus();
					boolean textChanged = !getInitialText().equals(filterText.getText().trim());
					if (hasFocus && textChanged && filterText.getText().trim().length() > 0) {
						Table table = getViewer().getTable();
						TableItem item;
						if (table.getSelectionCount() > 0) {
							item = getFirstMatchingItem(table.getSelection());
						} else {
							item = getFirstMatchingItem(table.getItems());
						}
						if (item != null) {
							table.setSelection(new TableItem[] { item });
							ISelection sel = getViewer().getSelection();
							getViewer().setSelection(sel, true);
						}
					}
				}
			}
		});
	}

	@Override
	protected Text doCreateFilterText(Composite parent) {
		return new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
	}

	@Override
	protected void textChanged() {
		// cancel currently running job first, to prevent unnecessary redraw
		refreshJob.cancel();
		refreshJob.schedule(getRefreshJobDelay());
	}

	/**
	 * Set the background for the widgets that support the filter text area.
	 *
	 * @param background background <code>Color</code> to set
	 */
	@Override
	public void setBackground(Color background) {
		super.setBackground(background);
		if (filterComposite != null) {
			filterComposite.setBackground(background);
		}
	}

	@Override
	public final PatternFilter getPatternFilter() {
		return (PatternFilter) super.getPatternFilter();
	}

	@Override
	public TableViewer getViewer() {
		return tableViewer;
	}

	/**
	 * Return a bold font if the given element matches the given pattern. Clients
	 * can opt to call this method from a Viewer's label provider to get a bold font
	 * for which to highlight the given element in the table.
	 *
	 * @param element element for which a match should be determined
	 * @param table   FilteredTable in which the element resides
	 * @param filter  PatternFilter which determines a match
	 *
	 * @return bold font
	 */
	public static Font getBoldFont(Object element, FilteredTable table, PatternFilter filter) {
		String filterText = table.getFilterString();

		if (filterText == null) {
			return null;
		}

		// Do nothing if it's empty string
		String initialText = table.getInitialText();
		if (!filterText.isEmpty() && !filterText.equals(initialText)) {
			if (table.getPatternFilter() != filter) {
				boolean initial = initialText != null && initialText.equals(filterText);
				if (initial) {
					filter.setPattern(null);
				} else if (filterText != null) {
					filter.setPattern(filterText);
				}
			}
			if (filter.isElementVisible(table.getViewer(), element) && filter.isLeafMatch(table.getViewer(), element)) {
				return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
			}
		}
		return null;
	}

	public boolean isShowFilterControls() {
		return showFilterControls;
	}

	public void setShowFilterControls(boolean showFilterControls) {
		this.showFilterControls = showFilterControls;
		if (filterComposite != null) {
			Object filterCompositeLayoutData = filterComposite.getLayoutData();
			if (filterCompositeLayoutData instanceof GridData) {
				((GridData) filterCompositeLayoutData).exclude = !isShowFilterControls();
			} else if (filterCompositeLayoutData instanceof RowData) {
				((RowData) filterCompositeLayoutData).exclude = !isShowFilterControls();
			}
			filterComposite.setVisible(isShowFilterControls());
			layout();
		}
	}

	/**
	 * Custom table viewer subclass that clears the caches in patternFilter on any
	 * change to the table. See bug 187200.
	 */
	class NotifyingTableViewer extends TableViewer {

		public NotifyingTableViewer(Composite parent, int style) {
			super(parent, style);
		}

		@Override
		public void add(Object element) {
			getPatternFilter().clearCaches();
			super.add(element);
		}

		@Override
		public void add(Object[] elements) {
			getPatternFilter().clearCaches();
			super.add(elements);
		}

		@Override
		protected void inputChanged(Object input, Object oldInput) {
			getPatternFilter().clearCaches();
			super.inputChanged(input, oldInput);
		}

		@Override
		public void insert(Object element, int position) {
			getPatternFilter().clearCaches();
			super.insert(element, position);
		}

		@Override
		public void refresh() {
			getPatternFilter().clearCaches();
			super.refresh();
		}

		@Override
		public void refresh(boolean updateLabels) {
			getPatternFilter().clearCaches();
			super.refresh(updateLabels);
		}

		@Override
		public void refresh(Object element) {
			getPatternFilter().clearCaches();
			super.refresh(element);
		}

		@Override
		public void refresh(Object element, boolean updateLabels) {
			getPatternFilter().clearCaches();
			super.refresh(element, updateLabels);
		}

		@Override
		public void remove(Object element) {
			getPatternFilter().clearCaches();
			super.remove(element);
		}

		@Override
		public void remove(Object[] elements) {
			getPatternFilter().clearCaches();
			super.remove(elements);
		}

		@Override
		public void replace(Object element, int index) {
			getPatternFilter().clearCaches();
			super.replace(element, index);
		}

		@Override
		public void setContentProvider(IContentProvider provider) {
			getPatternFilter().clearCaches();
			super.setContentProvider(provider);
		}
	}
}
