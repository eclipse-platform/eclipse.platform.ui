package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
 
/**
 * Action delegate for handling popup menu actions on a readme file.
 */
public class PopupMenuActionDelegate implements IObjectActionDelegate {

	private IWorkbenchPart part;
/** (non-Javadoc)
 * Method declared on IDropActionDelegate
 */
public void run(IAction action) {
	MessageDialog.openInformation(
		this.part.getSite().getShell(),
		"Readme Example",
		"Popup Menu Action executed");
}
/** (non-Javadoc)
 * Method declared on IActionDelegate
 */
public void selectionChanged(IAction action, ISelection selection) {
	//Ignored for this example
}
/** (non-Javadoc)
 * Method declared on IObjectActionDelegate
 */
public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	this.part = targetPart;
}
}
