/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.wizards.AbstractExtensionWizardRegistry#getExtensionPoint()
	 */
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_IMPORT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.wizards.AbstractExtensionWizardRegistry#getPlugin()
	 */
	protected String getPlugin() {
		return PlatformUI.PLUGIN_ID;
	}
}
