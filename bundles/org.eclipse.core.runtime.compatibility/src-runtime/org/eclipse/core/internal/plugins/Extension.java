/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.plugins;

import org.eclipse.core.runtime.*;

public class Extension implements IExtension {
	private org.eclipse.core.runtime.registry.IExtension extension;

	public Extension(org.eclipse.core.runtime.registry.IExtension toAdapt) {
		extension = toAdapt;
	}

	public IConfigurationElement[] getConfigurationElements() {
		return Utils.convertConfigurationElements(extension.getConfigurationElements());
	}

	public IPluginDescriptor getDeclaringPluginDescriptor() {
		return InternalPlatform.getPluginRegistry().getPluginDescriptor(extension.getParentIdentifier());
	}

	public String getExtensionPointUniqueIdentifier() {
		return extension.getExtensionPointIdentifier();
	}

	public String getLabel() {
		return extension.getLabel();
	}

	public String getSimpleIdentifier() {
		return extension.getSimpleIdentifier();
	}

	public String getUniqueIdentifier() {
		return extension.getUniqueIdentifier();
	}

}
