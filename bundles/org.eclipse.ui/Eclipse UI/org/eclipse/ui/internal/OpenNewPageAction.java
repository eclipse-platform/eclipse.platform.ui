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
public class OpenNewPageAction  extends Action {
	private IWorkbenchWindow window;
/**
 * 
 */
public OpenNewPageAction(IWorkbenchWindow window) {
	super("Open In Same Window");
	setToolTipText("Open In Same Window");
	setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_PAGE));
	this.window = window;
}
/**
 * Open the selected resource in the default perspective.
 */
public void run() {
	// Open the page.
	try {
		IContainer element = ResourcesPlugin.getWorkspace().getRoot();
		window.openPage(element);
	} catch (WorkbenchException e) {
		MessageDialog.openError(window.getShell(), "Problems Opening Perspective",
			e.getMessage());
	}
}
}
