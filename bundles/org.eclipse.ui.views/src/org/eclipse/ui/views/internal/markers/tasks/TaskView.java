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

package org.eclipse.ui.views.internal.markers.tasks;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.internal.markers.CreationTime;
import org.eclipse.ui.views.internal.markers.Folder;
import org.eclipse.ui.views.internal.markers.IField;
import org.eclipse.ui.views.internal.markers.IFilter;
import org.eclipse.ui.views.internal.markers.LineNumber;
import org.eclipse.ui.views.internal.markers.MarkerRegistry;
import org.eclipse.ui.views.internal.markers.MarkerUtil;
import org.eclipse.ui.views.internal.markers.MarkerView;
import org.eclipse.ui.views.internal.markers.Message;
import org.eclipse.ui.views.internal.markers.Resource;

public class TaskView extends MarkerView {

	private final static ColumnLayoutData[] DEFAULT_COLUMN_LAYOUTS = { 
		new ColumnPixelData(19, false),
		new ColumnPixelData(19, false),
		new ColumnWeightData(200),
		new ColumnWeightData(75),
		new ColumnWeightData(150),
		new ColumnWeightData(60)
	};
		
	private final static IField[] HIDDEN_FIELDS = { 
		new CreationTime() 
	};
	
	private final static String[] ROOT_TYPES = { 
		IMarker.TASK
	};

	private final static String[] TABLE_COLUMN_PROPERTIES = {
		TaskViewConstants.COMPLETION,
		IMarker.PRIORITY,
		IMarker.MESSAGE,
		"", //$NON-NLS-1$
		"", //$NON-NLS-1$
		""
	}; 
	
	private final static String TAG_DIALOG_SECTION = "org.eclipse.ui.views.task"; //$NON-NLS-1$

	private final static IField[] VISIBLE_FIELDS = { 
		new Completion(),
		new Priority(), 
		new Message(), 
		new Resource(), 
		new Folder(), 
		new LineNumber() 
	};

	private ICellModifier cellModifier = new ICellModifier() {
		public Object getValue(Object element, String property) {							
			if (element instanceof IMarker) {
				IMarker marker = (IMarker) element;
				
				if (TaskViewConstants.COMPLETION.equals(property))
					return new Boolean(marker.getAttribute(IMarker.DONE, false));

				if (IMarker.PRIORITY.equals(property))
					return new Integer(IMarker.PRIORITY_HIGH - marker.getAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL));
				
				if (IMarker.MESSAGE.equals(property))
					return marker.getAttribute(IMarker.MESSAGE, "");
			}

			return null;				
		}

		public boolean canModify(Object element, String property) {
			return MarkerUtil.isEditable((IMarker) element);
		}

		public void modify(Object element, String property, Object value) {
			if (element instanceof Item) {
				Item item = (Item) element;
				Object data = item.getData();
				
				if (data instanceof IMarker) {				
					IMarker marker = (IMarker) data;
					
					try {
						if (!getValue(marker, property).equals(value)) {
							if (TaskViewConstants.COMPLETION.equals(property))
								marker.setAttribute(IMarker.DONE, value);
							else if (IMarker.PRIORITY.equals(property))
								marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH - ((Integer) value).intValue());
							else if (IMarker.MESSAGE.equals(property))
								marker.setAttribute(IMarker.MESSAGE, value);
					
							if (taskFilter != null && !taskFilter.select(marker))
								filtersChanged();
						}
					} catch (CoreException e) {
						ErrorDialog.openError(getSite().getShell(), Messages.getString("errorModifyingTask") , null, e.getStatus()); //$NON-NLS-1$
					}
				}
			}
		}
	};

	private CellEditorActionHandler cellEditorActionHandler;	
	private TaskFilter taskFilter;
	private TaskRegistry taskRegistry;
	private AddGlobalTaskAction addGlobalTaskAction;
	private DeleteCompletedAction deleteCompletedAction;
	private MarkCompletedAction markCompletedAction;

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		TableViewer tableViewer = getViewer();
		CellEditor cellEditors[] = new CellEditor[tableViewer.getTable().getColumnCount()];
		cellEditors[0] = new CheckboxCellEditor(tableViewer.getTable());
		
		String[] priorities = new String[] { 
			Messages.getString("priority.high"), //$NON-NLS-1$
			Messages.getString("priority.normal"), //$NON-NLS-1$
			Messages.getString("priority.low") //$NON-NLS-1$
		};
		
		cellEditors[1] = new ComboBoxCellEditor(tableViewer.getTable(), priorities, SWT.READ_ONLY);
		CellEditor descriptionCellEditor = new TextCellEditor(tableViewer.getTable());
		cellEditors[2] = descriptionCellEditor;
		tableViewer.setCellEditors(cellEditors);
		tableViewer.setCellModifier(cellModifier);
		tableViewer.setColumnProperties(TABLE_COLUMN_PROPERTIES);
		
		cellEditorActionHandler = new CellEditorActionHandler(getViewSite().getActionBars());
		cellEditorActionHandler.addCellEditor(descriptionCellEditor);
		cellEditorActionHandler.setCopyAction(copyAction);
		cellEditorActionHandler.setPasteAction(pasteAction);
		cellEditorActionHandler.setDeleteAction(deleteAction);
		cellEditorActionHandler.setSelectAllAction(selectAllAction);
	}

	public void dispose() {
		if (cellEditorActionHandler != null)
			cellEditorActionHandler.dispose();		
		
		if (markCompletedAction != null)
			markCompletedAction.dispose();
		
		super.dispose();
	}

	public void init(IViewSite viewSite, IMemento memento) throws PartInitException {
		super.init(viewSite, memento);
		taskFilter = new TaskFilter();
		IDialogSettings dialogSettings = getDialogSettings();
		
		if (taskFilter != null)
			taskFilter.restoreState(dialogSettings);
			
		taskRegistry = TaskRegistry.getInstance();
		taskRegistry.setFilter(taskFilter);
		taskRegistry.setInput((IResource) getViewerInput());
	}

	public void saveState(IMemento memento) {
		IDialogSettings dialogSettings = getDialogSettings();
		
		if (taskFilter != null)
			taskFilter.saveState(dialogSettings);
		
		super.saveState(memento);	
	}

	protected ColumnLayoutData[] getDefaultColumnLayouts() {
		return DEFAULT_COLUMN_LAYOUTS;
	}

	protected IDialogSettings getDialogSettings() {
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		IDialogSettings workbenchSettings = plugin.getDialogSettings();
		IDialogSettings settings = workbenchSettings.getSection(TAG_DIALOG_SECTION);
		
		if (settings == null)
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);

		return settings;
	}

	protected void createActions() {
		super.createActions();
		addGlobalTaskAction = new AddGlobalTaskAction(this);
		deleteCompletedAction = new DeleteCompletedAction(this, getViewer(), getRegistry());
		markCompletedAction = new MarkCompletedAction(getViewer());
		propertiesAction = new TaskPropertiesAction(this, getViewer());
	}

	protected void createColumns(Table table) {
		super.createColumns(table);
		TableColumn[] columns = table.getColumns();
		
		if (columns != null && columns.length >= 1) {
			columns[0].setResizable(false);
		
			if (columns.length >= 2)
				columns[1].setResizable(false);	
		}
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(addGlobalTaskAction);
		manager.add(new Separator());
		super.fillContextMenu(manager);
	}

	protected void fillContextMenuAdditions(IMenuManager manager) {
		manager.add(new Separator());
		manager.add(markCompletedAction);
		manager.add(deleteCompletedAction);
	}

	protected IFilter getFilter() {
		return taskFilter;
	}
	
	protected Dialog getFiltersDialog() {
		return new TaskFiltersDialog(getSite().getShell(), taskFilter);
	}
	
	protected IField[] getHiddenFields() {
		return HIDDEN_FIELDS;
	}

	protected MarkerRegistry getRegistry() {
		return taskRegistry;
	}

	protected String[] getRootTypes() {
		return ROOT_TYPES;
	}

	protected Object getViewerInput() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	protected IField[] getVisibleFields() {
		return VISIBLE_FIELDS;
	}

	protected void initToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(addGlobalTaskAction);
		super.initToolBar(toolBarManager);
	}
}
