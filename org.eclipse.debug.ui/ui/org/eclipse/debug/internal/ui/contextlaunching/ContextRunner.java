/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *	   Daniel Friederich (freescale) -  Bug 293210 -  Context launch launches shared launch configuration twice.
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contextlaunching;

import java.util.List;

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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;

import com.ibm.icu.text.MessageFormat;

/**
 * Static runner for context launching to provide the base capability of context 
 * launching to more than one form of action (drop down, toolbar, view, etc)
 * 
 * @see org.eclipse.debug.ui.actions.AbstractLaunchHistoryAction
 * @see org.eclipse.debug.ui.actions.LaunchShortcutsAction
 * @see org.eclipse.debug.ui.actions.ContextualLaunchAction
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
	 * The one instance of <code>LaunchingResourceManager</code> we need
	 * @since 3.4
	 */
	private LaunchingResourceManager fLRM = DebugUIPlugin.getDefault().getLaunchingResourceManager();
	
	/**
	 * Performs the context launching given the object context and the mode to launch in.
	 * @param group the launch group to launch using
	 */
	public void launch(ILaunchGroup group) {
		IStructuredSelection selection = SelectedResourceManager.getDefault().getCurrentSelection();
		IResource resource = SelectedResourceManager.getDefault().getSelectedResource();
		selectAndLaunch(resource, group, selection);
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
			if(config != null) {
				launch(config, group.getMode());
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Prompts the user to select a way of launching the current resource, where a 'way'
	 * is defined as a launch shortcut.
	 * 
	 * @param resource the resource context
	 * @param group the launch group to launch with
	 * @param selection the current selection
	 */
	protected void selectAndLaunch(IResource resource, ILaunchGroup group, IStructuredSelection selection) {
		if(group != null) {			
			LaunchConfigurationManager lcm = DebugUIPlugin.getDefault().getLaunchConfigurationManager();
			String mode = group.getMode();
			List shortcuts = fLRM.getShortcutsForSelection(selection, mode);
		// allow the shortcut to translate/provide the resource for the launch
			IResource overrideResource = fLRM.getLaunchableResource(shortcuts, selection);
			if(overrideResource != null) {
				resource = overrideResource;
			}
			shortcuts = fLRM.pruneShortcuts(shortcuts, resource, mode);
		//see if the context is a shared configuration
			ILaunchConfiguration config = lcm.isSharedConfig(resource);
			if(config != null) {
				launch(config, mode);
				return;
			}
		//get the configurations from the resource and participants	
			List configs = fLRM.getParticipatingLaunchConfigurations(selection, resource, shortcuts, mode);
			int csize = configs.size();
			if(csize == 1) {
				launch((ILaunchConfiguration) configs.get(0), mode);
			}
			else if(csize < 1) {
				int esize = shortcuts.size();
				if(esize == 1) {
					launchShortcut(selection, (LaunchShortcutExtension) shortcuts.get(0), mode);
				}
				else if(esize > 1) {
					showShortcutSelectionDialog(resource, shortcuts, mode, selection);
				}
				else if(esize < 1) {
					if(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_LAUNCH_LAST_IF_NOT_LAUNCHABLE)) {
						if(!launchLast(group)) {
							MessageDialog.openInformation(DebugUIPlugin.getShell(), ContextMessages.ContextRunner_0, ContextMessages.ContextRunner_7);
						}
					} else {
						if(resource != null) {
							IProject project = resource.getProject();
							if(project != null && !project.equals(resource)) {
								selectAndLaunch(project, group, new StructuredSelection(project));
							}
							else {
								String msg = ContextMessages.ContextRunner_7;
								if(!resource.isAccessible()) {
									msg = MessageFormat.format(ContextMessages.ContextRunner_13, new String[] {resource.getName()});
								}
								MessageDialog.openInformation(DebugUIPlugin.getShell(), ContextMessages.ContextRunner_0, msg);
							}
						}
						else {
							if (!launchLast(group)) {
								MessageDialog.openInformation(DebugUIPlugin.getShell(), ContextMessages.ContextRunner_0, ContextMessages.ContextRunner_7);
							}
						}
					}
				}
			}
			else if(csize > 1){
				config = lcm.getMRUConfiguration(configs, group, resource);
				if(config != null) {
					launch(config, mode);
				} else {
					showConfigurationSelectionDialog(configs, mode);
				}
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
	 * Delegate method that calls the appropriate launch method on a <code>LaunchShortcutExtension</code> given 
	 * the current resource and selection context 
	 * @param selection the current selection
	 * @param shortcut the shortcut that wants to launch
	 * @param mode the mode to launch in
	 * 
	 * @since 3.4
	 */
	private void launchShortcut(IStructuredSelection selection, LaunchShortcutExtension shortcut, String mode) {
		Object o = selection.getFirstElement();
		if(o instanceof IEditorPart) {
			shortcut.launch((IEditorPart) o, mode);
		}
		else {
			shortcut.launch(selection, mode);
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
		LaunchConfigurationSelectionDialog lsd = new LaunchConfigurationSelectionDialog(DebugUIPlugin.getShell(), configurations);
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
	 * @param shortcuts the list of applicable shortcuts
	 * @param mode the mode
	 * @param selection the current selection
	 */
	protected void showShortcutSelectionDialog(IResource resource, List shortcuts, String mode, IStructuredSelection selection) {
		LaunchShortcutSelectionDialog dialog = new LaunchShortcutSelectionDialog(shortcuts, resource, mode);
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			if(result.length > 0) {
				LaunchShortcutExtension method = (LaunchShortcutExtension) result[0];
				if(method != null) {
					launchShortcut(selection, method, mode);
				}
			}
		}
	}
}
