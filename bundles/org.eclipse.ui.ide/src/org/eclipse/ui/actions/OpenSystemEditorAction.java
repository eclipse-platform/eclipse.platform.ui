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
package org.eclipse.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.jface.viewers.IStructuredSelection;
import java.util.Iterator;

/**
 * Standard action for opening a system editor on the currently selected file 
 * resource.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class OpenSystemEditorAction extends SelectionListenerAction  {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".OpenSystemEditorAction";//$NON-NLS-1$
	
	/**
	 * The workbench page to open the editor in.
	 */
	private IWorkbenchPage workbenchPage;
/**
 * Creates a new action that will open system editors on the then-selected file 
 * resources.
 *
 * @param page the workbench page in which to open the editor
 */
public OpenSystemEditorAction(IWorkbenchPage page) {
	super(WorkbenchMessages.getString("OpenSystemEditorAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("OpenSystemEditorAction.toolTip")); //$NON-NLS-1$
	setId(ID);
	WorkbenchHelp.setHelp(this, IHelpContextIds.OPEN_SYSTEM_EDITOR_ACTION);
	Assert.isNotNull(page);
	this.workbenchPage = page;
}
/**
 * Return the workbench page to open the editor in.
 *
 * @return the workbench page to open the editor in
 */
IWorkbenchPage getWorkbenchPage() {
	return workbenchPage;
}
/**
 * Opens a system editor on the given file resource.
 *
 * @param file the file resource
 */
void openFile(IFile file) {
	if (getWorkbenchPage() == null) {
		IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("OpenSystemEditorAction.pageError"), null); //$NON-NLS-1$
		WorkbenchPlugin.log(WorkbenchMessages.getString("OpenSystemEditorAction.logTitle"), status); //$NON-NLS-1$
		return;
	}
	try {
		getWorkbenchPage().openSystemEditor(file);
	} catch (PartInitException e) {
		DialogUtil.openError(
			getWorkbenchPage().getWorkbenchWindow().getShell(),
			WorkbenchMessages.getString("OpenSystemEditorAction.dialogTitle"), //$NON-NLS-1$
			e.getMessage(),
			e);
	}
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void run() {
	Iterator enum = getSelectedResources().iterator();
	while (enum.hasNext()) {
		IResource resource = (IResource) enum.next();
		if (resource instanceof IFile)
			openFile((IFile) resource);
	}
}
/**
 * The <code>OpenSystemEditorAction</code> implementation of this
 * <code>SelectionListenerAction</code> method enables the action only
 * if the selection contains just file resources.
 */
protected boolean updateSelection(IStructuredSelection selection) {
	return super.updateSelection(selection) && selectionIsOfType(IResource.FILE);
}
}
