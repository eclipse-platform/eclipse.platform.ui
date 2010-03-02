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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.preferences.SettingsTransfer;

/**
 * The WorkbenchSettingsTransfer is the abstract superclass of settings
 * transfers in the workbench.
 * 
 * @since 3.3
 * 
 */
public abstract class WorkbenchSettingsTransfer extends SettingsTransfer {

	/**
	 * Return a status message for missing workspace settings.
	 * @return IStatus
	 */
	protected IStatus noWorkingSettingsStatus() {
		return new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
				WorkbenchMessages.WorkbenchSettings_CouldNotFindLocation);
	}

	/**
	 * Return the workbench settings location for the new root 
	 * @param newWorkspaceRoot
	 * @return IPath or <code>null</code> if it can't be determined.
	 */
	protected IPath getNewWorkbenchStateLocation(IPath newWorkspaceRoot) {
		IPath currentWorkspaceRoot = Platform.getLocation();
	
		IPath dataLocation = WorkbenchPlugin.getDefault().getDataLocation();
	
		if (dataLocation == null)
			return null;
		int segmentsToRemove = dataLocation
				.matchingFirstSegments(currentWorkspaceRoot);
	
		// Strip it down to the extension
		dataLocation = dataLocation.removeFirstSegments(segmentsToRemove);
		// Now add in the
		dataLocation = newWorkspaceRoot.append(dataLocation);
		return dataLocation;
	}

}
