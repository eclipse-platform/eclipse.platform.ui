package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.window.*;
import java.util.*;

/**
 * A <code>NewWizardMenu</code> is used to populate a menu manager with
 * New Wizard actions.  The visible actions are determined by user preference
 * from the Perspective Customize dialog.
 */
public class NewWizardMenu extends ShortcutMenu {
	private Action showDlgAction = new NewWizardAction();
	private Action newProjectAction;
	private Action createProjectAction;
	private Map actions = new HashMap(21);
	private NewWizardsRegistryReader reader = new NewWizardsRegistryReader();
	private boolean enabled = true;
/**
 * Create a new wizard shortcut menu.  
 * <p>
 * If the menu will appear on a semi-permanent basis, for instance within
 * a toolbar or menubar, the value passed for <code>register</code> should be true.
 * If set, the menu will listen to perspective activation and update itself
 * to suit.  In this case clients are expected to call <code>deregister</code> 
 * when the menu is no longer needed.  This will unhook any perspective
 * listeners.
 * </p>
 *
 * @param innerMgr the location for the shortcut menu contents
 * @param window the window containing the menu
 * @param register if <code>true</code> the menu listens to perspective changes in
 * 		the window
 */
public NewWizardMenu(IMenuManager innerMgr, IWorkbenchWindow window, boolean register) {
	super(innerMgr, window, register);
	createProjectAction = new CreateProjectAction(window);
	newProjectAction = new NewProjectAction(window);
	fillMenu(); // Must be done after constructor to ensure field initialization.
}
/* (non-Javadoc)
 * Fills the menu with New Wizards.
 */
protected void fillMenu() {
	// Remove all.
	IMenuManager innerMgr = getMenuManager();
	innerMgr.removeAll();

	if (this.enabled) {
		// Add new project ..
		innerMgr.add(createProjectAction);
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
/* (non-Javadoc)
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
 * Sets the enabled state of the receiver.
 * 
 * @param enabledValue if <code>true</code> the menu is enabled; else
 * 		it is disabled
 */
public void setEnabled(boolean enabledValue) {
	this.enabled = enabledValue;
	updateMenu();
}
}
