/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.Arrays;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.preferences.ViewPreferencesAction;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * The TableView is a view that generically implements views with tables.
 * 
 */
public abstract class TableView extends ViewPart {

	private static final String TAG_COLUMN_WIDTH = "columnWidth"; //$NON-NLS-1$

	private static final String TAG_VERTICAL_POSITION = "verticalPosition"; //$NON-NLS-1$

	private static final String TAG_HORIZONTAL_POSITION = "horizontalPosition"; //$NON-NLS-1$

	private TreeViewer viewer;

	private IMemento memento;

	private IAction sortAction;

	private IAction filtersAction;

	private IAction preferencesAction;

	private MarkerTreeContentProvider contentProvider;

	/*
	 * (non-Javadoc) Method declared on IViewPart.
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.memento = memento;
	}

	/**
	 * 
	 */
//	void haltTableUpdates() {
//		content.cancelPendingChanges();
//	}

//	void change(Collection toRefresh) {
//		content.change(toRefresh);
//	}

//	void setContents(Collection contents, IProgressMonitor mon) {
//		content.set(contents, mon);
//	}


	abstract protected void viewerSelectionChanged(
			IStructuredSelection selection);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		viewer = new TreeViewer(createTree(parent));
		createColumns(viewer.getTree());

		contentProvider = new MarkerTreeContentProvider(getSite());

		viewer.setContentProvider(contentProvider);

		viewer.setLabelProvider(new TableViewLabelProvider(getVisibleFields()));

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				viewerSelectionChanged(selection);
			}
		});

		viewer.setSorter(buildSorter());

		// create the actions before the input is set on the viewer but after
		// the
		// sorter and filter are set so the actions will be enabled correctly.
		createActions();

		viewer.setInput(createViewerInput());

		viewer.setSelection(restoreSelection(memento));

		Scrollable scrollable = (Scrollable) viewer.getControl();
		ScrollBar bar = scrollable.getVerticalBar();
		if (bar != null) {
			bar.setSelection(restoreVerticalScrollBarPosition(memento));
		}
		bar = scrollable.getHorizontalBar();
		if (bar != null) {
			bar.setSelection(restoreHorizontalScrollBarPosition(memento));
		}

		MenuManager mgr = initContextMenu();
		Menu menu = mgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(mgr, getViewer());

		getSite().setSelectionProvider(getViewer());

		IActionBars actionBars = getViewSite().getActionBars();
		initMenu(actionBars.getMenuManager());
		initToolBar(actionBars.getToolBarManager());

		registerGlobalActions(getViewSite().getActionBars());

		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				handleOpenEvent(event);
			}
		});
		viewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
	}

	/**
	 * Create the viewer input for the receiver.
	 * @return Object
	 */
	abstract Object createViewerInput();

	/**
	 * @param sorter2
	 */
	void setSorter(TableSorter sorter2) {
		TableSorter newSorter = new TableSorter(sorter2);
		viewer.setSorter(newSorter);
		newSorter.saveState(getDialogSettings());
		viewer.refresh();
	}

	/**
	 * Create the main tree control
	 * 
	 * @param parent
	 * @return Tree
	 */
	protected Tree createTree(Composite parent) {
		Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION);
		tree.setLinesVisible(true);
		return tree;
	}

	/**
	 * Get the pixel data for the columns.
	 * @return ColumnPixelData[]
	 */
	public ColumnPixelData[] getSavedColumnData() {
		ColumnPixelData[] defaultData = getDefaultColumnLayouts();

		ColumnPixelData[] result = new ColumnPixelData[defaultData.length];
		for (int i = 0; i < defaultData.length; i++) {
			int width = defaultData[i].width;
			ColumnPixelData defaultPixelData = defaultData[i];

			// non-resizable columns are always left at their default width
			if (defaultPixelData.resizable) {
				if (memento != null) {
					Integer widthInt = memento.getInteger(TAG_COLUMN_WIDTH + i);

					if (widthInt != null && widthInt.intValue() > 0) {
						width = widthInt.intValue();
					}
				}
			}

			result[i] = new ColumnPixelData(width, defaultPixelData.resizable,
					defaultPixelData.addTrim);
		}

		return result;
	}

	/**
	 * Return the column sizes from the actual widget. Returns the saved column
	 * sizes if the widget hasn't been created yet or its columns haven't been
	 * initialized yet. (Note that TableLayout only initializes the column
	 * widths after the first layout, so it is possible for the widget to exist
	 * but have all its columns incorrectly set to zero width - see bug 86329)
	 * 
	 * @return ColumnPixelData
	 */
	public ColumnPixelData[] getColumnData() {
		ColumnPixelData[] defaultData = getSavedColumnData();

		Tree tree = getTree();

		if (tree != null && (tree.isDisposed() || tree.getBounds().width == 0)) {
			tree = null;
		}

		TreeColumn[] column = null;
		if (tree != null) {
			column = tree.getColumns();
		}

		ColumnPixelData[] result = new ColumnPixelData[defaultData.length];
		for (int i = 0; i < defaultData.length; i++) {
			ColumnPixelData defaultPixelData = defaultData[i];
			int width = defaultData[i].width;

			if (column != null && i < column.length) {
				TreeColumn col = column[i];

				if (col.getWidth() > 0) {
					width = col.getWidth();
				}
			}

			result[i] = new ColumnPixelData(width, defaultPixelData.resizable,
					defaultPixelData.addTrim);
		}

		return result;
	}

	/**
	 * Create the columns in the tree.
	 * 
	 * @param tree
	 */
	protected void createColumns(final Tree tree) {
		SelectionListener headerListener = getHeaderListener();
		TableLayout layout = new TableLayout();
		tree.setLayout(layout);
		tree.setHeaderVisible(true);

		final IField[] fields = getVisibleFields();
		ColumnLayoutData[] columnWidths = getSavedColumnData();
		for (int i = 0; i < fields.length; i++) {
			layout.addColumnData(columnWidths[i]);
			TreeColumn tc = new TreeColumn(tree, SWT.NONE, i);
			tc.setText(fields[i].getColumnHeaderText());
			tc.setImage(fields[i].getColumnHeaderImage());
			tc.setResizable(columnWidths[i].resizable);
			tc.addSelectionListener(headerListener);
		}
	}

	/**
	 * Create the actions for the receiver.
	 */
	protected void createActions() {
		if (getSortDialog() != null) {
			sortAction = new TableSortAction(this, getSortDialog());
		}
	}

	protected MenuManager initContextMenu() {
		MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {

				getViewer().cancelEditing();
				fillContextMenu(mgr);
			}
		});
		return mgr;
	}

	protected abstract void initToolBar(IToolBarManager tbm);

	/**
	 * Init the menu for the receiver.
	 * 
	 * @param menu
	 */
	protected void initMenu(IMenuManager menu) {
		if (sortAction != null)
			menu.add(sortAction);
		addDropDownContributions(menu);
		if (filtersAction != null)
			menu.add(filtersAction);
		if (preferencesAction != null)
			menu.add(preferencesAction);
	}

	/**
	 * Add any extra contributions to the drop down.
	 * 
	 * @param menu
	 */
	void addDropDownContributions(IMenuManager menu) {
		// Do nothing by default.
	}

	protected abstract void registerGlobalActions(IActionBars actionBars);

	protected abstract void fillContextMenu(IMenuManager manager);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		Viewer viewer = getViewer();
		if (viewer != null && !viewer.getControl().isDisposed()) {

			viewer.getControl().setFocus();
		}
	}

	/**
	 * Build a sorter from the default settings.
	 * @return TableSorter
	 */
	protected ViewerSorter buildSorter() {

		IField[] sortingFields = getSortingFields();
		int[] priorities = new int[sortingFields.length];
		int[] directions = new int[sortingFields.length];
		for (int i = 0; i < sortingFields.length; i++) {
			priorities[i] = i;
		}
		Arrays.fill(directions, TableSorter.ASCENDING);
		TableSorter sorter = new TableSorter(sortingFields, priorities,
				directions);
		sorter.restoreState(getDialogSettings());
		

		return sorter;
	}

	// protected abstract ITableViewContentProvider getContentProvider();

	protected abstract IField[] getSortingFields();

	protected abstract IField[] getVisibleFields();

	protected abstract IDialogSettings getDialogSettings();

	/**
	 * Return the viewer.
	 * 
	 * @return TreeViewer
	 */
	protected TreeViewer getViewer() {
		return viewer;
	}

	/**
	 * Return the tree for the receiver.
	 * 
	 * @return Tree
	 */
	protected Tree getTree() {
		return getViewer().getTree();
	}

	protected SelectionListener getHeaderListener() {
		return new SelectionAdapter() {
			/**
			 * Handles the case of user selecting the header area.
			 */
			public void widgetSelected(SelectionEvent e) {

				TableSorter sorter = getTableSorter();
				int column = getViewer().getTree().indexOf(
						(TreeColumn) e.widget);
				if (column == sorter.getTopPriority())
					sorter.reverseTopPriority();
				else {
					sorter.setTopPriority(column);
				}
				viewer.refresh();
			}
		};
	}

	protected abstract ColumnPixelData[] getDefaultColumnLayouts();

	/**
	 * Return a sort dialog for the receiver.
	 * @return TableSortDialog
	 */
	protected TableSortDialog getSortDialog() {
		return new TableSortDialog(getSite(),getTableSorter());
		
	}

	/**
	 * Return the table sorter portion of the sorter.
	 * @return TableSorter
	 */
	TableSorter getTableSorter() {
		return (TableSorter) viewer.getSorter();
	}

	protected abstract void handleKeyPressed(KeyEvent event);

	protected abstract void handleOpenEvent(OpenEvent event);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		super.saveState(memento);

		ColumnPixelData[] data = getColumnData();

		for (int i = 0; i < data.length; i++) {
			ColumnPixelData data2 = data[i];

			memento.putInteger(TAG_COLUMN_WIDTH + i, data2.width);
		}

		saveSelection(memento);

		// save vertical position
		Scrollable scrollable = (Scrollable) viewer.getControl();
		ScrollBar bar = scrollable.getVerticalBar();
		int position = (bar != null) ? bar.getSelection() : 0;
		memento.putInteger(TAG_VERTICAL_POSITION, position);
		// save horizontal position
		bar = scrollable.getHorizontalBar();
		position = (bar != null) ? bar.getSelection() : 0;
		memento.putInteger(TAG_HORIZONTAL_POSITION, position);
	}

	protected abstract void saveSelection(IMemento memento);

	protected abstract IStructuredSelection restoreSelection(IMemento memento);

	private int restoreVerticalScrollBarPosition(IMemento memento) {
		if (memento == null) {
			return 0;
		}
		Integer position = memento.getInteger(TAG_VERTICAL_POSITION);
		return (position == null) ? 0 : position.intValue();
	}

	private int restoreHorizontalScrollBarPosition(IMemento memento) {
		if (memento == null) {
			return 0;
		}
		Integer position = memento.getInteger(TAG_HORIZONTAL_POSITION);
		return (position == null) ? 0 : position.intValue();
	}

	/**
	 * Get the IWorkbenchSiteProgressService for the receiver.
	 * 
	 * @return IWorkbenchSiteProgressService or <code>null</code>.
	 */
	protected IWorkbenchSiteProgressService getProgressService() {
		IWorkbenchSiteProgressService service = null;
		Object siteService = getSite().getAdapter(
				IWorkbenchSiteProgressService.class);
		if (siteService != null)
			service = (IWorkbenchSiteProgressService) siteService;
		return service;
	}

	/**
	 * Set the filters action.
	 * 
	 * @param action
	 */
	void setFilterAction(FiltersAction action) {
		filtersAction = action;

	}

	/**
	 * Return the filter action for the receiver.
	 * 
	 * @return IAction
	 */
	IAction getFilterAction() {
		return filtersAction;
	}

	/**
	 * Return the preferences action.
	 * 
	 * @return IAction
	 */
	IAction getPreferencesAction() {
		return preferencesAction;
	}

	/**
	 * Set the preferences action.
	 * 
	 * @param preferencesAction
	 */
	void setPreferencesAction(ViewPreferencesAction preferencesAction) {
		this.preferencesAction = preferencesAction;
	}

	/**
	 * Get the content provider
	 * @return MarkerTreeContentProvider
	 */
	MarkerTreeContentProvider getContentProvider() {
		return contentProvider;
	}

	/**
	 * Return the input to the viewer.
	 * @return Object
	 */
	public Object getViewerInput() {
		return getViewer().getInput();
	}



}
