/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.internal.markers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.navigator.ShowInNavigatorAction;
import org.eclipse.ui.views.tasklist.ITaskListResourceAdapter;

public class MarkerView extends ViewPart {
	
	static final String TAG_DIALOG_SECTION = "org.eclipse.ui.views.markerview"; //$NON-NLS-1$
	
	protected IField[] fields;
	
	protected IResource input;
	protected IResource[] focusResources;

	protected TableViewer viewer;
	protected Composite compositeMarkerLimitExceeded;
	protected StackLayout stackLayout = new StackLayout();
	protected MarkerSorter sorter;
	protected MarkerFilter filter;
	protected ColumnLayoutData[] columnLayouts;
	
	protected CopyMarkerAction copyAction;
	protected PasteMarkerAction pasteAction;
	protected SelectionProviderAction revealAction;
	protected SelectionProviderAction openAction;
	protected SelectionProviderAction showInNavigatorAction;
	protected SelectionProviderAction deleteAction;
	protected SelectionProviderAction selectAllAction;
	protected SelectionProviderAction propertiesAction;	
	protected IAction sortAction;
	protected IAction filtersAction;
	
	protected Clipboard clipboard;
	
	private IMarkerChangedListener markerListener = new IMarkerChangedListener() {
		public void markerChanged(List additions, List removals, List changes) {
			MarkerView.this.markerChanged(additions, removals, changes);
		}
	};
	
	private ISelectionListener focusListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			MarkerView.this.selectionChanged(part, selection);
		}
	};
	
	private IMemento memento;
	
	private static final String TAG_COLUMN_WIDTH = "columnWidth"; //$NON-NLS-1$
	private static final String TAG_SELECTION = "selection"; //$NON-NLS-1$
	private static final String TAG_MARKER = "marker"; //$NON-NLS-1$
	private static final String TAG_RESOURCE = "resource"; //$NON-NLS-1$
	private static final String TAG_ID = "id"; //$NON-NLS-1$
	private static final String TAG_VERTICAL_POSITION = "verticalPosition"; //$NON-NLS-1$
	private static final String TAG_HORIZONTAL_POSITION = "horizontalPosition"; //$NON-NLS-1$
	
	public MarkerView() {
	}

	/* (non-Javadoc)
	 * Method declared on IViewPart.
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.memento = memento;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		getSite().getPage().addSelectionListener(focusListener);
		
		clipboard = new Clipboard(parent.getDisplay());
		
		getRegistry().setTypes(getRootTypes());
		
		viewer = new TableViewer(createTable(parent));
		restoreColumnWidths(memento);
		createColumns(viewer.getTable());
		viewer.setContentProvider(new MarkerContentProvider(getSite(), getRegistry()));
		viewer.setLabelProvider(new MarkerLabelProvider(getVisibleFields()));

		getRegistry().setComparator(getSorter());
		viewer.setSorter(getSorter());
		
		getRegistry().setFilter(getFilter());
		getRegistry().addMarkerChangedListener(markerListener);
		
		//create the actions before the input is set on the viewer but after the 
		//sorter and filter are set so the actions will be enabled correctly.
		createActions();

		getRegistry().setInput(ResourcesPlugin.getWorkspace().getRoot());
		viewer.setInput(getRegistry().getInput());
		viewer.setSelection(restoreSelection(memento), true);
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
		getSite().registerContextMenu(mgr, viewer);
		initToolBar(getViewSite().getActionBars().getToolBarManager());
		initActionBars(getViewSite().getActionBars());
		registerActions(getViewSite().getActionBars());
		
		initDragAndDrop();
		
		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				openAction.run();
			}
		});
		viewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
	
		compositeMarkerLimitExceeded = new Composite(parent, SWT.NONE);
		compositeMarkerLimitExceeded.setLayout(new GridLayout());
		Label labelMarkerLimitExceeded =
			new Label(compositeMarkerLimitExceeded, SWT.WRAP);
		labelMarkerLimitExceeded.setText(Messages.getString("markerLimitExceeded.title")); //$NON-NLS-1$
		parent.setLayout(stackLayout);
		
		checkMarkerLimit();
		updateTitle();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		getRegistry().removeMarkerChangedListener(markerListener);
		getSite().getPage().removeSelectionListener(focusListener);
		
		//dispose of selection provider actions
		openAction.dispose();
		copyAction.dispose();
		selectAllAction.dispose();
		deleteAction.dispose();
		revealAction.dispose();
		showInNavigatorAction.dispose();
		propertiesAction.dispose();
	}


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
		
		//save selection
		Scrollable scrollable = (Scrollable) viewer.getControl();
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		IMemento selectionMem = memento.createChild(TAG_SELECTION);
		for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
			IMarker marker = (IMarker) iterator.next();
			IMemento elementMem = selectionMem.createChild(TAG_MARKER);
			elementMem.putString(TAG_RESOURCE, marker.getResource().getFullPath().toString());
			elementMem.putString(TAG_ID, String.valueOf(marker.getId()));
		}

		//save vertical position
		ScrollBar bar = scrollable.getVerticalBar();
		int position = (bar != null) ? bar.getSelection() : 0;
		memento.putInteger(TAG_VERTICAL_POSITION, position);
		//save horizontal position
		bar = scrollable.getHorizontalBar();
		position = (bar != null) ? bar.getSelection() : 0;
		memento.putInteger(TAG_HORIZONTAL_POSITION, position);
	}

	private void restoreColumnWidths(IMemento memento) {
		if (memento == null) {
			return;
		}
		columnLayouts = new ColumnLayoutData[getFields().length];
		for (int i = 0; i < columnLayouts.length; i++) {
			Integer width = memento.getInteger(TAG_COLUMN_WIDTH + i);
			if (width != null) {
				columnLayouts[i] = new ColumnPixelData(width.intValue(), true);
			}
		}
	}
	
	private IStructuredSelection restoreSelection(IMemento memento) {
		if (memento == null) {
			return new StructuredSelection();
		}
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IMemento selectionMemento = memento.getChild(TAG_SELECTION);
		if (selectionMemento == null) {
			return new StructuredSelection();
		}
		ArrayList selectionList = new ArrayList();
		IMemento[] markerMems = selectionMemento.getChildren(TAG_MARKER);
		for (int i = 0; i < markerMems.length; i++) {
			try {
				long id = new Long(markerMems[i].getString(TAG_ID)).longValue();
				IResource resource = root.findMember(markerMems[i].getString(TAG_RESOURCE));
				if (resource != null) {
					IMarker marker = resource.findMarker(id);
					if (marker != null)
						selectionList.add(marker);
				}
			} 
			catch (CoreException e) {
			}
		}
		return new StructuredSelection(selectionList);
	}
	
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
	 * Creates the table control.
	 */
	protected Table createTable(Composite parent) {
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setFont(parent.getFont());
		return table;
	}
	
	protected void createColumns(final Table table) {
		SelectionListener headerListener = getSelectionListener();
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		table.setHeaderVisible(true);
		final IField[] PROPERTIES = getVisibleFields();
		ColumnLayoutData[] columnWidths = columnLayouts;
		if (columnWidths == null) {
			columnWidths = getDefaultColumnLayouts();
		}
		for (int i = 0; i < PROPERTIES.length; i++) {
			if (columnWidths == null || i >= columnWidths.length || columnWidths[i] == null) {
				layout.addColumnData(new ColumnPixelData(504 / PROPERTIES.length, true));
			}
			else {
				layout.addColumnData(columnWidths[i]);
			}
			TableColumn tc = new TableColumn(table, SWT.NONE,i);
			tc.setText(PROPERTIES[i].getColumnHeaderText());
			tc.setImage(PROPERTIES[i].getColumnHeaderImage());
			tc.addSelectionListener(headerListener);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		if (viewer != null && !viewer.getControl().isDisposed()) { 
			viewer.getControl().setFocus();
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
	
	protected void fillContextMenu(IMenuManager manager) {
		if (manager == null)
			return;
		manager.add(openAction);
		manager.add(showInNavigatorAction);
		manager.add(new Separator());
		manager.add(copyAction);
		pasteAction.updateEnablement();
		manager.add(pasteAction);
		manager.add(deleteAction);
		manager.add(selectAllAction);
		fillContextMenuAdditions(manager);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(new Separator());
		manager.add(propertiesAction);
	}
	
	protected void fillContextMenuAdditions(IMenuManager manager) {
	}
	
	protected void initToolBar(IToolBarManager tbm) {
		tbm.add(deleteAction);
		tbm.add(openAction);
		tbm.update(false);
	}
	
	protected void initActionBars(IActionBars actionBars) {
		IMenuManager menu = actionBars.getMenuManager();
		if (sortAction != null)
			menu.add(sortAction);
		if (filtersAction != null)
			menu.add(filtersAction);
	}
	
	protected void registerActions(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, copyAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.PASTE, pasteAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.DELETE, deleteAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.SELECT_ALL, selectAllAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.PROPERTIES, propertiesAction);
	}
	
	protected void createActions() {
		revealAction = new RevealMarkerAction(this, viewer);
		openAction = new OpenMarkerAction(this, viewer);
		copyAction = new CopyMarkerAction(this, viewer);
		copyAction.setClipboard(clipboard);
		copyAction.setProperties(getFields());
		pasteAction = new PasteMarkerAction(this, viewer);
		pasteAction.setClipboard(clipboard);
		pasteAction.setPastableTypes(getRegistry().getTypes());
		deleteAction = new RemoveMarkerAction(this, viewer);
		selectAllAction = new SelectAllAction(viewer, getRegistry());
		showInNavigatorAction = new ShowInNavigatorAction(getViewSite().getPage(), viewer);
		propertiesAction = new MarkerPropertiesAction(this, viewer);
		
		if (getSortDialog() != null) {
			sortAction = new MarkerSortAction(this, getSortDialog());
		}
		if (getFiltersDialog() != null) {
			filtersAction = new FiltersAction(this, getFiltersDialog());
		}
	}
	
	/**
	 * Handles key events in viewer.
	 */
	void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0 && deleteAction.isEnabled())
			deleteAction.run();
	}
	
	/**
	 * Adds drag and drop support to the bookmark navigator.
	 */
	protected void initDragAndDrop() {
		int operations = DND.DROP_COPY;
		Transfer[] transferTypes = new Transfer[] {
			MarkerTransfer.getInstance(), 
			TextTransfer.getInstance()};
		DragSourceListener listener = new DragSourceAdapter() {
			public void dragSetData(DragSourceEvent event) {
				performDragSetData(event);
			}
			public void dragFinished(DragSourceEvent event) {
			}
		};
		viewer.addDragSupport(operations, transferTypes, listener);	
	}

	/**
	 * The user is attempting to drag marker data.  Add the appropriate
	 * data to the event depending on the transfer type.
	 */
	void performDragSetData(DragSourceEvent event) {
		if (MarkerTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = ((IStructuredSelection) viewer.getSelection()).toArray();
			return;
		}
		if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			List selection = ((IStructuredSelection) viewer.getSelection()).toList();
			try {
				IMarker[] markers = new IMarker[selection.size()];
				selection.toArray(markers);
				if (markers != null) {
					event.data = copyAction.createMarkerReport(markers);
				}
			}
			catch (ArrayStoreException e) {
			}
		}
	}
	
	void checkMarkerLimit() {
		int itemCount = getRegistry().getElements().size();
		Composite parent = viewer.getTable().getParent();
		if (getFilter() == null || !getFilter().getFilterOnMarkerLimit() || itemCount <= getFilter().getMarkerLimit()) {
			if (stackLayout.topControl != viewer.getTable()) {
				stackLayout.topControl = viewer.getTable();
				parent.layout();
			}
		}
		else {
			if (stackLayout.topControl != compositeMarkerLimitExceeded) {
				stackLayout.topControl = compositeMarkerLimitExceeded;
				parent.layout();
			}
		}
	}
	
	void updateTitle() {
		String currentTitle = getTitle();
		String viewName = getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
		String status = ""; //$NON-NLS-1$
		int filteredCount = getRegistry().getElements().size();
		int totalCount = getRegistry().getRawElements().size();
		if (filteredCount == totalCount) {
			status = Messages.format("filter.itemsMessage", new Object[] {new Integer(totalCount)});
		}
		else {
			status = Messages.format("filter.matchedMessage", new Object[] {new Integer(filteredCount), new Integer(totalCount)});
		}
		String newTitle = Messages.format("view.title", new String[] {viewName, status});
		if (!newTitle.equals(currentTitle)) {
			setTitle(newTitle);
		}
	}

	/**
	 * @return
	 */
	public TableViewer getViewer() {
		return viewer;
	}
	
	void updateFocusResource(IResource[] resources) {
		boolean updateNeeded = updateNeeded(focusResources, resources);
		getFilter().setFocusResource(resources);
		focusResources = resources;
		if (updateNeeded) {
			filtersChanged();
		}
	}
	
	private boolean updateNeeded(IResource[] oldResources, IResource[] newResources) {
		//determine if an update if refiltering is required
		if (getFilter() == null) {
			return false;
		}
		int onResource = getFilter().getOnResource();
		if (onResource == MarkerFilter.ON_ANY_RESOURCE || onResource == MarkerFilter.ON_WORKING_SET) {
			return false;
		}
		if (newResources == null || newResources.length < 1) {
			return false;
		}
		if (oldResources == null || oldResources.length < 1) {
			return true;
		}
		if (Arrays.equals(oldResources, newResources)) {
			return false;
		}
		if (onResource == MarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT) {
			List oldProjects = new ArrayList(newResources.length);
			List newProjects = new ArrayList(newResources.length);
			for (int i = 0; i < oldResources.length; i++) {
				IProject project = oldResources[i].getProject();
				if (!oldProjects.contains(project)) {
					oldProjects.add(project);
				}
			}
			for (int i = 0; i < newResources.length; i++) {
				IProject project = newResources[i].getProject();
				if (!newProjects.contains(project)) {
					newProjects.add(project);
				}
			}
			if (oldProjects.size() == newProjects.size()) {
				for (int i = 0; i < oldProjects.size(); i++) {
					if (!newProjects.contains(oldProjects.get(i))) {
						return true;
					}
				}
			}
			else {
				return true;
			}
		}
		else {
			return true;
		}
		return false;
	}
	
	protected void markerChanged(List additions, List removals, List changes) {
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				checkMarkerLimit();
				updateTitle();
			}
		});
	}

	protected void selectionChanged(IWorkbenchPart part, ISelection selection) {
		List resources = new ArrayList();
		if (part instanceof IEditorPart) {
			IEditorInput input = ((IEditorPart) part).getEditorInput();
			if (input instanceof FileEditorInput) {
				resources.add(((FileEditorInput) input).getFile());
			}
		}
		else {
			if (selection instanceof IStructuredSelection) {
				for (Iterator iterator = ((IStructuredSelection) selection).iterator(); iterator.hasNext();) {
					Object object = iterator.next();
					if (object instanceof IAdaptable) {
						ITaskListResourceAdapter taskListResourceAdapter;
						Object adapter =
							((IAdaptable) object).getAdapter(
								ITaskListResourceAdapter.class);
						if (adapter != null
							&& adapter instanceof ITaskListResourceAdapter) {
							taskListResourceAdapter =
								(ITaskListResourceAdapter) adapter;
						} else {
							taskListResourceAdapter =
								DefaultMarkerResourceAdapter.getDefault();
						}

						IResource resource =
							taskListResourceAdapter.getAffectedResource(
								(IAdaptable) object);
						if (resource != null) {
							resources.add(resource);
						}
					}
				}
			}
		}
		IResource[] focus = new IResource[resources.size()];
		resources.toArray(focus);
		updateFocusResource(focus);
	}
	
	void sorterChanger() {
		getRegistry().resort();
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				viewer.refresh(false);
			}
		});
		getSorter().saveState(getDialogSettings());
	}
	
	public void filtersChanged() {
		getRegistry().refilter();
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				checkMarkerLimit();
				updateTitle();
			}
		});
		getFilter().saveState(getDialogSettings());
	}

	/**
	 * @return
	 */
	protected MarkerFilter getFilter() {
		if (filter == null) {
			filter = new MarkerFilter(getRootTypes());
			filter.restoreState(getDialogSettings());
		}
		return filter;
	}

	/**
	 * @return
	 */
	protected MarkerSorter getSorter() {
		if (sorter == null) {
			int[] priorities = new int[getFields().length];
			int[] directions = new int[getFields().length];
			for (int i = 0; i < getFields().length; i++) {
				priorities[i] = i;
			}
			Arrays.fill(directions, MarkerSorter.ASCENDING);
			sorter = new MarkerSorter(getFields(), priorities, directions);
			sorter.restoreState(getDialogSettings());
		}
		return sorter;
	}
	
	protected String[] getRootTypes() {
		return MarkerViewConstants.ROOT_TYPES;
	}
	
	protected IField[] getVisibleFields() {
		return new IField[0];
	}
	
	protected IField[] getHiddenFields() {
		return new IField[0];
	}
	
	protected IField[] getFields() {
		if (fields == null) {
			IField[] vProps = getVisibleFields();
			IField[] hProps = getHiddenFields();
			fields = new IField[vProps.length + hProps.length];
			System.arraycopy(vProps, 0, fields, 0, vProps.length);
			System.arraycopy(hProps, 0, fields, vProps.length, hProps.length);
		}
		return fields;
	}
	
	protected IDialogSettings getDialogSettings() {
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		IDialogSettings workbenchSettings = plugin.getDialogSettings();
		IDialogSettings settings = workbenchSettings.getSection(TAG_DIALOG_SECTION);
		if (settings == null) {
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);
		}
		return settings;
	}
	
	protected SelectionListener getSelectionListener() {
		return new SelectionAdapter() {
			/**
			 * Handles the case of user selecting the
			 * header area.
			 */
			public void widgetSelected(SelectionEvent e) {
				int column = getViewer().getTable().indexOf((TableColumn) e.widget);
				if (column == getSorter().getTopPriority())
					getSorter().reverseTopPriority();
				else {
					getSorter().setTopPriority(column);
				}
				getRegistry().resort();
				viewer.refresh(false);
				getSorter().saveState(getDialogSettings());
			}
		};
	}
	
	protected MarkerRegistry getRegistry() {
		return null;
	}
	
	protected ColumnLayoutData[] getDefaultColumnLayouts() {
		return null;
	}
	
	protected FiltersDialog getFiltersDialog() {
		if (getFilter() != null) {
			return new FiltersDialog(getSite().getShell(), getFilter());
		}
		return null;
	}
	
	protected MarkerSortDialog getSortDialog() {
		if (getSorter() != null) {
			return new MarkerSortDialog(getSite().getShell(), getSorter());
		}
		return null;
	}
	
}
