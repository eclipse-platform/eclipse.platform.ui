package org.eclipse.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import java.text.MessageFormat;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.ILaunchHistoryChangedListener;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchHistory;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;

/**
 * Abstract implementation of an action that displays a drop-down launch
 * history for a specific launch group.
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 2.1
 */
public abstract class AbstractLaunchHistoryAction implements IWorkbenchWindowPulldownDelegate2, ILaunchHistoryChangedListener {
	
	/**
	 * The menu created by this action
	 */
	private Menu fMenu;
		
	/**
	 * The action used to render this delegate.
	 */
	private IAction fAction;
	
	/**
	 * Launch group identifier
	 */
	private String fLaunchGroupIdentifier;
	
	/**
	 * Indicates whether the launch history has changed and
	 * the sub menu needs to be recreated.
	 */
	protected boolean fRecreateMenu= false;
	
	/**
	 * Constructs a launch history action.
	 * 
	 * @param launchGroupIdentifier unique identifier of the launch group
	 * extension that this action displays a launch history for.
	 */
	public AbstractLaunchHistoryAction(String launchGroupIdentifier) {
		fLaunchGroupIdentifier = launchGroupIdentifier;
	}

	/**
	 * Sets the action used to render this delegate.
	 * 
	 * @param action the action used to render this delegate	 */
	private void setAction(IAction action) {
		fAction = action;
	}

	/**
	 * Returns the action used to render this delegate.
	 * 
	 * @return the action used to render this delegate	 */
	protected IAction getAction() {
		return fAction;
	}
	
	/**
	 * Adds the given action to the specified menu with an accelerator specified
	 * by the given number.
	 * 
	 * @param menu the menu to add the action to	 * @param action the action to add	 * @param accelerator the number that should appear as an accelerator	 */
	protected void addToMenu(Menu menu, IAction action, int accelerator) {
		if (accelerator > 0) {
			StringBuffer label= new StringBuffer();
			if (accelerator < 10) {
				//add the numerical accelerator
				label.append('&');
				label.append(accelerator);
				label.append(' ');
			}
			label.append(action.getText());
			action.setText(label.toString());
		}
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(menu, -1);
	}

	/**
	 * Initialize this action so that it can dynamically set its tooltip.  Also set the enabled state
	 * of the underlying action based on whether there are any registered launch configuration types that 
	 * understand how to launch in the mode of this action.
	 */
	private void initialize(IAction action) {
		getLaunchConfigurationManager().addLaunchHistoryListener(this);
		setAction(action);
		updateTooltip();	
		action.setEnabled(existsConfigTypesForMode());	
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
	
	/**
	 * Updates this action's tooltip to correspond to the most recent launch.
	 */
	protected void updateTooltip() {
		ILaunchConfiguration lastLaunched = getLastLaunch();
		String tooltip = null;
		if (lastLaunched == null) {
			tooltip = getLaunchHistory().getLaunchGroup().getLabel();
		} else {
			String mode = getMode();
			String launchName = lastLaunched.getName();
			if (mode.equals(ILaunchManager.RUN_MODE)) {
				tooltip = MessageFormat.format("Run {0}", new String[]{launchName});
			} else {
				tooltip = MessageFormat.format("Debug {0}", new String[]{launchName});
			}
		}
		getAction().setToolTipText(tooltip);
	}
	
	/**
	 * @see ILaunchHistoryChangedListener#launchHistoryChanged()
	 */
	public void launchHistoryChanged() {
		fRecreateMenu= true;
		updateTooltip();
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		setMenu(null);
		getLaunchConfigurationManager().removeLaunchHistoryListener(this);
	}
	
	/**
	 * Return the last launch in this action's launch history
	 */
	private ILaunchConfiguration getLastLaunch() {
		return getLaunchConfigurationManager().getLastLaunch(fLaunchGroupIdentifier);
	}

	/**
	 * @see IWorkbenchWindowPulldownDelegate#getMenu(Control)
	 */
	public Menu getMenu(Control parent) {
		setMenu(new Menu(parent));
		fillMenu(fMenu);
		initMenu();
		return fMenu;
	}
	
	/**
	 * @see IMenuCreator#getMenu(Menu)
	 */
	public Menu getMenu(Menu parent) {
		setMenu(new Menu(parent));
		fillMenu(fMenu);
		initMenu();
		return fMenu;
	}
	
	/**
	 * Creates the menu for the action
	 */
	private void initMenu() {
		// Add listener to repopulate the menu each time
		// it is shown because of dynamic history list
		fMenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				if (fRecreateMenu) {
					Menu m = (Menu)e.widget;
					MenuItem[] items = m.getItems();
					for (int i=0; i < items.length; i++) {
						items[i].dispose();
					}
					fillMenu(m);
					fRecreateMenu= false;
				}
			}
		});
	}

	/**
	 * Sets this action's drop-down menu, disposing the previous menu.
	 * 
	 * @param menu the new menu	 */
	private void setMenu(Menu menu) {
		if (fMenu != null) {
			fMenu.dispose();
		}
		fMenu = menu;
	}

	/**
	 * Fills the drop-down menu with favorites and launch history
	 * 
	 * @param menu the menu to fill
	 */
	protected void fillMenu(Menu menu) {	
		ILaunchConfiguration[] historyList= getLaunchHistory().getHistory();
		ILaunchConfiguration[] favoriteList = getLaunchHistory().getFavorites();		
		
		// Add favorites
		int accelerator = 1;
		for (int i = 0; i < favoriteList.length; i++) {
			ILaunchConfiguration launch= favoriteList[i];
			LaunchAction action= new LaunchAction(launch, getMode());
			addToMenu(menu, action, accelerator);
			accelerator++;
		}		
		
		// Separator between favorites and history
		if (favoriteList.length > 0) {
			addSeparator(menu);
		}
		
		// Add history launches next
		for (int i = 0; i < historyList.length; i++) {
			ILaunchConfiguration launch= historyList[i];
			LaunchAction action= new LaunchAction(launch, getMode());
			addToMenu(menu, action, accelerator);
			accelerator++;
		}
		
		// Separator between history and common actions
		if (historyList.length > 0) {
			addSeparator(menu);
		}

		addToMenu(menu, new LaunchAsAction(fLaunchGroupIdentifier), -1);
		addToMenu(menu, new OpenLaunchDialogAction(fLaunchGroupIdentifier), -1);
	}
	
	/**
	 * Adds a separator to the given menu
	 * 
	 * @param menu 
	 */
	protected void addSeparator(Menu menu) {
		new MenuItem(menu, SWT.SEPARATOR);
	}
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		ILaunchConfiguration configuration = getLastLaunch();
		if (configuration == null) {
			DebugUITools.openLaunchConfigurationDialogOnGroup(DebugUIPlugin.getShell(), new StructuredSelection(), fLaunchGroupIdentifier);
		} else {
			DebugUITools.launch(configuration, getMode());
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection){
		if (fAction == null) {
			initialize(action);
		} 
	}
	
	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window){
	}
	
	/**
	 * Returns the launch history associated with this action's launch group.
	 * 
	 * @return the launch history associated with this action's launch group	 */
	private LaunchHistory getLaunchHistory() {
		return getLaunchConfigurationManager().getLaunchHistory(fLaunchGroupIdentifier);
	} 
		
	/**
	 * Returns the mode (e.g., 'run' or 'debug') of this drop down.
	 * 
	 * @return the mode of this action
	 */
	protected String getMode() {
		return getLaunchHistory().getLaunchGroup().getMode();
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