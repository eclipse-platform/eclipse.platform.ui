/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Implements a action to enable the user switch between parts
 * using keyboard.
 */
public class CyclePartAction extends PageEventAction {
	boolean forward;
	private Object selection;
	private int accelerator;	
	
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
		WorkbenchHelp.setHelp(this, IHelpContextIds.CYCLE_PART_FORWARD_ACTION);
	} else {
		setText(WorkbenchMessages.getString("CyclePartAction.prev.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("CyclePartAction.prev.toolTip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.CYCLE_PART_BACKWARD_ACTION);
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
	int count = page.getViewReferences().length;
	if (page.getSortedEditors().length > 0) {
		++count;
	}
	setEnabled(count >= 1);
}
/**
 * @see Action#run()
 */
public void runWithEvent(Event e) {
	accelerator = e.detail;
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
		if (selection instanceof IEditorReference) {
			page.setEditorAreaVisible(true);
		}
		IWorkbenchPart part = ((IWorkbenchPartReference)selection).getPart(true);
		if(part != null) 
			page.activate(part);
	}	
}
/*
 * Open a dialog showing all views in the activation order
 */
private void openDialog(WorkbenchPage page) {
	final int MAX_ITEMS = 22;
	
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
	Rectangle tableBounds = table.getBounds();
	tableBounds.height = Math.min(tableBounds.height, table.getItemHeight()*MAX_ITEMS);
	dialog.setBounds(tableBounds);

 	tc.setWidth(table.getClientArea().width);
	table.showSelection();
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

	table.removeHelpListener(getHelpListener());
	table.addHelpListener(new HelpListener() {
		public void helpRequested(HelpEvent event) {
		}
	});
	
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
			item.setText(refs[i].getTitle());
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

private int getAcceleratorKey() {
	int accelerator = this.accelerator;
	accelerator = accelerator & ~ SWT.CTRL;
	accelerator = accelerator & ~ SWT.SHIFT;
	accelerator = accelerator & ~ SWT.ALT;
	return accelerator;
}
}

