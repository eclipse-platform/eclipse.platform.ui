/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.Pair;
import org.eclipse.debug.internal.ui.StringMatcher;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.debug.ui.ILaunchFilter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * An action delegate that builds a context menu with applicable launch shortcuts.
 * <p>
 * This class can be contributed as pop-up menu extension action. When envoked,
 * it becomes a sub-menu that dynamically builds a list of applicable shortcuts
 * for the current selection (ISelection in the workspace). The LaunchShortCut
 * extension is consulted to obtain the list of registered shortcuts. Each short
 * cut may have optional information to support a context menu action. The extra
 * information includes a "filterClass", a list of "contextLabel"s, and a list of
 * "filter" elements. ContextLabels allow custom labels to appear for any mode
 * (run, debug, profile, etc.) in the contextual launch sub-menu. The filterClass
 * is loaded and run over the list of "filter" elements to determine if the
 * shortcut extension item is appropriate for the selected resource.
 * </p>
 */
public class ContextualLaunchObjectActionDelegate
		implements
			IObjectActionDelegate,
			IMenuCreator {

	private IResource fSelection;
	
	/*
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// We don't have a need for the active part.
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		// nothing to do
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		// never called
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		//Create the new menu. The menu will get filled when it is about to be shown. see fillMenu(Menu).
		Menu menu = new Menu(parent);
		/**
		 * Add listener to repopulate the menu each time
		 * it is shown because MenuManager.update(boolean, boolean) 
		 * doesn't dispose pulldown ActionContribution items for each popup menu.
		 */
		menu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				Menu m = (Menu)e.widget;
				MenuItem[] items = m.getItems();
				for (int i=0; i < items.length; i++) {
					items[i].dispose();
				}
				fillMenu(m);
			}
		});
		return menu;
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		// Never called because we become a menu.
	}
	
	IAction delegateAction;
	/*
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// if the selection is an IResource, save it and enable our action
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			if (ss.size() == 1 && action instanceof Action) {
				if (delegateAction != action) {
					delegateAction = (Action) action;
					delegateAction.setMenuCreator(this);
				}
				Object object = ss.getFirstElement(); // already tested size above
				if(object instanceof IResource) {
					fSelection = (IResource)object;
					if (fSelection.getType() == IResource.FILE) {
						action.setEnabled(true);
						return;
					}
				}
			}
		}
		action.setEnabled(false);
	}
	/*
	 * Fake action to put in the Run context menu when no actions apply
     * This action is always disabled
	 */
	private class FakeAction extends Action {
		public FakeAction(String name) {
			super(name);
			setEnabled(false);
		}
	}
	/**
	 * Fill pull down menu with the pages of the JTabbedPane
	 */
	protected void fillMenu(Menu menu) {
		// lookup appropriate launch config types and build launch actions for them.
		// Retrieve the current perspective and the registered shortcuts
		String activePerspID = getActivePerspectiveID();
		if (activePerspID == null || fSelection == null) {
			return;
		}
		// gather all shortcuts and run their filters so that we only run the
		// filters one time for each shortcut. Running filters can be expensive.
		// Also, only *LOADED* plugins get their filters run.
		List /* <LaunchShortcutExtension> */ allShortCuts = getLaunchConfigurationManager().getLaunchShortcuts();
		Iterator iter = allShortCuts.iterator();
		List filteredShortCuts = new ArrayList(10);
		while (iter.hasNext()) {
			LaunchShortcutExtension ext = (LaunchShortcutExtension) iter.next();
			if (isApplicable(ext)) {
				filteredShortCuts.add(ext);
			}
		}
		iter = filteredShortCuts.iterator();
		if (iter.hasNext()) {
			while (iter.hasNext()) {
				LaunchShortcutExtension ext = (LaunchShortcutExtension) iter.next();
				Set modes = ext.getModes(); // supported launch modes
				Iterator modeIter = modes.iterator();
				while (modeIter.hasNext()) {
					String mode = (String) modeIter.next();
					populateMenu(mode, ext, menu);
				}
			}
		} else {
			// put in a fake action to show there are none
			IAction action = new FakeAction(ActionMessages.getString("ContextualLaunchObjectActionDelegate.0")); //$NON-NLS-1$
			ActionContributionItem item= new ActionContributionItem(action);
			item.fill(menu, -1);
		}
	}
	
	private ILaunchFilter getFilterClassIfLoaded(LaunchShortcutExtension ext) {
		IExtension extensionPoint = ext.getConfigurationElement().getDeclaringExtension();
		IPluginDescriptor pluginDescriptor = extensionPoint.getDeclaringPluginDescriptor();
		if (pluginDescriptor.isPluginActivated()) {
			ILaunchFilter filter = ext.getFilterClass();
			return filter;
		} else {
			return null;
		}
	}
	/* (non-javadoc)
	 * Apply contextFilters for this extension to decide visibility. 
	 *
 	 * @return true if this shortcut should appear in the contextual launch menu
	 */
	private boolean isApplicable(LaunchShortcutExtension ext) {
		String nameFilterPattern = ext.getNameFilter();
		boolean nameMatches = false;
		if (nameFilterPattern != null) {
			StringMatcher sm = new StringMatcher(nameFilterPattern, true, false);
			nameMatches = sm.match(fSelection.getName());
			if (!nameMatches) {
				// return now to avoid loading the filterClass
				return false;
			}
		}

		// Only loaded plugins will be used, so the launchFilter is null if the filterClass is not loaded
		ILaunchFilter launchFilter = getFilterClassIfLoaded(ext);
		if (launchFilter == null) {
			// no launch filter available, just use nameMatches (see bug# 51420)
			return nameMatches;
		}

		List filters = ext.getFilters();
		if (filters.isEmpty()) {
			return false;
		}
		Iterator iter = filters.listIterator();
		while (iter.hasNext()) {
			Pair pair = (Pair) iter.next();
			String name = pair.firstAsString();
			String value= pair.secondAsString();
			// any filter that returns false makes the shortcut non-visible
			if (!launchFilter.testAttribute(fSelection,name,value)) {
				return false;
			}
		}
		return true;
	}
	/* Add the shortcut to the context menu's launch submenu.
	 * 
	 */
	private void populateMenu(String mode, LaunchShortcutExtension ext, Menu menu) {
		LaunchShortcutAction action = new LaunchShortcutAction(mode, ext);
		action.setActionDefinitionId(ext.getId());
		String helpContextId = ext.getHelpContextId();
		if (helpContextId != null) {
			WorkbenchHelp.setHelp(action, helpContextId);
		}
		// replace default action label with context label if specified.
		String label = ext.getContextLabel(mode);
		label = (label != null) ? label : action.getText();
		action.setText(label);
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(menu, -1);
	}

/**
 * Return the ID of the currently active perspective.
 * 
 * @return the active perspective ID or <code>null</code> if there is none.
 */
private String getActivePerspectiveID() {
	IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
	if (window != null) {
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			IPerspectiveDescriptor persp = page.getPerspective();
			if (persp != null) {
				return persp.getId();
			}
		}
	}
	return null;
}
/**
 * Returns the launch configuration manager.
*
* @return launch configuration manager
*/
private LaunchConfigurationManager getLaunchConfigurationManager() {
	return DebugUIPlugin.getDefault().getLaunchConfigurationManager();
}

}
