package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
	super(WorkbenchMessages.getString("OpenNewPage.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("OpenNewPage.toolTip")); //$NON-NLS-1$
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
		MessageDialog.openError(window.getShell(), WorkbenchMessages.getString("OpenNewPage.errorTitle"), //$NON-NLS-1$
			e.getMessage());
	}
}
}
