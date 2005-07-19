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
package org.eclipse.debug.internal.ui;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Prompts the user to re-launch when a working directory
 * is not supported by the Eclipse runtime.
 */
public class WorkingDirectoryStatusHandler implements IStatusHandler {

	/**
	 * @see IStatusHandler#handleStatus(IStatus, Object)
	 */
	public Object handleStatus(IStatus status, Object source) {
		final boolean[] result = new boolean[1];
		DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
			public void run() {
				String title= DebugUIMessages.WorkingDirectoryStatusHandler_Eclipse_Runtime_1; //$NON-NLS-1$
				String message= DebugUIMessages.WorkingDirectoryStatusHandler_Eclipse_is_not_able_to_set_the_working_directory_specified_by_the_program_being_launched_as_the_current_runtime_does_not_support_working_directories__nContinue_launch_without_setting_the_working_directory__2; //$NON-NLS-1$
				result[0]= (MessageDialog.openQuestion(DebugUIPlugin.getShell(), title, message));
			}
		});
		return Boolean.valueOf(result[0]);
	}

}
