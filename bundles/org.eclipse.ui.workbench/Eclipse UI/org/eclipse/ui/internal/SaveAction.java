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
import org.eclipse.ui.ISaveablePart;
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
	 * Performs the <code>Save</code> action by calling the
	 * <code>IEditorPart.doSave</code> method on the active editor.
	 */
	public void run() {
		/* **********************************************************************************
		 * The code below was added to track the view with focus
		 * in order to support save actions from a view. Remove this
		 * experimental code if the decision is to not allow views to 
		 * participate in save actions (see bug 10234) 
		 */
		ISaveablePart saveView = getSaveableView();
		if (saveView != null) {
			((WorkbenchPage)getActivePart().getSite().getPage()).savePart(saveView, getActivePart(), false);
			return;
		}
		/* **********************************************************************************/

		IEditorPart part = getActiveEditor();
		IWorkbenchPage page = part.getSite().getPage();
		page.saveEditor(part, false);
	}
	
	/* (non-Javadoc)
	 * Method declared on ActiveEditorAction.
	 */
	protected void updateState() {
		/* **********************************************************************************
		 * The code below was added to track the view with focus
		 * in order to support save actions from a view. Remove this
		 * experimental code if the decision is to not allow views to 
		 * participate in save actions (see bug 10234) 
		 */
		ISaveablePart saveView = getSaveableView();
		if (saveView != null) {
			setEnabled(saveView.isDirty());
			return;
		}
		/* **********************************************************************************/
			
		IEditorPart editor = getActiveEditor();
		setEnabled(editor != null && editor.isDirty());
	}
}
