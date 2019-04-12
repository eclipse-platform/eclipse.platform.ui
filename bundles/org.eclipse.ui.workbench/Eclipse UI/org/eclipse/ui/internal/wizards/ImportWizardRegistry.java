/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.wizards;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * Registry that contains wizards contributed via the <code>importWizards</code>
 * extension point.
 *
 * @since 3.1
 */
public class ImportWizardRegistry extends AbstractExtensionWizardRegistry {

	private static ImportWizardRegistry singleton;

	/**
	 * Return the singleton instance of this class.
	 *
	 * @return the singleton instance of this class
	 */
	public static synchronized ImportWizardRegistry getInstance() {
		if (singleton == null) {
			singleton = new ImportWizardRegistry();
		}
		return singleton;
	}

	/**
	 *
	 */
	public ImportWizardRegistry() {
		super();
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_IMPORT;
	}

	@Override
	protected String getPlugin() {
		return PlatformUI.PLUGIN_ID;
	}
}
