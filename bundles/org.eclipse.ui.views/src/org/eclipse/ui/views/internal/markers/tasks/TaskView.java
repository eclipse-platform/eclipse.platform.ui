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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.internal.markers.CreationTime;
import org.eclipse.ui.views.internal.markers.FiltersDialog;
import org.eclipse.ui.views.internal.markers.Folder;
import org.eclipse.ui.views.internal.markers.IField;
import org.eclipse.ui.views.internal.markers.LineNumber;
import org.eclipse.ui.views.internal.markers.MarkerFilter;
import org.eclipse.ui.views.internal.markers.MarkerRegistry;
import org.eclipse.ui.views.internal.markers.MarkerUtil;
import org.eclipse.ui.views.internal.markers.MarkerView;
import org.eclipse.ui.views.internal.markers.Message;
import org.eclipse.ui.views.internal.markers.Resource;


public class TaskView extends MarkerView {
	
	static final String TAG_DIALOG_SECTION = "org.eclipse.ui.views.tasklist"; //$NON-NLS-1$
	
	private static final ColumnLayoutData[] DEFAULT_COLUMN_LAYOUTS =
		{
			new ColumnPixelData(19, false),
			new ColumnPixelData(19, false),
			new ColumnWeightData(200),
			new ColumnWeightData(75),
			new ColumnWeightData(150),
			new ColumnWeightData(60)};
			
	private static String[] tableColumnProperties =
		{
			TaskViewConstants.COMPLETION,
			IMarker.PRIORITY,
			IMarker.MESSAGE,
			"", //$NON-NLS-1$
			"", //$NON-NLS-1$
			""  }; //$NON-NLS-1$
			
	private ICellModifier cellModifier = new ICellModifier() {
		public Object getValue(Object element, String property) {
			return TaskView.this.getValue(element, property);
		}
		public boolean canModify(Object element, String property) {
			return MarkerUtil.isEditable((IMarker) element);
		}
		/**
		 * Modifies a marker as a result of a successfully completed direct editing.
		 */
		public void modify(Object element, String property, Object value) {
			Item item = (Item) element;
			IMarker marker = (IMarker) item.getData();
			setProperty(marker, property, value);
		}
	};

	protected SelectionProviderAction markCompletedAction;
	protected Action deleteCompletedAction;
	
	private CellEditorActionHandler editorActionHandler;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		TableViewer viewer = getViewer();
		CellEditor editors[] = new CellEditor[viewer.getTable().getColumnCount()];
		editors[0] = new CheckboxCellEditor(viewer.getTable());
		String[] priorities = new String[] { Messages.getString("priority.high"), //$NON-NLS-1$
			Messages.getString("priority.normal"), //$NON-NLS-1$
			Messages.getString("priority.low") //$NON-NLS-1$
		};
		editors[1] = new ComboBoxCellEditor(viewer.getTable(), priorities, SWT.READ_ONLY);
		CellEditor descriptionEditor = new TextCellEditor(viewer.getTable());
		editors[2] = descriptionEditor;
		viewer.setCellEditors(editors);
		viewer.setCellModifier(cellModifier);
		viewer.setColumnProperties(tableColumnProperties);
		
		//Add global action handlers.	
		editorActionHandler =
			new CellEditorActionHandler(getViewSite().getActionBars());
		editorActionHandler.addCellEditor(descriptionEditor);
		editorActionHandler.setCopyAction(copyAction);
		editorActionHandler.setPasteAction(pasteAction);
		editorActionHandler.setDeleteAction(deleteAction);
		editorActionHandler.setSelectAllAction(selectAllAction);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getRootTypes()
	 */
	protected String[] getRootTypes() {
		return new String[] {IMarker.TASK};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getDialogSettings()
	 */
	protected IDialogSettings getDialogSettings() {
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		IDialogSettings workbenchSettings = plugin.getDialogSettings();
		IDialogSettings settings = workbenchSettings.getSection(TAG_DIALOG_SECTION);
		if (settings == null) {
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);
		}
		return settings;
	}
	
	protected IField[] getVisibleFields() {
		return new IField[] {new Completion(),
							   new Priority(),
							   new Message(), 
							   new Resource(), 
							   new Folder(), 
							   new LineNumber()
		};
	}
	
	protected IField[] getHiddenFields() {
		return new IField[] {new CreationTime()
		};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#createColumns(org.eclipse.swt.widgets.Table)
	 */
	protected void createColumns(Table table) {
		super.createColumns(table);
		TableColumn[] columns = table.getColumns();
		if (columns != null && columns.length >= 2) {
			columns[0].setResizable(false);
			columns[1].setResizable(false);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getRegistry()
	 */
	protected MarkerRegistry getRegistry() {
		return TaskRegistry.getInstance();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#createActions()
	 */
	protected void createActions() {
		super.createActions();
		propertiesAction = new TaskPropertiesAction(this, getViewer());
		markCompletedAction = new MarkCompletedAction(getViewer());
		deleteCompletedAction = new DeleteCompletedAction(this, getRegistry());
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#fillContextMenuAdditions(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenuAdditions(IMenuManager manager) {
		manager.add(new Separator());
		manager.add(markCompletedAction);
		manager.add(deleteCompletedAction);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getFilter()
	 */
	protected MarkerFilter getFilter() {
		if (filter == null) {
			filter = new TaskFilter();
			filter.restoreState(getDialogSettings());
		}
		return filter;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getColumnLayouts()
	 */
	protected ColumnLayoutData[] getDefaultColumnLayouts() {
		return DEFAULT_COLUMN_LAYOUTS;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#dispose()
	 */
	public void dispose() {
		super.dispose();
		markCompletedAction.dispose();
		if (editorActionHandler != null) {
			editorActionHandler.dispose();
			editorActionHandler = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markerview.MarkerView#getFiltersDialog()
	 */
	protected FiltersDialog getFiltersDialog() {
		if (getFilter() != null && getFilter() instanceof TaskFilter) {
			return new TaskFiltersDialog(getSite().getShell(), (TaskFilter) getFilter());
		}
		return super.getFiltersDialog();
	}

	/**
	 * Sets the property on a marker to the given value.
	 *
	 * @exception CoreException if an error occurs setting the value
	 */
	protected void setProperty(IMarker marker, String property, Object value) {
		if (getValue(marker, property).equals(value)) {
			return;
		}
		try {
			if (property.equals(TaskViewConstants.COMPLETION)) { // Completed
				marker.setAttribute(IMarker.DONE, value);
			} 
			else if (property.equals(IMarker.PRIORITY)) { // Priority
				// this property is used only by cell editor, where order is High, Normal, Low
				marker.setAttribute(
					IMarker.PRIORITY,
					IMarker.PRIORITY_HIGH - ((Integer) value).intValue());
			} 
			else if (property.equals(IMarker.MESSAGE)) { // Description
				marker.setAttribute(IMarker.MESSAGE, value);
			}
			MarkerFilter filter = getFilter();
			if (filter != null && !filter.select(marker)) {
				filtersChanged();
			}
		} catch (CoreException e) {
			String msg = Messages.getString("errorModifyingTask"); //$NON-NLS-1$
			ErrorDialog.openError(
				getSite().getShell(),
				msg,
				null,
				e.getStatus());
		}
	}
	
	private Object getValue(Object element, String property) {
		if (!(element instanceof IMarker)) {
			return null;
		}
		IMarker marker = (IMarker) element;
		if (property.equals(TaskViewConstants.COMPLETION)) {
			return new Boolean(marker.getAttribute(IMarker.DONE, false));
		}
		if (property.equals(IMarker.PRIORITY)) {
			int priority = marker.getAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
			priority = IMarker.PRIORITY_HIGH - priority;
			return new Integer(priority);
		}
		if (property.equals(IMarker.MESSAGE)) {
			return (new Message()).getValue(element);
		}
		return null;
	}
	
}
