package org.eclipse.ui.internal;

/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This is the abstract superclass for NextEditorAction and PrevEditorAction.
 */
public class CycleEditorAction extends CyclePartAction {
	
/**
 * Creates a CycleEditorAction.
 */
protected CycleEditorAction(IWorkbenchWindow window, boolean forward) {
	super(window,forward); //$NON-NLS-1$
	window.getPartService().addPartListener(this);
	updateState();
}

protected void setText() {
	// TBD: Remove text and tooltip when this becomes an invisible action.
	if (forward) {
		setText(WorkbenchMessages.getString("CycleEditorAction.next.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("CycleEditorAction.next.toolTip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.CYCLE_EDITOR_FORWARD_ACTION);
	}
	else {
		setText(WorkbenchMessages.getString("CycleEditorAction.prev.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("CycleEditorAction.prev.toolTip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.CYCLE_EDITOR_BACKWARD_ACTION);
	}
}

/**
 * Updates the enabled state.
 */
public void updateState() {
	WorkbenchPage page = (WorkbenchPage)getActivePage();
	if (page == null) {
		setEnabled(false);
		return;
	}
	// enable iff there is at least one other editor to switch to
	setEnabled(page.getSortedEditors().length >= 1);
}

/**
 * Add all views to the dialog in the activation order
 */
protected void addItems(Table table,WorkbenchPage page) {
	IEditorReference refs[] = page.getSortedEditors();
	for (int i = refs.length - 1; i >= 0 ; i--) {
		TableItem item  = null;
		item = new TableItem(table,SWT.NONE);
		if(refs[i].isDirty())
			item.setText("*" + refs[i].getTitle()); //$NON-NLS-1$
		else
			item.setText(refs[i].getTitle());
		item.setImage(refs[i].getTitleImage());
		item.setData(refs[i]);
	}
}
/**
 * Returns the string which will be shown in the table header.
 */ 
protected String getTableHeader() {
	return WorkbenchMessages.getString("CycleEditorAction.header"); //$NON-NLS-1$
}
}
