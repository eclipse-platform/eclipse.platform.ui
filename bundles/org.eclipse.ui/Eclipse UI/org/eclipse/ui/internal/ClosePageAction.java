package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * The <code>OpenNewPageAction</code> is used to open a new page
 * in a window.
 */
public class ClosePageAction  extends Action {
	private IWorkbenchWindow window;
/**
 * 
 */
public ClosePageAction(IWorkbenchWindow window) {
	super("Close");
	setToolTipText("Close The Active Perspective");
	this.window = window;
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.CLOSE_PAGE_ACTION});
}
/**
 * Open the selected resource in the default perspective.
 */
public void run() {
	IWorkbenchPage page = window.getActivePage();
	if (page != null)
		page.close();
}
}
