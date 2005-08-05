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
package org.eclipse.debug.internal.ui.launchConfigurations;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;


public class CompileErrorProjectPromptStatusHandler implements IStatusHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		ILaunchConfiguration config = null;
		List projects = new ArrayList();
		
		if (source instanceof List) {
			List args = (List) source;
			Iterator iterator = args.iterator();
			while (iterator.hasNext()) {
				Object arg = iterator.next();
				if (arg instanceof ILaunchConfiguration) {
					config = (ILaunchConfiguration) arg;
					if (DebugUITools.isPrivate(config)) {
						return Boolean.TRUE;
					}
				} else if (arg instanceof IProject) {
					projects.add(arg);
				}
			}
		}
				
		Shell shell = DebugUIPlugin.getShell();
		String title = LaunchConfigurationsMessages.CompileErrorPromptStatusHandler_0; 
		StringBuffer projectMessage = new StringBuffer();
		for (int i = 0; i < projects.size(); i++) {
			if (i > 0) {
				projectMessage.append(", "); //$NON-NLS-1$
			}
			projectMessage.append(((IProject)projects.get(i)).getName());
		}
		String message = MessageFormat.format(LaunchConfigurationsMessages.CompileErrorPromptStatusHandler_2, new String[]{projectMessage.toString()}); 
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore(); 
		
		String pref = store.getString(IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR);
		if (pref != null) {
			if (pref.equals(MessageDialogWithToggle.ALWAYS)) {
				return Boolean.TRUE;
			}
		}

		MessageDialogWithToggle dialog = new MessageDialogWithToggle(shell, title, null, message, MessageDialog.WARNING,
				new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 1, null, false);
		dialog.setPrefKey(IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR);
		dialog.setPrefStore(store);
		dialog.open();
		
		int returnValue = dialog.getReturnCode();
		if (returnValue == IDialogConstants.YES_ID) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
}
