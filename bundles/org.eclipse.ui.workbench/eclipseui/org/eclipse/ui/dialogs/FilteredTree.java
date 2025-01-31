/*******************************************************************************
 * Copyright (c) 2004, 2025 IBM Corporation and others.
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
 *     Jacek Pospychala - bug 187762
 *     Mohamed Tarief - tarief@eg.ibm.com - IBM - Bug 174481
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422040
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ToolBarManager;
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * A simple control that provides a text widget and a tree viewer. The contents
 * of the text widget are used to drive a PatternFilter that is on the viewer.
 *
 * @see org.eclipse.ui.dialogs.PatternFilter
 * @since 3.2
 */
public class FilteredTree extends AbstractFilteredViewerComposite<PatternFilter> {

	/**
	 * <p>
	 * <strong>Note:</strong> As of 4.13 not used anymore
	 * </p>
	 *
	 * @deprecated As of 4.13 not used anymore
	 */
	@Deprecated(since = "2025-03", forRemoval = true)
	protected ToolBarManager filterToolBar;

	/**
	 * <p>
	 * <strong>Note:</strong> As of 4.13 not used anymore
	 * </p>
	 *
	 * @since 3.5
	 * @deprecated As of 4.13 not used anymore
	 */
	@Deprecated(since = "2025-03", forRemoval = true)
	protected Control clearButtonControl;

	/**
	 * The viewer for the filtered tree. This value should never be
	 * <code>null</code> after the widget creation methods are complete.
	 */
	protected TreeViewer treeViewer;

	/**
	 * The job used to refresh the tree.
	 */
	private Job refreshJob;

	/**
	 * @since 3.3
	 */
	protected Composite treeComposite;

	/**
	 * Tells whether this filtered tree is used to make quick selections. In this
	 * mode the first match in the tree is automatically selected while filtering
	 * and the 'Enter' key is not used to move the focus to the tree.
	 *
	 * @since 3.105
	 */
	private boolean quickSelectionMode = false;

	/**
	 * Maximum time spent expanding the tree after the filter text has been updated
	 * (this is only used if we were able to at least expand the visible nodes)
	 */
	private static final long SOFT_MAX_EXPAND_TIME = 200;

	/**
	 * Default time for refresh job delay in ms
	 */
	private static final long DEFAULT_REFRESH_TIME = 200;

	/**
	 * Create a new instance of the receiver. Subclasses that wish to override the
	 * default creation behavior may use this constructor, but must ensure that the
	 * <code>init(composite, int, PatternFilter)</code> method is called in the
	 * overriding constructor. *
	 * <p>
	 *
	 * <b>WARNING:</b> Passing false as parameter for useFastHashLookup results in a
	 * slow performing tree and should not be used if the underlying data model uses
	 * a stable and correct hashCode and equals implementation.
	 *
	 * </p>
	 *
	 * @param parent            the parent <code>Composite</code>
	 * @param useNewLook        ignored, keep for API compliance
	 * @param useFastHashLookup true, if tree should use fast hashlookup, false, if
	 *                          the tree should be slow but working for data with
	 *                          mutable or broken hashcode implementation. Only used
	 *                          if treeViewer is already initialized
	 * @since 3.116
	 */
	public FilteredTree(Composite parent, boolean useNewLook, boolean useFastHashLookup) {
		super(parent, SWT.NONE, DEFAULT_REFRESH_TIME);
		this.parent = parent;
		if (treeViewer != null) {
			treeViewer.setUseHashlookup(useFastHashLookup);
		}
	}

	/**
	 * Calls
	 * {@link #FilteredTree(Composite, int, PatternFilter, boolean, boolean, long)}
	 * with a default refresh time
	 *
	 * @since 3.116
	 */
	public FilteredTree(Composite parent, int treeStyle, PatternFilter filter, boolean useNewLook,
			boolean useFastHashLookup) {
		this(parent, treeStyle, filter, useNewLook, useFastHashLookup, DEFAULT_REFRESH_TIME);
	}

	/**
	 * Create a new instance of the receiver.
	 *
	 * <p>
	 *
	 * <b>WARNING:</b> Passing false as parameter for useFastHashLookup results in a
	 * slow performing tree and should not be used if the underlying data model uses
	 * a stable and correct hashCode and equals implementation.
	 *
	 * </p>
	 *
	 * @param parent                  the parent <code>Composite</code>
	 * @param treeStyle               the style bits for the <code>Tree</code>
	 * @param filter                  the filter to be used
	 * @param useNewLook              ignored, keep for API compliance
	 * @param useFastHashLookup       true, if tree should use fast hash lookup,
	 *                                false, if the tree should be slow but working
	 *                                for data with mutable or broken hashcode
	 *                                implementation. Only used if treeViewer is
	 *                                already initialized
	 * @param refreshJobDelayInMillis refresh delay in ms, the time to expand the
	 *                                tree after debounce
	 * @since 3.132
	 */
	public FilteredTree(Composite parent, int treeStyle, PatternFilter filter, boolean useNewLook,
			boolean useFastHashLookup, long refreshJobDelayInMillis) {
		super(parent, SWT.NONE, refreshJobDelayInMillis);
		this.parent = parent;
		init(treeStyle, filter);
		if (treeViewer != null) {
			treeViewer.setUseHashlookup(useFastHashLookup);
		}
	}

	/**
	 * Create a new instance of the receiver. Subclasses that wish to override the
	 * default creation behavior may use this constructor, but must ensure that the
	 * <code>init(composite, int, PatternFilter)</code> method is called in the
	 * overriding constructor.
	 *
	 * <p>
	 * <b>WARNING:</b> Using this constructor results in a slow performing tree and
	 * should not be used if the underlying data model uses a stable and correct
	 * hashCode and equals implementation. Prefer the usage of
	 * {@link #FilteredTree(Composite, boolean, boolean)} if possible.
	 * </p>
	 *
	 *
	 * @param parent the parent <code>Composite</code>
	 * @see #init(int, PatternFilter)
	 *
	 * @since 3.3
	 * @deprecated As of 3.116, replaced by
	 *             {@link #FilteredTree(Composite, boolean, boolean)}
	 */
	@Deprecated
	protected FilteredTree(Composite parent) {
		super(parent, SWT.NONE, DEFAULT_REFRESH_TIME);
		this.parent = parent;
	}

	/**
	 * Create a new instance of the receiver. Subclasses that wish to override the
	 * default creation behavior may use this constructor, but must ensure that the
	 * <code>init(composite, int, PatternFilter)</code> method is called in the
	 * overriding constructor. *
	 *
	 * <p>
	 * <b>WARNING:</b> Using this constructor results in a slow performing tree and
	 * should not be used if the underlying data model uses a stable and correct
	 * hashCode and equals implementation. Prefer the usage of
	 * {@link #FilteredTree(Composite, int, PatternFilter, boolean, boolean)} if
	 * possible.
	 * </p>
	 *
	 * @param parent     the parent <code>Composite</code>
	 * @param useNewLook ignored, look introduced in 3.5 is always used
	 * @see #init(int, PatternFilter)
	 *
	 * @since 3.5
	 *
	 * @deprecated As of 3.116, replaced by
	 *             {@link #FilteredTree(Composite, int, PatternFilter, boolean, boolean)}
	 */
	@Deprecated
	protected FilteredTree(Composite parent, boolean useNewLook) {
		this(parent);
	}

	/**
	 * Create a new instance of the receiver.
	 * <p>
	 * <b>WARNING:</b> Using this constructor results in a slow performing tree and
	 * should not be used if the underlying data model uses a stable and correct
	 * hashCode and equals implementation. Prefer the usage of
	 * {@link #FilteredTree(Composite, int, PatternFilter, boolean, boolean)} if
	 * possible.
	 * </p>
	 *
	 * @param parent    the parent <code>Composite</code>
	 * @param treeStyle the style bits for the <code>Tree</code>
	 * @param filter    the filter to be used
	 *
	 * @deprecated As of 3.116, replaced by
	 *             {@link #FilteredTree(Composite, int, PatternFilter, boolean, boolean)}
	 */
	@Deprecated
	public FilteredTree(Composite parent, int treeStyle, PatternFilter filter) {
		this(parent);
		init(treeStyle, filter);
	}

	/**
	 * Create a new instance of the receiver.
	 *
	 * <p>
	 * <b>WARNING:</b> Using this constructor results in a slow performing tree and
	 * should not be used if the underlying data model uses a stable and correct
	 * hashCode and equals implementation. Prefer the usage of
	 * {@link #FilteredTree(Composite, int, PatternFilter, boolean, boolean)} if
	 * possible
	 * </p>
	 *
	 * @param parent     the parent <code>Composite</code>
	 * @param treeStyle  the style bits for the <code>Tree</code>
	 * @param filter     the filter to be used
	 * @param useNewLook ignored, look introduced in 3.5 is always used
	 * @since 3.5
	 * @deprecated As of 3.116, replaced by
	 *             {@link #FilteredTree(Composite, int, PatternFilter, boolean, boolean)}
	 */
	@Deprecated
	public FilteredTree(Composite parent, int treeStyle, PatternFilter filter, boolean useNewLook) {
		this(parent, treeStyle, filter);
	}

	@Override
	protected void init(int treeStyle, PatternFilter filter) {
		showFilterControls = PlatformUI.getPreferenceStore()
				.getBoolean(IWorkbenchPreferenceConstants.SHOW_FILTERED_TEXTS);
		super.init(treeStyle, filter);
		createRefreshJob();
		setInitialText(WorkbenchMessages.FilteredTree_FilterMessage);
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
	 * @param parent parent <code>Composite</code>
	 * @param style  SWT style bits used to create the tree
	 * @return the tree
	 */
	protected Control createTreeControl(Composite parent, int style) {
		treeViewer = doCreateTreeViewer(parent, style);
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
	 * @param parent the parent composite
	 * @param style  SWT style bits used to create the tree viewer
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
	protected WorkbenchJob doCreateRefreshJob() {
		return new WorkbenchJob("Refresh Filter") {//$NON-NLS-1$
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
						 * Expand elements one at a time. After each is expanded, check to see if the
						 * filter text has been modified. If it has, then cancel the refresh job so the
						 * user doesn't have to endure expansion of all the nodes.
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
					if (quickSelectionMode)
						updateTreeSelection(false);
					redrawFalseControl.setRedraw(true);
				}
				return Status.OK_STATUS;
			}

			/**
			 * Returns true if the job should be canceled (because of timeout or actual
			 * cancellation).
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

	/**
	 * Updates the toolbar. The default implementation does nothing. Subclasses may
	 * override.
	 *
	 * @param visible boolean
	 */
	protected void updateToolbar(boolean visible) {
		// nothing to do
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
					e.result = NLS.bind(WorkbenchMessages.FilteredTree_AccessibleListenerFiltered,
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
			if (quickSelectionMode) {
				return;
			}
			if (e.detail == SWT.TRAVERSE_RETURN) {
				e.doit = false;
				updateTreeSelection(true);
			}
		});
	}

	/**
	 * Updates the selection in the tree, based on the filter text.
	 *
	 * @param setFocus <code>true</code> if the focus should be set on the tree,
	 *                 <code>false</code> otherwise
	 * @since 3.105
	 */
	protected void updateTreeSelection(boolean setFocus) {
		Tree tree = getViewer().getTree();
		if (tree.getItemCount() == 0) {
			if (setFocus)
				Display.getCurrent().beep();
		} else {
			// if the initial filter text hasn't changed, do not try
			// to match
			boolean hasFocus = setFocus ? tree.setFocus() : true;
			boolean textChanged = !getInitialText().equals(filterText.getText().trim());
			if (hasFocus && textChanged && filterText.getText().trim().length() > 0) {
				TreeItem item;
				if (tree.getSelectionCount() > 0)
					item = getFirstMatchingItem(tree.getSelection());
				else
					item = getFirstMatchingItem(tree.getItems());
				if (item != null) {
					tree.setSelection(new TreeItem[] { item });
					ISelection sel = getViewer().getSelection();
					getViewer().setSelection(sel, true);
				}
			}
		}
	}

	@Override
	protected Text doCreateFilterText(Composite parent) {
		return new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
	}

	private String previousFilterText;

	private boolean narrowingDown;

	@Override
	protected void textChanged() {
		narrowingDown = previousFilterText == null
				|| previousFilterText.equals(WorkbenchMessages.FilteredTree_FilterMessage)
				|| getFilterString().startsWith(previousFilterText);
		previousFilterText = getFilterString();
		// cancel currently running job first, to prevent unnecessary redraw
		refreshJob.cancel();
		refreshJob.schedule(getRefreshJobDelay());
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
	 * Sets whether this filtered tree is used to make quick selections. In this
	 * mode the first match in the tree is automatically selected while filtering
	 * and the 'Enter' key is not used to move the focus to the tree.
	 * <p>
	 * By default, this is set to <code>false</code>.
	 * </p>
	 *
	 * @param enabled <code>true</code> if this filtered tree is used to make quick
	 *                selections, <code>false</code> otherwise
	 * @since 3.105
	 */
	public void setQuickSelectionMode(boolean enabled) {
		this.quickSelectionMode = enabled;
	}

	/**
	 * Return a bold font if the given element matches the given pattern. Clients
	 * can opt to call this method from a Viewer's label provider to get a bold font
	 * for which to highlight the given element in the tree.
	 *
	 * @param element element for which a match should be determined
	 * @param tree    FilteredTree in which the element resides
	 * @param filter  PatternFilter which determines a match
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

	/**
	 * Custom tree viewer subclass that clears the caches in patternFilter on any
	 * change to the tree. See bug 187200.
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
