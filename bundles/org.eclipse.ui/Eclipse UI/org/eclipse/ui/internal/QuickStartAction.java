package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.swt.widgets.*;

/**
 * Launch the quick start action.
 */
public class QuickStartAction extends PartEventAction {
	private static final String EDITOR_ID = "org.eclipse.ui.internal.dialogs.WelcomeEditor";  //$NON-NLS-1$
	
	private IWorkbench workbench;
	
/**
 *	Create an instance of this class
 */
public QuickStartAction(IWorkbench aWorkbench) {
	super(WorkbenchMessages.getString("QuickStart.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("QuickStart.toolTip")); //$NON-NLS-1$
	this.workbench = aWorkbench;
}
/**
 *	The user has invoked this action
 */
public void run() {
	WorkbenchPage page = (WorkbenchPage)workbench.getActiveWorkbenchWindow().getActivePage();
	page.setEditorAreaVisible(true);

	// see if we already have a welcome editor
	IEditorPart[] editors = page.getEditors();
	for (int i = 0; i < editors.length; i++){
		if (editors[i] instanceof WelcomeEditor) {
			page.bringToTop(editors[i]);
			return;
		}
	}

	try {
		page.openEditor(new WelcomeEditorInput(), EDITOR_ID);
	} catch (PartInitException e) {
		IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("QuickStartAction.openEditorException"), e); //$NON-NLS-1$
		ErrorDialog.openError(
			workbench.getActiveWorkbenchWindow().getShell(),
			WorkbenchMessages.getString("QuickStartAction.errorDialogTitle"),  //$NON-NLS-1$
			WorkbenchMessages.getString("QuickStartAction.errorDialogMessage"),  //$NON-NLS-1$
			status);
	}

}
}
