/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.UpdateMarkersOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.part.CellEditorActionHandler;

/**
 * The TaskView is the view for displaying task markers.
 */
public class TaskView extends MarkerView {

	private static final String COMPLETION = "completion"; //$NON-NLS-1$

	private final IField[] HIDDEN_FIELDS = { new FieldCreationTime() };

	private final static String[] ROOT_TYPES = { IMarker.TASK };

	private final static String[] TABLE_COLUMN_PROPERTIES = {
			 COMPLETION, IMarker.PRIORITY, IMarker.MESSAGE,
			Util.EMPTY_STRING, Util.EMPTY_STRING, Util.EMPTY_STRING };

	private final static String TAG_DIALOG_SECTION = "org.eclipse.ui.views.task"; //$NON-NLS-1$

	private final IField[] VISIBLE_FIELDS = {
			new FieldDone(), new FieldPriority(), new FieldMessage(),
			new FieldResource(), new FieldFolder(), new FieldLineNumber() };

	private ICellModifier cellModifier = new ICellModifier() {
		public Object getValue(Object element, String property) {
			if (element instanceof ConcreteMarker) {
				IMarker marker = ((ConcreteMarker) element).getMarker();

				if (COMPLETION.equals(property)) {
					return marker.getAttribute(IMarker.DONE, false) ? Boolean.TRUE : Boolean.FALSE;
				}

				if (IMarker.PRIORITY.equals(property)) {
					return new Integer(IMarker.PRIORITY_HIGH
							- marker.getAttribute(IMarker.PRIORITY,
									IMarker.PRIORITY_NORMAL));
				}

				if (IMarker.MESSAGE.equals(property)) {
					return marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
				}
			}

			return null;
		}

		public boolean canModify(Object element, String property) {
			return Util.isEditable(((ConcreteMarker) element).getMarker());
		}

		public void modify(Object element, String property, Object value) {
			if (element instanceof Item) {
				Item item = (Item) element;
				Object data = item.getData();

				if (data instanceof ConcreteMarker) {
					ConcreteMarker concreteMarker = (ConcreteMarker) data;

					IMarker marker = concreteMarker.getMarker();

					try {
						Object oldValue = getValue(data, property);
						if (oldValue != null && !oldValue.equals(value)) {
							Map attrs = new HashMap();
							if (COMPLETION.equals(property))
								attrs.put(IMarker.DONE, value);
							else if (IMarker.PRIORITY.equals(property))
								attrs.put(IMarker.PRIORITY,
										new Integer(IMarker.PRIORITY_HIGH
												- ((Integer) value).intValue()));
							else if (IMarker.MESSAGE.equals(property))
								attrs.put(IMarker.MESSAGE, value);
							if (!attrs.isEmpty()) {
								IUndoableOperation op = new UpdateMarkersOperation(marker, attrs, MarkerMessages.modifyTask_title, true);
						           PlatformUI.getWorkbench().getOperationSupport().getOperationHistory().execute(
						        		   op, null, WorkspaceUndoUtil.getUIInfoAdapter(getSite().getShell()));
							}
						}
						concreteMarker.refresh();
					} catch (ExecutionException e) {
						if (e.getCause() instanceof CoreException) {
							ErrorDialog.openError(
									getSite().getShell(),
									MarkerMessages.errorModifyingTask, null, ((CoreException)e.getCause()).getStatus()); 
						} else {
							// something rather unexpected occurred.
							IDEWorkbenchPlugin.log(MarkerMessages.errorModifyingTask, e); 
						}
					}				
				}
			}
		}
	};

	private CellEditorActionHandler cellEditorActionHandler;

	private ActionAddGlobalTask addGlobalTaskAction;

	private ActionDeleteCompleted deleteCompletedAction;

	private ActionMarkCompleted markCompletedAction;

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		TreeViewer treeViewer = getViewer();
		CellEditor cellEditors[] = new CellEditor[treeViewer.getTree()
				.getColumnCount()];
		cellEditors[0] = new CheckboxCellEditor(treeViewer.getTree());

		String[] priorities = new String[] { MarkerMessages.priority_high,
				MarkerMessages.priority_normal, MarkerMessages.priority_low };

		cellEditors[1] = new ComboBoxCellEditor(treeViewer.getTree(),
				priorities, SWT.READ_ONLY);
		CellEditor descriptionCellEditor = new TextCellEditor(treeViewer
				.getTree());
		cellEditors[2] = descriptionCellEditor;
		treeViewer.setCellEditors(cellEditors);
		treeViewer.setCellModifier(cellModifier);
		treeViewer.setColumnProperties(TABLE_COLUMN_PROPERTIES);

		cellEditorActionHandler = new CellEditorActionHandler(getViewSite()
				.getActionBars());
		cellEditorActionHandler.addCellEditor(descriptionCellEditor);
		cellEditorActionHandler.setCopyAction(copyAction);
		cellEditorActionHandler.setPasteAction(pasteAction);
		cellEditorActionHandler.setDeleteAction(deleteAction);
		cellEditorActionHandler.setSelectAllAction(selectAllAction);
		cellEditorActionHandler.setUndoAction(undoAction);
		cellEditorActionHandler.setRedoAction(redoAction);

	}

	public void dispose() {
		if (cellEditorActionHandler != null) {
			cellEditorActionHandler.dispose();
		}

		if (markCompletedAction != null) {
			markCompletedAction.dispose();
		}

		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.TableView#getDialogSettings()
	 */
	protected IDialogSettings getDialogSettings() {
		IDialogSettings workbenchSettings = IDEWorkbenchPlugin.getDefault()
				.getDialogSettings();
		IDialogSettings settings = workbenchSettings
				.getSection(TAG_DIALOG_SECTION);

		if (settings == null) {
			settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);
		}

		return settings;
	}

	protected void createActions() {
		super.createActions();

		addGlobalTaskAction = new ActionAddGlobalTask(this);
		deleteCompletedAction = new ActionDeleteCompleted(this, getViewer());
		markCompletedAction = new ActionMarkCompleted(getViewer());
		propertiesAction = new ActionTaskProperties(this, getViewer());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.TableView#getSortingFields()
	 */
	protected IField[] getSortingFields() {
		IField[] all = new IField[VISIBLE_FIELDS.length + HIDDEN_FIELDS.length];

		System.arraycopy(VISIBLE_FIELDS, 0, all, 0, VISIBLE_FIELDS.length);
		System.arraycopy(HIDDEN_FIELDS, 0, all, VISIBLE_FIELDS.length,
				HIDDEN_FIELDS.length);

		return all;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.TableView#getAllFields()
	 */
	protected IField[] getAllFields() {
		return getSortingFields();
	}

	protected String[] getRootTypes() {
		return ROOT_TYPES;
	}

	protected void initToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(addGlobalTaskAction);
		super.initToolBar(toolBarManager);
	}

	public void setSelection(IStructuredSelection structuredSelection,
			boolean reveal) {
		// TODO: added because nick doesn't like public API inherited from
		// internal classes
		super.setSelection(structuredSelection, reveal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerTypes()
	 */
	protected String[] getMarkerTypes() {
		return new String[] { IMarker.TASK };
	}

	protected String getStaticContextId() {
		return PlatformUI.PLUGIN_ID + ".task_list_view_context"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#createFiltersDialog()
	 */
	protected DialogMarkerFilter createFiltersDialog() {

		MarkerFilter[] filters = getUserFilters();
		TaskFilter[] taskFilters = new TaskFilter[filters.length];
		System.arraycopy(filters, 0, taskFilters, 0, filters.length);
		return new DialogTaskFilter(getSite().getShell(), taskFilters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#createFilter(java.lang.String)
	 */
	protected MarkerFilter createFilter(String name) {
		return new TaskFilter(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getSectionTag()
	 */
	protected String getSectionTag() {
		return TAG_DIALOG_SECTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerEnablementPreferenceName()
	 */
	String getMarkerEnablementPreferenceName() {
		return IDEInternalPreferences.LIMIT_TASKS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerLimitPreferenceName()
	 */
	String getMarkerLimitPreferenceName() {
		return IDEInternalPreferences.TASKS_LIMIT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getFiltersPreferenceName()
	 */
	String getFiltersPreferenceName() {
		return IDEInternalPreferences.TASKS_FILTERS;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.TableView#updateDirectionIndicator(org.eclipse.swt.widgets.TreeColumn)
	 */
	void updateDirectionIndicator(TreeColumn column) {
		// Do nothing due to images being obscured
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerName()
	 */
	protected String getMarkerName() {
		return MarkerMessages.task_title;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getUndoContext()
	 */
	protected IUndoContext getUndoContext() {
		return WorkspaceUndoUtil.getTasksUndoContext();
	}
}
