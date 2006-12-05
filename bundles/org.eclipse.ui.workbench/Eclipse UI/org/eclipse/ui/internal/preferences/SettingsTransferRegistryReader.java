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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * The SettingsTransferRegistryReader is the class that supplies all of the
 * transfer settings used by the settingsTransfer in the preferencesTransfer
 * extension point.
 * 
 * 
 * @since 3.3
 * 
 */
public class SettingsTransferRegistryReader extends RegistryReader {

	Collection settingsTransfers = new ArrayList();

	/**
	 * Create an instance of the receiver.
	 */
	public SettingsTransferRegistryReader() {

	}

	/**
	 * Get all of the currently registered settings transfers.
	 * 
	 * @return IConfigurationElement[]
	 */
	public IConfigurationElement[] getSettingTransfers() {

		settingsTransfers = new ArrayList();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		readRegistry(registry, WorkbenchPlugin.PI_WORKBENCH,
				IWorkbenchRegistryConstants.PL_PREFERENCE_TRANSFER);

		IConfigurationElement[] transfers = new IConfigurationElement[settingsTransfers
				.size()];
		settingsTransfers.toArray(transfers);
		return transfers;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.registry.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
	 */
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(
				IWorkbenchRegistryConstants.TAG_SETTINGS_TRANSFER)) {

			settingsTransfers.add(element);
			return true;
		}

		//Ignore the preference transfers
		return element.getName().equals(
				IWorkbenchRegistryConstants.TAG_TRANSFER);
	}

}
