package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;

public class EditorCoolBar {
	private CoolBar coolBar;
	private Composite dropDownComposite;
	private CoolItem dropDownItem;
	private CLabel dropDownLabel;
	private Button dropDownButton;
	
	private CoolItem bookMarkItem;
	private ToolBar bookMarkToolBar;
	private ToolItem bookMarkToolItem;
	private MenuManager chevronMenuManager;
	private MenuManager bookMarkMenuManager = new MenuManager();

	private EditorList editorList;
	private IWorkbenchWindow window;
	private EditorWorkbook workbook;
	private int style;
	private boolean onBottom;
	private boolean firstResize = true; // infw cheezy workaround
	private boolean mouseDownListenerAdded = false;
	private boolean editorListIsOpen = false;

	private boolean singleClick = false;
	private boolean dragEvent = false;
	private boolean doubleClick = false;
			
	public EditorCoolBar(IWorkbenchWindow window, EditorWorkbook workbook, int style) {
		this.window = window;
		this.workbook = workbook;
		this.style = style;
		this.onBottom = (SWT.BOTTOM != 0);
		this.editorList = new EditorList(window, workbook);
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
	
		bookMarkToolItem = new ToolItem(bookMarkToolBar, SWT.NONE);
		bookMarkToolItem.setText(title);
		bookMarkToolItem.setToolTipText(toolTip);
		bookMarkToolItem.setData(shortcut);
	
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
	
	/**
	 * Sets the location for a hovering shell
	 * @param shell the object that is to hover
	 * @param position the position of a widget to hover over
	 */
	private void setShellBounds(Shell shell, Point position) {
		Control editorListControl = editorList.getControl();
		final int maxItems = 11; // displays x-1 items without a scrollbar
		Rectangle displayBounds = shell.getDisplay().getClientArea();
		Rectangle shellBounds = shell.getBounds();
		Point pullDownSize = dropDownItem.getSize();
	
		shellBounds.x = position.x;
		if (position.y + pullDownSize.y + shellBounds.height >  displayBounds.height) {
			shellBounds.y = position.y - shellBounds.height;
		} else {
			shellBounds.y = position.y + pullDownSize.y;
		}
		shellBounds.height = Math.min(shellBounds.height, maxItems*((Table) editorListControl).getItemHeight());
		shellBounds.width = dropDownItem.getSize().x;
		shell.setBounds(shellBounds);
	}
	
	private void displayEditorList() {
		if (editorListIsOpen) {
			return;
		}
		Shell parent = workbook.getEditorArea().getWorkbenchWindow().getShell();
		Display display = parent.getDisplay();
		final Shell shell = new Shell (parent, SWT.ON_TOP);
		shell.setLayout(new FillLayout());	
		editorList.createControl(shell);
		shell.pack();
		
		Table editorsTable = ((Table)editorList.getControl());
		TableItem[] items = editorsTable.getItems();
 		if (items.length == 0) {
 			updateEmptyEditorLabel();
 			return;
 		}
 					
		shell.addShellListener(new ShellAdapter() {
			public void shellDeactivated(ShellEvent e) {
				shell.close();
			}
		}); 
		
		Point point = coolBar.getParent().toDisplay (coolBar.getLocation ());
		setShellBounds(shell, point);
		
		try {
			editorListIsOpen = true;
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} finally {
			editorListIsOpen = false;
			// Should never happen
			if(!shell.isDisposed()) {
				shell.dispose();
			}
		}	
	}
	
	public void createControl(Composite parent) {	
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
				singleClick = true;
			}

			public void mouseDoubleClick(MouseEvent e) {
				doubleClick = true;
			}			

			public void mouseUp(final MouseEvent e) {
				final int doubleClickTime = dropDownLabel.getDisplay().getDoubleClickTime();
				if (singleClick == doubleClick) {
					doubleClick = false;
					singleClick = false;
	 				EditorPane visibleEditor = workbook.getVisibleEditor();
	 				if (visibleEditor != null) {
	 					visibleEditor.getPage().toggleZoom(visibleEditor.getPartReference());
	 				}					
				} else {
	 				(new Thread() {
						public void run() {
							try { 
								Thread.sleep(doubleClickTime);
							} catch (InterruptedException e){
							}
							if (singleClick) {
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										if (singleClick) {
											singleClick = false;
											if (dragEvent) {
												dragEvent = false;
												return;
											}
							 				EditorPane visibleEditor = workbook.getVisibleEditor();
											if (e.button == 3) {
												visibleEditor.showPaneMenu(dropDownLabel, new Point(e.x, e.y));
											} else {
												if ((e.button == 1) && overImage(visibleEditor, e.x)) {
													visibleEditor.showPaneMenu();
												} else {
								 					displayEditorList();
												}
											}												
										}
									}
								});
							}
						}
					}).start();
				}								

			}
			
		});	
		
		dropDownLabel.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (singleClick) {
					if (!dragEvent) {					
					}
					dragEvent = true;
					return;
				}
			}
		});
				

 		// register the interested mouse down listener
		if (!mouseDownListenerAdded && workbook.getEditorArea() != null) {
			dropDownLabel.addListener(SWT.MouseDown, workbook.getEditorArea().getMouseDownListener());
			mouseDownListenerAdded = true;
		}
		
 		dropDownButton.addMouseListener(new MouseAdapter() {
 			public void mouseDown(MouseEvent e) {
 				displayEditorList();
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
		});
	}
	
	public Control getControl() {
		return coolBar;
	}

	/**
	 * Sets the parent for this part.
	 */
	public void setContainer(ILayoutContainer container) {;
		if (!mouseDownListenerAdded && workbook.getEditorArea() != null) {
			dropDownLabel.addListener(SWT.MouseDown, workbook.getEditorArea().getMouseDownListener());
			mouseDownListenerAdded = true;
		}
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

		// Create a pop-up menu with items for each of the hidden buttons.
		if (chevronMenuManager != null) {
			chevronMenuManager.dispose();
		}
		chevronMenuManager = new MenuManager();
		for (int i = visibleItemCount; i < toolCount; i++) {
			BookMarkAction contribution = new BookMarkAction(tools[i]);
			chevronMenuManager.add(contribution);
		}
		Menu popUp = chevronMenuManager.createContextMenu(coolBar);
		popUp.setLocation(chevronPosition.x, chevronPosition.y);
		popUp.setVisible(true);
		
//		chevronMenuManager.addMenuListener(new )
	}

	 private void popupCoolBarMenu(MouseEvent e) {
		if ((e.button != 3) || (bookMarkToolBar.getItemCount() == 0)){
			return;
		}
		
		Point pt = new Point(e.x, e.y);
		ToolItem[] items = bookMarkToolBar.getItems();
		int index = -1;
		for (int i = 0; i < items.length; i++) {
			if (items[i].getBounds().contains(pt)) {
				index = i;
				break;
			}
		}
		
		if (index == -1) {
			return;
		}
		
		bookMarkMenuManager.dispose(); // infw is this part necessary?
		bookMarkMenuManager.removeAll();
		
		bookMarkMenuManager.add(new OpenBookMarkAction(new ToolItem[] {items[index]}));
		bookMarkMenuManager.add(new RenameBookMarkAction(new ToolItem[] {items[index]}));
		bookMarkMenuManager.add(new DeleteBookMarkAction(new ToolItem[] {items[index]}));
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
	 * Open the selected bookmark.
	 */
	private class OpenBookMarkAction extends Action {
		private ToolItem[] toolItems;
		/**
		 *	Create an instance of this class
		 */
		private OpenBookMarkAction(ToolItem[] toolItems) {
			this.toolItems = toolItems;
			setText(WorkbenchMessages.getString("EditorCoolBar.OpenAction.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorCoolBar.OpenAction.toolTip")); //$NON-NLS-1$
			WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_PART_ACTION);
		}
		/**
		 * Close the selected editor.
		 */
		public void run() {
			for (int i = 0; i < toolItems.length; i++) {
				EditorShortcut shortcut = (EditorShortcut) toolItems[i].getData();
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
	}
	
	/**
	 * Delete the selected bookmark.
	 */	
	private class DeleteBookMarkAction extends Action {
		private ToolItem[] toolItems;
		/**
		 *	Create an instance of this class
		 */
		private DeleteBookMarkAction(ToolItem[] toolItems) {
			this.toolItems = toolItems;
			setText(WorkbenchMessages.getString("EditorCoolBar.DeleteAction.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorCoolBar.DeleteAction.toolTip")); //$NON-NLS-1$
//			WorkbenchHelp.setHelp(this, IHelpContextIds.SAVE_ACTION);
		}
		/** 
		 * Performs the save.
		 */
		public void run() {
			for (int i = 0; i < toolItems.length; i++) {
				EditorShortcut shortcut = (EditorShortcut) toolItems[i].getData();
				if (shortcut != null) {
					((Workbench) window.getWorkbench()).getEditorShortcutManager().remove(shortcut);
					shortcut.dispose();
				}
				toolItems[i].dispose();
			}
		}
	}
		
	/**
	 * Rename the selected bookmark.
	 */	
	private class RenameBookMarkAction extends Action {
		private ToolItem[] toolItems;
		/**
		 *	Create an instance of this class
		 */
		private RenameBookMarkAction(ToolItem[] toolItems) {
			this.toolItems = toolItems;
			setText(WorkbenchMessages.getString("EditorCoolBar.RenameAction.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("EditorCoolBar.RenameAction.toolTipt")); //$NON-NLS-1$
			WorkbenchHelp.setHelp(this, IHelpContextIds.SAVE_ACTION);
		}
		/** 
		 * Performs the save.
		 */
		public void run() {
			for (int i = 0; i < toolItems.length; i++) {			
				// how get the new name?
				toolItems[i].setText("NewName");
			}
		}
	}
		
}
