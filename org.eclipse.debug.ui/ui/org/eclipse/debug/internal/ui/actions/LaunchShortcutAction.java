/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.contextlaunching.LaunchingResourceManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;

/**
 * Launch shortcut action (proxy to a launch shortcut extension)
 */
public class LaunchShortcutAction extends Action {
	
	private String fMode;
	private LaunchShortcutExtension fShortcut; 

	/**
	 * Constructor
	 * @param groupid the id of the launch group
	 * @param mode the mode to launch in
	 * @param shortcut the underlying shortcut
	 */
	public LaunchShortcutAction(String mode, LaunchShortcutExtension shortcut) {
		super(shortcut.getLabel(), shortcut.getImageDescriptor());
		fShortcut = shortcut;
		fMode = mode;
		updateEnablement();
	}
	
	/**
	 * Runs with either the active editor or workbench selection.
	 * 
	 * @see IAction#run()
	 */
	public void run() {
		IStructuredSelection ss = SelectedResourceManager.getDefault().getCurrentSelection();
		Object o = ss.getFirstElement();
		if(o instanceof IEditorPart) {
			fShortcut.launch((IEditorPart) o, fMode);
		}
		else {
			fShortcut.launch(ss, fMode);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(Event event) {
		if ((event.stateMask & SWT.MOD1) > 0) {
			Set types = fShortcut.getAssociatedConfigurationTypes();
			if(!types.isEmpty()) {
				LaunchingResourceManager lrm = DebugUIPlugin.getDefault().getLaunchingResourceManager();
				IStructuredSelection selection = SelectedResourceManager.getDefault().getCurrentSelection();
				ArrayList shortcuts = new ArrayList();
				shortcuts.add(fShortcut);
				IResource resource = SelectedResourceManager.getDefault().getSelectedResource();
				if(resource == null) {
					resource = lrm.getLaunchableResource(shortcuts, selection);
				}
				List configs = lrm.getParticipatingLaunchConfigurations(selection, resource, shortcuts, fMode);
				LaunchConfigurationManager lcm = DebugUIPlugin.getDefault().getLaunchConfigurationManager();
				ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType((String) types.toArray()[0]);
				String groupid = null;
				ILaunchGroup group = lcm.getLaunchGroup(type, fMode);
				if(group != null) {
					groupid = group.getIdentifier();
				}
				ILaunchConfiguration config = lcm.getMRUConfiguration(configs, group, resource);
				if(config == null) {
					if(configs.size() > 0) {
						config = (ILaunchConfiguration) configs.get(0);
					}
				}
				if(config != null) {
					selection = new StructuredSelection(config);
				}
				else {
					if(type != null) {
						selection = new StructuredSelection(type);
					}
				}
				DebugUITools.openLaunchConfigurationDialogOnGroup(DebugUIPlugin.getShell(), selection, groupid);
			}
			else {
				run();
			}
		}
		else {
			run();
		}
	}
	
	/**
	 * Since these actions are re-created each time the run/debug as menu is
	 * filled, the enablement of this action is static.
	 */
	private void updateEnablement() {
		boolean enabled = false;
		IStructuredSelection ss = SelectedResourceManager.getDefault().getCurrentSelection();
		Object o = ss.getFirstElement();
		if(o instanceof IEditorPart) {
			enabled = true;
		}
		else {
			try {
				// check enablement logic, if any
				Expression expression = fShortcut.getShortcutEnablementExpression();
				if (expression == null) {
					enabled = !ss.isEmpty();
				} else {
					List list = ss.toList();
					IEvaluationContext context = DebugUIPlugin.createEvaluationContext(list);
					context.addVariable("selection", list); //$NON-NLS-1$
					enabled = fShortcut.evalEnablementExpression(context, expression);
				}
			} catch (CoreException e) {}
		}
		setEnabled(enabled);
	}

}
