package org.eclipse.ui.views.tasklist;

/**********************************************************************
Copyright (c) 2000, 2001, 2002, International Business Machines Corp and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
  Cagatay Kavukcuoglu <cagatayk@acm.org> - Filter for markers in same project
**********************************************************************/

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.*;

import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Main class for the Task List view for displaying tasks and problem annotations
 * on resources, and for opening an editor on the resource when the user commands.
 * <p>
 * This standard view has id <code>"org.eclipse.ui.views.TaskList"</code>.
 * </p>
 * <p>
 * The workbench will automatically instantiate this class when a Task List
 * view is needed for a workbench window. This class is not intended
 * to be instantiated or subclassed by clients.
 * </p>
 */
public class TaskList extends ViewPart {
	
	private Table table;
	private TableEditor tableEditor;
	private MenuManager contextMenu;
	
	private CellEditor descriptionEditor;
	private TableViewer viewer;
	private TasksFilter filter = new TasksFilter();
	private IMemento memento;

	private CellEditorActionHandler editorActionHandler;
	private TaskAction newTaskAction;
	private TaskAction copyTaskAction;
	private TaskAction pasteTaskAction;
	private TaskAction removeTaskAction;
	private TaskAction purgeCompletedAction;
	private TaskAction gotoTaskAction;
	private TaskAction selectAllAction;
	private TaskAction resolveMarkerAction;
	private TaskAction filtersAction;
	private TaskAction markCompletedAction;
	private TaskAction propertiesAction;
	
	private Clipboard clipboard;
	 
	private static String[] tableColumnProperties = {
		IBasicPropertyConstants.P_IMAGE,
		IMarker.DONE,
		IMarker.PRIORITY,
		IMarker.MESSAGE,
		IMarkerConstants.P_RESOURCE_NAME,
		IMarkerConstants.P_CONTAINER_NAME,
		IMarkerConstants.P_LINE_AND_LOCATION};

	// Persistance tags.
	private static final String TAG_COLUMN = "column"; //$NON-NLS-1$
	private static final String TAG_NUMBER = "number"; //$NON-NLS-1$
	private static final String TAG_WIDTH = "width"; //$NON-NLS-1$
	private static final String TAG_SORTER_COLUMN = "sorterColumn";  //$NON-NLS-1$
	private static final String TAG_SORTER_REVERSED = "sorterReversed";  //$NON-NLS-1$
	private static final String TAG_FILTER = "filter";  //$NON-NLS-1$
	private static final String TAG_SELECTION = "selection";  //$NON-NLS-1$
	private static final String TAG_ID = "id"; //$NON-NLS-1$
	private static final String TAG_MARKER = "marker"; //$NON-NLS-1$
	private static final String TAG_RESOURCE = "resource"; //$NON-NLS-1$
	private static final String TAG_TOP_INDEX = "topIndex"; //$NON-NLS-1$

	static class TaskListLabelProvider
		extends LabelProvider
		implements ITableLabelProvider {

		private static String[] keys = {
			IBasicPropertyConstants.P_IMAGE, 
			IMarkerConstants.P_COMPLETE_IMAGE, 
			IMarkerConstants.P_PRIORITY_IMAGE, 
			IMarker.MESSAGE, 
			IMarkerConstants.P_RESOURCE_NAME, 
			IMarkerConstants.P_CONTAINER_NAME, 
			IMarkerConstants.P_LINE_AND_LOCATION};
				
		public String getColumnText(Object element, int columnIndex) {
			if (columnIndex >= 3 && columnIndex <= 6)
				return (String) MarkerUtil.getProperty(element, keys[columnIndex]);
			return ""; //$NON-NLS-1$
		}
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex >= 0 && columnIndex <= 2) {
				return (Image) MarkerUtil.getProperty(element, keys[columnIndex]);
			}
			return null;
		}
	}

	private String columnHeaders[] = {
		TaskListMessages.getString("TaskList.headerIcon"), //$NON-NLS-1$
		TaskListMessages.getString("TaskList.headerCompleted"), //$NON-NLS-1$
		TaskListMessages.getString("TaskList.headerPriority"), //$NON-NLS-1$
		TaskListMessages.getString("TaskList.headerDescription"), //$NON-NLS-1$
		TaskListMessages.getString("TaskList.headerResource"), //$NON-NLS-1$
		TaskListMessages.getString("TaskList.headerFolder"), //$NON-NLS-1$
		TaskListMessages.getString("TaskList.headerLocation") //$NON-NLS-1$
	};

	private ColumnLayoutData columnLayouts[] = {
		new ColumnPixelData(19, false),
		new ColumnPixelData(19, false),
		new ColumnPixelData(19, false),
		new ColumnWeightData(200),
		new ColumnWeightData(75),
		new ColumnWeightData(150),
		new ColumnWeightData(60)};
	

	private IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
			TaskList.this.partActivated(part);
		}
		public void partBroughtToTop(IWorkbenchPart part) {
		}
		public void partClosed(IWorkbenchPart part) {
			TaskList.this.partClosed(part);
		}
		public void partDeactivated(IWorkbenchPart part) {
		}
		public void partOpened(IWorkbenchPart part) {
		}
	};
	private ISelectionChangedListener focusSelectionChangedListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			TaskList.this.focusSelectionChanged(event);
		}
	};
	private IResource focusResource;
	private IWorkbenchPart focusPart;
	private ISelectionProvider focusSelectionProvider;

	private ICellModifier cellModifier = new ICellModifier() {
		public Object getValue(Object element, String property) {
			return MarkerUtil.getProperty(element,property);
		}

		public boolean canModify(Object element, String property) {
			IMarker marker = (IMarker) element;
			return MarkerUtil.isMarkerType(marker, IMarker.TASK);
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
		
/**
 * Creates a new task list view.
 */
public TaskList() {
	super();
}
void addDragSupport(Control control) {

	int operations = DND.DROP_COPY;
	Transfer[] transferTypes = new Transfer[]{MarkerTransfer.getInstance(), 
		TextTransfer.getInstance()};
	DragSourceListener listener = new DragSourceAdapter() {
		public void dragSetData(DragSourceEvent event){
			performDragSetData(event);
		}
		public void dragFinished(DragSourceEvent event){
		}
	};
	viewer.addDragSupport(operations, transferTypes, listener);	
}
void cancelEditing() {
	getTableViewer().cancelEditing();
}
void createColumns() {
	/**
 	 * This class handles selections of the column headers.
	 * Selection of the column header will cause resorting
	 * of the shown tasks using that column's sorter.
	 * Repeated selection of the header will toggle
	 * sorting order (ascending versus descending).
	 */
	SelectionListener headerListener = new SelectionAdapter() {
		/**
		 * Handles the case of user selecting the
		 * header area.
		 * <p>If the column has not been selected previously,
		 * it will set the sorter of that column to be
		 * the current tasklist sorter. Repeated
		 * presses on the same column header will
		 * toggle sorting order (ascending/descending).
		 */
		public void widgetSelected(SelectionEvent e) {
			// column selected - need to sort
			int column = table.indexOf((TableColumn) e.widget);
			TaskSorter oldSorter = (TaskSorter) viewer.getSorter();
			if (oldSorter != null && column == oldSorter.getColumnNumber()) {
				oldSorter.setReversed(!oldSorter.isReversed());
				viewer.refresh();
			} else {
				viewer.setSorter(new TaskSorter(TaskList.this, column));
			}
		}
	};

	if(memento != null) {
		//restore columns width
		IMemento children[] = memento.getChildren(TAG_COLUMN);
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				Integer val = children[i].getInteger(TAG_NUMBER);
				if (val != null) {
					int index = val.intValue();
					val = children[i].getInteger(TAG_WIDTH);
					if (val != null) {
						columnLayouts[index] = new ColumnPixelData(val.intValue(), true);
					}
				}
			}
		}
	}
	
	TableLayout layout = new TableLayout();
	table.setLayout(layout);
	table.setHeaderVisible(true);
	for (int i = 0; i < columnHeaders.length; i++) {
		layout.addColumnData(columnLayouts[i]);
		TableColumn tc = new TableColumn(table, SWT.NONE,i);
		tc.setResizable(columnLayouts[i].resizable);
		tc.setText(columnHeaders[i]);
		tc.addSelectionListener(headerListener);
	}

}
/**
 * Returns a string that summarizes the contents of the
 * given markers.
 */
/*package*/ static String createMarkerReport(IMarker[] markers) {
	StringBuffer buf = new StringBuffer();
	// Create the header
	buf.append(TaskListMessages.getString("TaskList.reportKind")); //$NON-NLS-1$
	buf.append("\t"); //$NON-NLS-1$
	buf.append(TaskListMessages.getString("TaskList.reportStatus")); //$NON-NLS-1$
	buf.append("\t"); //$NON-NLS-1$
	buf.append(TaskListMessages.getString("TaskList.reportPriority")); //$NON-NLS-1$
	buf.append("\t"); //$NON-NLS-1$
	buf.append(TaskListMessages.getString("TaskList.headerDescription")); //$NON-NLS-1$
	buf.append("\t"); //$NON-NLS-1$
	buf.append(TaskListMessages.getString("TaskList.headerResource")); //$NON-NLS-1$
	buf.append("\t"); //$NON-NLS-1$
	buf.append(TaskListMessages.getString("TaskList.headerFolder")); //$NON-NLS-1$
	buf.append("\t"); //$NON-NLS-1$
	buf.append(TaskListMessages.getString("TaskList.headerLocation")); //$NON-NLS-1$
	buf.append(System.getProperty("line.separator")); //$NON-NLS-1$
	
	// Create the report for the markers
	for (int i = 0; i < markers.length; i++) {
		writeMarker(buf, markers[i]);
	}
	return buf.toString();
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public void createPartControl(Composite parent) {
	clipboard = new Clipboard(parent.getDisplay());
	createTable(parent);
	viewer = new TableViewer(table);
	viewer.setUseHashlookup(true);
	createColumns();
	makeActions();
	fillActionBars();
	addDragSupport(table);

	viewer.setContentProvider(new TaskListContentProvider(this));
	viewer.setLabelProvider(new TaskListLabelProvider());
	viewer.setSorter(new TaskSorter(this, 5));
	if(memento != null) {
		//restore filter
		IMemento filterMem = memento.getChild(TAG_FILTER);
		if(filterMem != null)
			getFilter().restoreState(filterMem);
		//restore sorter
		Integer columnNumber = memento.getInteger(TAG_SORTER_COLUMN);
		if(columnNumber != null) {
			boolean reversed = memento.getInteger(TAG_SORTER_REVERSED).intValue() == 1;
			TaskSorter sorter = new TaskSorter(this, columnNumber.intValue());
			sorter.setReversed(reversed);
			viewer.setSorter(sorter);
		}
	}
	viewer.setInput(getWorkspace().getRoot());
	viewer.addSelectionChangedListener(new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			TaskList.this.selectionChanged(event);
		}
	});
	viewer.addOpenListener(new IOpenListener() {
		public void open(OpenEvent event) {
			gotoTaskAction.run();
		}
	});
	viewer.getControl().addKeyListener(new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			handleKeyPressed(e);
		}
	});
	
	CellEditor editors[] = new CellEditor[columnHeaders.length];
	editors[1] = new CheckboxCellEditor(table);
	editors[2] = new ComboBoxCellEditor(table,new String[] {
		TaskListMessages.getString("TaskList.high"), //$NON-NLS-1$
		TaskListMessages.getString("TaskList.normal"), //$NON-NLS-1$
		TaskListMessages.getString("TaskList.low") //$NON-NLS-1$
	});
	editors[3] = descriptionEditor = new TextCellEditor(table);
	viewer.setCellEditors(editors);
	viewer.setCellModifier(cellModifier);
	viewer.setColumnProperties(tableColumnProperties);

	// Configure the context menu to be lazily populated on each pop-up.
	MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
	menuMgr.setRemoveAllWhenShown(true);
	menuMgr.addMenuListener(
		new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				TaskList.this.fillContextMenu(manager);
			}
		});
	Menu menu = menuMgr.createContextMenu(table);
	table.setMenu(menu);
	// Be sure to register it so that other plug-ins can add actions.
	getSite().registerContextMenu(menuMgr, viewer);
	this.contextMenu = menuMgr;
	
	// Track selection in the page.
	getSite().getPage().addPartListener(partListener);

	// Add global action handlers.
	editorActionHandler = new CellEditorActionHandler(getViewSite().getActionBars());
	editorActionHandler.addCellEditor(descriptionEditor);
	editorActionHandler.setCopyAction(copyTaskAction);
	editorActionHandler.setPasteAction(pasteTaskAction);
	editorActionHandler.setDeleteAction(removeTaskAction);
	editorActionHandler.setSelectAllAction(selectAllAction);
	
	getViewSite().getActionBars().setGlobalActionHandler(
		IWorkbenchActionConstants.PROPERTIES,
		propertiesAction);
		
	
	if (memento != null) restoreState(memento);
	memento = null;
	
	// Set help on the view itself
	viewer.getControl().addHelpListener(new HelpListener() {
		/*
		 * @see HelpListener#helpRequested(HelpEvent)
		 */
		public void helpRequested(HelpEvent e) {
			String contextId = null;
			// See if there is a context registered for the current selection
			IMarker marker = (IMarker)((IStructuredSelection)getSelection()).getFirstElement();
			if (marker != null) {
				IWorkbench workbench = getViewSite().getWorkbenchWindow().getWorkbench();
				contextId = workbench.getMarkerHelpRegistry().getHelp(marker);
			}
			
			if (contextId == null) 
				contextId = ITaskListHelpContextIds.TASK_LIST_VIEW;

			WorkbenchHelp.displayHelp(contextId);
		}
	});

	// Prime the status line and title.
	updateStatusMessage();
	updateTitle();
}
/**
 * Creates the table control.
 */
void createTable(Composite parent) {
	table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
	table.setLinesVisible(true);
	//table.setLayout(new TableLayout());
	
	tableEditor = new TableEditor(table);
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public void dispose() {
	super.dispose();
	getSite().getPage().removePartListener(partListener);
	if (focusSelectionProvider != null) {
		focusSelectionProvider.removeSelectionChangedListener(focusSelectionChangedListener);
		focusSelectionProvider = null;
	}
	focusPart = null;	
	if (editorActionHandler != null) {
		editorActionHandler.dispose();
		editorActionHandler = null;
	}
	if (clipboard != null)
		clipboard.dispose();
}
/**
 * Activates the editor on the given marker.
 */
public void edit(IMarker marker) {
	viewer.editElement(marker, 3);
}
/**
 * Fills the local tool bar and menu manager with actions.
 */
void fillActionBars() {
	IActionBars actionBars = getViewSite().getActionBars();
	IToolBarManager toolBar = actionBars.getToolBarManager();
	toolBar.add(newTaskAction);
	toolBar.add(removeTaskAction);
	toolBar.add(filtersAction);
}
/**
 * Contributes actions to the pop-up menu.
 */
void fillContextMenu(IMenuManager menu) {
	menu.add(newTaskAction);
	menu.add(copyTaskAction);
	menu.add(pasteTaskAction);
	menu.add(removeTaskAction);
	menu.add(gotoTaskAction);
	menu.add(new Separator());
	menu.add(markCompletedAction);
	menu.add(purgeCompletedAction);
	menu.add(new Separator());
	menu.add(resolveMarkerAction);
	menu.add(new Separator());
	menu.add(filtersAction);
	menu.add(new Separator());
	menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));	
	menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS+"-end"));	 //$NON-NLS-1$
	menu.add(new Separator());
	menu.add(propertiesAction);
}
/**
 * The filter settings have changed.
 * Refreshes the viewer and title bar.
 */
void filterChanged() {
	
	BusyIndicator.showWhile(viewer.getControl().getShell().getDisplay(), new Runnable() {
		public void run() {
		// Filter has already been updated by dialog; just refresh.
		// Don't need to update labels for existing elements 
		// since changes to filter settings don't affect them.
		viewer.getControl().setRedraw(false);
		viewer.refresh(false);
		viewer.getControl().setRedraw(true);
		// update after refresh since the content provider caches summary info
		updateStatusMessage();
		updateTitle();}
	});

}
void focusSelectionChanged(SelectionChangedEvent event) {
	updateFocusResource(event.getSelection());
}

/**
 * Returns a clipboard for cut/copy/paste actions.
 * <p>
 * May only be called after this part's viewer has been created.
 * The clipboard is disposed when this part is disposed.
 * </p>
 * @return a clipboard
 * @since 2.0
 */
/*package*/ Clipboard getClipboard() {
	return clipboard;
} 

/**
 * Returns the filter for the viewer.
 */
TasksFilter getFilter() {
	return filter;
}

/**
 * Returns the UI plugin for the task list.
 */
static AbstractUIPlugin getPlugin() {
	return (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
}
/**
 * Returns the resource for which the task list is showing tasks.
 *
 * @return the resource, possibly the workspace root
 */
public IResource getResource() {
	if (showSelections()) {
		if (focusResource != null) {
			return focusResource;
		}
	}
	return getWorkspace().getRoot();
}
/**
 * Returns the resource depth which the task list is using to show tasks.
 *
 * @return an <code>IResource.DEPTH_*</code> constant
 */
int getResourceDepth() {
	if (showSelections() && !showChildrenHierarchy())
		return IResource.DEPTH_ZERO;
	else
		return IResource.DEPTH_INFINITE;
}
/**
 * API method which returns the current selection.
 *
 * @return the current selection (element type: <code>IMarker</code>)
 */
public ISelection getSelection() {
	return viewer.getSelection();
}
/**
 * Returns the message to display in the status line.
 */
String getStatusMessage(IStructuredSelection selection) {
	if (selection.size() == 1) {
		IMarker marker = (IMarker) selection.getFirstElement();
		return MarkerUtil.getMessage(marker);
	}
	TaskListContentProvider provider = (TaskListContentProvider) viewer.getContentProvider();
	if (selection.size() > 1) {
		return provider.getStatusSummary(selection);
	}
	return provider.getStatusSummary();
}
/**
 * When created, new task instance is cached in
 * order to keep it at the top of the list until
 * first edited. This method returns it, or
 * null if there is no task instance pending
 * for first editing.
 */
TableViewer getTableViewer() {
	return viewer;
}
/**
 * Returns the workspace.
 */
IWorkspace getWorkspace() {
	return ResourcesPlugin.getWorkspace();
}
/**
 * Handles key events in viewer.
 */
void handleKeyPressed(KeyEvent event) {
	if (event.character == SWT.DEL && event.stateMask == 0 
		&& removeTaskAction.isEnabled())
		removeTaskAction.run();
}
/* (non-Javadoc)
 * Method declared on IViewPart.
 */
public void init(IViewSite site,IMemento memento) throws PartInitException {
	super.init(site,memento);
	this.memento = memento;
}

/**
 * Returns whether we are interested in the given marker delta.
 */
boolean isAffectedBy(IMarkerDelta markerDelta) {
	return checkResource(markerDelta.getResource()) && getFilter().select(markerDelta);
}

/**
 * Returns whether the given marker is a subtype of one of the root types.
 */
boolean isRootType(IMarker marker) {
	String[] types = TasksFilter.ROOT_TYPES;
	for (int i = 0; i < types.length; ++i) {
		if (MarkerUtil.isMarkerType(marker, types[i])) {
			return true;
		}
	}
	return false;
}	

/**
 * Returns whether the given marker delta is a subtype of one of the root types.
 */
boolean isRootType(IMarkerDelta markerDelta) {
	String[] types = TasksFilter.ROOT_TYPES;
	for (int i = 0; i < types.length; ++i) {
		if (markerDelta.isSubtypeOf(types[i])) {
			return true;
		}
	}
	return false;
}

/**
 * Returns whether we are interested in markers on the given resource.
 */
boolean checkResource(IResource resource) {
	if (!showSelections()) {
		return true;
	}
	if (showOwnerProject()) {
		IProject currentProj = getResource().getProject();
		return currentProj == null ? true : currentProj.equals(resource.getProject());
	}
	if (showChildrenHierarchy()) {
		return getResource().getFullPath().isPrefixOf(resource.getFullPath());
	}
	else {
		return resource.equals(getResource());
	}
}

/**
 * Returns whether the given marker should be shown,
 * given the current filter settings.
 */
boolean shouldShow(IMarker marker) {
	return checkResource(marker.getResource()) && getFilter().select(marker);
}

/**
 * Makes actions used in the local tool bar and
 * popup menu.
 */
void makeActions() {
	// goto
	gotoTaskAction = new GotoTaskAction(this, "gotoFile"); //$NON-NLS-1$
	gotoTaskAction.setText(TaskListMessages.getString("GotoTask.text")); //$NON-NLS-1$
	gotoTaskAction.setToolTipText(TaskListMessages.getString("GotoTask.tooltip")); //$NON-NLS-1$
	gotoTaskAction.setHoverImageDescriptor(MarkerUtil.getImageDescriptor("gotoobj")); //$NON-NLS-1$
	gotoTaskAction.setImageDescriptor(MarkerUtil.getImageDescriptor("gotoobj_grey")); //$NON-NLS-1$
	gotoTaskAction.setEnabled(false);

	// new task
	newTaskAction = new NewTaskAction(this, "newTask"); //$NON-NLS-1$
	newTaskAction.setText(TaskListMessages.getString("NewTask.text")); //$NON-NLS-1$
	newTaskAction.setToolTipText(TaskListMessages.getString("NewTask.tooltip")); //$NON-NLS-1$
	newTaskAction.setHoverImageDescriptor(MarkerUtil.getImageDescriptor("addtsk")); //$NON-NLS-1$
	newTaskAction.setImageDescriptor(MarkerUtil.getImageDescriptor("addtsk_grey")); //$NON-NLS-1$
	newTaskAction.setDisabledImageDescriptor(MarkerUtil.getImageDescriptor("addtsk_disabled")); //$NON-NLS-1$

	// copy task
	copyTaskAction = new CopyTaskAction(this, "copy"); //$NON-NLS-1$
	copyTaskAction.setText(TaskListMessages.getString("CopyTask.text")); //$NON-NLS-1$
	copyTaskAction.setToolTipText(TaskListMessages.getString("CopyTask.tooltip")); //$NON-NLS-1$
	copyTaskAction.setHoverImageDescriptor(MarkerUtil.getImageDescriptor("copy")); //$NON-NLS-1$
	copyTaskAction.setEnabled(false);

	// paste task
	pasteTaskAction = new PasteTaskAction(this, "paste"); //$NON-NLS-1$
	pasteTaskAction.setText(TaskListMessages.getString("PasteTask.text")); //$NON-NLS-1$
	pasteTaskAction.setToolTipText(TaskListMessages.getString("PasteTask.tooltip")); //$NON-NLS-1$
	pasteTaskAction.setHoverImageDescriptor(MarkerUtil.getImageDescriptor("paste")); //$NON-NLS-1$
	pasteTaskAction.setEnabled(false);
	
	// remove task
	removeTaskAction = new RemoveTaskAction(this, "delete"); //$NON-NLS-1$
	removeTaskAction.setText(TaskListMessages.getString("RemoveTask.text")); //$NON-NLS-1$
	removeTaskAction.setToolTipText(TaskListMessages.getString("RemoveTask.tooltip")); //$NON-NLS-1$
	removeTaskAction.setHoverImageDescriptor(MarkerUtil.getImageDescriptor("remtsk")); //$NON-NLS-1$
	removeTaskAction.setImageDescriptor(MarkerUtil.getImageDescriptor("remtsk_grey")); //$NON-NLS-1$
	removeTaskAction.setDisabledImageDescriptor(MarkerUtil.getImageDescriptor("remtsk_disabled")); //$NON-NLS-1$
	removeTaskAction.setEnabled(false);

	//mark completed task
	markCompletedAction = new MarkCompletedAction(this,"markCompleted"); //$NON-NLS-1$
	markCompletedAction.setText(TaskListMessages.getString("MarkCompleted.text")); //$NON-NLS-1$
	markCompletedAction.setToolTipText(TaskListMessages.getString("MarkCompleted.tooltip")); //$NON-NLS-1$
	markCompletedAction.setEnabled(false);
	
	//delete completed task
	purgeCompletedAction = new PurgeCompletedAction(this, "deleteCompleted"); //$NON-NLS-1$
	purgeCompletedAction.setText(TaskListMessages.getString("PurgeCompleted.text")); //$NON-NLS-1$
	purgeCompletedAction.setToolTipText(TaskListMessages.getString("PurgeCompleted.tooltip")); //$NON-NLS-1$
	purgeCompletedAction.setImageDescriptor(MarkerUtil.getImageDescriptor("delete_edit")); //$NON-NLS-1$
	purgeCompletedAction.setEnabled(true);

	// select all
	selectAllAction = new SelectAllTasksAction(this, "selectAll"); //$NON-NLS-1$
	selectAllAction.setText(TaskListMessages.getString("SelectAll.text")); //$NON-NLS-1$
	selectAllAction.setToolTipText(TaskListMessages.getString("SelectAll.tooltip")); //$NON-NLS-1$

	// resolutions
	resolveMarkerAction = new ResolveMarkerAction(this, "resolve"); //$NON-NLS-1$
	resolveMarkerAction.setText(TaskListMessages.getString("Resolve.text")); //$NON-NLS-1$
	resolveMarkerAction.setToolTipText(TaskListMessages.getString("Resolve.tooltip")); //$NON-NLS-1$

	// filters...
	filtersAction = new FiltersAction(this, "filter"); //$NON-NLS-1$
	filtersAction.setText(TaskListMessages.getString("Filters.text")); //$NON-NLS-1$
	filtersAction.setToolTipText(TaskListMessages.getString("Filters.tooltip")); //$NON-NLS-1$
	filtersAction.setImageDescriptor(MarkerUtil.getImageDescriptor("filter")); //$NON-NLS-1$
	
	// properties
	propertiesAction = new TaskPropertiesAction(this, "properties"); //$NON-NLS-1$
	propertiesAction.setText(TaskListMessages.getString("Properties.text")); //$NON-NLS-1$
	propertiesAction.setToolTipText(TaskListMessages.getString("Properties.tooltip")); //$NON-NLS-1$
	propertiesAction.setEnabled(false);
}
/**
 * The markers have changed.  Update the status line and title bar.
 */
void markersChanged() {
	// Only update the status if active, since this may be expensive.
	if (getSite().getPage().getActivePart() == this) {
		updateStatusMessage();
	}
	// Always update the title, since it can be seen even if not active
	updateTitle();
}
void partActivated(IWorkbenchPart part) {
	if (part == focusPart)
		return;
		
	if (focusSelectionProvider != null) {
		focusSelectionProvider.removeSelectionChangedListener(focusSelectionChangedListener);
		focusSelectionProvider = null;
	}

	focusPart = part;
	if (focusPart != null) {
		focusSelectionProvider = focusPart.getSite().getSelectionProvider();
		if (focusSelectionProvider != null) {
			focusSelectionProvider.addSelectionChangedListener(focusSelectionChangedListener);
			updateFocusResource(focusSelectionProvider.getSelection());
		}
		else {
			updateFocusResource(null);
		}
	}
	
}
void partClosed(IWorkbenchPart part) {
	if (part != focusPart)
		return;
	if (focusSelectionProvider != null) {
		focusSelectionProvider.removeSelectionChangedListener(focusSelectionChangedListener);
		focusSelectionProvider = null;
	}
	focusPart = null;
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
		Object[] data = ((IStructuredSelection) viewer.getSelection()).toArray();
		if (data != null) {
			IMarker[] markers = new IMarker[data.length];
			for (int i = 0; i < markers.length; i++) {
				markers[i] = (IMarker)data[i];
			}
			event.data = createMarkerReport(markers);
		}
		return;
	}
}
void restoreState(IMemento memento) {
	//restore selection
	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	IMemento selectionMem = memento.getChild(TAG_SELECTION);
	if(selectionMem != null) {
		ArrayList selectionList = new ArrayList();
		IMemento markerMems[] = selectionMem.getChildren(TAG_MARKER);
		for (int i = 0; i < markerMems.length; i++){
			try {
				long id = Long.parseLong(markerMems[i].getString(TAG_ID));
				IResource resource = root.findMember(markerMems[i].getString(TAG_RESOURCE));
				if(resource != null) {
					IMarker marker = resource.findMarker(id);
					if (marker != null) {
						selectionList.add(marker);
					}
				}
			} catch (NumberFormatException e) {
			} catch (CoreException e) {
			}
			
		}
		viewer.setSelection(new StructuredSelection(selectionList));
	}
	
	Table table = viewer.getTable();
	//restore vertical position
	try {
		String topIndexStr = memento.getString(TAG_TOP_INDEX);
		table.setTopIndex(Integer.parseInt(topIndexStr));
	} catch (NumberFormatException e) {}
}
/* (non-Javadoc)
 * Method declared on IViewPart.
 */
public void saveState(IMemento memento) {
	if(viewer == null) {
		if(this.memento != null) //Keep the old state;
			memento.putMemento(this.memento);
		return;
	}

	//save filter
	getFilter().saveState(memento.createChild(TAG_FILTER));
	
	//save sorter
	TaskSorter sorter = (TaskSorter) viewer.getSorter();
	memento.putInteger(TAG_SORTER_COLUMN,sorter.getColumnNumber());
	memento.putInteger(TAG_SORTER_REVERSED,sorter.isReversed()?1:0);

	//save columns width
	Table table = viewer.getTable();
	TableColumn columns[] = table.getColumns();
	//check whether it has ever been layed out
	//workaround for 1GDTU19: ITPUI:WIN2000 - Task list columns "collapsed" left
	boolean shouldSave = false;
	for (int i = 0; i < columns.length; i++) {
		if (columnLayouts[i].resizable && columns[i].getWidth() != 0) {
			shouldSave = true;
			break;
		}
	}
	if (shouldSave) {
		for (int i = 0; i < columns.length; i++) {
			if (columnLayouts[i].resizable) {
				IMemento child = memento.createChild(TAG_COLUMN);
				child.putInteger(TAG_NUMBER,i);
				child.putInteger(TAG_WIDTH,columns[i].getWidth());
			}
		}
	}

	//save selection
	Object markers[] = ((IStructuredSelection)viewer.getSelection()).toArray();
 	if(markers.length > 0) {
 		IMemento selectionMem = memento.createChild(TAG_SELECTION);
 		for (int i = 0; i < markers.length; i++) {
	 		IMemento elementMem = selectionMem.createChild(TAG_MARKER);
	 		IMarker marker = (IMarker)markers[i];
 			elementMem.putString(TAG_RESOURCE,marker.getResource().getFullPath().toString());
 			elementMem.putString(TAG_ID,String.valueOf(marker.getId()));
 		}
 	}

 	//save vertical position
	int topIndex = table.getTopIndex();
	memento.putString(TAG_TOP_INDEX,String.valueOf(topIndex));
}
/**
 * Handles marker selection change in the task list by updating availability of
 * the actions in the local tool bar.
 */
void selectionChanged(SelectionChangedEvent event) {
	IStructuredSelection selection = (IStructuredSelection) event.getSelection();
	updateStatusMessage(selection);
		
	updatePasteEnablement();

	// If selection is empty, then disable copy, remove and goto.	
	if (selection.isEmpty()) {
		copyTaskAction.setEnabled(false);
		removeTaskAction.setEnabled(false);
		gotoTaskAction.setEnabled(false);
		propertiesAction.setEnabled(false);
		return;
	};

	// Can only open properties for a single task at a time
	propertiesAction.setEnabled(selection.size() == 1);
	
	// Can always copy
	copyTaskAction.setEnabled(true);

	// Determine if goto should be enabled
	IMarker selectedMarker = (IMarker) selection.getFirstElement();
	boolean canJump = selection.size() == 1 && selectedMarker.getResource().getType() == IResource.FILE;
	gotoTaskAction.setEnabled(canJump);

	// Determine if remove should be enabled
	boolean canRemove = true;
	for (Iterator markers = selection.iterator(); markers.hasNext();) {
		IMarker m = (IMarker) markers.next();
		if (!MarkerUtil.isMarkerType(m, IMarker.TASK)) {
			canRemove = false;
			break;
		}
	}
	removeTaskAction.setEnabled(canRemove);

	// if there is an active editor on the selection's input, tell
	// the editor to goto the marker
	if (canJump) {
		IEditorPart editor = getSite().getPage().getActiveEditor();
		if (editor != null) {
			IEditorInput input = editor.getEditorInput();
			if (input instanceof IFileEditorInput) {
				IFile file = ((IFileEditorInput)input).getFile();
				if (selectedMarker.getResource().equals(file))
					editor.gotoMarker(selectedMarker);
			}
		}
	}
}

/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public void setFocus() {
	viewer.getControl().setFocus();
}

/**
 * Sets the property on a marker to the given value.
 *
 * @exception CoreException if an error occurs setting the value
 */
void setProperty(IMarker marker, String property, Object value) {
	if (MarkerUtil.getProperty(marker, property).equals(value)) {
		return;
	}
	try {
		if (property == tableColumnProperties[1]) { // Completed
			marker.setAttribute(IMarker.DONE, value);
		} else if (property == tableColumnProperties[2]) { // Priority
			// this property is used only by cell editor, where order is High, Normal, Low
			marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH - ((Integer) value).intValue());
		} else if (property == tableColumnProperties[3]) { // Description
			marker.setAttribute(IMarker.MESSAGE, value);
			// Let's not refilter too lightly - see if it is needed
//			TaskSorter sorter = (TaskSorter) viewer.getSorter();
//			if (sorter != null && sorter.getColumnNumber() == 3) {
//				viewer.refresh();
//			}
		}
	}
	catch (CoreException e) {
		String msg = TaskListMessages.getString("TaskList.errorModifyingTask"); //$NON-NLS-1$
		ErrorDialog.openError(getSite().getShell(), msg, null, e.getStatus());
	}
}
/**
 * API method which sets the current selection of this viewer.
 *
 * @param selection a structured selection of <code>IMarker</code> objects
 * @param reveal <code>true</code> to reveal the selection, <false> otherwise
 */
public void setSelection(ISelection selection, boolean reveal) {
	Assert.isTrue(selection instanceof IStructuredSelection);
	IStructuredSelection ssel = (IStructuredSelection) selection;
	for (Iterator i = ssel.iterator(); i.hasNext();)
		Assert.isTrue(i.next() instanceof IMarker);
	viewer.setSelection(selection, reveal);
}
boolean showChildrenHierarchy() {
	switch (getFilter().onResource) {
		case TasksFilter.ON_ANY_RESOURCE:
		case TasksFilter.ON_SELECTED_RESOURCE_AND_CHILDREN:
		case TasksFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT: // added by cagatayk@acm.org
		default:
			return true;
		case TasksFilter.ON_SELECTED_RESOURCE_ONLY:
			return false;
	}
}
boolean showSelections() {
	switch (getFilter().onResource) {
		case TasksFilter.ON_SELECTED_RESOURCE_ONLY:
		case TasksFilter.ON_SELECTED_RESOURCE_AND_CHILDREN:
		case TasksFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT: // added by cagatayk@acm.org
			return true;
		case TasksFilter.ON_ANY_RESOURCE:
		default:
			return false;
	}
}

// showOwnerProject() added by cagatayk@acm.org 
boolean showOwnerProject() {
	return getFilter().onResource == TasksFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT;
}
/**
 * Processes state change of the 'showSelections' switch.
 * If true, it will resync with the saved input element.
 * Otherwise, it will reconfigure to show all the
 * problems/tasks in the workbench.
 */
void toggleInputSelection(boolean value) {
	/*
	if (value) {
		handleInput(inputSelection, false);
	} else {
		// detach from input and link to the workbench object
		handleInput(WorkbenchPlugin.getPluginWorkbench(), true);
	}
	updateTitle();
	*/
}
/**
 * If true, current input will be
 * remembered and further selections will be
 * ignored.
 */
void toggleLockInput(boolean value) {
	/*
	if (!value) {
		handleInput(inputSelection, false);
		lockedInput = null;
	} else {
		lockedInput = (IElement) getInput();
	}
	String lockedInputPath = "";
	if (lockedInput != null && lockedInput instanceof IResource) {
		IResource resource = (IResource) lockedInput;
		lockedInputPath = resource.getFullPath().toString();
	}
	IDialogStore store = WorkbenchPlugin.getDefault().getDialogStore();
	store.put(STORE_LOCKED_INPUT, lockedInputPath);
	updateTitle();
	*/
}
/**
 * Updates the focus resource, and refreshes if we're showing only tasks for the focus resource.
 */
void updateFocusResource(ISelection selection) {
	IResource resource = null;
	if (selection instanceof IStructuredSelection) {
		IStructuredSelection ssel = (IStructuredSelection) selection;
		if (ssel.size() == 1) {
			Object object = ssel.getFirstElement();
			ITaskListResourceAdapter adapter =
				getTaskListAdapter(object);
			//Will only return an adapter if the object
			//is adaptable
			if(adapter != null)
				resource = adapter.getAffectedResource((IAdaptable) object);			
		}
	}
	if (resource == null) {
		if (focusPart instanceof IEditorPart) {
			IEditorInput input = ((IEditorPart) focusPart).getEditorInput();
			if (input != null) {
				if (input instanceof IFileEditorInput) {
					resource = ((IFileEditorInput) input).getFile();
				}
				else {
					resource = (IResource) input.getAdapter(IResource.class);
					if (resource == null) {
						resource = (IFile) input.getAdapter(IFile.class);
					}
				}
			}
		}
	}
	
	if (resource != null && !resource.equals(focusResource)) {
		boolean updateNeeded = false;
		if (showOwnerProject()) {
			IProject oldProject = focusResource == null ? null : focusResource.getProject();
			IProject newProject = resource.getProject();
			boolean projectsEqual = (oldProject == null ? newProject == null : oldProject.equals(newProject));
			updateNeeded = !projectsEqual;
		}
		else if (showSelections()) {
			updateNeeded = true;
		}
		
		// remember the focus resource even if update is not needed,
		// so that we know it if the filter settings change
		focusResource = resource;
		
		if (updateNeeded) {
			viewer.getControl().setRedraw(false);
			viewer.refresh();
			viewer.getControl().setRedraw(true);
			updateStatusMessage();
			updateTitle();
		}
	}
}
/**
 * Updates the enablement of the paste action
 */
void updatePasteEnablement() {
	// Paste if clipboard contains tasks
	MarkerTransfer transfer = MarkerTransfer.getInstance();
	IMarker[] markerData = (IMarker[])getClipboard().getContents(transfer);
	boolean canPaste = false;
	if (markerData != null) {
		for (int i = 0; i < markerData.length; i++) {
			if (MarkerUtil.isMarkerType(markerData[i], IMarker.TASK)) {
				canPaste = true;	
				break;
			}
		}
	}
	pasteTaskAction.setEnabled(canPaste);
}

/**
 * Updates that message displayed in the status line.
 */
void updateStatusMessage() {
	updateStatusMessage((IStructuredSelection) viewer.getSelection());
}
/**
 * Updates that message displayed in the status line.
 */
void updateStatusMessage(IStructuredSelection selection) {
	String message = getStatusMessage(selection);
	getViewSite().getActionBars().getStatusLineManager().setMessage(message);
}
/**
 * Updates the title of the view.  Should be called when filters change.
 */
void updateTitle() {
	String viewName = getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
	TaskListContentProvider provider = (TaskListContentProvider) getTableViewer().getContentProvider();
	String summary = provider.getFilterSummary();
	if ("".equals(summary)) { //$NON-NLS-1$
		setTitle(viewName);
	}
	else {
		String title = TaskListMessages.format("TaskList.titleFmt", new Object[] { viewName, summary }); //$NON-NLS-1$
		setTitle(title);
	}
}
/**
 * Writes a string representation of the given marker to the buffer.
 */
static void writeMarker(StringBuffer buf, IMarker marker) {
	buf.append(MarkerUtil.getKindText(marker));
	buf.append("\t"); //$NON-NLS-1$
	buf.append(MarkerUtil.getCompleteText(marker));
	buf.append("\t"); //$NON-NLS-1$
	buf.append(MarkerUtil.getPriorityText(marker));
	buf.append("\t"); //$NON-NLS-1$
	buf.append(MarkerUtil.getMessage(marker));
	buf.append("\t"); //$NON-NLS-1$
	buf.append(MarkerUtil.getResourceName(marker));
	buf.append("\t"); //$NON-NLS-1$
	buf.append(MarkerUtil.getContainerName(marker));
	buf.append("\t"); //$NON-NLS-1$
	buf.append(MarkerUtil.getLineAndLocation(marker));
	buf.append(System.getProperty("line.separator")); //$NON-NLS-1$
}

/**
 * Get the adapter that is to be used on object. Return the
 * default adapter if there is not one registered and the
 * object is adaptable, else return null.
 */
private ITaskListResourceAdapter getTaskListAdapter(Object object){
	if(object instanceof IAdaptable){
		Object adapter =
			((IAdaptable) object).getAdapter(ITaskListResourceAdapter.class);
		if(adapter == null)
			return DefaultTaskListResourceAdapter.getDefault();
		else 
			return (ITaskListResourceAdapter) adapter;
	}
	return null;
}
}
