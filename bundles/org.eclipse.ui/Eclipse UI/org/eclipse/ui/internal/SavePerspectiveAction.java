package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.*;

/**
 * Reset the layout within the active perspective.
 */
public class SavePerspectiveAction extends Action {
	private IWorkbenchWindow window;	
/**
 *	Create an instance of this class
 */
public SavePerspectiveAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("SavePerspective.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("SavePerspective.toolTip")); //$NON-NLS-1$
	setEnabled(false);
	this.window = window;
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.SAVE_PERSPECTIVE_ACTION});
}
/**
 *	The user has invoked this action
 */
public void run() {
	// Get reg.
	PerspectiveRegistry reg = (PerspectiveRegistry)WorkbenchPlugin.getDefault()
		.getPerspectiveRegistry();

	// Get persp name.
	SavePerspectiveDialog dlg = new SavePerspectiveDialog(window.getShell(), reg);
	IPerspectiveDescriptor description = reg.findPerspectiveWithId(window.getActivePage().getPerspective().getId());
	dlg.setInitialSelection(description);
	if (dlg.open() != IDialogConstants.OK_ID)
		return;

	// Create descriptor.
	PerspectiveDescriptor desc = (PerspectiveDescriptor)dlg.getPersp();
	if (desc == null) {
		String name = dlg.getPerspName();
		desc = reg.createPerspective(name,(PerspectiveDescriptor)description);
		if (desc == null) {
			MessageDialog.openError(dlg.getShell(), WorkbenchMessages.getString("SavePerspective.errorTitle"), //$NON-NLS-1$
				WorkbenchMessages.getString("SavePerspective.errorMessage")); //$NON-NLS-1$
			return;
		}
	}

	// Save state.
	IWorkbenchPage page = window.getActivePage();
	if (page != null) {
		page.savePerspectiveAs(desc);
	}
}
}
