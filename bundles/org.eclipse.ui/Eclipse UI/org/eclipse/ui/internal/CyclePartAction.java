package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import org.eclipse.ui.*;

/**
 * Implements a action to enable the user switch between parts
 * using keyboard.
 */
public class CyclePartAction extends PageEventAction {
	boolean forward;
	private Object selection;
	
/**
 * Creates a CyclePartAction.
 */
protected CyclePartAction(IWorkbenchWindow window, boolean forward) {
	super("", window); //$NON-NLS-1$
	this.forward = forward;
	setText();
	window.getPartService().addPartListener(this);
	updateState();
}
/**
 * Set text and tooltips in the action.
 */
protected void setText() {
	// TBD: Remove text and tooltip when this becomes an invisible action.
	if (forward) {
		setText(WorkbenchMessages.getString("CyclePartAction.next.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("CyclePartAction.next.toolTip")); //$NON-NLS-1$
		setAccelerator(SWT.CTRL | SWT.F7);
	} else {
		setText(WorkbenchMessages.getString("CyclePartAction.prev.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("CyclePartAction.prev.toolTip")); //$NON-NLS-1$
		setAccelerator(SWT.CTRL | SWT.SHIFT | SWT.F7);
	}
}
/**
 * See IPageListener
 */
public void pageActivated(IWorkbenchPage page) {
	super.pageActivated(page);
	updateState();
}
/**
 * See IPageListener
 */
public void pageClosed(IWorkbenchPage page) {
	super.pageClosed(page);
	updateState();
}
/**
 * See IPartListener
 */
public void partOpened(IWorkbenchPart part) {
	super.partOpened(part);
	updateState();
}
/**
 * See IPartListener
 */
public void partClosed(IWorkbenchPart part) {
	super.partClosed(part);
	updateState();
}
/** 
 * Dispose the resources cached by this action.
 */
protected void dispose() {
}
/**
 * Updates the enabled state.
 */
protected void updateState() {
	WorkbenchPage page = (WorkbenchPage)getActivePage();
	if (page == null) {
		setEnabled(false);
		return;
	}
	// enable iff there is at least one other part to switch to
	// (the editor area counts as one entry)
	int count = page.getViews().length;
	if (page.getSortedEditors().length > 0) {
		++count;
	}
	setEnabled(count >= 1);
}
/**
 * @see Action#run()
 */
public void run() {
	boolean direction = forward;
	try {
		IWorkbenchPage page = getActivePage();
		openDialog((WorkbenchPage)page); 
		activate(page,selection);
	} finally {
		forward = direction;
	}
}
/**
 * Activate the selected item.
 */
public void activate(IWorkbenchPage page,Object selection) {
	if(selection != null) {
		if (selection instanceof IEditorPart) {
			page.setEditorAreaVisible(true);
		}
		page.activate(((IWorkbenchPartReference)selection).getPart(true));
	}	
}
/*
 * Open a dialog showing all views in the activation order
 */
private void openDialog(WorkbenchPage page) {
	selection = null;
	final Shell dialog = new Shell(getWorkbenchWindow().getShell(),SWT.MODELESS);
	Display display = dialog.getDisplay();
	dialog.setLayout(new FillLayout());
	
	final Table table = new Table(dialog,SWT.SINGLE | SWT.FULL_SELECTION);
	table.setHeaderVisible(true);
	table.setLinesVisible(true);
	TableColumn tc = new TableColumn(table,SWT.NONE);
	tc.setResizable(false);
	tc.setText(getTableHeader());
	addItems(table,page);
	switch (table.getItemCount()) {
		case 0:
			// do nothing;
			break;
		case 1:
			table.setSelection(0);
			break;
		default:
			if(forward)
				table.setSelection(1);
			else
				table.setSelection(table.getItemCount() - 1);
	}
	tc.pack();
	table.pack();
	dialog.pack();

 	tc.setWidth(table.getClientArea().width);
	table.setFocus();
	table.addFocusListener(new FocusListener() {
		public void focusGained(FocusEvent e){}
		public void focusLost(FocusEvent e) {
			cancel(dialog);
		}
	});
	
	Rectangle dialogBounds = dialog.getBounds();
	Rectangle displayBounds = display.getClientArea();
	dialogBounds.x = (displayBounds.width - dialogBounds.width) / 2;
	dialogBounds.y = (displayBounds.height - dialogBounds.height) / 2;
	dialogBounds.height = dialogBounds.height + 3 - table.getHorizontalBar().getSize().y;
	
	dialog.setBounds(dialogBounds);

	addMouseListener(table,dialog);
	addKeyListener(table,dialog);
	
	try {
		dialog.open();
		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	} finally {
		if(!dialog.isDisposed()) {
			cancel(dialog);
		}
	}
}
/**
 * Returns the string which will be shown in the table header.
 */ 
protected String getTableHeader() {
	return WorkbenchMessages.getString("CyclePartAction.header"); //$NON-NLS-1$
}
/**
 * Add all views to the dialog in the activation order
 */
protected void addItems(Table table,WorkbenchPage page) {
	IWorkbenchPartReference refs[] = page.getSortedParts();
	boolean includeEditor = true;
	for (int i = refs.length - 1; i >= 0 ; i--) {
		if(refs[i] instanceof IEditorReference) {
			if(includeEditor) {
				IEditorReference activeEditor = (IEditorReference)refs[i];
				TableItem item = new TableItem(table,SWT.NONE);
				item.setText(WorkbenchMessages.getString("CyclePartAction.editor")); //$NON-NLS-1$
				item.setImage(activeEditor.getTitleImage());
				item.setData(activeEditor);
				includeEditor = false;
			}
		} else {
			TableItem item = new TableItem(table,SWT.NONE);
			item.setText(((WorkbenchPartReference)refs[i]).getRegisteredName());
			item.setImage(refs[i].getTitleImage());
			item.setData(refs[i]);
		}
	}
}
/*
 * Add a key listener to the table shifting the selection when
 * the acelarator key is pressed and closing the dialog when
 * Control or Alt is released.
 */
private void addKeyListener(final Table table,final Shell dialog) {
	table.addKeyListener(new KeyListener() {
		public void keyPressed(KeyEvent e) {
			int acelaratorKey = getAcceleratorKey();
			if((e.character == SWT.CR) || (e.character == SWT.LF)) {
				ok(dialog,table);
			} else if(e.keyCode == SWT.SHIFT) {
				forward = false;
			} else if(e.keyCode == acelaratorKey) {
				int index = table.getSelectionIndex();
				if(forward) {
					index = (index + 1) % table.getItemCount();
				} else {
					index--;
					index = index >= 0 ? index : table.getItemCount() - 1;
				}
				table.setSelection(index);
			} else if ((e.keyCode == SWT.ARROW_DOWN) ||
				(e.keyCode == SWT.ARROW_UP) ||
				(e.keyCode == SWT.ARROW_LEFT) ||
				(e.keyCode == SWT.ARROW_RIGHT)) {
					//Do nothing.
			} else {
				cancel(dialog);
			}
		}
		public void keyReleased(KeyEvent e) {
			if(e.keyCode == SWT.SHIFT) {
				forward = true;
			} else if((e.keyCode == SWT.ALT) || (e.keyCode == SWT.CTRL)) {
				ok(dialog, table);
			}
		}
	});
}
/*
 * Close the dialog saving the selection
 */
private void ok(Shell dialog, final Table table) {
	TableItem[] items = table.getSelection();
	if (items != null && items.length == 1)
		selection = items[0].getData();
	dialog.close();
	dispose();
}
/*
 * Close the dialog and set selection to null.
 */
private void cancel(Shell dialog) {
	selection = null;
	dialog.close();
	dispose();
}
/*
 * Add mouse listener to the table closing it when
 * the mouse is pressed.
 */			
private void addMouseListener(final Table table,final Shell dialog) {
	table.addMouseListener(new MouseListener() {
		public void mouseDoubleClick(MouseEvent e){
			ok(dialog,table);
		}
		public void mouseDown(MouseEvent e){
			ok(dialog,table);
		}
		public void mouseUp(MouseEvent e){
			ok(dialog,table);
		}
	});
}
/* 
 * If the acelarator is CTRL+ALT+SHIFT+F6
 * or any combination of CTRL ALT SHIFT
 * return F6
 */
private int getAcceleratorKey() {
	int acelaratorKey = getAccelerator();
	acelaratorKey = acelaratorKey & ~ SWT.CTRL;
	acelaratorKey = acelaratorKey & ~ SWT.SHIFT;
	acelaratorKey = acelaratorKey & ~ SWT.ALT;
	return acelaratorKey;
}
}

