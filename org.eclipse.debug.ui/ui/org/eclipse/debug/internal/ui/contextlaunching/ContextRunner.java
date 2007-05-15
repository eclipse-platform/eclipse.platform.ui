/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contextlaunching;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationSelectionDialog;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutSelectionDialog;
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

import com.ibm.icu.text.MessageFormat;

/**
 * Static runner for context launching to provide the base capability of context 
 * launching to more than one form of action (drop down, toolbar, view, etc)
 * 
 * @see org.eclipse.debug.ui.actions.AbstractLaunchHistoryAction
 * @see org.eclipse.debug.ui.actions.LaunchShortcutsAction
 * @see org.eclipse.debug.ui.actions.ContextualLaunchAction
 * @see org.eclipse.debug.internal.ui.preferences.ContextLaunchingPreferencePage
 * 
 *  @since 3.3
 *  CONTEXTLAUNCHING
 */
public final class ContextRunner {
	
	/**
	 * The singleton instance of the context runner
	 */
	private static ContextRunner fgInstance = null;
	
	/**
	 * Returns the singleton instance of <code>ContextRunner</code>
	 * @return the singleton instance of <code>ContextRunner</code>
	 */
	public static ContextRunner getDefault() {
		if(fgInstance == null) {
			fgInstance = new ContextRunner();
		}
		return fgInstance;
	}
	
	/**
	 * Performs the context launching given the object context and the mode to launch in.
	 * @param group 
	 */
	public void launch(ILaunchGroup group) {
		IResource resource = SelectedResourceManager.getDefault().getSelectedResource();
		//1. resolve resource
		if(resource != null) {
			selectAndLaunch(resource, group);
			return;
		}
		//2. launch last if no resource
		if(!launchLast(group)) {
			//3. might be empty workspace try to get shortcuts
			List shortcuts = getLaunchShortcutsForEmptySelection();
			if(!shortcuts.isEmpty()) {
				showShortcutSelectionDialog(resource, shortcuts, group.getMode());
			}
			else {
				MessageDialog.openInformation(DebugUIPlugin.getShell(), ContextMessages.ContextRunner_0, ContextMessages.ContextRunner_7);
			}
		}
	}
	
	/**
	 * This method launches the last configuration that was launched, if any.
	 * @param group the launch group to launch with
	 * @return true if there was a last launch and it was launched, false otherwise
	 */
	protected boolean launchLast(ILaunchGroup group) {
		ILaunchConfiguration config = null;
		if(group != null) {
			config = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getFilteredLastLaunch(group.getIdentifier());
		}
		if(config != null) {
			DebugUITools.launch(config, group.getMode());
			return true;
		}
		return false;
	}
	
	/**
	 * Creates a listing of the launch shortcut extensions that are applicable to the underlying resource
	 * @param resource the underlying resource
	 * @return a listing of applicable launch shortcuts or an empty list, never <code>null</code>
	 */
	public List getLaunchShortcutsForEmptySelection() {
		List list = new ArrayList(); 
		List sc = getLaunchConfigurationManager().getLaunchShortcuts();
		List ctxt = new ArrayList();
		IEvaluationContext context = new EvaluationContext(null, ctxt);
		context.addVariable("selection", ctxt); //$NON-NLS-1$
		LaunchShortcutExtension ext = null;
		for(Iterator iter = sc.iterator(); iter.hasNext();) {
			ext = (LaunchShortcutExtension) iter.next();
			try {
				if(ext.evalEnablementExpression(context, ext.getContextualLaunchEnablementExpression()) && !WorkbenchActivityHelper.filterItem(ext)) {
					if(!list.contains(ext)) {
						list.add(ext);
					}
				}
			}
			catch(CoreException ce) {/*do nothing*/}
		}
		return list;
	}
	
	/**
	 * Prompts the user to select a way of launching the current resource, where a 'way'
	 * is defined as a launch shortcut, and returns if a launch took place
	 * @param resource
	 * @param group
	 * @return if the context was launched in the given mode or not
	 */
	protected boolean selectAndLaunch(IResource resource, ILaunchGroup group) {
		if(group == null) {
			return false;
		}
		ILaunchConfiguration config = getLaunchConfigurationManager().isSharedConfig(resource);
		if(config != null) {
			DebugUITools.launch(config, group.getMode());
			return true;
		}
		List configs = getLaunchConfigurationManager().getApplicableLaunchConfigurations(resource); 
		int csize = configs.size();
		if(csize == 1) {
			DebugUITools.launch((ILaunchConfiguration) configs.get(0), group.getMode());
			return true;
		}
		if(csize < 1) {
			List exts = getLaunchConfigurationManager().getLaunchShortcuts(resource);
			int esize = exts.size();
			if(esize == 1) {
				LaunchShortcutExtension ext = (LaunchShortcutExtension) exts.get(0);
				ext.launch(new StructuredSelection(resource), group.getMode());
				return true;
			}
			if(esize > 1) {
				return showShortcutSelectionDialog(resource, null, group.getMode());
			}
			if(esize < 1) {
				if(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_LAUNCH_LAST_IF_NOT_LAUNCHABLE)) {
					if(launchLast(group)) {
						return true;
					}
					else {
						MessageDialog.openInformation(DebugUIPlugin.getShell(), ContextMessages.ContextRunner_0, ContextMessages.ContextRunner_7);
						return false;
					}
				}
				IProject project = resource.getProject();
				if(project != null && !project.equals(resource)) {
					selectAndLaunch(project, group);
				}
				else {
					String msg = ContextMessages.ContextRunner_7;
					if(!resource.isAccessible()) {
						msg = MessageFormat.format(ContextMessages.ContextRunner_13, new String[] {resource.getName()});
					}
					MessageDialog.openInformation(DebugUIPlugin.getShell(), ContextMessages.ContextRunner_0, msg);
				}
			}
		}
		else if(csize > 1){
			config = getLaunchConfigurationManager().getMRUConfiguration(configs, group, resource);
			if(config != null) {
				DebugUITools.launch(config, group.getMode());
				return true;
			}
			else {
				return showConfigurationSelectionDialog(configs, group.getMode());
			}
		}
		return false;
	}
	
	/**
	 * Presents the user with a dialog to pick the launch configuration to launch
	 * @param configurations the listing of applicable configurations to present
	 * @param mode the mode
	 * @return true if something was launched, false otherwise
	 */
	protected boolean showConfigurationSelectionDialog(List configurations, String mode) {
		LaunchConfigurationSelectionDialog lsd = new LaunchConfigurationSelectionDialog(DebugUIPlugin.getShell());
		if(configurations != null) {
			lsd.setInput(configurations);
		}
		if(lsd.open() == IDialogConstants.OK_ID) {
			ILaunchConfiguration config = (ILaunchConfiguration) lsd.getResult()[0];
			DebugUITools.launch(config, mode);
			return true;
		}
		return false;
	}
	
	/**
	 * Presents a selection dialog to the user to pick a launch shortcut
	 * @param resource the resource context
	 * @param mode the mode
	 * @return true if something was launched, false otherwise
	 */
	protected boolean showShortcutSelectionDialog(IResource resource, List shortcuts, String mode) {
		LaunchShortcutSelectionDialog dialog = new LaunchShortcutSelectionDialog(resource, mode);
		if(shortcuts != null) {
			dialog.setInput(shortcuts);
		}
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			if(result.length > 0) {
				LaunchShortcutExtension method = (LaunchShortcutExtension) result[0];
				if(method != null) {
					method.launch((resource == null ? new StructuredSelection() : new StructuredSelection(resource)), mode);
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns the launch configuration manager
	 * @return the launch configuration manager
	 */
	protected LaunchConfigurationManager getLaunchConfigurationManager() {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager();
	}
}
