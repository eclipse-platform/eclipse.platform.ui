package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.registry.NewWizardsRegistryReader;
import java.util.*;
import java.util.List;

/**
 * A <code>NewWizardMenu</code> is used to populate a menu manager with
 * New Wizard actions.  The visible actions are determined by user preference
 * from the Perspective Customize dialog.
 */
public class NewWizardMenu extends ContributionItem {
	private Action showDlgAction = new NewWizardAction();
	private Action newProjectAction;
	private Action newExampleAction;
	private Map actions = new HashMap(21);
	private NewWizardsRegistryReader reader = new NewWizardsRegistryReader();
	private boolean enabled = true;
	private IWorkbenchWindow window;
	
	private boolean dirty = true;
	private IMenuListener menuListener = new IMenuListener() {
		public void menuAboutToShow(IMenuManager manager) {
			manager.markDirty();
			dirty = true;
		}
	};	
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
	this(window);
	fillMenu(innerMgr); // Must be done after constructor to ensure field initialization.
}

public NewWizardMenu(IWorkbenchWindow window) {
	super();
	this.window = window;
	newProjectAction = new NewProjectAction(window);
	newExampleAction = new NewExampleAction(window);
}
/* (non-Javadoc)
 * Fills the menu with New Wizards.
 */
private void fillMenu(IContributionManager innerMgr) {
	// Remove all.
	innerMgr.removeAll();

	if (this.enabled) {
		// Add new project ..
		innerMgr.add(newProjectAction);
		innerMgr.add(new Separator());

		// Get visible actions.
		List actions = null;
		IWorkbenchPage page = window.getActivePage();
		if (page != null)
			actions = ((WorkbenchPage) page).getNewWizardActionIds();
		if (actions != null) {
			for (Iterator i = actions.iterator(); i.hasNext();) {
				String id = (String) i.next();
				IAction action = getAction(id);
				if (action != null)
					innerMgr.add(action);
			}
		}

		//		Add examples ..
		innerMgr.add(new Separator());
		innerMgr.add(newExampleAction);
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
			action = new NewWizardShortcutAction(window.getWorkbench(), element);
			actions.put(id, action);
		}
	}
	return action;
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public boolean isEnabled() {
	return enabled;
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public boolean isDynamic() {
	return true;
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public boolean isDirty() {
	return dirty;
}
/**
 * Sets the enabled state of the receiver.
 * 
 * @param enabledValue if <code>true</code> the menu is enabled; else
 * 		it is disabled
 */
public void setEnabled(boolean enabledValue) {
	this.enabled = enabledValue;
}
/**
 * Removes all listeners from the containing workbench window.
 * <p>
 * This method should only be called if the shortcut menu is created
 * with <code>register = true</code>.
 * </p>
 * 
 * @deprecated
 */
public void deregisterListeners() {}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public void fill(Menu menu, int index) {
	if(getParent() instanceof MenuManager)
		((MenuManager)getParent()).addMenuListener(menuListener);
			
	if(!dirty)
		return;
	
	MenuManager manager = new MenuManager();
	fillMenu(manager);
	IContributionItem items[] = manager.getItems();
	for (int i = 0; i < items.length; i++) {
		items[i].fill(menu,index++);
	}
	dirty = false;
}

}