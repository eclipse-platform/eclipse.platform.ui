/*******************************************************************************
 * Copyright (c) 2000, 2003, 2004 IBM Corporation and others.
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

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.Pair;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
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
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * An action delegate that builds a context menu with applicable launch shortcuts.
 * <p>
 * This class can be contributed as pop-up menu extension action. When envolked,
 * it becomes a sub-menu that dynamically builds a list of applicable shortcuts
 * for the current selection (ISelection in the workspace). The LaunchShortCut
 * extension is consulted to obtain the list of registered short cuts. Each short
 * cut may have optional information to support a context menu action. The extra
 * information includes a "filterClass", a list of "contextLabel"s, and a list of
 * "filter" elements. ContextLabels allow custom labels to appear for any mode
 * (run, debug, profile, etc.) in the contextual launch sub-menu. The filterClass
 * is loaded and run over the list of "filter" elements to determine if the
 * shortcut extension item is appropriate for the selected resource.
 * <p>
 * An example is the JDT Java Applet extension, which is only applicable on files
 * of extension "*.java" and being a sub-class of type Applet. Note that it is up
 * to the filterClass to provide attributes and methods to implement the test. In
 * this example, we have extended the AppletShortcut to implement the IActionFilter
 * interface so that it can function as the filterClass, adding only a testAttribute()
 * method.
 * <p>
 * <pre>
 * &lt;shortcut
 *          label="%AppletShortcut.label"
 *           icon="icons/full/ctool16/java_applet.gif"
 *           helpContextId="org.eclipse.jdt.debug.ui.shortcut_java_applet"
 *           modes="run, debug"
 *           filterClass="org.eclipse.jdt.internal.debug.ui.launcher.JavaAppletLaunchShortcut"
 *           class="org.eclipse.jdt.internal.debug.ui.launcher.JavaAppletLaunchShortcut"
 *           id="org.eclipse.jdt.debug.ui.javaAppletShortcut"&gt;
 *        &lt;filter
 *           name="NameMatches"
 *           value="*.java"/&gt;
 *        &lt;filter
 *        	name="ContextualLaunchActionFilter"
 *        	value="supportsContextualLaunch"/&gt;
 *        &lt;contextLabel
 *        	mode="run"
 *        	label="%RunJavaApplet.label"/&gt;
 * 		 &lt;contextLabel
 * 		 	mode="debug"
 * 		 	label="%DebugJavaApplet.label"/&gt;
 * 		  ...
 *   &lt;shortcut&gt;
 * </pre>
 */
public class ContextualLaunchObjectActionDelegate
		implements
			IObjectActionDelegate,
			IMenuCreator {

	private ISelection fSelection;
	
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
		if (((IStructuredSelection) selection).size() != 1)
			action.setEnabled(false);	// Can only handle one resource at a time
		else {
			if (action instanceof Action) {
				if (delegateAction != action) {
					delegateAction = (Action) action;
					delegateAction.setMenuCreator(this);
				}
				action.setEnabled(true);
				fSelection = selection;
			} else {
				action.setEnabled(false);
			}
		}
	}

	private int fCount = 0;
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
//			IAction action = new FakeAction("{ }");
//			ActionContributionItem item= new ActionContributionItem(action);
//			item.fill(menu, -1);
		}
	}
	
	private IActionFilter getFilterClassIfLoaded(LaunchShortcutExtension ext) {
		IExtension extensionPoint = ext.getConfigurationElement().getDeclaringExtension();
		IPluginDescriptor pluginDescriptor = extensionPoint.getDeclaringPluginDescriptor();
		if (pluginDescriptor.isPluginActivated()) {
			IActionFilter filter = ext.getFilterClass();
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
		// boolean hasMode = ext.getModes().contains(getMode(launchGroupIdentifier));
		// return false if there isn't a filter class or there are no filters specified by the shortcut
		// Only loaded plugins will be used, so the actionFilter is null if the filterClass is not loaded
		IActionFilter actionFilter = getFilterClassIfLoaded(ext);
		if (actionFilter == null) {
			return false;
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
			Object target = fSelection;
			// any filter that returns false makes the shortcut non-visible
			if (!actionFilter.testAttribute(target,name,value)) {
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
	
	private class FakeAction extends Action {
		public FakeAction(String name) {
			super(name);
		}
		public void run() {
		}
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
/**
 * Returns the launch group associatd with this action.
 * 
 * @return the launch group associatd with this action
 */
private LaunchGroupExtension getLaunchGroup(String fLaunchGroupIdentifier) {
	return getLaunchConfigurationManager().getLaunchGroup(fLaunchGroupIdentifier);
}
/**
 * Returns the mode of this action - run or debug 
 * 
 * @return the mode of this action - run or debug
 */
private String getMode(String fLaunchGroupIdentifier) {
	return getLaunchGroup(fLaunchGroupIdentifier).getMode();
}

/**
 * Returns the category of this action - possibly <code>null</code>
 *
 * @return the category of this action - possibly <code>null</code>
 */
private String getCategory(String fLaunchGroupIdentifier) {
	return getLaunchGroup(fLaunchGroupIdentifier).getCategory();
}

}
