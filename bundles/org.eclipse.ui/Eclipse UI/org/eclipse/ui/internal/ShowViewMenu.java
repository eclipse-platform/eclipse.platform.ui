package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import java.util.*;
import java.util.List; // otherwise ambiguous with org.eclipse.swt.List

/**
 * A dynamic contribution item which supports to switch to other Contexts.
 */
public class ShowViewMenu extends ShortcutMenu {
	private Action showDlgAction = new Action("&Other..") {
		public void run() {
			showOther();
		}
	};
	private Map actions = new HashMap(21);
/**
 * Create a show view menu.
 *
 * @param innerMgr the location for the shortcut menu contents
 * @param window the window containing the menu
 * @param register if <code>true</code> the menu listens to perspective changes in
 * 		the window
 */
public ShowViewMenu(IMenuManager innerMgr, IWorkbenchWindow window, boolean register) {
	super(innerMgr, window, register);
	fillMenu(); // Must be done after ctr to ensure field initialization.
}
/**
 * Fill the menu with views.
 */
protected void fillMenu() {
	// Remove all.
	IMenuManager innerMgr = getMenuManager();
	innerMgr.removeAll();

	// If no page disable all.
	IWorkbenchPage page = getWindow().getActivePage();
	if (page == null)
		return;
		
	// Get visible actions.
	List actions = ((WorkbenchPage) page).getShowViewActions();
	if (actions != null) {
		for (Iterator i = actions.iterator(); i.hasNext();) {
			String id = (String) i.next();
			IAction action = getAction(id);
			if (action != null) {
				innerMgr.add(action);
			}
		}
	}

	// Add other ..
	innerMgr.add(new Separator());
	innerMgr.add(showDlgAction);
}
/**
 * Returns the action for the given view id, or null if not found.
 */
private IAction getAction(String id) {
	// Keep a cache, rather than creating a new action each time,
	// so that image caching in ActionContributionItem works.
	IAction action = (IAction) actions.get(id);
	if (action == null) {
		IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();
		IViewDescriptor desc = reg.find(id);
		if (desc != null) {
			action = new ShowViewAction(getWindow(), desc);
			actions.put(id, action);
		}
	}
	return action;
}
/**
 * Open show view dialog.
 */
private void showOther() {
	IWorkbenchWindow window = getWindow();
	IWorkbenchPage page = window.getActivePage();
	if (page == null)
		return;
	ShowViewDialog dlg = new ShowViewDialog(window.getShell(),
		WorkbenchPlugin.getDefault().getViewRegistry());
	dlg.open();
	if (dlg.getReturnCode() == Window.CANCEL)
		return;
	IViewDescriptor desc = dlg.getSelection();
	if (desc != null) {
		try {
			page.showView(desc.getID());
		} catch (PartInitException e) {
			MessageDialog.openError(window.getShell(), "Problems Showing View",
				e.getMessage());
		}
	}
		
}
}
