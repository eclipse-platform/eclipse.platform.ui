package org.eclipse.ui.internal;

import org.eclipse.core.resources.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;

/**
 * The OpenNew action opens a new perspective depending on the open
 * in new passed on.
 */
public class OpenNewAction extends Action {
	private IWorkbenchWindow window;
/**
 * Create a new instance of the receiver using the workbench settings.
 * @param workbenchWindow the window being used as a parent
 */
public OpenNewAction(IWorkbenchWindow workbenchWindow) {
	super("Open");
	setToolTipText("Open Perspective");
	setImageDescriptor(
		WorkbenchImages.getImageDescriptor(
			IWorkbenchGraphicConstants.IMG_CTOOL_NEW_PAGE));
	this.window = workbenchWindow;

}
/**
 * Open the selected resource in the default perspective.
 */
public void openPerspectiveInNewPage() {
	// Open the page.
	try {
		IContainer element = ResourcesPlugin.getWorkspace().getRoot();
		window.openPage(element);
	} catch (WorkbenchException e) {
		MessageDialog.openError(window.getShell(), "Problems Opening Perspective",
			e.getMessage());
	}
}
/**
 * Open a new window in the default perspective.
 */
public void openPerspectiveInNewWindow() {
	try {
		IContainer element = ResourcesPlugin.getWorkspace().getRoot();
		IWorkbench wb = window.getWorkbench();
		wb.openWorkbenchWindow(element);
	} catch (WorkbenchException e) {
		MessageDialog.openError(window.getShell(), "Problems Opening Window",
			e.getMessage());
	}
}
/**
 * Open the selected resource in a perspective replacing the existing one
 */
public void replaceCurrentPerspective() {

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
/**
 * Implementation of method defined on <code>IAction</code>.
 */
public void run() {

	String openBehavior =
		WorkbenchPlugin.getDefault().getPreferenceStore().getString(
			IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE);

	if (openBehavior.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW))
		openPerspectiveInNewWindow();
	if (openBehavior.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE))
		openPerspectiveInNewPage();
	if (openBehavior
		.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE))
		replaceCurrentPerspective();
}
}
