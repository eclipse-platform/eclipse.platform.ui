package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.*;

/**
 * Reset the layout within the active perspective.
 */
public class ResetPerspectiveAction extends Action {
	private IWorkbenchWindow window;	
/**
 *	Create an instance of this class
 */
public ResetPerspectiveAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("ResetPerspective.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("ResetPerspective.toolTip")); //$NON-NLS-1$
	setEnabled(false);
	this.window = window;
}
/**
 *	The user has invoked this action
 */
public void run() {
	IWorkbenchPage page = this.window.getActivePage();
	if (page != null) {
		String message = WorkbenchMessages.format("ResetPerspective.message", new Object[] { page.getPerspective().getLabel() }); //$NON-NLS-1$
		String [] buttons= new String[] { 
			IDialogConstants.OK_LABEL,
			IDialogConstants.CANCEL_LABEL
		};
		MessageDialog d= new MessageDialog(
			this.window.getShell(),
			WorkbenchMessages.getString("ResetPerspective.title"), //$NON-NLS-1$
			null,
			message,
			MessageDialog.QUESTION,
			buttons,
			0
		);
		if (d.open() == 0)
			page.resetPerspective();
	}
}
}
