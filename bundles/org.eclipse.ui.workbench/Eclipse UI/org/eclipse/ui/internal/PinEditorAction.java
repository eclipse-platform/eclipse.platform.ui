/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

public class PinEditorAction extends ActiveEditorAction {

	private boolean visible = false;

/**
 * Creates a PinEditorAction.
 */
public PinEditorAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("PinEditorAction.text"), window); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("PinEditorAction.toolTip")); //$NON-NLS-1$
	setId("org.eclipse.ui.internal.PinEditorAction"); //$NON-NLS-1$
//	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.SAVE_ACTION});
}
/**
 * Returns true if the action should be visible in the toolbar or menu.
 */
public boolean getVisible() {
	return visible;
}
/**
 * Sets if the action should be visible or not.
 */
public void setVisible(boolean visible) {
	this.visible = visible;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void run() {
	if (getWorkbenchWindow() == null) {
		// action has been dispose
		return;
	}
	IEditorPart editor = getActiveEditor();
	((EditorSite)editor.getEditorSite()).setReuseEditor(!isChecked());
}
/* (non-Javadoc)
 * Method declared on ActiveEditorAction.
 */
protected void updateState() {
	if(getWorkbenchWindow() == null) {
		setChecked(false);
		setEnabled(false);
		return;
	}
	IWorkbenchPage page = getActivePage();
	if (page == null) {
		setChecked(false);
		setEnabled(false);
		return;
	}
	IEditorPart editor = getActiveEditor();
	boolean enabled = (editor != null);
	setEnabled(enabled);
	if (enabled) {
		EditorSite site = (EditorSite) editor.getEditorSite();
		EditorPane pane = (EditorPane) site.getPane();
		pane.setPinEditorAction(this);
		setChecked(!(site).getReuseEditor());
	} else {
		setChecked(false);
	}
}
}

