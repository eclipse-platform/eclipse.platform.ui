package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * The <code>CloseAllPagesAction</code> is used to close all of 
 * the open pages in a window
 */
public class CloseAllPagesAction  extends Action {
	private IWorkbenchWindow window;
/**
 * 
 */
public CloseAllPagesAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("CloseAllPages.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("CloseAllPages.toolTip")); //$NON-NLS-1$
	setEnabled(false);
	this.window = window;
}
/**
 * Close all of the open pages
 */
public void run() {
	((WorkbenchWindow)window).closeAllPages(true);
}
}
