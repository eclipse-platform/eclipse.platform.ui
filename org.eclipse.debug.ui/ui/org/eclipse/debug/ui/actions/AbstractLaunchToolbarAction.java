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
package org.eclipse.debug.ui.actions;


import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.OrganizeFavoritesAction;
import org.eclipse.debug.internal.ui.preferences.DebugWorkInProgressPreferencePage;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Menu;

/**
 * A launch history action that also includes launch shortcut actions (run/debug
 * as), and an action to open the launch configuration dialog.
 * 
 * @since 2.1
 */
public class AbstractLaunchToolbarAction extends AbstractLaunchHistoryAction {


	/**
	 * Constructs a launch toolbar action.
	 *
	 * @param launchGroupIdentifier unique identifier of the launch group
	 * extension that this action displays a launch history, shortcuts, and
	 * launch configuration dialog for.
	 */
	public AbstractLaunchToolbarAction(String launchGroupIdentifier) {
		super(launchGroupIdentifier);
	}

	/**
	 * Fills the drop-down menu with favorites and launch history,
	 * launch shortcuts, and an action to open the launch configuration dialog.
	 *
	 * @param menu the menu to fill
	 */
	protected void fillMenu(Menu menu) {
		super.fillMenu(menu);

		// Separator between history and common actions
		if (menu.getItemCount() > 0) {
			addSeparator(menu);
		}

		// TODO: work in progress cleanup
		if (DebugUIPlugin.getDefault().getPluginPreferences().getBoolean(DebugWorkInProgressPreferencePage.WIP_PREF_CONTEXT_LAUNCH)) {
			addToMenu(menu, new LaunchShortcutsAction(getLaunchGroupIdentifier()), -1);
		} else {
			addToMenu(menu, new LaunchAsAction(getLaunchGroupIdentifier()), -1);
		}
		addToMenu(menu, new OpenLaunchDialogAction(getLaunchGroupIdentifier()), -1);
		addToMenu(menu, new OrganizeFavoritesAction(getLaunchGroupIdentifier()), -1);
	}
	
	/**
	 * Launch the last launch, or open the launch config dialog if none.
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		ILaunchConfiguration configuration = getLastLaunch();
		if (configuration == null) {
			DebugUITools.openLaunchConfigurationDialogOnGroup(DebugUIPlugin.getShell(), new StructuredSelection(), getLaunchGroupIdentifier());
		} else {
			DebugUITools.launch(configuration, getMode());
		}
	}	
}
