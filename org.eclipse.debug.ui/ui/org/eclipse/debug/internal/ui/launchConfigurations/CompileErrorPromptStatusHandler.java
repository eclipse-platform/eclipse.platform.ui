/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.internal.ui.AlwaysNeverDialog;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;


public class CompileErrorPromptStatusHandler implements IStatusHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		if (source instanceof ILaunchConfiguration) {
			ILaunchConfiguration config = (ILaunchConfiguration)source;
			boolean privateConfig = config.getAttribute(IDebugUIConstants.ATTR_PRIVATE, false);
			if (privateConfig) {
				return new Boolean(false);
			}
		}
		
		Shell shell = DebugUIPlugin.getShell();
		String title = LaunchConfigurationsMessages.getString("CompileErrorPromptStatusHandler.0"); //$NON-NLS-1$
		String message = LaunchConfigurationsMessages.getString("CompileErrorPromptStatusHandler.1"); //$NON-NLS-1$
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore(); 
		
		String pref = store.getString(IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR);
		if (pref != null) {
			if (pref.equals(AlwaysNeverDialog.ALWAYS)) {
				return new Boolean(true);
			}
		}
		
		PromptDialog pd = new PromptDialog(shell, title, message, IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR, store);
		pd.open();
		
		int returnValue = pd.getReturnCode();
		if (returnValue == 0 || returnValue == 2) { //YES=0, ALWAYS=2
			return new Boolean(true);
		} else { // ESC=-1, NO=1
			return new Boolean(false);
		}
		
	}

	
	private class PromptDialog extends MessageDialog {
		
		private String fPreferenceKey = null;
		private String fResult = null;
		private IPreferenceStore fStore = null;
		
		public PromptDialog(Shell parent, String title, String message, String preferenceKey, IPreferenceStore store) {
			super(parent, title, null, message, QUESTION, new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, LaunchConfigurationsMessages.getString("CompileErrorPromptStatusHandler.2")},0);		// yes is the default //$NON-NLS-1$
			fStore = store;
			fPreferenceKey = preferenceKey;
		}

		protected void buttonPressed(int id) {
			if (id == 2) { // Always
				fResult= AlwaysNeverDialog.ALWAYS;
			} else {
				fResult= AlwaysNeverDialog.PROMPT;
			}
			
			if (fStore != null && fPreferenceKey != null) {
				fStore.setValue(fPreferenceKey, fResult);
			}
			
			super.buttonPressed(id);
		}
	}

	
}
