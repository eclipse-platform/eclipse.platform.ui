package org.eclipse.ui.internal;

import java.text.Collator;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

public class EditorShortcutList {
	private Table shortcutTable;
	private Object selection;
	private List elements = new ArrayList();

	private IWorkbenchWindow window;
	private EditorWorkbook workbook;
	private static Collator collator = Collator.getInstance();
	
	private static final int SELECT_ALL = 0;
	private static final int INVERT_SELECTION = 1;
	
	public EditorShortcutList(IWorkbenchWindow window, EditorWorkbook workbook) {
		this.window = window;
		this.workbook = workbook;
	}

	/**
	 * Create the Shortcut table and menu items.
	 */
	public Control createControl(Composite parent, EditorShortcut[] editorShortcut) {	
		shortcutTable = new Table(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
		updateItems(editorShortcut);
		shortcutTable.pack();
		shortcutTable.setFocus();

//		// Create the context menu						
		MenuManager menuMgr = new MenuManager("#PopUp"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				EditorShortcutList.this.fillContextMenu(manager);
				MenuManager menuMgr = (MenuManager) manager;
				menuMgr.getMenu().addMenuListener(new MenuListener() {
					public void menuHidden(MenuEvent e) {
						destroyControl();
					}
					public void menuShown(MenuEvent e) {
					}
				});				
			}
		});
//		menuMgr.getMenu().addMenuListener(new MenuListener() {
//			public void menuHidden(MenuEvent e) {
//				destroyControl();
//			}
//			public void menuShown(MenuEvent e) {
//			}
//		});

		shortcutTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = shortcutTable.getSelection();
				EditorShortcut[] shortcuts = new EditorShortcut[items.length];
				EditorShortcutManager manager = ((Workbench) window.getWorkbench()).getEditorShortcutManager();
				for (int i = 0; i < items.length; i++) {
					Adapter a = (Adapter)items[i].getData("Adapter");
					shortcuts[i] = a.getShortcut();					
				}					
				manager.setSelection(shortcuts);
			}						
		});
		shortcutTable.setMenu(menuMgr.createContextMenu(shortcutTable));
		return shortcutTable; 
	}

	public void destroyControl() {
		ViewForm parent = (ViewForm) shortcutTable.getParent();
		parent.setContent(null);
		parent.dispose();
		shortcutTable = null;
	}
		
	public Control getControl() {
		return shortcutTable;
	}
	
	public int getItemCount() {
		return shortcutTable.getItemCount();
	}
	
	/**
	 * Updates the specified item
	 */
	private void updateItem(TableItem item, Adapter adapter) {
		item.setData("Adapter", adapter);
		item.setText(adapter.getText());
		
		Image image = adapter.getImage();		
		if (image != null) {
			item.setImage(image);
		}
	}
	
	/**
	 * Sorts the shortcuts
	 */
	private void sort() {
		if (true) return;
		Adapter a[] = new Adapter[elements.size()];
		elements.toArray(a);
		Arrays.sort(a);
		elements = Arrays.asList(a);
	}
	
	/**
	 * Updates all items in the table
	 */
	private void updateItems(EditorShortcut[] items) {
		shortcutTable.removeAll();
		elements = new ArrayList();
		for (int i = 0; i < items.length; i++) {
			elements.add(new Adapter(items[i]));
		}
		sort();
		for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
			Adapter e = (Adapter) iterator.next();
			TableItem item = new TableItem(shortcutTable,SWT.NULL);
			updateItem(item,e);
		}		
	}


	private void fillContextMenu(IMenuManager menuMgr) {
		menuMgr.add(new OpenAction());
		menuMgr.add(new RenameAction());
		menuMgr.add(new DeleteAction());
	}

	/**
	 * Open the selected bookmark.
	 */
	private class OpenAction extends Action {
		/**
		 *	Create an instance of this class
		 */
		private OpenAction() {
			setText(WorkbenchMessages.getString("EditorCoolBar.OpenAction.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorCoolBar.OpenAction.toolTip")); //$NON-NLS-1$
//			WorkbenchHelp.setHelp(this, IHelpContextIds.OPEN_ACTION);
		}
		/**
		 * Open the selected editor.
		 */
		public void run() {
			EditorShortcut[] selection = ((Workbench) window.getWorkbench()).getEditorShortcutManager().getSelection();
			EditorShortcutManager shortcutManager = ((Workbench) window.getWorkbench()).getEditorShortcutManager();
			for (int i = 0; i < selection.length; i++) {
				EditorShortcut shortcut = selection[i];
				if (shortcut != null) {
					if(shortcut.getInput() != null) {
						try {
							window.getActivePage().openEditor(shortcut.getInput(),shortcut.getId());
						} catch (PartInitException e) {
						}
					}					
				}	
			}
			shortcutManager.setSelection(null);
		}			
	}
	
	/**
	 * Delete the selected bookmark.
	 */	
	private class DeleteAction extends Action {
		/**
		 *	Create an instance of this class
		 */
		private DeleteAction() {
			setText(WorkbenchMessages.getString("EditorCoolBar.DeleteAction.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorCoolBar.DeleteAction.toolTip")); //$NON-NLS-1$
//			WorkbenchHelp.setHelp(this, IHelpContextIds.DELETE_ACTION);
		}
		/** 
		 * Performs the delete.
		 */
		public void run() {
			EditorShortcut[] selection = ((Workbench) window.getWorkbench()).getEditorShortcutManager().getSelection();
			EditorShortcutManager shortcutManager = ((Workbench) window.getWorkbench()).getEditorShortcutManager();
			for (int i = 0; i < selection.length; i++) {
				EditorShortcut shortcut = selection[i];
				if (shortcut != null) {
					shortcutManager.remove(shortcut);
					shortcut.dispose();
				}	
			}
			shortcutManager.setSelection(null);
		}
	}
		
	/**
	 * Rename the selected bookmark.
	 */	
	private class RenameAction extends Action {
		private String newValue;
		/**
		 *	Create an instance of this class
		 */
		private RenameAction() {
			setText(WorkbenchMessages.getString("EditorCoolBar.RenameAction.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorCoolBar.RenameAction.toolTipt")); //$NON-NLS-1$
//			WorkbenchHelp.setHelp(this, IHelpContextIds.RENAME_ACTION);
		}
		/** 
		 * Performs the rename.
		 */
		public void run() {
			Shell shell = workbook.getEditorArea().getWorkbenchWindow().getShell();
			EditorShortcut[] selection = ((Workbench) window.getWorkbench()).getEditorShortcutManager().getSelection();
			EditorShortcut[] shortcuts = ((Workbench) window.getWorkbench()).getEditorShortcutManager().getItems();
			EditorShortcutManager shortcutManager = ((Workbench) window.getWorkbench()).getEditorShortcutManager();
			for (int i = 0; i < selection.length; i++) {
				EditorShortcut shortcut = selection[i];
				if (shortcut != null) {
					if (askForLabel(shell, shortcut.getTitle())) {
						for (int j = 0; j < shortcuts.length; j++) {
							if (shortcuts[j].getTitle().equals(newValue)) {
								if (checkOverwrite(shell)) {
									shortcutManager.remove(shortcuts[j]);
									shortcuts[j].dispose();	
								}
								break;								
							}
						}
						shortcut.setTitle(newValue);
						shortcutManager.add(shortcut);	
					}
				}	
			}
			shortcutManager.setSelection(null);
		}			
				
		private boolean askForLabel(Shell shell, String oldValue) {
			String proposal = oldValue;

			if (proposal == null) {
				proposal = ""; //$NON-NLS-1$
			}

			//String title= getString(fBundle, fPrefix + "dialog.title", fPrefix + "dialog.title"); //$NON-NLS-2$ //$NON-NLS-1$			
			String title = "Rename Shortcut"; //$NON-NLS-1$
			String message = "Enter new name"; //$NON-NLS-1$
			IInputValidator inputValidator = new IInputValidator() {
				public String isValid(String newText) {
					return  (newText == null || newText.length() == 0) ? " " : null;  //$NON-NLS-1$
				}
			};		
			
			InputDialog dialog = new InputDialog(shell, title, message, proposal, inputValidator);
			newValue = null;
						
			if (dialog.open() != Window.CANCEL) {
				newValue = dialog.getValue();
			}
				
			if (newValue == null) {
				return false;
			}
				
			newValue = newValue.trim();
			return (newValue.length() != 0);
		}
		
		/**
		 * Check if the user wishes to overwrite the supplied resource
		 * @returns true if there is no collision or delete was successful
		 * @param shell the shell to create the dialog in 
		 * @param destination - the resource to be overwritten
		 */
		private boolean checkOverwrite(Shell shell) {
			final String RESOURCE_EXISTS_TITLE = WorkbenchMessages.getString("RenameResourceAction.resourceExists"); //$NON-NLS-1$
			final String RESOURCE_EXISTS_MESSAGE = WorkbenchMessages.getString("RenameResourceAction.overwriteQuestion"); //$NON-NLS-1$

			return MessageDialog.openQuestion(shell, 
				RESOURCE_EXISTS_TITLE,
				MessageFormat.format(RESOURCE_EXISTS_MESSAGE,new Object[] {newValue}));
		}		
	}
	
	/**
	 * A helper inner class to adapt EditorHistoryItem and IEditorPart
	 * in the same type.
	 */
	private class Adapter implements Comparable {
		EditorShortcut shortcutRef;
		String text[];
		Image images[];
		Adapter(EditorShortcut shortcut) {
			shortcutRef = shortcut;
		}
		// file name without any dirty indication, used for sorting
		String[] getText() {
			if(text != null) {
				return text;
			}
			text = new String[2];
			text[0] = shortcutRef.getTitle();
			text[1] = shortcutRef.getTitleToolTip();
			return text;
		}
			
		Image getImage() {
			return shortcutRef.getTitleImage();
		}
		
		EditorShortcut getShortcut() {
			return shortcutRef;
		}
	
		public int compareTo(Object another) {
			Adapter adapter = (Adapter)another;
			int  result = collator.compare(getText(),adapter.getText());
			return result;
		}
	}
}
