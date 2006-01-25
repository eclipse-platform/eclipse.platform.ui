/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.actions.LaunchShortcutAction;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * A cascading sub-menu that shows all launch shortcuts pertinent to a
 * selection. This action is similar to <code>ContextualLaunchAction</code>
 * except this action is an <code>IAction</code> rather than an action
 * delegate.
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 3.1
 */
public class LaunchShortcutsAction extends Action implements IMenuCreator, IWorkbenchWindowPulldownDelegate2 {
	
	/**
	 * Cascading menu 
	 */
	private Menu fCreatedMenu;
	
	/**
	 * Launch group
	 */
	private ILaunchGroup fGroup;
	
	/**
	 * Whether this actions enablement has been initialized
	 */
	private boolean fInitialized = false;
		
	/**
	 * Creates a cascading menu action to populate with shortcuts in the given
	 * launch group.
	 *  
	 * @param launchGroupIdentifier launch group identifier
	 */
	public LaunchShortcutsAction(String launchGroupIdentifier) {
		super();
		fGroup = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(launchGroupIdentifier);
		ILaunchMode mode = DebugPlugin.getDefault().getLaunchManager().getLaunchMode(fGroup.getMode());
		setText(mode.getLaunchAsLabel()); 
		setMenuCreator(this);
		setEnabled(existsConfigTypesForMode());
	}
	
	/**
	 * Returns the launch group associatd with this action.
	 * 
	 * @return the launch group associatd with this action
	 */
	private ILaunchGroup getLaunchGroup() {
		return fGroup;
	}

	/**
	 * @see IAction#run()
	 */
	public void run() {
		//do nothing, this action just creates a cascading menu.
	}
	
	/**
	 * @see IMenuCreator#dispose()
	 */
	public void dispose() {
		if (getCreatedMenu() != null) {
			getCreatedMenu().dispose();
		}
	}
	
	/**
	 * @see IMenuCreator#getMenu(Control)
	 */
	public Menu getMenu(Control parent) {
		return null;
	}
	
	/**
	 * @see IMenuCreator#getMenu(Menu)
	 */
	public Menu getMenu(Menu parent) {
		if (getCreatedMenu() != null) {
			 getCreatedMenu().dispose();
		 }
		setCreatedMenu(new Menu(parent));
		initMenu();
		return getCreatedMenu();
	}
	
	/**
	 * @return an Evaluation context with default variable = selection
	 */
	private IEvaluationContext createContext() {
	    List list = null;
		IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
			    IWorkbenchPart activePart = page.getActivePart();
			    if (activePart instanceof IEditorPart) {
			        list = new ArrayList();
			        list.add(((IEditorPart)activePart).getEditorInput());
			    } else if (activePart != null) {
			        IWorkbenchPartSite site = activePart.getSite();
			        if (site != null) {
	                    ISelectionProvider selectionProvider = site.getSelectionProvider();
	                    if (selectionProvider != null) {
	                        ISelection selection = selectionProvider.getSelection();
					        if (selection instanceof IStructuredSelection) {
					            list = ((IStructuredSelection)selection).toList();
					        }
	                    }
			        }
			    }
			}
		}	    
		// create a default evaluation context with default variable
		// of the user selection or editor input
		if (list == null) {
		    list = Collections.EMPTY_LIST;
		}
		IEvaluationContext context = new EvaluationContext(null, list);
		context.addVariable("selection", list); //$NON-NLS-1$
		
		return context;
	}	
	
	private void fillMenu() {
		IEvaluationContext context = createContext();
		// gather all shortcuts and run their filters so that we only run the
		// filters one time for each shortcut. Running filters can be expensive.
		// Also, only *LOADED* plugins get their filters run.
		List /* <LaunchShortcutExtension> */ allShortCuts = getLaunchConfigurationManager().getLaunchShortcuts(getLaunchGroup().getCategory());
		Iterator iter = allShortCuts.iterator();
		List filteredShortCuts = new ArrayList(10);
		while (iter.hasNext()) {
			LaunchShortcutExtension ext = (LaunchShortcutExtension) iter.next();
			try {
				if (!WorkbenchActivityHelper.filterItem(ext) && isApplicable(ext, context)) {
					filteredShortCuts.add(ext);
				}
			} catch (CoreException e) {
				// not supported
			}
		}
		iter = filteredShortCuts.iterator();
		int accelerator = 1;
		while (iter.hasNext()) {
			LaunchShortcutExtension ext = (LaunchShortcutExtension) iter.next();
			Set modes = ext.getModes(); // supported launch modes
			Iterator modeIter = modes.iterator();
			while (modeIter.hasNext()) {
				String mode = (String) modeIter.next();
				if (mode.equals(getMode())) {
					populateMenuItem(mode, ext, fCreatedMenu, accelerator++);
				}
			}
		}
		if (accelerator == 1) {
			// No shortcuts added. Add "none available" action.
			IAction action= new Action(ActionMessages.LaunchShortcutsAction_1) {}; 
			action.setEnabled(false);
			ActionContributionItem item= new ActionContributionItem(action);
			item.fill(fCreatedMenu, -1);
		}
	}
	
	/**
	 * Add the shortcut to the context menu's launch submenu.
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
	 * Evaluate the enablement logic in the contextualLaunch
	 * element description. A true result means that we should
	 * include this shortcut in the context menu.
	 * @return true iff shortcut should appear in context menu
	 */
	private boolean isApplicable(LaunchShortcutExtension ext, IEvaluationContext context) throws CoreException {
		Expression expr = ext.getContextualLaunchEnablementExpression();
		return ext.evalEnablementExpression(context, expr);
	}
	
	/**
	 * Creates the menu for the action
	 */
	private void initMenu() {
		// Add listener to repopulate the menu each time
		// it is shown to reflect changes in selection or active perspective
		fCreatedMenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				Menu m = (Menu)e.widget;
				MenuItem[] items = m.getItems();
				for (int i=0; i < items.length; i++) {
					items[i].dispose();
				}
				fillMenu();
			}
		});
	}
		
	/**
	 * Returns the mode of this action - run or debug 
	 * 
	 * @return the mode of this action - run or debug
	 */
	private String getMode() {
		return getLaunchGroup().getMode();
	}
	
	private Menu getCreatedMenu() {
		return fCreatedMenu;
	}
	
	private void setCreatedMenu(Menu createdMenu) {
		fCreatedMenu = createdMenu;
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
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		// do nothing - this is just a menu
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	    if (!fInitialized) {
	        action.setEnabled(existsConfigTypesForMode());
	        fInitialized = true;
	    }
	}

	/**
	 * Return whether there are any registered launch configuration types for
	 * the mode of this action.
	 * 
	 * @return whether there are any registered launch configuration types for
	 * the mode of this action
	 */
	private boolean existsConfigTypesForMode() {
		ILaunchConfigurationType[] configTypes = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationTypes();
		for (int i = 0; i < configTypes.length; i++) {
			ILaunchConfigurationType configType = configTypes[i];
			if (configType.supportsMode(getMode())) {
				return true;
			}
		}		
		return false;
	}
}

