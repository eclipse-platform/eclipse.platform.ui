/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

public abstract class TableView extends ViewPart {

    private TableContentProvider content;

    private static final String TAG_COLUMN_WIDTH = "columnWidth"; //$NON-NLS-1$

    private static final String TAG_VERTICAL_POSITION = "verticalPosition"; //$NON-NLS-1$

    private static final String TAG_HORIZONTAL_POSITION = "horizontalPosition"; //$NON-NLS-1$

    public static final String SORT_ACTION_ID = "sort"; //$NON-NLS-1$

    public static final String FILTERS_ACTION_ID = "filters"; //$NON-NLS-1$

    private TableViewer viewer;

    private IMemento memento;

    protected ColumnLayoutData[] columnLayouts;

    private Map actions = new HashMap();

    private ISelectionProvider selectionProvider = new SelectionProviderAdapter();

    private TableSorter sorter;

    /* (non-Javadoc)
     * Method declared on IViewPart.
     */
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        this.memento = memento;
    }

    /**
     * 
     */
    void haltTableUpdates() {
        content.cancelPendingChanges();
    }

    void change(Collection toRefresh) {
        content.change(toRefresh);
    }

    void setContents(Collection contents, IProgressMonitor mon) {
        content.set(contents, mon);
    }

    protected ISelectionProvider getSelectionProvider() {
        return selectionProvider;
    }

    abstract protected void viewerSelectionChanged(
            IStructuredSelection selection);

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());

        viewer = new TableViewer(createTable(parent));
        restoreColumnWidths(memento);
        createColumns(viewer.getTable());
        content = new TableContentProvider(viewer, Messages.format(
                "TableView.populating", //$NON-NLS-1$
                new Object[] { getTitle() }), getProgressService());

        viewer.setContentProvider(content);

        viewer.setLabelProvider(new TableViewLabelProvider(getVisibleFields()));

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event
                        .getSelection();
                viewerSelectionChanged(selection);
            }
        });

        setSorter(getSorter());

        //create the actions before the input is set on the viewer but after the 
        //sorter and filter are set so the actions will be enabled correctly.
        createActions();

        viewer.setInput(getViewerInput());

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
        getSite().registerContextMenu(mgr, getSelectionProvider());

        getSite().setSelectionProvider(getSelectionProvider());

        initActionBars(getViewSite().getActionBars());
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
     * @param selection
     */
    protected void setSelection(IStructuredSelection selection) {
        getSelectionProvider().setSelection(selection);
    }

    /**
     * @param sorter2
     */
    void setSorter(TableSorter sorter2) {
        TableSorter newSorter = new TableSorter(sorter2);

        sorter = newSorter;
        content.setSorter(newSorter);
        newSorter.saveState(getDialogSettings());
        sorterChanged();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {

    }

    /**
     * Creates the table control.
     */
    protected Table createTable(Composite parent) {
        Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
                | SWT.FULL_SELECTION);
        table.setLinesVisible(true);
        table.setFont(parent.getFont());
        return table;
    }

    protected void createColumns(final Table table) {
        SelectionListener headerListener = getHeaderListener();
        TableLayout layout = new TableLayout();
        table.setLayout(layout);
        table.setHeaderVisible(true);
        final IField[] fields = getVisibleFields();
        ColumnLayoutData[] columnWidths = columnLayouts;
        if (columnWidths == null) {
            columnWidths = getDefaultColumnLayouts();
        }
        for (int i = 0; i < fields.length; i++) {
            if (columnWidths == null || i >= columnWidths.length
                    || columnWidths[i] == null) {
                layout.addColumnData(new ColumnPixelData(504 / fields.length,
                        true));
            } else {
                layout.addColumnData(columnWidths[i]);
            }
            TableColumn tc = new TableColumn(table, SWT.NONE, i);
            tc.setText(fields[i].getColumnHeaderText());
            tc.setImage(fields[i].getColumnHeaderImage());
            tc.addSelectionListener(headerListener);
        }
    }

    protected void createActions() {
        if (getSortDialog() != null) {
            putAction(SORT_ACTION_ID,
                    new TableSortAction(this, getSortDialog()));
        }
    }

    protected IAction getAction(String id) {
        return (IAction) actions.get(id);
    }

    protected void putAction(String id, IAction action) {
        actions.put(id, action);
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

    protected void initActionBars(IActionBars actionBars) {
        initMenu(actionBars.getMenuManager());
        initToolBar(actionBars.getToolBarManager());
    }

    protected void initMenu(IMenuManager menu) {
        IAction sortAction = getAction(SORT_ACTION_ID);
        if (sortAction != null)
            menu.add(sortAction);
        IAction filtersAction = getAction(FILTERS_ACTION_ID);
        if (filtersAction != null)
            menu.add(filtersAction);
    }

    protected abstract void registerGlobalActions(IActionBars actionBars);

    protected abstract void fillContextMenu(IMenuManager manager);

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus() {
        TableViewer viewer = getViewer();
        if (viewer != null && !viewer.getControl().isDisposed()) {

            viewer.getControl().setFocus();
        }
    }

    protected TableSorter getSorter() {
        if (sorter == null) {
            int[] priorities = new int[getFields().length];
            int[] directions = new int[getFields().length];
            for (int i = 0; i < getFields().length; i++) {
                priorities[i] = i;
            }
            Arrays.fill(directions, TableSorter.ASCENDING);
            sorter = new TableSorter(getFields(), priorities, directions);
            sorter.restoreState(getDialogSettings());
        }
        return sorter;
    }

    //protected abstract ITableViewContentProvider getContentProvider();

    protected IField[] getFields() {
        IField[] vProps = getVisibleFields();
        IField[] hProps = getHiddenFields();
        IField[] fields = new IField[vProps.length + hProps.length];
        System.arraycopy(vProps, 0, fields, 0, vProps.length);
        System.arraycopy(hProps, 0, fields, vProps.length, hProps.length);
        return fields;
    }

    protected abstract Object getViewerInput();

    protected abstract IField[] getVisibleFields();

    protected abstract IField[] getHiddenFields();

    protected abstract IDialogSettings getDialogSettings();

    protected TableViewer getViewer() {
        return viewer;
    }

    protected SelectionListener getHeaderListener() {
        return new SelectionAdapter() {
            /**
             * Handles the case of user selecting the
             * header area.
             */
            public void widgetSelected(SelectionEvent e) {

                int column = getViewer().getTable().indexOf(
                        (TableColumn) e.widget);
                if (column == getSorter().getTopPriority())
                    getSorter().reverseTopPriority();
                else {
                    getSorter().setTopPriority(column);
                }
                setSorter(getSorter());
            }
        };
    }

    protected abstract ColumnLayoutData[] getDefaultColumnLayouts();

    protected TableSortDialog getSortDialog() {
        if (getSorter() != null) {
            return new TableSortDialog(getSite().getShell(), getSorter());
        }
        return null;
    }

    protected void sorterChanged() {

        viewer.setSorter(getSorter());

        final TableViewer viewer = getViewer();
        if (viewer == null) {
            return;
        }

        getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                viewer.getControl().setRedraw(false);
                viewer.refresh(false);
                viewer.getControl().setRedraw(true);
            }
        });
    }

    protected abstract void handleKeyPressed(KeyEvent event);

    protected abstract void handleOpenEvent(OpenEvent event);

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento) {
        super.saveState(memento);

        //save column widths
        TableColumn[] columns = viewer.getTable().getColumns();
        for (int i = 0; i < columns.length; i++) {
            memento.putInteger(TAG_COLUMN_WIDTH + i, columns[i].getWidth());
        }

        saveSelection(memento);

        //save vertical position
        Scrollable scrollable = (Scrollable) viewer.getControl();
        ScrollBar bar = scrollable.getVerticalBar();
        int position = (bar != null) ? bar.getSelection() : 0;
        memento.putInteger(TAG_VERTICAL_POSITION, position);
        //save horizontal position
        bar = scrollable.getHorizontalBar();
        position = (bar != null) ? bar.getSelection() : 0;
        memento.putInteger(TAG_HORIZONTAL_POSITION, position);
    }

    protected abstract void saveSelection(IMemento memento);

    private void restoreColumnWidths(IMemento memento) {
        if (memento == null) {
            return;
        }
        columnLayouts = new ColumnLayoutData[getFields().length];
        for (int i = 0; i < columnLayouts.length; i++) {
            Integer width = memento.getInteger(TAG_COLUMN_WIDTH + i);
            if (width == null) {
                columnLayouts = null;
                break;
            } else {
                columnLayouts[i] = new ColumnPixelData(width.intValue(), true);
            }
        }
    }

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

}