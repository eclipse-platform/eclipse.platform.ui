package org.eclipse.ui.internal;

import org.eclipse.ui.*;
import org.eclipse.jface.action.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Show a View.
 */
public class ShowViewAction extends Action {
	private IWorkbenchWindow window;
	private IViewDescriptor desc;
/**
 * ShowViewAction constructor comment.
 */
protected ShowViewAction(IWorkbenchWindow window, IViewDescriptor desc) {
	super(desc.getLabel());
	setImageDescriptor(desc.getImageDescriptor());
	setToolTipText(desc.getLabel());
	this.window = window;
	this.desc = desc;
}
/**
 * Implementation of method defined on <code>IAction</code>.
 */
public void run() {
	IWorkbenchPage page = window.getActivePage();
	if (page != null) {
		try {
			page.showView(desc.getID());
		} catch (PartInitException e) {
			MessageDialog.openError(window.getShell(), "Problems Showing View",
				e.getMessage());
		}
	}
}
}
