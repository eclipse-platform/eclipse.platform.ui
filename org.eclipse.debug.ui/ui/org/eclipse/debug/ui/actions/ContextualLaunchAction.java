/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.LaunchConfigurationAction;
import org.eclipse.debug.internal.ui.actions.LaunchShortcutAction;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * An action delegate that builds a context menu with applicable launch shortcuts
 * for a specific launch mode.
 * <p>
 * This class can be sub-classed and contributed as an object contribution pop-up
 * menu extension action. When invoked, it becomes a sub-menu that dynamically
 * builds a list of applicable launch shortcuts for the current selection.
 * Each launch shortcut may have optional information to support a context menu action.
 * </p>
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 3.0
 */
public abstract class ContextualLaunchAction implements IObjectActionDelegate, IMenuCreator {

	private IAction fDelegateAction;
	private String fMode;
	// default launch group for this mode (null category)
	private ILaunchGroup fGroup = null;
	// map of launch groups by (non-null) categories, for this mode
	private Map fGroupsByCategory = null;
	// whether to re-fill the menu (reset on selection change)
	private boolean fFillMenu = true;
	
	/**
	 * Constructs a contextual launch action for the given launch mode.
	 * 
	 * @param mode launch mode
	 */
	public ContextualLaunchAction(String mode) {
		fMode = mode;
		ILaunchGroup[] groups = DebugUITools.getLaunchGroups();
		fGroupsByCategory = new HashMap(3);
		for (int i = 0; i < groups.length; i++) {
			ILaunchGroup group = groups[i];
			if (group.getMode().equals(mode)) {
				if (group.getCategory() == null) {
					fGroup = group;
				} else {
					fGroupsByCategory.put(group.getCategory(), group);
				}
			}
		}
	}
	
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
		 * Add listener to re-populate the menu each time
		 * it is shown because MenuManager.update(boolean, boolean) 
		 * doesn't dispose pull-down ActionContribution items for each popup menu.
		 */
		menu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				if (fFillMenu) {
					Menu m = (Menu)e.widget;
					MenuItem[] items = m.getItems();
					for (int i=0; i < items.length; i++) {
						items[i].dispose();
					}
					fillMenu(m);
					fFillMenu = false;
				}
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
	
	/*
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// if the selection is an IResource, save it and enable our action
		if (selection instanceof IStructuredSelection) {
			fFillMenu = true;
			if (fDelegateAction != action) {
				fDelegateAction = action;
				fDelegateAction.setMenuCreator(this);
			}
			//enable our menu
			action.setEnabled(true);
			return;
		}
		action.setEnabled(false);
	}
	
	/**
	 * Returns the launch manager
	 * @return the launch manager
	 * @since 3.3
	 */
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
    /**
     * Fills the menu with applicable launch shortcuts
     * @param menu The menu to fill
     */
	protected void fillMenu(Menu menu) {
		IStructuredSelection ss = SelectedResourceManager.getDefault().getCurrentSelection();
		int accelerator = 1;
		if(!ss.isEmpty()) {
			try {
				//try to add the shared config it the context is one.
				ILaunchConfiguration config = getLaunchConfigurationManager().isSharedConfig(ss.getFirstElement());
		        if(config != null && config.exists() && config.supportsMode(fMode)) {
		        	IAction action = new LaunchConfigurationAction(config, fMode, config.getName(), DebugUITools.getDefaultImageDescriptor(config), accelerator++);
		            ActionContributionItem item = new ActionContributionItem(action);
		            item.fill(menu, -1);
				}
			}
			catch (CoreException ce) {}
		}
		
		List allShortCuts = getLaunchConfigurationManager().getLaunchShortcuts();
		Iterator iter = allShortCuts.iterator();
		List filteredShortCuts = new ArrayList(10);
	
	//create a context
		List selection = ss.toList();
		//in 3.4 we are correctly passing the IEditorPart and ISelection, so we have to perform
		//some sneekyness to make sure the IEditorInput is passed to the eval expressions
		//for backwards compatibility
		Object o = ss.getFirstElement();
		if(o instanceof IEditorPart) {
			selection.set(0, ((IEditorPart)o).getEditorInput());
		}
		IEvaluationContext context = DebugUIPlugin.createEvaluationContext(selection);
		context.setAllowPluginActivation(true);
		context.addVariable("selection", selection); //$NON-NLS-1$
		while (iter.hasNext()) {
			LaunchShortcutExtension ext = (LaunchShortcutExtension) iter.next();
			try {
				if (!WorkbenchActivityHelper.filterItem(ext) && isApplicable(ext, context)) {
					filteredShortCuts.add(ext);
				}
			} 
			catch (CoreException e) {
				IStatus status = new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), "Launch shortcut '" + ext.getId() + "' enablement expression caused exception. Shortcut was removed.", e); //$NON-NLS-1$ //$NON-NLS-2$
				DebugUIPlugin.log(status);
				iter.remove();
			}
		}
		iter = filteredShortCuts.iterator();
	
	//we need a separator iff the shared config entry has been added and there are following shortcuts
		if(menu.getItemCount() > 0 && filteredShortCuts.size() > 0) {
			 new MenuItem(menu, SWT.SEPARATOR);
		}
		List categories = new ArrayList();
		while (iter.hasNext()) {
			LaunchShortcutExtension ext = (LaunchShortcutExtension) iter.next();
			Set modes = ext.getModes(); // supported launch modes
			Iterator modeIter = modes.iterator();
			while (modeIter.hasNext()) {
				String mode = (String) modeIter.next();
				if (mode.equals(fMode)) {
					String category = ext.getCategory();
					// NOTE: category can be null
					if (category != null && !categories.contains(category)) {
						categories.add(category);
					}
					populateMenuItem(mode, ext, menu, accelerator++);
				}
			}
		}
		
	// add in the open ... dialog shortcut(s)
		if (categories.isEmpty()) {
			if (accelerator > 1) {
				new MenuItem(menu, SWT.SEPARATOR);
			}
			IAction action = new OpenLaunchDialogAction(fGroup.getIdentifier());
		    ActionContributionItem item = new ActionContributionItem(action);
		    item.fill(menu, -1);
		} else {
			boolean addedSep = false;
			iter = categories.iterator();
			while (iter.hasNext()) {
				// NOTE: category can be null
				String category = (String) iter.next();
				ILaunchGroup group = fGroup;
				if (category != null) {
					group = (ILaunchGroup) fGroupsByCategory.get(category);
				}
				if (group != null) {
					if (accelerator > 1 && !addedSep) {
						new MenuItem(menu, SWT.SEPARATOR);
						addedSep = true;
					}
				    IAction action = new OpenLaunchDialogAction(group.getIdentifier());
				    ActionContributionItem item= new ActionContributionItem(action);
				    item.fill(menu, -1);
				}
			}
		}

	}

	/**
	 * Evaluate the enablement logic in the contextualLaunch
	 * element description. A true result means that we should
	 * include this shortcut in the context menu.
	 * @param ext the shortcut extension to get the enablement expression from
	 * @param context the evaluation context to use
	 * @return true iff shortcut should appear in context menu
	 * @throws CoreException if an exception occurs
	 */
	private boolean isApplicable(LaunchShortcutExtension ext, IEvaluationContext context) throws CoreException {
		Expression expr = ext.getContextualLaunchEnablementExpression();
		return ext.evalEnablementExpression(context, expr);
	}

	/**
	 * Add the shortcut to the context menu's launch sub-menu.
	 * @param mode the id of the mode
	 * @param ext the extension to get label and help info from
	 * @param menu the menu to add to
	 * @param accelerator the accelerator to use with the new menu item
	 */
	private void populateMenuItem(String mode, LaunchShortcutExtension ext, Menu menu, int accelerator) {
		LaunchShortcutAction action = new LaunchShortcutAction(mode, ext);
		action.setActionDefinitionId(ext.getId() + "." + mode); //$NON-NLS-1$
		String helpContextId = ext.getHelpContextId();
		if (helpContextId != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(action, helpContextId);
		}
		StringBuffer label= new StringBuffer();
		if (accelerator >= 0 && accelerator < 10) {
			//add the numerical accelerator
			label.append('&');
			label.append(accelerator);
			label.append(' ');
		}
		String contextLabel= ext.getContextLabel(mode);
		// replace default action label with context label if specified.
		label.append((contextLabel != null) ? contextLabel : action.getText());
		action.setText(label.toString());
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(menu, -1);
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
