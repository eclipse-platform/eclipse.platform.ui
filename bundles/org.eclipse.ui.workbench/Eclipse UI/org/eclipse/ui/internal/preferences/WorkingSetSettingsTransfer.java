/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.preferences;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.AbstractWorkingSetManager;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkingSetManager;

/**
 * The WorkingSetSettingsTransfer is the settings transfer for the workbench
 * working sets.
 * 
 * @since 3.3
 * 
 */
public class WorkingSetSettingsTransfer extends WorkbenchSettingsTransfer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.preferences.SettingsTransfer#getName()
	 */
	public String getName() {
		return WorkbenchMessages.WorkingSets_Name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.preferences.SettingsTransfer#transferSettings(org.eclipse.core.runtime.IPath)
	 */
	public IStatus transferSettings(IPath newWorkspaceRoot) {
		IPath dataLocation = getNewWorkbenchStateLocation(newWorkspaceRoot);

		if (dataLocation == null)
			return noWorkingSettingsStatus();

		dataLocation = dataLocation
				.append(WorkingSetManager.WORKING_SET_STATE_FILENAME);

		File stateFile = new File(dataLocation.toOSString());

		try {
			IWorkingSetManager manager = PlatformUI.getWorkbench()
					.getWorkingSetManager();
			if (manager instanceof AbstractWorkingSetManager)
				((AbstractWorkingSetManager) manager).saveState(stateFile);
			else
				return new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
						WorkbenchMessages.WorkingSets_CannotSave);
		} catch (IOException e) {
			new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					WorkbenchMessages.ProblemSavingWorkingSetState_message, e);
		}
		return Status.OK_STATUS;

	}
}
