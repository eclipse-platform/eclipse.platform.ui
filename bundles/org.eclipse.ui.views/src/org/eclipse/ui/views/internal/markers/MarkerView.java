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
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.views.internal.tableview.IItemsChangedListener;
import org.eclipse.ui.views.internal.tableview.ITableViewContentProvider;
import org.eclipse.ui.views.internal.tableview.TableView;
import org.eclipse.ui.views.navigator.ShowInNavigatorAction;
import org.eclipse.ui.views.tasklist.ITaskListResourceAdapter;


public abstract class MarkerView extends TableView {
	
	private static final String TAG_SELECTION = "selection"; //$NON-NLS-1$
	private static final String TAG_MARKER = "marker"; //$NON-NLS-1$
	private static final String TAG_RESOURCE = "resource"; //$NON-NLS-1$
	private static final String TAG_ID = "id"; //$NON-NLS-1$
	
	protected IResource[] focusResources;

	private MarkerFilter filter;
	private Clipboard clipboard;
	
	protected CopyMarkerAction copyAction;
	protected PasteMarkerAction pasteAction;
	protected SelectionProviderAction revealAction;
	protected SelectionProviderAction openAction;
	protected SelectionProviderAction showInNavigatorAction;
	protected SelectionProviderAction deleteAction;
	protected SelectionProviderAction selectAllAction;
	protected SelectionProviderAction propertiesAction;	
	
	protected Composite compositeMarkerLimitExceeded;
	protected StackLayout stackLayout = new StackLayout();

	private IItemsChangedListener itemsListener = new IItemsChangedListener() {
		public void itemsChanged(List additions, List removals, List changes) {
			MarkerView.this.itemsChanged(additions, removals, changes);
		}
	};
	
	private ISelectionListener focusListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			MarkerView.this.focusSelectionChanged(part, selection);
		}
	};
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		getSite().getPage().addSelectionListener(focusListener);
		getRegistry().addItemsChangedListener(itemsListener);
		
		clipboard = new Clipboard(parent.getDisplay());

		super.createPartControl(parent);

		initDragAndDrop();
		
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
	 * @see org.eclipse.ui.views.internal.tableview.TableView#dispose()
	 */
	public void dispose() {
		super.dispose();
		getRegistry().removeItemsChangedListener(itemsListener);
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
	 * @see org.eclipse.ui.views.internal.tableview.TableView#createActions()
	 */
	protected void createActions() {
		TableViewer viewer = getViewer();
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
		
		super.createActions();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#initToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void initToolBar(IToolBarManager tbm) {
		tbm.add(deleteAction);
		tbm.add(openAction);
		tbm.update(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#registerGlobalActions(org.eclipse.ui.IActionBars)
	 */
	protected void registerGlobalActions(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, copyAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.PASTE, pasteAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.DELETE, deleteAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.SELECT_ALL, selectAllAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.PROPERTIES, propertiesAction);
	}

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
		getViewer().addDragSupport(operations, transferTypes, listener);	
	}

	/**
	 * The user is attempting to drag marker data.  Add the appropriate
	 * data to the event depending on the transfer type.
	 */
	private void performDragSetData(DragSourceEvent event) {
		if (MarkerTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = ((IStructuredSelection) getViewer().getSelection()).toArray();
			return;
		}
		if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			List selection = ((IStructuredSelection) getViewer().getSelection()).toList();
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#getFilter()
	 */
	protected IFilter getFilter() {
		if (filter == null) {
			filter = new MarkerFilter(getRootTypes());
			filter.restoreState(getDialogSettings());
		}
		return filter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#getContentProvider()
	 */
	protected ITableViewContentProvider getContentProvider() {
		return getRegistry();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#handleKeyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	protected void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0 && deleteAction.isEnabled()) {
			deleteAction.run();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#handleOpenEvent(org.eclipse.jface.viewers.OpenEvent)
	 */
	protected void handleOpenEvent(OpenEvent event) {
		openAction.run();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#saveSelection(org.eclipse.ui.IMemento)
	 */
	protected void saveSelection(IMemento memento) {
		IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
		IMemento selectionMem = memento.createChild(TAG_SELECTION);
		for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
			IMarker marker = (IMarker) iterator.next();
			IMemento elementMem = selectionMem.createChild(TAG_MARKER);
			elementMem.putString(TAG_RESOURCE, marker.getResource().getFullPath().toString());
			elementMem.putString(TAG_ID, String.valueOf(marker.getId()));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#restoreSelection(org.eclipse.ui.IMemento)
	 */
	protected IStructuredSelection restoreSelection(IMemento memento) {
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

	protected abstract String[] getRootTypes();
	
	protected abstract MarkerRegistry getRegistry();
	
	protected Dialog getFiltersDialog() {
		IFilter filter = getFilter();
		if (filter != null && filter instanceof MarkerFilter) {
			return new FiltersDialog(getSite().getShell(), (MarkerFilter) filter);
		}
		return null;
	}
	
	/**
	 * @param part
	 * @param selection
	 */
	protected void focusSelectionChanged(IWorkbenchPart part, ISelection selection) {
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

	void updateFocusResource(IResource[] resources) {
		boolean updateNeeded = updateNeeded(focusResources, resources);
		if (getFilter() != null && getFilter() instanceof MarkerFilter) {
			((MarkerFilter) getFilter()).setFocusResource(resources);
		}
		focusResources = resources;
		if (updateNeeded) {
			filtersChanged();
		}
	}
	
	private boolean updateNeeded(IResource[] oldResources, IResource[] newResources) {
		//determine if an update if refiltering is required
		IFilter iFilter = getFilter();
		if (iFilter == null) {
			return false;
		}
		MarkerFilter filter = (MarkerFilter) iFilter;
		int onResource = filter.getOnResource();
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
	
	/**
	 * @param additions
	 * @param removals
	 * @param changes
	 */
	protected void itemsChanged(List additions, List removals, List changes) {
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				checkMarkerLimit();
				updateTitle();
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.tableview.TableView#filtersChanged()
	 */
	public void filtersChanged() {
		super.filtersChanged();
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				checkMarkerLimit();
				updateTitle();
			}
		});
	}


	void checkMarkerLimit() {
		IFilter iFilter = getFilter();
		if (iFilter == null || !(iFilter instanceof MarkerFilter)) {
			return; 
		}
		MarkerFilter filter = (MarkerFilter) iFilter;
		TableViewer viewer = getViewer();
		int itemCount = getRegistry().getItemCount();
		Composite parent = viewer.getTable().getParent();
		if (getFilter() == null || !filter.getFilterOnMarkerLimit() || itemCount <= filter.getMarkerLimit()) {
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
		int filteredCount = getRegistry().getItemCount();
		int totalCount = getRegistry().getRawItemCount();
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

}
