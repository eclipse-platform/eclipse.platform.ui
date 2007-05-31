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
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchMode;
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
			launch(config, group.getMode());
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
	 * is defined as a launch shortcut.
	 * 
	 * @param resource
	 * @param group
	 */
	protected void selectAndLaunch(IResource resource, ILaunchGroup group) {
		if(group == null) {
			return;
		}
		ILaunchConfiguration config = getLaunchConfigurationManager().isSharedConfig(resource);
		if(config != null) {
			launch(config, group.getMode());
			return;
		}
		List configs = getLaunchConfigurationManager().getApplicableLaunchConfigurations(resource);
		configs = getConfigsApplicableToMode(configs, group.getMode());
		int csize = configs.size();
		if(csize == 1) {
			launch((ILaunchConfiguration) configs.get(0), group.getMode());
			return;
		}
		if(csize < 1) {
			List exts = getLaunchConfigurationManager().getLaunchShortcuts(resource);
			List modeSpecificExts = getLaunchConfigurationManager().getShortcutsSupportingMode(exts, group.getMode());
			int esize = modeSpecificExts.size();
			if(esize == 1) {
				LaunchShortcutExtension ext = (LaunchShortcutExtension) modeSpecificExts.get(0);
				ext.launch(new StructuredSelection(resource), group.getMode());
				return;
			}
			if(esize > 1) {
				showShortcutSelectionDialog(resource, modeSpecificExts, group.getMode());
				return;
			}
			if(esize < 1) {
				if(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_LAUNCH_LAST_IF_NOT_LAUNCHABLE)) {
					if(!launchLast(group)) {
						MessageDialog.openInformation(DebugUIPlugin.getShell(), ContextMessages.ContextRunner_0, ContextMessages.ContextRunner_7);
					}
					return;
				}
				if (exts.size() > 0) {
					// there are shortcuts, but not applicable to the selected mode
					ILaunchMode launchMode = DebugPlugin.getDefault().getLaunchManager().getLaunchMode(group.getMode());
					if (launchMode == null) {
						DebugUIPlugin.logErrorMessage("Unsupported launch mode: " + group.getMode()); //$NON-NLS-1$
					} else {
						String label = launchMode.getLabel();
						String modeLabel = DebugUIPlugin.removeAccelerators(label);
						MessageDialog.openInformation(DebugUIPlugin.getShell(), MessageFormat.format(ContextMessages.ContextRunner_1, new String[]{modeLabel}),
								MessageFormat.format(ContextMessages.ContextRunner_2, new String[]{modeLabel.toLowerCase()}));
					}
				} else {
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
		}
		else if(csize > 1){
			config = getLaunchConfigurationManager().getMRUConfiguration(configs, group, resource);
			if(config != null) {
				launch(config, group.getMode());
			} else {
				showConfigurationSelectionDialog(configs, group.getMode());
			}
		}
	}
	
	/**
	 * Validates the given launch mode and launches.
	 * 
	 * @param configuration configuration to launch
	 * @param mode launch mode identifier
	 */
	private void launch(ILaunchConfiguration configuration, String mode) {
		if (validateMode(configuration, mode)) {
			DebugUITools.launch(configuration, mode);
		}
	}
	
	/**
	 * Validates the given launch mode is supported, and returns whether to continue with
	 * the launch.
	 * 
	 * @param configuration launch configuration
	 * @param mode launch mode
	 * @return whether the mode is supported
	 */
	private boolean validateMode(ILaunchConfiguration configuration, String mode) {
		try {
			// if this is a multi-mode launch, allow the launch dialog to be opened
			// to resolve a valid mode, if needed.
			if (configuration.getModes().isEmpty()) {
				if (!configuration.supportsMode(mode)) {
					ILaunchMode launchMode = DebugPlugin.getDefault().getLaunchManager().getLaunchMode(mode);
					if (launchMode == null) {
						DebugUIPlugin.logErrorMessage("Unsupported launch mode: " + mode); //$NON-NLS-1$
					} else {
						String label = launchMode.getLabel();
						String modeLabel = DebugUIPlugin.removeAccelerators(label);
						MessageDialog.openInformation(DebugUIPlugin.getShell(), MessageFormat.format(ContextMessages.ContextRunner_1, new String[]{modeLabel}),
								MessageFormat.format(ContextMessages.ContextRunner_3, new String[]{configuration.getName(), modeLabel.toLowerCase()}));
					}
					return false;
				}
			}
		} catch (CoreException e) {
			DebugUIPlugin.log(e.getStatus());
			return false;
		}
		return true;
	}
	
	/**
	 * Presents the user with a dialog to pick the launch configuration to launch
	 * and launches that configuration.
	 * 
	 * @param configurations the listing of applicable configurations to present
	 * @param mode the mode
	 */
	protected void showConfigurationSelectionDialog(List configurations, String mode) {
		LaunchConfigurationSelectionDialog lsd = new LaunchConfigurationSelectionDialog(DebugUIPlugin.getShell());
		if(configurations != null) {
			lsd.setInput(configurations);
		}
		if(lsd.open() == IDialogConstants.OK_ID) {
			ILaunchConfiguration config = (ILaunchConfiguration) lsd.getResult()[0];
			launch(config, mode);
		}
	}
	
	/**
	 * Presents a selection dialog to the user to pick a launch shortcut and
	 * launch using that shortcut.
	 * 
	 * @param resource the resource context
	 * @param mode the mode
	 */
	protected void showShortcutSelectionDialog(IResource resource, List shortcuts, String mode) {
		LaunchShortcutSelectionDialog dialog = new LaunchShortcutSelectionDialog(resource, mode);
		dialog.setInput(shortcuts);
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			if(result.length > 0) {
				LaunchShortcutExtension method = (LaunchShortcutExtension) result[0];
				if(method != null) {
					method.launch((resource == null ? new StructuredSelection() : new StructuredSelection(resource)), mode);
				}
			}
		}
	}
	
	/**
	 * Returns the launch configuration manager
	 * @return the launch configuration manager
	 */
	protected LaunchConfigurationManager getLaunchConfigurationManager() {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager();
	}
	
	private List getConfigsApplicableToMode(List configs, String mode) {
		ArrayList applicable = new ArrayList(configs);
		ListIterator iterator = applicable.listIterator();
		while (iterator.hasNext()) {
			ILaunchConfiguration config = (ILaunchConfiguration) iterator.next();
			try {
				Set modes = config.getModes();
				modes.add(mode);
				if (!config.getType().supportsModeCombination(modes)) {
					iterator.remove();
				}
			} catch (CoreException e) {
			}
		}
		return applicable;
	}
}
