/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * The SettingsTransfer is the abstract superclass of settings transfers used
 * when switching workspaces.
 *
 * @since 3.3
 */
public abstract class SettingsTransfer {

	/**
	 * Return the configuration elements for all of the settings transfers.
	 *
	 * @return IConfigurationElement[]
	 */
	public static IConfigurationElement[] getSettingsTransfers() {
		return (new SettingsTransferRegistryReader()).getSettingTransfers();
	}

	/**
	 * Transfer the settings to a workspace rooted at newWorkspacwe
	 *
	 * @param newWorkspaceRoot root of the new workspace
	 * @return {@link IStatus} the status of the transfer.
	 */
	public abstract IStatus transferSettings(IPath newWorkspaceRoot);

	/**
	 * Return the name for the receiver.
	 *
	 * @return receiver name
	 */
	public abstract String getName();

}
