/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

/**
 * This class provides a mechanism to prompt users in the UI thread from debug.core in the case where
 * duplicate launch delegates have been detected and a preferred delegate needs to be selected.
 * 
 * As this handler is used once a launch has been started, and only prompts in the event that the launch <i>can</i>
 * continue with further input, it must be a blocking operation.
 * 
 * @since 3.3
 * 
 * EXPERIMENTAL
 */
public class DuplicateLaunchDelegatesStatusHandler implements IStatusHandler {
	
	/**
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		if(source instanceof Object[]) {
			Object[] infos = (Object[]) source;
			if(infos.length == 2) {
				ILaunchConfiguration config = (ILaunchConfiguration) infos[0];
				String mode = (String) infos[1];
				Shell shell = DebugUIPlugin.getShell();
				
				IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
				boolean prompt = store.getBoolean(IDebugPreferenceConstants.PREF_PROMPT_FOR_DUPLICATE_DELEGATES);
				if(prompt) {
					SelectLaunchDelegateActionDialog sldad = new SelectLaunchDelegateActionDialog(shell);
					int ret = sldad.open();
					if(ret != IDialogConstants.CANCEL_ID) {
						String action = (String) sldad.getResult();
						return doAction(action, config, mode, shell);
					}
				}
				else {
					String action = store.getString(IDebugPreferenceConstants.PREF_DEFAULT_DUPLICATE_DELEGATE_ACTION);
					return doAction(action, config, mode, shell);
				}
			}
		}
		return Status.CANCEL_STATUS;
	}

	/**
	 * Runs the specified action which is one of: 'list', 'dialog', 'page'
	 * @param action the short name of the action to be performed
	 * @param config the <code>ILaunchConfiguration</code> associated with the launch in progress
	 * @param mode the mode of the launch in progress
	 * @param shell the <code>DebugUIPlugin</code> shell to open dialogs on
	 * @throws CoreException
	 */
	private IStatus doAction(String action, ILaunchConfiguration config, String mode, Shell shell) throws CoreException{
		if("dialog".equals(action)) { //$NON-NLS-1$
			int ret = DebugUITools.openLaunchConfigurationEditDialog(shell, 
					config, 
					DebugUITools.getLaunchGroup(config, mode).getIdentifier(), 
					null, true);
			if(ret == IDialogConstants.CANCEL_ID) {
				return Status.CANCEL_STATUS;
			}
		}
		else if("list".equals(action)) { //$NON-NLS-1$
			HashSet modes = new HashSet();
			modes.add(mode);
			modes.addAll(config.getModes());
			SelectLaunchDelegatesDialog sld = new SelectLaunchDelegatesDialog(shell, config.getType().getDelegates(modes));
			if(sld.open() == IDialogConstants.OK_ID) {
				Object[] res = sld.getResult();
				if(res != null && res.length > 0) {
					config.getType().setPreferredDelegate(modes, (ILaunchDelegate) res[0]);
				}
				else {
					return Status.CANCEL_STATUS;
				}
			}
		}
		else if("page".equals(action)) { //$NON-NLS-1$
			SWTUtil.showPreferencePage("org.eclipse.debug.ui.LaunchDelegatesPreferencePage"); //$NON-NLS-1$
		}
		//check that the delegate has been set
		HashSet modes = new HashSet();
		modes.add(mode);
		modes.addAll(config.getModes());
		return (config.getType().getPreferredDelegate(modes) == null ? Status.CANCEL_STATUS : Status.OK_STATUS);
	}
	
}
