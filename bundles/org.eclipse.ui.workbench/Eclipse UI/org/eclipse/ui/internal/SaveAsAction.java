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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Workbench common <code>Save As</code> action.
 */
public class SaveAsAction extends BaseSaveAction {
	
	/**
	 *	Create an instance of this class
	 */
	public SaveAsAction(IWorkbenchWindow window) {
		super(WorkbenchMessages.getString("SaveAs.text"), window); //$NON-NLS-1$
		setText(WorkbenchMessages.getString("SaveAs.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("SaveAs.toolTip")); //$NON-NLS-1$
		setId(IWorkbenchActionConstants.SAVE_AS);
		WorkbenchHelp.setHelp(this, IHelpContextIds.SAVE_AS_ACTION);
	}
	
	/**
	 * Performs the <code>Save As</code> action by calling the
	 * <code>IEditorPart.doSaveAs</code> method on the active editor.
	 */
	public void run() {
		getActiveEditor().doSaveAs();
	}
	
	/* (non-Javadoc)
	 * Method declared on ActiveEditorAction.
	 */
	protected void updateState() {
		IEditorPart editor = getActiveEditor();
		setEnabled(editor != null && editor.isSaveAsAllowed());
	}
}
