package org.eclipse.ui.internal;

import java.text.Collator;
import java.util.*;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;

public class EditorList {
	private WorkbenchWindow window;	
	private EditorWorkbook workbook;
	private Table editorsTable;
	private Object selection;
	private List elements = new ArrayList();

	private SaveAction saveAction;
	private CloseEditorAction closeAction;
	private SelectionAction selectAllAction;
	private FullNameAction fullNameAction;
	private SortAction nameSortAction;
	private SortAction MRUSortAction;
	private SetScopeAction windowScopeAction;
	private SetScopeAction pageScopeAction;
	private SetScopeAction tabGroupScopeAction;
	private BookMarkAction bookMarkAction;

	private static boolean displayFullPath = false;
	private static Collator collator = Collator.getInstance();
	private static final int SELECT_ALL = 0;
	private static final int INVERT_SELECTION = 1;
	private static final int SELECT_CLEAN = 2;

	private static final int NAME_SORT = 0;
	private static final int MRU_SORT = 1;
	private static int sortOrder = NAME_SORT;
	
	private static final int SET_WINDOW_SCOPE = 0;
	private static final int SET_PAGE_SCOPE = 1;
	private static final int SET_TAB_GROUP_SCOPE = 2;
	
	public EditorList(IWorkbenchWindow window, EditorWorkbook workbook) {
		this.window = (WorkbenchWindow) window;
		this.workbook = workbook;
		saveAction = new SaveAction();
		closeAction = new CloseEditorAction();
		selectAllAction = new SelectionAction(SELECT_ALL);
		fullNameAction = new FullNameAction();
		windowScopeAction = new SetScopeAction(SET_WINDOW_SCOPE);
	  	pageScopeAction = new SetScopeAction(SET_PAGE_SCOPE);
	  	tabGroupScopeAction = new SetScopeAction(SET_TAB_GROUP_SCOPE);
		nameSortAction = new SortAction(NAME_SORT);
	 	MRUSortAction = new SortAction(MRU_SORT);
	  	bookMarkAction = new BookMarkAction();
	}
	
	public Control getControl() {
		return editorsTable;
	}
	
	public int getItemCount() {
		return editorsTable.getItemCount();
	}
	
	/**
	 * Updates the specified item
	 */
	private void updateItem(TableItem item, Adapter editor) {
		int index = fullNameAction.isChecked() ? 1:0;
		item.setData(editor);
		item.setText(editor.getDisplayText()[index]);
		
		Image image = editor.getImage();		
		if (image != null) {
			item.setImage(image);
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

	/**
	 *Sorts the editors
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
			break;
		}
	}
	/**
	 * Updates all items in the table
	 */
	private void updateItems() {
		editorsTable.removeAll();
		elements = new ArrayList();
		if(windowScopeAction.isChecked()) {
			IWorkbenchWindow windows[] = window.getWorkbench().getWorkbenchWindows();
			for (int i = 0; i < windows.length; i++) {
				updateEditors(windows[i].getPages());
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
			}
		}
	}


	private void fillContextMenu(IMenuManager menuMgr) {
		// SortBy SubMenu
		MenuManager sortMenuMgr = new MenuManager(WorkbenchMessages.getString("EditorList.SortBy.text")); //$NON-NLS-1$
		sortMenuMgr.add(nameSortAction);
		sortMenuMgr.add(MRUSortAction);
						
		menuMgr.add(saveAction);
		menuMgr.add(closeAction);
		menuMgr.add(new Separator());
		menuMgr.add(selectAllAction);
		menuMgr.add(new Separator());
		menuMgr.add(fullNameAction);
		menuMgr.add(sortMenuMgr);
		menuMgr.add(bookMarkAction);
	}
	/**
	 * Create the EditorList table and menu items.
	 */
	public void createControl(Composite parent) {	
		editorsTable = new Table(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
		updateItems();
		editorsTable.pack();
		editorsTable.setFocus();
		editorsTable.setVisible(true);

//		// Create the context menu						
		MenuManager menuMgr = new MenuManager("#PopUp"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				EditorList.this.fillContextMenu(manager);
			}
		});

		editorsTable.setMenu(menuMgr.createContextMenu(editorsTable));
		editorsTable.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = editorsTable.getSelection();
				if (items.length > 0) {
					saveAction.setEnabled(true);
					closeAction.setEnabled(true); 
					
					if (items.length == 1) {
						Adapter selection = (Adapter)items[0].getData();
						selection.activate(false);
					}
				} else {
					saveAction.setEnabled(false);
					closeAction.setEnabled(false);
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				TableItem[] items = editorsTable.getSelection();
				if (items.length > 0) {
					saveAction.setEnabled(true);
					closeAction.setEnabled(true);
					
					if (items.length == 1) {
						Adapter selection = (Adapter)items[0].getData();
						selection.activate(true);
					}
				} else {
					saveAction.setEnabled(false);
					closeAction.setEnabled(false);
				}
			}
		});
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
			if(items.length == 0) {
				return;
			}
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(editorsTable.getShell());
			pmd.open();
			for (int i = 0; i < items.length; i++) {
				Adapter editor = (Adapter)items[i].getData();
				editor.save(pmd.getProgressMonitor());
				updateItem(items[i], editor);
			}
			pmd.close();
			updateItems();
		}
	}

	/**
	 * Closes the selected editor.
	 */
	private class CloseEditorAction extends Action {
		/**
		 *	Create an instance of this class
		 */
		private CloseEditorAction() {
			setText(WorkbenchMessages.getString("EditorList.closeSelected.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorList.closeSelected.toolTip")); //$NON-NLS-1$
			WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_PART_ACTION);
		}
		/**
		 * Close the selected editor.
		 */
		public void run() {
			TableItem[] items = editorsTable.getSelection();
			if(items.length == 0) {
				return;
			}
			for (int i = 0; i < items.length; i++) {
				Adapter e = (Adapter)items[i].getData();
				e.close();
			}
			updateItems();
		}
	}

	/**
	 * Closes all editors.
	 */
	private class CloseAllAction extends Action {
		/**
		 *	Create an instance of this class
		 */
		private CloseAllAction() {
			setText(WorkbenchMessages.getString("EditorList.closeAll.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorList.closeAll.toolTip")); //$NON-NLS-1$
			WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_ALL_ACTION);
		}
		/**
		 *	Close all editors.
		 */
		public void run() {
			TableItem[] items = editorsTable.getItems();
			if(items.length == 0) {
				return;
			}
			for (int i = 0; i < items.length; i++) {
				Adapter e = (Adapter)items[i].getData();
				e.close();
			}
			updateItems();
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
//			WorkbenchHelp.setHelp(this, IHelpContextIds.SELECTION_ACTION);			
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
				Adapter editor = (Adapter)allItems[i].getData();
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
			setChecked(displayFullPath);
			setText(WorkbenchMessages.getString("EditorList.FullName.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorList.FullName.toolTip")); //$NON-NLS-1$
//			WorkbenchHelp.setHelp(this, IHelpContextIds.FULL_NAME_ALL_ACTION);
		}
		/**
		 *	Display full file name.
		 */
		public void run() {
			displayFullPath = !displayFullPath;
			setChecked(displayFullPath);
			TableItem[] items = editorsTable.getItems();
			if(items.length == 0) {
				return;
			}
			updateItems();
			editorsTable.pack();
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
				setChecked(EditorList.sortOrder==NAME_SORT);
				setText(WorkbenchMessages.getString("EditorList.SortByName.text")); //$NON-NLS-1$
				setToolTipText(WorkbenchMessages.getString("EditorList.SortByName.toolTip")); //$NON-NLS-1$
				break;
			case MRU_SORT:
				setChecked(EditorList.sortOrder==MRU_SORT);
				setText(WorkbenchMessages.getString("EditorList.SortByMostRecentlyUsed.text")); //$NON-NLS-1$
				setToolTipText(WorkbenchMessages.getString("EditorList.SortByMostRecentlyUsed.toolTip")); //$NON-NLS-1$
				break;
			default:
				break;
			}
//			WorkbenchHelp.setHelp(this, IHelpContextIds.SORT_ACTION);
		}
		
		/** 
		 * Performs the sort.
		 */
		public void run() {
			EditorList.sortOrder = this.sortOrder;
			nameSortAction.setChecked(EditorList.sortOrder==NAME_SORT);
			MRUSortAction.setChecked(EditorList.sortOrder==MRU_SORT);
			TableItem[] items = editorsTable.getItems();
			if(items.length == 0) {
				return;
			}
			updateItems();
		}
	}
	
	private class SetScopeAction extends Action {
		private int whichScope;
		private boolean showAllPersp = false;
		private boolean showAllPage = false;
		private boolean showTabGroup = true;

		/**
		 *	Create an instance of this class
		 */
		private SetScopeAction (int whichScope) {
			this.whichScope = whichScope;
			switch (whichScope) {
			case SET_WINDOW_SCOPE:
				setChecked(showAllPersp);
				setText(WorkbenchMessages.getString("EditorList.DisplayAllWindows.text")); //$NON-NLS-1$
				setToolTipText(WorkbenchMessages.getString("EditorList.DisplayAllWindows.toolTip")); //$NON-NLS-1$
				break;
			case SET_PAGE_SCOPE:
				setChecked(showAllPage);
				setText(WorkbenchMessages.getString("EditorList.DisplayAllPage.text")); //$NON-NLS-1$
				setToolTipText(WorkbenchMessages.getString("EditorList.DisplayAllPage.toolTip")); //$NON-NLS-1$
				break;
			case SET_TAB_GROUP_SCOPE:
				setChecked(!showAllPage);
				setText(WorkbenchMessages.getString("EditorList.DisplayTabGroup.text")); //$NON-NLS-1$
				setToolTipText(WorkbenchMessages.getString("EditorList.DisplayTabGroup.toolTip")); //$NON-NLS-1$
				break;
			default:
				break;
			}
//			WorkbenchHelp.setHelp(this, IHelpContextIds.SORT_EDITOR_SCOPE_ACTION);
		}
		
		/** 
		 * Performs the sort.
		 */
		public void run() {
			switch (whichScope) {
				case SET_WINDOW_SCOPE:
					showAllPersp = true;
					showAllPage = showTabGroup = false;
					break;
				case SET_PAGE_SCOPE:
					showAllPage = true;
					showAllPersp = showTabGroup = false;
					break;
				case SET_TAB_GROUP_SCOPE:
					showTabGroup = true;
					showAllPersp = showAllPage = false;
					break;
			}
			windowScopeAction.setChecked(showAllPersp);
			pageScopeAction.setChecked(showAllPage);
			tabGroupScopeAction.setChecked(showTabGroup);
			updateItems();
		}
	}
	
	private class BookMarkAction extends Action {
		/**
		 *	Create an instance of this class
		 */
		private BookMarkAction() {
			setText(WorkbenchMessages.getString("EditorList.BookMark.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorList.BookMark.toolTip")); //$NON-NLS-1$
//			WorkbenchHelp.setHelp(this, IHelpContextIds.SAVE_ACTION);
		}
		/** 
		 * Performs the save.
		 */
		public void run() {
			TableItem[] items = editorsTable.getSelection();
			if(items.length == 0) {
				return;
			}
			for (int i = 0; i < items.length; i++) {
				Adapter e = (Adapter)items[i].getData();
				workbook.addBookMark(e.editorRef);
			}
		}
	}
	/**
	 * A helper inner class to adapt EditorHistoryItem and IEditorPart
	 * in the same type.
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
		boolean isOpened() {
			return editorRef != null;
		}
		void close() {
			WorkbenchPage p = ((WorkbenchPartReference)editorRef).getPane().getPage();
			p.closeEditor(editorRef,true);
		}
		void save(IProgressMonitor monitor) {
			IEditorPart editor = (IEditorPart)editorRef.getPart(true);
			if(editor != null)
				editor.doSave(monitor);
		}
		// file name without any dirty indication, used for sorting
		String[] getText() {
			if(text != null) {
				return text;
			}
			text = new String[2];
			text[0] = editorRef.getTitle();
			text[1] = editorRef.getTitleToolTip();
			return text;
		}
		// file name with dirty indication, used for displaying
		String[] getDisplayText() {
			if(displayText != null) {
				return displayText;
			}
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