/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.preferences;

import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.PreferenceTransferRegistryReader;

/**
 * Manages preference transfer support for the workbench
 *
 * @since 3.1
 */
public class PreferenceTransferManager {

	/**
	 * Return an array of <code>IPreferenceTransfer</code> objects
	 * 
	 * @return an array of <code>IPreferenceTransfer</code> objects
	 */
	public static PreferenceTransferElement[] getPreferenceTransfers() {
		return new PreferenceTransferRegistryReader(IWorkbenchRegistryConstants.PL_PREFERENCE_TRANSFER)
				.getPreferenceTransfers();
	}
}
