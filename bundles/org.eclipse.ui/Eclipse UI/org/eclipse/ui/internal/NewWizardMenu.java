package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import java.util.*;
import java.util.List; // otherwise ambiguous with org.eclipse.swt.List

/**
 * A shortcut menu is used to populate a menu manager with
 * perspective specific actions.  For instance, it provides the base
 * implementation for the ShowView and FileNew menu items, which change
 * when the active perspective changes.
 */
public class NewWizardMenu extends ShortcutMenu {
	private Action showDlgAction = new NewWizardAction();
	private Action newProjectAction = new NewProjectAction();
	private Map actions = new HashMap(21);
	private NewWizardsRegistryReader reader = new NewWizardsRegistryReader();
	private boolean enabled = true;
/**
 * Create a new wizard shortcut menu.
 *
 * @param innerMgr the location for the shortcut menu contents
 * @param window the window containing the menu
 * @param register if <code>true</code> the menu listens to perspective changes in
 * 		the window
 */
public NewWizardMenu(IMenuManager innerMgr, IWorkbenchWindow window, boolean register) {
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

	if (this.enabled) {
		// Add new project ..
		innerMgr.add(newProjectAction);
		innerMgr.add(new Separator());

		// Get visible actions.
		List actions = null;
		IWorkbenchPage page = getWindow().getActivePage();
		if (page != null)
			actions = ((WorkbenchPage) page).getNewWizardActions();
		if (actions != null) {
			for (Iterator i = actions.iterator(); i.hasNext();) {
				String id = (String) i.next();
				IAction action = getAction(id);
				if (action != null)
					innerMgr.add(action);
			}
		}

		// Add other ..
		innerMgr.add(new Separator());
		innerMgr.add(showDlgAction);
	}
}
/**
 * Returns the action for the given wizard id, or null if not found.
 */
private IAction getAction(String id) {
	// Keep a cache, rather than creating a new action each time,
	// so that image caching in ActionContributionItem works.
	IAction action = (IAction) actions.get(id);
	if (action == null) {
		WorkbenchWizardElement element = reader.findWizard(id);
		if (element != null) {
			action = new NewWizardShortcutAction(getWindow().getWorkbench(), element);
			actions.put(id, action);
		}
	}
	return action;
}
/**
 * Set the enabled state of the receiver to the enabled flag.
 */
public void setEnabled(boolean enabledValue) {
	this.enabled = enabledValue;
	updateMenu();
}
}
