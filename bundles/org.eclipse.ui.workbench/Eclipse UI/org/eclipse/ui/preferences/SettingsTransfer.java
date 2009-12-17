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

package org.eclipse.ui.preferences;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.internal.preferences.SettingsTransferRegistryReader;

/**
 * The SettingsTransfer is the abstract superclass of settings transfers
 * used when switching workspaces.
 * @since 3.3
 *
 */
public abstract class SettingsTransfer {
	
	/**
	 * Return the configuration elements for all of the settings 
	 * transfers.
	 * @return IConfigurationElement[]
	 */
	public static IConfigurationElement[] getSettingsTransfers(){
		return (new SettingsTransferRegistryReader()).getSettingTransfers();
	}
	
	/**
	 * Transfer the settings to a workspace rooted at newWorkspacwe
	 * @param newWorkspaceRoot
	 * @return {@link IStatus} the status of the transfer.
	 */
	public abstract IStatus transferSettings(IPath newWorkspaceRoot);

	/**
	 * Return the name for the receiver.
	 * @return String
	 */
	public abstract String getName() ;

}
