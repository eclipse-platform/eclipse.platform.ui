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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class TaskView extends MarkerView {

    private static final String COMPLETION = "completion"; //$NON-NLS-1$

    private final ColumnPixelData[] DEFAULT_COLUMN_LAYOUTS = {
			new ColumnPixelData(16, false, true),
			new ColumnPixelData(16, false, true),
			new ColumnPixelData(200), new ColumnPixelData(75),
			new ColumnPixelData(150), new ColumnPixelData(60) };

    private final IField[] HIDDEN_FIELDS = { new FieldCreationTime() };

    private final static String[] ROOT_TYPES = { IMarker.TASK };

    private final static String[] TABLE_COLUMN_PROPERTIES = { COMPLETION,
            IMarker.PRIORITY, IMarker.MESSAGE, "", //$NON-NLS-1$
            "", //$NON-NLS-1$
            "" //$NON-NLS-1$
    };

    private final static String TAG_DIALOG_SECTION = "org.eclipse.ui.views.task"; //$NON-NLS-1$

    private final IField[] VISIBLE_FIELDS = { new FieldDone(),
            new FieldPriority(), new FieldMessage(), new FieldResource(),
            new FieldFolder(), new FieldLineNumber() };

    private ICellModifier cellModifier = new ICellModifier() {
        public Object getValue(Object element, String property) {
            if (element instanceof ConcreteMarker) {
                IMarker marker = ((ConcreteMarker) element).getMarker();

                if (COMPLETION.equals(property))
                    return new Boolean(marker.getAttribute(IMarker.DONE, false));

                if (IMarker.PRIORITY.equals(property))
                    return new Integer(IMarker.PRIORITY_HIGH
                            - marker.getAttribute(IMarker.PRIORITY,
                                    IMarker.PRIORITY_NORMAL));

                if (IMarker.MESSAGE.equals(property))
                    return marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
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
                            if (COMPLETION.equals(property))
                                marker.setAttribute(IMarker.DONE, value);
                            else if (IMarker.PRIORITY.equals(property))
                                marker.setAttribute(IMarker.PRIORITY,
                                        IMarker.PRIORITY_HIGH
                                                - ((Integer) value).intValue());
                            else if (IMarker.MESSAGE.equals(property))
                                marker.setAttribute(IMarker.MESSAGE, value);
                        }

                        concreteMarker.refresh();
                    } catch (CoreException e) {
                        ErrorDialog
                                .openError(
                                        getSite().getShell(),
                                        MarkerMessages.errorModifyingTask, null, e.getStatus()); 
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

        TableViewer tableViewer = getViewer();
        CellEditor cellEditors[] = new CellEditor[tableViewer.getTable()
                .getColumnCount()];
        cellEditors[0] = new CheckboxCellEditor(tableViewer.getTable());

        String[] priorities = new String[] {
                MarkerMessages.priority_high,
                MarkerMessages.priority_normal,
                MarkerMessages.priority_low
        };

        cellEditors[1] = new ComboBoxCellEditor(tableViewer.getTable(),
                priorities, SWT.READ_ONLY);
        CellEditor descriptionCellEditor = new TextCellEditor(tableViewer
                .getTable());
        cellEditors[2] = descriptionCellEditor;
        tableViewer.setCellEditors(cellEditors);
        tableViewer.setCellModifier(cellModifier);
        tableViewer.setColumnProperties(TABLE_COLUMN_PROPERTIES);

        cellEditorActionHandler = new CellEditorActionHandler(getViewSite()
                .getActionBars());
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


    protected ColumnPixelData[] getDefaultColumnLayouts() {
        return DEFAULT_COLUMN_LAYOUTS;
    }

    protected IDialogSettings getDialogSettings() {
        AbstractUIPlugin plugin = (AbstractUIPlugin) Platform
                .getPlugin(PlatformUI.PLUGIN_ID);
        IDialogSettings workbenchSettings = plugin.getDialogSettings();
        IDialogSettings settings = workbenchSettings
                .getSection(TAG_DIALOG_SECTION);

        if (settings == null)
            settings = workbenchSettings.addNewSection(TAG_DIALOG_SECTION);

        return settings;
    }

    protected void createActions() {
        super.createActions();

        ISelectionProvider selProvider = getSelectionProvider();

        addGlobalTaskAction = new ActionAddGlobalTask(this);
        deleteCompletedAction = new ActionDeleteCompleted(this, selProvider);
        markCompletedAction = new ActionMarkCompleted(selProvider);
        propertiesAction = new ActionTaskProperties(this, selProvider);
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
    
    protected IField[] getHiddenFields() {
        return HIDDEN_FIELDS;
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

    public void setSelection(IStructuredSelection structuredSelection,
            boolean reveal) {
        // TODO: added because nick doesn't like public API inherited from internal classes
        super.setSelection(structuredSelection, reveal);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerTypes()
     */
    protected String[] getMarkerTypes() {
        return new String[] { IMarker.TASK };
    }

	protected String getStaticContextId() {
        return PlatformUI.PLUGIN_ID + ".task_list_view_context"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#createFiltersDialog()
	 */
	protected DialogMarkerFilter createFiltersDialog() {

		MarkerFilter[] filters = getUserFilters();
		TaskFilter[] taskFilters = new TaskFilter[filters.length];
		System.arraycopy(filters, 0, taskFilters, 0, filters.length);
		return new DialogTaskFilter(getSite().getShell(), taskFilters);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#createFilter(java.lang.String)
	 */
	protected MarkerFilter createFilter(String name) {
		return new TaskFilter(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getSectionTag()
	 */
	protected String getSectionTag() {
		return TAG_DIALOG_SECTION;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerEnablementPreferenceName()
	 */
	String getMarkerEnablementPreferenceName() {
		return IDEInternalPreferences.LIMIT_TASKS;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getMarkerLimitPreferenceName()
	 */
	String getMarkerLimitPreferenceName() {
		return IDEInternalPreferences.TASKS_LIMIT;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getFiltersPreferenceName()
	 */
	String getFiltersPreferenceName() {
		return IDEInternalPreferences.TASKS_FILTERS;
	}
}