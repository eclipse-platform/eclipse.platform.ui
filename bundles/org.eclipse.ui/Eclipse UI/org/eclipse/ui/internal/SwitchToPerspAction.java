package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * The <code>OpenNewPageAction</code> is used to open a new page
 * in a window.
 */
public class SwitchToPerspAction  extends Action {
	private IWorkbenchWindow window;
/**
 * 
 */
public SwitchToPerspAction(IWorkbenchWindow window) {
	super("Replace With");
	setToolTipText("Replace Perspective");
	setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_PAGE));
	this.window = window;
}
/**
 * Open the selected resource in the default perspective.
 */
public void run() {

	// Add the default perspective first.
	IPerspectiveRegistry reg =
		WorkbenchPlugin.getDefault().getPerspectiveRegistry();
	IPerspectiveDescriptor defDesc =
		reg.findPerspectiveWithId(IWorkbenchConstants.DEFAULT_LAYOUT_ID);
	if (defDesc != null) {
		IWorkbenchPage persp = window.getActivePage();
		if (persp != null) {
			persp.setPerspective(defDesc);
		}
	}

}
}
