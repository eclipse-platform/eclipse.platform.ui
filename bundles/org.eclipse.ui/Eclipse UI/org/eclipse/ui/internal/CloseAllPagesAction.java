package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
	super("Close All");
	setToolTipText("Close All");
	this.window = window;
}
/**
 * Close all of the open pages
 */
public void run() {
	IWorkbenchPage[] pages = window.getPages();
	for (int i = 0; i < pages.length; i++) {
		pages[i].close();
	}
}
}
