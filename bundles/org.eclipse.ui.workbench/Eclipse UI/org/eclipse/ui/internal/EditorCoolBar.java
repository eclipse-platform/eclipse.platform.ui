package org.eclipse.ui.internal;

import java.text.MessageFormat;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

public class EditorCoolBar {
	private CoolBar coolBar;
	private Composite dropDownComposite;
	private CoolItem dropDownItem;
	private CLabel dropDownLabel;
	private Button dropDownButton;
	
	private CoolItem bookMarkItem;
	private ToolBar bookMarkToolBar;
	private MenuManager chevronMenuManager;
	private MenuManager bookMarkMenuManager = new MenuManager();

	private EditorList editorList;
	private ViewForm listComposite;
	private EditorShortcutList shortcutList;
	private ViewForm shortcutListComposite;
	
	private IWorkbenchWindow window;
	private EditorWorkbook workbook;
	private int style;
	
	private boolean onBottom;
	private boolean firstResize = true; // infw cheezy workaround
	private boolean mouseDownListenerAdded = false;
	private boolean editorListLostFocusByButton = false;
	private boolean singleClick = false;
	private boolean dragEvent = false;
	private boolean doubleClick = false;
	
	private int xAnchor = 0;
	private int yAnchor = 0;
	private static final int MAX_ITEMS = 11;
	private static final int HYSTERESIS = 8;
			
	public EditorCoolBar(IWorkbenchWindow window, EditorWorkbook workbook, int style) {
		this.window = window;
		this.workbook = workbook;
		this.style = style;
		this.onBottom = (SWT.BOTTOM != 0);
		this.editorList = new EditorList(window, workbook);
		this.shortcutList = new EditorShortcutList(window, workbook);
	}

	/**
	 * Update the tab for an editor.  This is typically called
	 * by a site when the tab title changes.
	 */
	public void updateEditorLabel(String title, boolean isDirty, Image image, String toolTip) {	
		// Update title.
		if (isDirty) {
			title = "*" + title;//$NON-NLS-1$
		}
		dropDownLabel.setText(title);
		dropDownLabel.setToolTipText(toolTip);
	
		// Update the tab image
		if (image == null) {
			dropDownLabel.setImage(null);
		} else if (!image.equals(dropDownLabel.getImage())) {
			dropDownLabel.setImage(image);
		}
		Point p2 = dropDownButton.getSize();
		Point p3 = dropDownComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point p4 = dropDownItem.computeSize(p2.x + 5, p3.y);
		dropDownItem.setMinimumSize(p4);
		dropDownItem.setPreferredSize(p4);
	}	
	
	/**
	 * Update the tab for an editor.  This is typically called
	 * by a site when the tab title changes.
	 */
	public void updateBookMarks(IEditorReference ref) {
		EditorShortcut shortcut = EditorShortcut.create(ref);
		updateBookMarks(shortcut);
	}
	
	public void updateBookMarks(EditorShortcut shortcut) {
		if(shortcut == null)
			return; //Should tell the user that could not add a short cut for this ref.
		ToolItem[] items = bookMarkToolBar.getItems();
		for (int i = 0; i < items.length; i++) {
			if (shortcut.equals(items[i].getData())) {
				//Should open a dialog telling the user there
				//is already one with this name; should replace?
				//Try IE.
				return;
			}
		}
		((Workbench) window.getWorkbench()).getEditorShortcutManager().add(shortcut);
		String title = shortcut.getTitle(); 
		Image image = shortcut.getTitleImage();
		String toolTip = shortcut.getTitleToolTip();	
	
		final ToolItem bookMarkToolItem = new ToolItem(bookMarkToolBar, SWT.NONE);
		bookMarkToolItem.setText(title);
		bookMarkToolItem.setToolTipText(toolTip);
		bookMarkToolItem.setData(shortcut);
		shortcut.addListener(new IEditorShortcutListener() {
			public void shortcutDisposed() {
				bookMarkToolItem.dispose();
			}
			public void shortcutRename(String newString) {
				bookMarkToolItem.setText(newString);
			}
		});
	
		// Update the tab image
		if (image == null) {
			bookMarkToolItem.setImage(null);
		} else { 
			if (!image.equals(bookMarkToolItem.getImage())) {
				bookMarkToolItem.setImage(image);
			}
		}
				
		bookMarkToolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				ToolItem item = (ToolItem) event.widget;
				EditorShortcut shortcut = (EditorShortcut)item.getData();
				if (shortcut != null && shortcut.getInput() != null)
					try {
						((Workbench) window.getWorkbench()).getEditorShortcutManager().setSelection(new EditorShortcut[] {shortcut});					
						window.getActivePage().openEditor(shortcut.getInput(),shortcut.getId());
					} catch (PartInitException e) {
					}

			}			
		});
			
		Point p1 = bookMarkToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point p2 = bookMarkItem.computeSize(p1.x, p1.y);
		int p3 = bookMarkToolBar.getItem(0).getWidth();
		bookMarkItem.setMinimumSize(p3, p2.y);
		bookMarkItem.setPreferredSize(p2);
	}	
	
	public void updateEmptyEditorLabel() {
		String title = WorkbenchMessages.getString("EditorCoolBar.NoEditors.text"); //$NON-NLS-1$
		String toolTip = WorkbenchMessages.getString("EditorCoolBar.NoEditors.toolTip"); //$NON-NLS-1$
		updateEditorLabel(title, false, null, toolTip);		
	}

	public Image getLabelImage() {
		return dropDownLabel.getImage();
	}
	
	private void activateEditor(IEditorReference editorRef) {
		IEditorPart editor = editorRef.getEditor(true);
		if (editor != null) {
			WorkbenchPage p = (WorkbenchPage)editor.getEditorSite().getPage();
			Shell s = p.getWorkbenchWindow().getShell();
			if(s.getMinimized()) {
				s.setMinimized(false);
			}
			s.moveAbove(null);
			p.getWorkbenchWindow().setActivePage(p);
			
			if (editor == p.getActivePart()) {
				editor.setFocus();
			} else {
				p.activate(editor);
			}	
		}		
	}
	
	private void closeEditorList() {
		editorList.destroyControl();
		Control focusControl = workbook.getParent().getDisplay ().getFocusControl();
		editorListLostFocusByButton = (focusControl == dropDownButton);
	}
	public void openEditorList() {
		// don't think this check is necessary, need to verify
		if (listComposite != null) {
			return;
		}
		
		Shell parent = workbook.getEditorArea().getWorkbenchWindow().getShell();
		Display display = parent.getDisplay();
		listComposite = new ViewForm(parent, SWT.BORDER);
		listComposite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				listComposite = null;
			}
		});
		
		Control editorListControl = editorList.createControl(listComposite);
				
		editorListLostFocusByButton = false;
		Table editorsTable = ((Table)editorListControl);
		TableItem[] items = editorsTable.getItems();
 		if (items.length == 0) {
 			updateEmptyEditorLabel();
 			listComposite.dispose();
 			return;
 		}
 	
		listComposite.setContent(editorListControl);
		listComposite.pack();

		Rectangle coolbarBounds = coolBar.getBounds();
		Point point = coolBar.getParent().toDisplay(new Point(coolbarBounds.x,coolbarBounds.y));
		point = parent.toControl(point);
		point.y += coolbarBounds.height + 1;
		int x = point.x;
		int y = point.y;
		int width = dropDownItem.getSize().x;
		int height = Math.min(listComposite.getBounds().height, MAX_ITEMS * ((Table)editorList.getControl()).getItemHeight());
		listComposite.setBounds(listComposite.computeTrim(x, y, width, height));
		listComposite.setVisible(true);
		listComposite.moveAbove(null);
		listComposite.setLocation(point);
				 
 		editorListControl.addListener(SWT.Deactivate, new Listener() {
			public void handleEvent(Event event) {
				listComposite.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (singleClick) return;
						if (listComposite != null) {
							closeEditorList();
						}
					}
				});

			}
 		});
	}
	
	public Control createControl(Composite parent) {	
		coolBar = new CoolBar(parent, style);
		coolBar.setLocked(false);
		
		// Make the left hand side.
		dropDownItem = new CoolItem(coolBar, SWT.PUSH);
		dropDownComposite = new Composite(coolBar, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginHeight = 2;
		gridLayout.marginWidth = 0;
		gridLayout.numColumns = 3;
		dropDownComposite.setLayout(gridLayout);

		dropDownLabel = new CLabel(dropDownComposite, SWT.NONE);
		dropDownLabel.addKeyListener(new KeyAdapter() {
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);	
 		dropDownLabel.setLayoutData(gd);
 		
		dropDownButton = new Button(dropDownComposite, SWT.ARROW | SWT.DOWN | SWT.FLAT);
		dropDownButton.setToolTipText(WorkbenchMessages.getString("Menu")); //$NON-NLS-1$
		updateEmptyEditorLabel();
		
		dropDownItem.setControl(dropDownComposite);
		Point p1 = dropDownComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point p = dropDownItem.computeSize(p1.x, p1.y);
		dropDownItem.setSize(0,p.y);
		
		dropDownLabel.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				xAnchor = e.x;
				yAnchor = e.y;				
				singleClick = true;
			}
			public void mouseDoubleClick(MouseEvent e) {
				doubleClick = true;
			}			
			public void mouseUp(final MouseEvent e) {
				final int doubleClickTime = dropDownLabel.getDisplay().getDoubleClickTime();
				final EditorPane visibleEditor = workbook.getVisibleEditor();
				final boolean overImage = overImage(visibleEditor, e.x);

				if (doubleClick) {
					// double Click
					doubleClick = false;
					singleClick = false;
 		
	 				if ((visibleEditor != null) && !overImage) {
	 					if (listComposite != null) {
							closeEditorList();
						}
	 					visibleEditor.getPage().toggleZoom(visibleEditor.getPartReference());
	 				}					
				} else {
					// Could be a single click, need to wait, but first what we can do  ...	
					if (listComposite != null && overImage) {
						singleClick = false;
						return;
					}
					if ((e.button == 3)  && (listComposite == null)) {
						singleClick = false;
						visibleEditor.showPaneMenu(dropDownLabel, new Point(e.x, e.y));
						return;
					}
					if ((e.button == 1) && overImage && (listComposite == null)) {
						singleClick = false;
						visibleEditor.showPaneMenu();
						return;
					} else {
	 					Thread t = new Thread() {
							public void run() {
								try {
									Thread.sleep(doubleClickTime);
								} catch (InterruptedException e){}
								if (singleClick) {
									Display.getDefault().asyncExec(new Runnable() {
										public void run() {						
											if (singleClick) {
												singleClick = false;
												if (listComposite != null) { 
													if (e.button == 1) {
														closeEditorList();
													}
												} else {
													if (e.button == 1) {
														openEditorList();
													} else {
														visibleEditor.showPaneMenu();
													}
												}
																								

											}
										}												
									});
								}
							}
						};
						t.start();
					}
				}
			}	
		});	
		

		dropDownLabel.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (!singleClick) {
					return;
				}
				if (hasMovedEnough(e)) {
					singleClick = false;
					if (listComposite != null) {
						closeEditorList();
					}					
				}
			}
		});
				

 		// register the interested mouse down listener
		if (!mouseDownListenerAdded && workbook.getEditorArea() != null) {
			dropDownLabel.addListener(SWT.MouseDown, workbook.getEditorArea().getMouseDownListener());
			mouseDownListenerAdded = true;
		}
		
		// button takes focus when it is hit, so the list is already
		// closed.  Don't want the editorList open the second time ...
		dropDownButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!editorListLostFocusByButton) {
					openEditorList();
				} else {
					editorListLostFocusByButton = false;
				}				
			}
		});

		bookMarkItem = new CoolItem(coolBar, SWT.DROP_DOWN);
//		bookMarkItem.setText("Links");
		bookMarkToolBar = new ToolBar(coolBar, SWT.RIGHT|SWT.FLAT);
		bookMarkItem.setControl(bookMarkToolBar);
		bookMarkItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (event.detail == SWT.ARROW) {
					handleChevron(event);
				}
			}
		});	
			
		// Add existing entries
		EditorShortcut[] items = ((Workbench) window.getWorkbench()).getEditorShortcutManager().getItems();
		for (int i = 0; i < items.length; i++) {
			updateBookMarks(items[i]);
		}

		coolBar.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				Rectangle r = coolBar.getParent().getClientArea();
				// infw: Need a good way to detect first real resize.
				if (r.width > 7 && firstResize) {
					dropDownItem.setSize(r.width / 4, dropDownItem.getSize().y);
					firstResize = false;
				}
				coolBar.getParent().layout();
			}
		});
		
		bookMarkToolBar.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				popupCoolBarMenu(e);
			}
			public void mouseDoubleClick(MouseEvent e) {
				ToolItem[] items = bookMarkToolBar.getItems();
				for (int i = 0; i < items.length; i++) {
					if (items[i].getBounds().contains(e.x, e.y)) {
						EditorPane visibleEditor = workbook.getVisibleEditor();			
						if (visibleEditor != null) {
			 				visibleEditor.getPage().toggleZoom(visibleEditor.getPartReference());
			 			}
						break;
					}
				}
			}
		});
		return coolBar;
	}
	
	public Control getControl() {
		return coolBar;
	}
	
	public void destroyControl() {
		coolBar.dispose();
		coolBar = null;
	}
	
	public CLabel getDragControl() {
		return dropDownLabel;
	}
//	public Control getDragControl() {
//		return dropDownComposite;
//	}
	/**
	 * Sets the parent for this part.
	 */
	public void setContainer(ILayoutContainer container) {;
		if (!mouseDownListenerAdded && workbook.getEditorArea() != null) {
			dropDownLabel.addListener(SWT.MouseDown, workbook.getEditorArea().getMouseDownListener());
			mouseDownListenerAdded = true;
		}
	}	

	private boolean hasMovedEnough(MouseEvent e) {
		int dx= e.x - xAnchor;
		int dy= e.y - yAnchor;
		if (Math.abs(dx) < HYSTERESIS && Math.abs(dy) < HYSTERESIS)
			return false;
		else
			return true;	
	}
	/*
	 * Return true if <code>x</code> is over the label image.
	 */
	private boolean overImage(EditorPane pane,int x) {
		Image image = getLabelImage();
		if (image == null) {
			return false;
		} else {
			Rectangle imageBounds = getLabelImage().getBounds();
			return x < (pane.getBounds().x + imageBounds.x + imageBounds.width);
		}
	}
	
	/**
	 * Create and display the chevron menu.
	 */
	private void handleChevron(SelectionEvent event) {
		CoolItem item = (CoolItem) event.widget;
		Control control = item.getControl();

		Point chevronPosition = coolBar.toDisplay(new Point(event.x, event.y));
		ToolBar toolBar = (ToolBar) control;

		ToolItem[] tools = toolBar.getItems();
		int toolCount = tools.length;
		int visibleItemCount = 0;
		while (visibleItemCount < toolCount) {
			Rectangle toolBounds = tools[visibleItemCount].getBounds();
			Point point = toolBar.toDisplay(new Point(toolBounds.x, toolBounds.y));
			toolBounds.x = point.x;
			toolBounds.y = point.y;
			// stop if the tool is at least partially hidden by the drop down chevron
			if (chevronPosition.x >= toolBounds.x && chevronPosition.x - toolBounds.x <= toolBounds.width) {
				break;
			}
			visibleItemCount++;
		}

		// Create a pop-up with items for each of the hidden buttons
		EditorShortcut[] shortcuts = new EditorShortcut[toolCount-visibleItemCount];
		int j = 0;
		for (int i = visibleItemCount; i < toolCount; i++) { 
			shortcuts[j++] = (EditorShortcut) tools[i].getData();
		}	
		openShortcutList(shortcuts);
	}

	private void closeShortcutList() {
		shortcutList.destroyControl();
	}
	
	private void openShortcutList(EditorShortcut[] shortcuts) {
		Shell parent = workbook.getEditorArea().getWorkbenchWindow().getShell();
		Display display = parent.getDisplay();
		shortcutListComposite = new ViewForm(parent, SWT.BORDER);
		shortcutListComposite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				shortcutListComposite = null;
			}
		});
		Control shortcutListControl = shortcutList.createControl(shortcutListComposite, shortcuts);
		Table shortcutTable = ((Table)shortcutListControl);
		TableItem[] items = shortcutTable.getItems();
 		if (items.length == 0) {
 			shortcutListComposite.dispose();
 			return;
 		}
 		shortcutListComposite.setContent(shortcutListControl);
		shortcutListComposite.pack();
 	
 		Rectangle parentBounds = coolBar.getParent().getBounds(); 
 		Rectangle shortcutBounds = shortcutListComposite.getBounds();
		int x = parentBounds.x + parentBounds.width - shortcutBounds.width;
		int y = parentBounds.y + coolBar.getSize().y + 1;
		Point point = new Point(x,y);
		point = coolBar.getParent().toDisplay(point);
		point = parent.toControl(point);
		shortcutListComposite.setLocation(point);
		shortcutListComposite.setVisible(true);
		shortcutListComposite.moveAbove(null);
			 
 		shortcutListControl.addListener(SWT.Deactivate, new Listener() {
			public void handleEvent(Event event) {
				shortcutListComposite.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (shortcutListComposite != null) {
							closeShortcutList();
						}
					}
				});

			}
 		});
	}	

	 private void popupCoolBarMenu(MouseEvent e) {
		if ((e.button != 3) || (bookMarkToolBar.getItemCount() == 0)){
			return;
		}
		Point pt = new Point(e.x, e.y);
		ToolItem[] items = bookMarkToolBar.getItems();
		
		boolean found = false;
		for (int i = 0; i < items.length; i++) {
			if (items[i].getBounds().contains(pt)) {
				EditorShortcut shortcut = (EditorShortcut)items[i].getData();
				((Workbench) window.getWorkbench()).getEditorShortcutManager().setSelection(new EditorShortcut[] {shortcut});
				found = true;
				break;
			}
		}
		
		if (!found) {
			return;
		}
		
		bookMarkMenuManager.dispose(); // infw is this part necessary?
		bookMarkMenuManager.removeAll();
		
		bookMarkMenuManager.add(new OpenAction());
		bookMarkMenuManager.add(new RenameAction());
		bookMarkMenuManager.add(new DeleteAction());

		Menu popUp = bookMarkMenuManager.createContextMenu(bookMarkToolBar);
		pt = ((Control) e.widget).toDisplay(pt);
		popUp.setLocation(pt.x, pt.y);
		popUp.setVisible(true);
	}	

	private class BookMarkAction extends Action {
		private ToolItem toolItem;
		private BookMarkAction(ToolItem toolItem)  {
		EditorShortcut shortcut = (EditorShortcut) toolItem.getData();
		setText(shortcut.getTitle());
		setToolTipText(shortcut.getTitleToolTip());
		this.toolItem = toolItem;
		}
		
		public void run() {
			EditorShortcut shortcut = (EditorShortcut) toolItem.getData();
			if (shortcut != null) {
				if(shortcut.getInput() != null) {
					try {
						window.getActivePage().openEditor(shortcut.getInput(),shortcut.getId());
					} catch (PartInitException e) {
					}
				}
			}
		}
	}
	
	/**
	 * Open the selected shortcut.
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
		 * Perform the open.
		 */
		public void run() {
			EditorShortcutManager shortcutManager = ((Workbench) window.getWorkbench()).getEditorShortcutManager();
			EditorShortcut[] selection = shortcutManager.getSelection();
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
	 * Delete the selected shortcut.
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
			EditorShortcutManager shortcutManager = ((Workbench) window.getWorkbench()).getEditorShortcutManager();
			EditorShortcut[] selection = shortcutManager.getSelection();
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
	 * Rename the selected shortcut.
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
			EditorShortcutManager shortcutManager = ((Workbench) window.getWorkbench()).getEditorShortcutManager();
			EditorShortcut[] selection = shortcutManager.getSelection();
			EditorShortcut[] shortcuts = shortcutManager.getItems();
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
}