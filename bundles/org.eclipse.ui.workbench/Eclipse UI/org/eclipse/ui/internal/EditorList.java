/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import java.text.Collator;
import java.util.*;
import java.util.List;

import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;

public class EditorList {
	private WorkbenchWindow window;
	private EditorWorkbook workbook;
	private Table editorsTable;
	private static List editorListViews = new ArrayList();
	private Object selection;
	private List elements = new ArrayList();

	private SaveAction saveAction;
	private CloseAction closeAction;
	private SelectionAction selectCleanAction;
	private SelectionAction InvertSelectionAction;
	private SelectionAction selectAllAction;
	private FullNameAction fullNameAction;
	private SortAction nameSortAction;
	private SortAction MRUSortAction;	
	private SetScopeAction windowScopeAction;
	private SetScopeAction pageScopeAction;
	private SetScopeAction tabGroupScopeAction;

	private boolean dropDown; // initialized in constructor
	
	// options same among all instances of editorList
	private static int sortOrder; // initialized in constructor
	private static int listScope; // initialized in constructor
	private static boolean displayFullPath; // initialized in constructor
	
	private static Collator collator = Collator.getInstance();
	
	private static final int SELECT_ALL = 0;
	private static final int INVERT_SELECTION = 1;
	private static final int SELECT_CLEAN = 2;

	private static final int NAME_SORT = IPreferenceConstants.EDITOR_LIST_NAME_SORT;
	private static final int MRU_SORT = IPreferenceConstants.EDITOR_LIST_MRU_SORT;

	private static final int SET_WINDOW_SCOPE = IPreferenceConstants.EDITOR_LIST_SET_WINDOW_SCOPE;
	private static final int SET_PAGE_SCOPE = IPreferenceConstants.EDITOR_LIST_SET_PAGE_SCOPE;
	private static final int SET_TAB_GROUP_SCOPE = IPreferenceConstants.EDITOR_LIST_SET_TAB_GROUP_SCOPE;
	
	private static final String editorListData = "editorListData";
	
	/**
	 * Listen for notifications from the editor part that its title has change or
	 * it's dirty, and update view
	 *
	 * @see IPropertyListener
	 */
	private IPropertyListener propertyListener = new IPropertyListener() {
		public void propertyChanged(Object source, int property) {
			if (property == IEditorPart.PROP_DIRTY || property == IWorkbenchPart.PROP_TITLE) {
				if (source instanceof IEditorPart) {
					EditorSite site = (EditorSite) ((IEditorPart)source).getEditorSite();
					IEditorReference ref = (IEditorReference)site.getPane().getPartReference();

					TableItem[] items = editorsTable.getItems();
					for (int i = 0; i < items.length; i++) {
						Adapter editor = (Adapter)items[i].getData(editorListData);
						if (editor.editorRef == ref) {
							updateItem(items[i], editor);
						}
					}
				}
			}
		}
	};

	private IPartListener2 partListener = new IPartListener2() {
		int counter = 0;
		private void updateEditorList(IWorkbenchPartReference ref) {
			if (ref instanceof IEditorReference) { 
				final Display display = window.getShell().getDisplay();
				final int TIMER_INTERVAL = 100;
				counter ++;				
				display.timerExec(TIMER_INTERVAL, new Runnable() {
					public void run() {
						counter --;
						// When closing the workbench the delay may allow the editorTable
						// to dispose prior to running.						
						if ((counter == 0) && (editorsTable !=null)){
							updateItems();
							notifyEditorListViews();
						}
					}
				});				
			}
		}
		// select in navigator
		public void partBroughtToTop(IWorkbenchPartReference ref) {
			updateEditorList(ref);
		}
		// select tabs, open editor
		public void partActivated(IWorkbenchPartReference ref) {
			updateEditorList(ref);
		}
		// closeAll
		public void partClosed(IWorkbenchPartReference ref) {
			updateEditorList(ref);
		}
		// delete
		public void partDeactivated(IWorkbenchPartReference ref) {
			updateEditorList(ref);
		}
		public void partOpened(IWorkbenchPartReference ref) {
		}
		public void partHidden(IWorkbenchPartReference ref) {
		}
		public void partVisible(IWorkbenchPartReference ref) {
		}
		public void partInputChanged(IWorkbenchPartReference ref) {
		}				
	};

public EditorList(IWorkbenchWindow window, EditorWorkbook workbook) {
	this.window = (WorkbenchWindow) window;
	this.workbook = workbook;
	
	listScope = WorkbenchPlugin.getDefault().getPreferenceStore().getInt(IPreferenceConstants.EDITOR_LIST_SELECTION_SCOPE);
	sortOrder = WorkbenchPlugin.getDefault().getPreferenceStore().getInt(IPreferenceConstants.EDITOR_LIST_SORT_CRITERIA);
	displayFullPath = WorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.EDITOR_LIST_DISPLAY_FULL_NAME);

	// Special handling for scope selection. The concept of tab groups does
	// not make sense in this situation, so over-ride to page scope
	// uses dropDown for this
	//
	// in addition drop down used to indicate if the editorList must also
	// close the viewform etc.
	dropDown = (workbook != null);
 	if (!dropDown) {
		// Views need to listen for part activation/deactivation.
		window.getPartService().addPartListener(partListener);
		
		// When selectionScope (listScope) is changed, or displayFullPath is 
		// changed there is no notification to the editorList views that this 
		// happened (i.e. if it happens from a different window or from
		// the pullDown).  Keep track of the views and update them
		// appropriately.
		editorListViews.add(this);
	}
	
	saveAction = new SaveAction();
	closeAction = new CloseAction();
	selectCleanAction = new SelectionAction(SELECT_CLEAN);
	InvertSelectionAction = new SelectionAction(INVERT_SELECTION);
	selectAllAction = new SelectionAction(SELECT_ALL);
	fullNameAction = new FullNameAction();
	nameSortAction = new SortAction(NAME_SORT);
	MRUSortAction = new SortAction(MRU_SORT);
	windowScopeAction = new SetScopeAction(SET_WINDOW_SCOPE);
	pageScopeAction = new SetScopeAction(SET_PAGE_SCOPE);
	tabGroupScopeAction = new SetScopeAction(SET_TAB_GROUP_SCOPE);		
}

/**
 * Create the EditorList table and menu items.
 */
public Control createControl(Composite parent) {
	editorsTable = new Table(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
	updateItems();
	editorsTable.pack();
	editorsTable.setFocus();

	// Create the context menu
	MenuManager menuMgr = new MenuManager("#PopUp"); //$NON-NLS-1$
	menuMgr.setRemoveAllWhenShown(true);
	menuMgr.addMenuListener(new IMenuListener() {
		public void menuAboutToShow(IMenuManager manager) {
			setCheckedMenuItems();
			EditorList.this.fillContextMenu(manager);
		}
	});

	editorsTable.setMenu(menuMgr.createContextMenu(editorsTable));
	editorsTable.addKeyListener(new KeyListener() {
		public void keyPressed(KeyEvent e) {
			if(e.character == SWT.ESC)
				destroyControl();
			if(e.character == ' ' || e.character == SWT.CR)
				handleSelectionEvent(true);
		}
		public void keyReleased(KeyEvent e) {
		}
	});

	editorsTable.addMouseListener(new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			if ((e.stateMask & SWT.CTRL) != 0 || (e.stateMask & SWT.SHIFT) != 0) {
				return;
			}
			if (e.button != 3) {
				handleSelectionEvent(true);
			}
		}
	});

	editorsTable.addSelectionListener(new SelectionAdapter() {
		public void widgetDefaultSelected(SelectionEvent e) {
			handleSelectionEvent(true);
		}
	});

	return editorsTable;
}

public void dispose() {
	editorsTable = null;
	editorListViews.remove(this);

	// remove the listeners
	elements = new ArrayList();
	getAllEditors(elements);
	for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
		Adapter e = (Adapter) iterator.next();
		e.editorRef.removePropertyListener(propertyListener);
	}		
	window.getPartService().removePartListener(partListener);
}

public void destroyControl() {
	if (dropDown && (editorsTable != null)) {
		ViewForm parent = (ViewForm) editorsTable.getParent();
		parent.setContent(null);
		parent.dispose();
		dispose();
	}
}

public Control getControl() {
	return editorsTable;
}

public int getItemCount() {
	return editorsTable.getItemCount();
}

private void notifyEditorListViews() {
	for (Iterator iterator = editorListViews.iterator(); iterator.hasNext();) {
		EditorList editorList = (EditorList) iterator.next();
		if (editorList != this) {
			editorList.updateItems();
		}
	}
}

private void handleSelectionEvent(boolean mouseEvent) {
	TableItem[] selection = editorsTable.getSelection();
	if (selection.length > 0) {
		boolean enableSaveAction = false;					
		for (int i = 0; i < selection.length; i++) {
			Adapter editor = (Adapter)selection[i].getData(editorListData);
			if (editor.isDirty()) {
				enableSaveAction = true;
				break;
			}
		}
		saveAction.setEnabled(enableSaveAction);
		closeAction.setEnabled(true);
	} else {
		saveAction.setEnabled(false);
		closeAction.setEnabled(false);
	}
	if ((selection.length == 1) && mouseEvent){
		Adapter a = (Adapter)selection[0].getData(editorListData);
		destroyControl();
		a.activate(dropDown);
	}
	notifyEditorListViews();
}

private void setCheckedMenuItems() {
	fullNameAction.setChecked(displayFullPath);
	nameSortAction.setChecked(EditorList.sortOrder==NAME_SORT);
	MRUSortAction.setChecked(EditorList.sortOrder==MRU_SORT);
	windowScopeAction.setChecked(EditorList.listScope==SET_WINDOW_SCOPE);
	pageScopeAction.setChecked(EditorList.listScope==SET_PAGE_SCOPE);
	if (dropDown) {
		tabGroupScopeAction.setChecked(EditorList.listScope==SET_TAB_GROUP_SCOPE);	
	} else {
		tabGroupScopeAction.setEnabled(false);
		if (listScope == SET_TAB_GROUP_SCOPE) {
			pageScopeAction.setChecked(true);
		}
	}	
}

/**
 * Updates the specified item
 */
private void updateItem(TableItem item, Adapter editor) {
	int index = fullNameAction.isChecked() ? 1:0;
	item.setData(editorListData, editor);
	item.setText(editor.getDisplayText()[index]);

	Image image = editor.getImage();
	if (image != null) {
		item.setImage(image);
	}

	if (!dropDown) {
		editor.editorRef.addPropertyListener(propertyListener);
	}
}
	
/**
 * Sorts the editors
 */
private void sort() {
	switch (sortOrder) {
	case NAME_SORT:
		Adapter a[] = new Adapter[elements.size()];
		elements.toArray(a);
		Arrays.sort(a);
		elements = Arrays.asList(a);
		break;
	case MRU_SORT:
		// The elements are already in MRU order
		// TODO: Not in MRU if multiple windows open.  Within each window
		// group they are in order, but not overall
		break;
	default:
		break;
	}
}

/**
 * Adds all editors to elements
 */
private void updateEditors(IWorkbenchPage[] pages) {
	for (int j = 0; j < pages.length; j++) {
		IEditorReference editors[] = ((WorkbenchPage)pages[j]).getSortedEditors();
		for (int k = editors.length-1; k >= 0; k--) {
			elements.add(new Adapter(editors[k]));
		}
	}
}

private void getAllEditors(List elements) {
	if(windowScopeAction.isChecked()) {
		IWorkbenchWindow windows[] = window.getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			updateEditors(windows[i].getPages());
		}
		// TODO: when multiple windows open, loose files from one of the windows
		// When the view is being restored the active window is not included
		// in the collection.  Handle this case, get editors from active page
		if (!dropDown  && elements.size() == 0) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {		
				updateEditors(new IWorkbenchPage[]{page});
			}
		}		
	} else {
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			if (pageScopeAction.isChecked()) {
				updateEditors(new IWorkbenchPage[]{page});
			} else {
				EditorPane editors[] = workbook.getEditors();
				for (int j = 0 ; j < editors.length; j ++) {
					elements.add(new Adapter(editors[j].getEditorReference()));
				}
			}
		}
	}
}

/**
 * Updates all items in the table
 */
private void updateItems() {
	setCheckedMenuItems();
	editorsTable.removeAll();
	elements = new ArrayList();
	getAllEditors(elements);
	
	sort();
	
	Object selection = null;
	if(window.getActivePage() != null) {
		selection = window.getActivePage().getActiveEditor();
	}
	for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
		Adapter e = (Adapter) iterator.next();
		TableItem item = new TableItem(editorsTable,SWT.NULL);
		updateItem(item,e);
		if((selection != null) && (selection == e.editorRef.getPart(false))) {
			editorsTable.setSelection(new TableItem[]{item});
			saveAction.setEnabled(e.isDirty());
		}
	}
}


private void fillContextMenu(IMenuManager menuMgr) {
	// SortBy SubMenu
	MenuManager sortMenuMgr = new MenuManager(WorkbenchMessages.getString("EditorList.SortBy.text")); //$NON-NLS-1$
	sortMenuMgr.add(nameSortAction);
	sortMenuMgr.add(MRUSortAction);
	
	// ApplyTo SubMenu
	MenuManager applyToMenuMgr = new MenuManager(WorkbenchMessages.getString("EditorList.ApplyTo.text")); //$NON-NLS-1$
	applyToMenuMgr.add(windowScopeAction);
	applyToMenuMgr.add(pageScopeAction);
	if (dropDown) {
		applyToMenuMgr.add(tabGroupScopeAction);
	}
	
	// Main menu	
	menuMgr.add(saveAction);
	menuMgr.add(closeAction);
	menuMgr.add(new Separator());
	menuMgr.add(selectCleanAction);
	menuMgr.add(InvertSelectionAction);
	menuMgr.add(selectAllAction);
	menuMgr.add(new Separator());
	menuMgr.add(fullNameAction);
	menuMgr.add(sortMenuMgr);
	menuMgr.add(applyToMenuMgr);
}

private class SaveAction extends Action {
	/**
	 *	Create an instance of this class
	 */
	private SaveAction() {
		setText(WorkbenchMessages.getString("EditorList.saveSelected.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("EditorList.saveSelected.toolTip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.SAVE_ACTION);
	}
	
	/**
	 * Performs the save.
	 */
	public void run() {
		TableItem[] items = editorsTable.getSelection();
		List dirtyEditorList = new ArrayList();

		for (int i = 0; i < items.length; i++) {
			Adapter editor = (Adapter)items[i].getData(editorListData);
			Object element = editor.editorRef.getPart(false);
			if (editor.isDirty()) {
				dirtyEditorList.add(element);	
			}
		}
		
		if (dirtyEditorList.size() != 0) {
			EditorManager.saveAll(dirtyEditorList, false, window);
		}
		destroyControl();
	}
}

/**
 * Closes the selected editor.
 */
private class CloseAction extends Action {
	/**
	 *	Create an instance of this class
	 */
	private CloseAction() {
		setText(WorkbenchMessages.getString("EditorList.closeSelected.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("EditorList.closeSelected.toolTip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_PART_ACTION);
	}
	/**
	 * Close the selected editor.
	 */
	public void run() {
		TableItem[] items = editorsTable.getSelection();
		Adapter[] editorRef = new Adapter[items.length];
		List dirtyEditorList = new ArrayList();

		// store the editor references as editorsTable will
		// be disposed when focus is lost.
		for (int i = 0; i < items.length; i++) {
			editorRef[i] = (Adapter)items[i].getData(editorListData);
			Object element = editorRef[i].editorRef.getPart(false);
			if (editorRef[i].isDirty()) {
				dirtyEditorList.add(element);	
			}			
		}
	
		boolean result = true;
		if (dirtyEditorList.size() != 0) {
			result = EditorManager.saveAll(dirtyEditorList, true, window);
		}
		
		// now close the editors
		if (result) {
			for (int i = 0; i < editorRef.length; i++) {
				editorRef[i].close();
			}
		}
		
		notifyEditorListViews();
		destroyControl();
	}
}

/**
 * Selects editors.
 */
private class SelectionAction extends Action {
	private int selectionType;
	/**
	 *	Create an instance of this class
	 */
	private SelectionAction(int selectionType) {
		this.selectionType = selectionType;

		switch (selectionType) {
		case SELECT_ALL:
			setText(WorkbenchMessages.getString("EditorList.selectAll.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorList.selectAll.toolTip")); //$NON-NLS-1$
			break;
		case INVERT_SELECTION:
			setText(WorkbenchMessages.getString("EditorList.invertSelection.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorList.invertSelection.toolTip")); //$NON-NLS-1$
			break;
		case SELECT_CLEAN:
			setText(WorkbenchMessages.getString("EditorList.selectClean.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorList.selectClean.toolTip")); //$NON-NLS-1$
			break;
		default:
			break;
		}
//		WorkbenchHelp.setHelp(this, IHelpContextIds.SELECTION_ACTION);
	}

	private TableItem[] invertSelection(TableItem[] allItems,TableItem[] selectedItems ) {
		if(allItems.length == 0) {
			return allItems;
		}
		ArrayList invertedSelection = new ArrayList(allItems.length - selectedItems.length);
		outerLoop: for (int i = 0; i < allItems.length; i++) {
			for (int j = 0; j < selectedItems.length; j++) {
				if(allItems[i] == selectedItems[j])
					continue outerLoop;
			}
			invertedSelection.add(allItems[i]);
		}

		TableItem result[] = new TableItem[invertedSelection.size()];
		invertedSelection.toArray(result);
		return result;
	}

	private TableItem[] selectClean(TableItem[] allItems) {
		if(allItems.length == 0) {
			return new TableItem[0];
		}
		ArrayList cleanItems = new ArrayList(allItems.length);
		for (int i = 0; i < allItems.length; i++) {
			Adapter editor = (Adapter)allItems[i].getData(editorListData);
			if(!editor.isDirty())
				cleanItems.add(allItems[i]);
		}
		TableItem result[] = new TableItem[cleanItems.size()];
		cleanItems.toArray(result);

		return result;
	}
	/**
	 *	Select editors.
	 */
	public void run() {
		switch (selectionType) {
			case SELECT_ALL:
				editorsTable.setSelection(editorsTable.getItems());
				break;
			case INVERT_SELECTION:
				editorsTable.setSelection(invertSelection(editorsTable.getItems(), editorsTable.getSelection()));
				break;
			case SELECT_CLEAN:
				editorsTable.setSelection(selectClean(editorsTable.getItems()));
				break;
		}
		handleSelectionEvent(false);
	}
}


/**
 * Displays the full file name.
 */
private class FullNameAction extends Action {
	/**
	 *	Create an instance of this class
	 */
	private FullNameAction() {
		setText(WorkbenchMessages.getString("EditorList.FullName.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("EditorList.FullName.toolTip")); //$NON-NLS-1$
//		WorkbenchHelp.setHelp(this, IHelpContextIds.FULL_NAME_ALL_ACTION);
	}
	/**
	 *	Display full file name.
	 */
	public void run() {
		displayFullPath = !displayFullPath;
		WorkbenchPlugin.getDefault().getPreferenceStore().setValue(IPreferenceConstants.EDITOR_LIST_DISPLAY_FULL_NAME, displayFullPath);
		setChecked(displayFullPath);
		int[] indices = editorsTable.getSelectionIndices();
		updateItems();
		if (dropDown) {
			workbook.resizeEditorList();
		}
		editorsTable.setSelection(indices);
		notifyEditorListViews();
	}
}

private class SortAction extends Action {
	private int sortOrder;
	/**
	 *	Create an instance of this class
	 */
	private SortAction(int sortOrder) {
		this.sortOrder = sortOrder;
		switch (sortOrder) {
		case NAME_SORT:
			setText(WorkbenchMessages.getString("EditorList.SortByName.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorList.SortByName.toolTip")); //$NON-NLS-1$
			break;
		case MRU_SORT:
			setText(WorkbenchMessages.getString("EditorList.SortByMostRecentlyUsed.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorList.SortByMostRecentlyUsed.toolTip")); //$NON-NLS-1$
			break;
		default:
			break;
		}
//		WorkbenchHelp.setHelp(this, IHelpContextIds.SORT_ACTION);
	}

	/**
	 * Performs the sort.
	 */
	public void run() {
		EditorList.sortOrder = this.sortOrder;
		WorkbenchPlugin.getDefault().getPreferenceStore().setValue(IPreferenceConstants.EDITOR_LIST_SORT_CRITERIA, this.sortOrder);
		TableItem[] items = editorsTable.getItems();
		if(items.length == 0) {
			return;
		}
		updateItems();
		notifyEditorListViews();
	}
}

private class SetScopeAction extends Action {
	private int whichScope;
	/**
	 *	Create an instance of this class
	 */
	private SetScopeAction (int whichScope) {
		this.whichScope = whichScope;
		switch (whichScope) {
		case SET_WINDOW_SCOPE:
			setText(WorkbenchMessages.getString("EditorList.DisplayAllWindows.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorList.DisplayAllWindows.toolTip")); //$NON-NLS-1$
			break;
		case SET_PAGE_SCOPE:
			setText(WorkbenchMessages.getString("EditorList.DisplayAllPage.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorList.DisplayAllPage.toolTip")); //$NON-NLS-1$
			break;
		case SET_TAB_GROUP_SCOPE:
			setText(WorkbenchMessages.getString("EditorList.DisplayTabGroup.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorList.DisplayTabGroup.toolTip")); //$NON-NLS-1$
			break;
		default:
			break;
		}
//		WorkbenchHelp.setHelp(this, IHelpContextIds.SORT_EDITOR_SCOPE_ACTION);
	}

	/**
	 * Display the appropriate scope.
	 */
	public void run() {
		EditorList.listScope = this.whichScope;
		WorkbenchPlugin.getDefault().getPreferenceStore().setValue(IPreferenceConstants.EDITOR_LIST_SELECTION_SCOPE, this.whichScope);
		updateItems();
		if (dropDown) {
			workbook.resizeEditorList();
		}
		notifyEditorListViews();
	}
}

/**
 * A helper inner class
 */
private class Adapter implements Comparable {
	IEditorReference editorRef;
	String text[], displayText[];
	Image images[];
	Adapter(IEditorReference ref) {
		editorRef = ref;
	}
	
	boolean isDirty() {
		return editorRef.isDirty();
	}

	void close() {
		WorkbenchPage p = ((WorkbenchPartReference)editorRef).getPane().getPage();
		p.closeEditor(editorRef,false);
	}

	// file name without any dirty indication, used for sorting
	String[] getText() {
		text = new String[2];
		text[0] = editorRef.getTitle();
		text[1] = editorRef.getTitleToolTip();
		return text;
	}
	// file name with dirty indication, used for displaying
	String[] getDisplayText() {
		displayText = new String[2];

		if(editorRef.isDirty()) {
			displayText[0] = "*" + editorRef.getTitle(); //$NON-NLS-1$
			displayText[1] = "*" + editorRef.getTitleToolTip(); //$NON-NLS-1$
		} else {
			displayText[0] = editorRef.getTitle();
			displayText[1] = editorRef.getTitleToolTip();
		}
		return displayText;
	}

	Image getImage() {
		return editorRef.getTitleImage();
	}

	private void activate(boolean activate){
		IEditorPart editor = editorRef.getEditor(true);
		if (editor != null) {
			WorkbenchPage p = (WorkbenchPage)editor.getEditorSite().getPage();
			Shell s = p.getWorkbenchWindow().getShell();
			if(s.getMinimized()) {
				s.setMinimized(false);
			}
			s.moveAbove(null);
			p.getWorkbenchWindow().setActivePage(p);
			if (activate) {
				if (editor == p.getActivePart()) {
					editor.setFocus();
				} else {
					p.activate(editor);
				}
			} else {
				p.bringToTop(editor);
			}
		}
	}
	
	public int compareTo(Object another) {
		int index = fullNameAction.isChecked() ? 1:0;
		Adapter adapter = (Adapter)another;
		int  result = collator.compare(getText()[index],adapter.getText()[index]);
		return result;
	}
}
}