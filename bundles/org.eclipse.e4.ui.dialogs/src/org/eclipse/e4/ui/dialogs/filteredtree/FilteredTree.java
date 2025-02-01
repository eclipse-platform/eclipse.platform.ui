/*******************************************************************************
 * Copyright (c) 2014, 2025 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - initial API and implementation based on org.eclipse.ui.dialogs.FilteredTree
 *******************************************************************************/
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
import org.eclipse.jface.viewers.TreeViewer;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Based on org.eclipse.ui.dialogs.FilteredTree.
 *
 * @since 1.2
 */
public class FilteredTree extends AbstractFilteredViewerComposite<PatternFilter> {

	/**
	 * The viewer for the filtered tree. This value should never be
	 * <code>null</code> after the widget creation methods are complete.
	 */
	private TreeViewer treeViewer;

	/**
	 * The job used to refresh the tree.
	 */
	private Job refreshJob;

	private Composite treeComposite;

	/**
	 * Maximum time spent expanding the tree after the filter text has been
	 * updated (this is only used if we were able to at least expand the visible
	 * nodes)
	 */
	private static final long SOFT_MAX_EXPAND_TIME = 200;

	/**
	 * Default time for refresh job delay in ms
	 */
	private static final long DEFAULT_REFRESH_TIME = 200;

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param parent           the parent <code>Composite</code>
	 * @param treeStyle        the style bits for the <code>Tree</code>
	 * @param filter           the filter to be used
	 * @param refreshDelayTime refresh delay in ms, the time to expand the tree
	 *                         after debounce
	 * @since 1.5
	 */
	public FilteredTree(Composite parent, int treeStyle, PatternFilter filter, long refreshDelayTime) {
		super(parent, SWT.NONE, refreshDelayTime);
		this.parent = getParent();
		init(treeStyle, filter);
	}

	/**
	 * Calls {@link #FilteredTree(Composite, int, PatternFilter, long)} with a
	 * default refresh time
	 */
	public FilteredTree(Composite parent, int treeStyle, PatternFilter filter) {
		this(parent, treeStyle, filter, DEFAULT_REFRESH_TIME);
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
	protected FilteredTree(Composite parent) {
		super(parent, SWT.NONE, DEFAULT_REFRESH_TIME);
	}

	@Override
	protected void init(int treeStyle, PatternFilter filter) {
		setShowFilterControls(true);
		super.init(treeStyle, filter);
		createRefreshJob();
		setInitialText(E4DialogMessages.FilteredTree_FilterMessage);
	}

	@Override
	protected void createControl(Composite parent, int treeStyle) {
		super.createControl(parent, treeStyle);

		treeComposite = new Composite(this, SWT.NONE);
		GridLayout treeCompositeLayout = new GridLayout();
		treeCompositeLayout.marginHeight = 0;
		treeCompositeLayout.marginWidth = 0;
		treeComposite.setLayout(treeCompositeLayout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		treeComposite.setLayoutData(data);
		createTreeControl(treeComposite, treeStyle);
	}


	/**
	 * Creates and set up the tree and tree viewer. This method calls
	 * {@link #doCreateTreeViewer(Composite, int)} to create the tree viewer.
	 * Subclasses should override {@link #doCreateTreeViewer(Composite, int)}
	 * instead of overriding this method.
	 *
	 * @param parent
	 *            parent <code>Composite</code>
	 * @param style
	 *            SWT style bits used to create the tree
	 * @return the tree
	 */
	protected Control createTreeControl(Composite parent, int style) {
		treeViewer = doCreateTreeViewer(parent, style);
		treeViewer.setUseHashlookup(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		treeViewer.getControl().setLayoutData(data);
		treeViewer.getControl().addDisposeListener(e -> refreshJob.cancel());
		if (treeViewer instanceof NotifyingTreeViewer) {
			getPatternFilter().setUseCache(true);
		}
		treeViewer.addFilter(getPatternFilter());
		return treeViewer.getControl();
	}

	/**
	 * Creates the tree viewer. Subclasses may override.
	 *
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            SWT style bits used to create the tree viewer
	 * @return the tree viewer
	 *
	 * @since 3.3
	 */
	protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
		return new NotifyingTreeViewer(parent, style);
	}

	/**
	 * Return the first item in the tree that matches the filter pattern.
	 *
	 * @return the first matching TreeItem
	 */
	private TreeItem getFirstMatchingItem(TreeItem[] items) {
		for (TreeItem item : items) {
			if (getPatternFilter().isLeafMatch(treeViewer, item.getData())
					&& getPatternFilter().isElementSelectable(item.getData())) {
				return item;
			}
			TreeItem treeItem = getFirstMatchingItem(item.getItems());
			if (treeItem != null) {
				return treeItem;
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
	 * Creates a workbench job that will refresh the tree based on the current
	 * filter text. Subclasses may override.
	 *
	 * @return a workbench job that can be scheduled to refresh the tree
	 *
	 * @since 3.4
	 */
	protected BasicUIJob doCreateRefreshJob() {
		return new BasicUIJob("Refresh Filter", getDisplay()) {//$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (treeViewer.getControl().isDisposed()) {
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

				Control redrawFalseControl = treeComposite != null ? treeComposite : treeViewer.getControl();
				try {
					// don't want the user to see updates that will be made to
					// the tree
					// we are setting redraw(false) on the composite to avoid
					// dancing scrollbar
					redrawFalseControl.setRedraw(false);
					if (!narrowingDown) {
						// collapse all
						TreeItem[] is = treeViewer.getTree().getItems();
						for (TreeItem item : is) {
							if (item.getExpanded()) {
								treeViewer.setExpandedState(item.getData(), false);
							}
						}
					}
					treeViewer.refresh(true);

					if (text.length() > 0 && !initial) {
						/*
						 * Expand elements one at a time. After each is
						 * expanded, check to see if the filter text has been
						 * modified. If it has, then cancel the refresh job so
						 * the user doesn't have to endure expansion of all the
						 * nodes.
						 */
						TreeItem[] items = getViewer().getTree().getItems();
						int treeHeight = getViewer().getTree().getBounds().height;
						int numVisibleItems = treeHeight / getViewer().getTree().getItemHeight();
						long stopTime = SOFT_MAX_EXPAND_TIME + System.currentTimeMillis();

						updateToolbar(true);

						if (items.length > 0
								&& recursiveExpand(items, monitor, stopTime, new int[] { numVisibleItems })) {
							return Status.CANCEL_STATUS;
						}
					} else {
						updateToolbar(false);
					}
				} finally {
					// done updating the tree - set redraw back to true
					TreeItem[] items = getViewer().getTree().getItems();
					if (items.length > 0 && getViewer().getTree().getSelectionCount() == 0) {
						treeViewer.getTree().setTopItem(items[0]);
					}
					redrawFalseControl.setRedraw(true);
				}
				return Status.OK_STATUS;
			}

			/**
			 * Returns true if the job should be canceled (because of timeout or
			 * actual cancellation).
			 *
			 * @return true if canceled
			 */
			private boolean recursiveExpand(TreeItem[] items, IProgressMonitor monitor, long cancelTime,
					int[] numItemsLeft) {
				boolean canceled = false;
				for (int i = 0; !canceled && i < items.length; i++) {
					TreeItem item = items[i];
					boolean visible = numItemsLeft[0]-- >= 0;
					if (monitor.isCanceled() || (!visible && System.currentTimeMillis() > cancelTime)) {
						canceled = true;
					} else {
						Object itemData = item.getData();
						if (itemData != null) {
							if (!item.getExpanded()) {
								// do the expansion through the viewer so that
								// it can refresh children appropriately.
								treeViewer.setExpandedState(itemData, true);
							}
							TreeItem[] children = item.getItems();
							if (items.length > 0) {
								canceled = recursiveExpand(children, monitor, cancelTime, numItemsLeft);
							}
						}
					}
				}
				return canceled;
			}

		};
	}

	protected void updateToolbar(boolean visible) {
		// nothing to do
	}

	/**
	 * Creates the filter text and adds listeners. This method calls
	 * {@link #doCreateFilterText(Composite)} to create the text control.
	 * Subclasses should override {@link #doCreateFilterText(Composite)} instead
	 * of overriding this method.
	 *
	 * @param parent
	 *            <code>Composite</code> of the filter text
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
					e.result = NLS.bind(E4DialogMessages.FilteredTree_AccessibleListenerFiltered,
							new String[] { filterTextString, String.valueOf(getFilteredItemsCount()) });
				}
			}

			/**
			 * Return the number of filtered items
			 *
			 * @return int
			 */
			private int getFilteredItemsCount() {
				int total = 0;
				TreeItem[] items = getViewer().getTree().getItems();
				for (TreeItem item : items) {
					total += itemCount(item);

				}
				return total;
			}

			/**
			 * Return the count of treeItem and it's children to infinite depth.
			 *
			 * @return int
			 */
			private int itemCount(TreeItem treeItem) {
				int count = 1;
				TreeItem[] children = treeItem.getItems();
				for (TreeItem element : children) {
					count += itemCount(element);

				}
				return count;
			}
		});

		filterText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// on a CR we want to transfer focus to the list
				boolean hasItems = getViewer().getTree().getItemCount() > 0;
				if (hasItems && e.keyCode == SWT.ARROW_DOWN) {
					treeViewer.getTree().setFocus();
					return;
				}
			}
		});

		// enter key set focus to tree
		filterText.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				e.doit = false;
				if (getViewer().getTree().getItemCount() == 0) {
					Display.getCurrent().beep();
				} else {
					// if the initial filter text hasn't changed, do not try
					// to match
					boolean hasFocus = getViewer().getTree().setFocus();
					boolean textChanged = !getInitialText().equals(filterText.getText().trim());
					if (hasFocus && textChanged && filterText.getText().trim().length() > 0) {
						Tree tree = getViewer().getTree();
						TreeItem item;
						if (tree.getSelectionCount() > 0) {
							item = getFirstMatchingItem(tree.getSelection());
						} else {
							item = getFirstMatchingItem(tree.getItems());
						}
						if (item != null) {
							tree.setSelection(new TreeItem[] { item });
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

	private String previousFilterText;

	private boolean narrowingDown;

	@Override
	protected void textChanged() {
		narrowingDown = previousFilterText == null
				|| previousFilterText.equals(E4DialogMessages.FilteredTree_FilterMessage)
				|| getFilterString().startsWith(previousFilterText);
		previousFilterText = getFilterString();
		// cancel currently running job first, to prevent unnecessary redraw
		refreshJob.cancel();
		refreshJob.schedule(getRefreshJobDelay());
	}

	/**
	 * Set the background for the widgets that support the filter text area.
	 *
	 * @param background
	 *            background <code>Color</code> to set
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
	public TreeViewer getViewer() {
		return treeViewer;
	}

	/**
	 * Return a bold font if the given element matches the given pattern.
	 * Clients can opt to call this method from a Viewer's label provider to get
	 * a bold font for which to highlight the given element in the tree.
	 *
	 * @param element
	 *            element for which a match should be determined
	 * @param tree
	 *            FilteredTree in which the element resides
	 * @param filter
	 *            PatternFilter which determines a match
	 *
	 * @return bold font
	 */
	public static Font getBoldFont(Object element, FilteredTree tree, PatternFilter filter) {
		String filterText = tree.getFilterString();

		if (filterText == null) {
			return null;
		}

		// Do nothing if it's empty string
		String initialText = tree.getInitialText();
		if (!filterText.isEmpty() && !filterText.equals(initialText)) {
			if (tree.getPatternFilter() != filter) {
				boolean initial = initialText != null && initialText.equals(filterText);
				if (initial) {
					filter.setPattern(null);
				} else if (filterText != null) {
					filter.setPattern(filterText);
				}
			}
			if (filter.isElementVisible(tree.getViewer(), element) && filter.isLeafMatch(tree.getViewer(), element)) {
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
	 * Custom tree viewer subclass that clears the caches in patternFilter on
	 * any change to the tree. See bug 187200.
	 *
	 * @since 3.3
	 */
	class NotifyingTreeViewer extends TreeViewer {

		public NotifyingTreeViewer(Composite parent, int style) {
			super(parent, style);
		}

		@Override
		public void add(Object parentElementOrTreePath, Object childElement) {
			getPatternFilter().clearCaches();
			super.add(parentElementOrTreePath, childElement);
		}

		@Override
		public void add(Object parentElementOrTreePath, Object... childElements) {
			getPatternFilter().clearCaches();
			super.add(parentElementOrTreePath, childElements);
		}

		@Override
		protected void inputChanged(Object input, Object oldInput) {
			getPatternFilter().clearCaches();
			super.inputChanged(input, oldInput);
		}

		@Override
		public void insert(Object parentElementOrTreePath, Object element, int position) {
			getPatternFilter().clearCaches();
			super.insert(parentElementOrTreePath, element, position);
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
		public void remove(Object elementsOrTreePaths) {
			getPatternFilter().clearCaches();
			super.remove(elementsOrTreePaths);
		}

		@Override
		public void remove(Object parent, Object... elements) {
			getPatternFilter().clearCaches();
			super.remove(parent, elements);
		}

		@Override
		public void remove(Object... elementsOrTreePaths) {
			getPatternFilter().clearCaches();
			super.remove(elementsOrTreePaths);
		}

		@Override
		public void replace(Object parentElementOrTreePath, int index, Object element) {
			getPatternFilter().clearCaches();
			super.replace(parentElementOrTreePath, index, element);
		}

		@Override
		public void setChildCount(Object elementOrTreePath, int count) {
			getPatternFilter().clearCaches();
			super.setChildCount(elementOrTreePath, count);
		}

		@Override
		public void setContentProvider(IContentProvider provider) {
			getPatternFilter().clearCaches();
			super.setContentProvider(provider);
		}

		@Override
		public void setHasChildren(Object elementOrTreePath, boolean hasChildren) {
			getPatternFilter().clearCaches();
			super.setHasChildren(elementOrTreePath, hasChildren);
		}

	}

}
