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

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import java.util.Iterator;

/**
 * Standard action for creating a file resource within the currently
 * selected folder or project.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @deprecated should use NewWizardMenu to populate a New submenu instead (see Navigator view)
 */
public class CreateFileAction extends SelectionListenerAction {
	
	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".CreateFileAction";//$NON-NLS-1$
	
	/**
	 * The shell in which to show any dialogs.
	 */
	private Shell shell;
/**
 * Creates a new action for creating a file resource.
 *
 * @param shell the shell for any dialogs
 * 
 * @deprecated see deprecated tag on class
 */
public CreateFileAction(Shell shell) {
	super(WorkbenchMessages.getString("CreateFileAction.text")); //$NON-NLS-1$
	Assert.isNotNull(shell);
	this.shell = shell;
	setToolTipText(WorkbenchMessages.getString("CreateFileAction.toolTip")); //$NON-NLS-1$
	setImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
	setId(ID);
	WorkbenchHelp.setHelp(this, IHelpContextIds.CREATE_FILE_ACTION);
}
/**
 * The <code>CreateFileAction</code> implementation of this
 * <code>IAction</code> method opens a <code>BasicNewFileResourceWizard</code>
 * in a wizard dialog under the shell passed to the constructor.
 */
public void run() {
	BasicNewFileResourceWizard wizard = new BasicNewFileResourceWizard();
	wizard.init(PlatformUI.getWorkbench(), getStructuredSelection());
	wizard.setNeedsProgressMonitor(true);
	WizardDialog dialog = new WizardDialog(shell, wizard);
	dialog.create();
	dialog.getShell().setText(WorkbenchMessages.getString("CreateFileAction.title")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.NEW_FILE_WIZARD);
	dialog.open();
}
/**
 * The <code>CreateFileAction</code> implementation of this
 * <code>SelectionListenerAction</code> method enables the action only
 * if the selection contains folders and open projects.
 */
protected boolean updateSelection(IStructuredSelection s) {
	if (!super.updateSelection(s)) {
		return false;
	}
	Iterator resources = getSelectedResources().iterator();
	while (resources.hasNext()) {
		IResource resource = (IResource)resources.next();
		if (!resourceIsType(resource, IResource.PROJECT | IResource.FOLDER) || !resource.isAccessible()) {
			return false;
		}
	}
	return true;
}
}
