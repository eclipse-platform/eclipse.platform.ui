/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.ide.NewWizardShortcutAction;
import org.eclipse.ui.internal.registry.NewWizardsRegistryReader;

/**
 * A <code>NewWizardMenu</code> is used to populate a menu manager with
 * New Wizard actions.  The visible actions are determined by user preference
 * from the Perspective Customize dialog.
 */
public class NewWizardMenu extends ContributionItem {
	private IAction showDlgAction;
	private IAction newProjectAction;
	private IAction newExampleAction;
	private Map actions = new HashMap(21);
	private NewWizardsRegistryReader reader = new NewWizardsRegistryReader();
	private boolean enabled = true;
	private IWorkbenchWindow window;

	private boolean dirty = true;
	
	private static final String NO_TARGETS_MSG = WorkbenchMessages.getString("Workbench.noApplicableItems"); //$NON-NLS-1$
	
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
		fillMenu(innerMgr);
		// Must be done after constructor to ensure field initialization.
	}

	public NewWizardMenu(IWorkbenchWindow window) {
		super();
		this.window = window;
		showDlgAction = ActionFactory.NEW.create(window);
		newProjectAction = new NewProjectAction(window);
		newExampleAction = new NewExampleAction(window);
	}
	/* (non-Javadoc)
	 * Fills the menu with New Wizards.
	 */
	private void fillMenu(IContributionManager innerMgr) {
		// Remove all.
		innerMgr.removeAll();

		IWorkbenchPart sourcePart = getSourcePart();
		if (sourcePart == null) {
			return;
		}

		if (this.enabled) {
			// Add new project ..
			innerMgr.add(newProjectAction);

			// Get visible actions.
			List actions = null;
			IWorkbenchPage page = window.getActivePage();
			if (page != null) 
				actions = ((WorkbenchPage) page).getNewWizardActionIds();
			
			if (actions != null) {				
				if(actions.size() > 0)
					innerMgr.add(new Separator());
				for (Iterator i = actions.iterator(); i.hasNext();) {
					String id = (String) i.next();
					IAction action = getAction(id);
					if (action != null) {
                        if (WorkbenchActivityHelper.filterItem(action))
                            continue;                        
						innerMgr.add(action);
                    }
				}
			}

			if (hasExamples()) {
				//Add examples ..
				innerMgr.add(new Separator());
				innerMgr.add(newExampleAction);
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
				action =
					new NewWizardShortcutAction(window, element);
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
	
	private IWorkbenchPart getSourcePart() {
		IWorkbenchPage page = getWindow().getActivePage();
		if (page != null) {
			return page.getActivePart();
		}
		return null;
	}
	
	protected IWorkbenchWindow getWindow() {
		return window;
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
	public void deregisterListeners() {
	}
	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public void fill(Menu menu, int index) {
		if (getParent() instanceof MenuManager)
			 ((MenuManager) getParent()).addMenuListener(menuListener);

		if (!dirty)
			return;

		MenuManager manager = new MenuManager();
		fillMenu(manager);
		IContributionItem items[] = manager.getItems();
		if(items.length == 0){
			MenuItem item = new MenuItem(menu, SWT.NONE);
			item.setText(NO_TARGETS_MSG);
			item.setEnabled(false);
		}
		for (int i = 0; i < items.length; i++) {
			items[i].fill(menu, index++);
		}
		dirty = false;
	}

	/**
	 * Return whether or not any examples are in the current
	 * install.
	 * @return boolean
	 */
	private boolean hasExamples() {
		NewWizardsRegistryReader rdr = new NewWizardsRegistryReader(false);

		Object[] children = rdr.getWizardElements().getChildren(null);
		for (int i = 0; i < children.length; i++) {
			WizardCollectionElement currentChild =
				(WizardCollectionElement) children[i];
			if (currentChild
				.getId()
				.equals(NewWizardsRegistryReader.FULL_EXAMPLES_WIZARD_CATEGORY))
				return true;
		}
		return false;
	}

}
