package org.eclipse.ui.internal;

/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Workbench common <code>Save</code> action.
 */
public class SaveAction extends BaseSaveAction {
	
	/**
	 *	Create an instance of this class
	 */
	public SaveAction(IWorkbenchWindow window) {
		super(WorkbenchMessages.getString("SaveAction.text"), window); //$NON-NLS-1$
		setText(WorkbenchMessages.getString("SaveAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("SaveAction.toolTip")); //$NON-NLS-1$
		setId(IWorkbenchActionConstants.SAVE);
		WorkbenchHelp.setHelp(this, IHelpContextIds.SAVE_ACTION);
	}
	
	/**
	 * Performs the <code>Save As</code> action by calling the
	 * <code>IEditorPart.doSave</code> method on the active editor.
	 */
	public void run() {
		IEditorPart part = getActiveEditor();
		IWorkbenchPage page = part.getSite().getPage();
		page.saveEditor(part, false);
	}
	
	/* (non-Javadoc)
	 * Method declared on ActiveEditorAction.
	 */
	protected void updateState() {
		IEditorPart editor = getActiveEditor();
		setEnabled(editor != null && editor.isDirty());
	}
}
