package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
	super("&Reset");
	setToolTipText("Reset the current perspective");
	this.window = window;
}
/**
 *	The user has invoked this action
 */
public void run() {
	IWorkbenchPage page = this.window.getActivePage();
	if (page != null) {
		String message = "Do you want to reset the current perspective (" + page.getPerspective().getLabel() + ")?";
		String [] buttons= new String[] { 
			IDialogConstants.OK_LABEL,
			IDialogConstants.CANCEL_LABEL
		};
		MessageDialog d= new MessageDialog(
			this.window.getShell(),
			"Reset Perspective",
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
