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

import org.eclipse.core.runtime.registry.IConfigurationElement;
import org.eclipse.core.runtime.registry.IExtension;

public class ExtensionWrapper implements IExtension {
	private org.eclipse.core.runtime.IExtension toAdapt;
	public ExtensionWrapper(org.eclipse.core.runtime.IExtension toAdapt) {
		this.toAdapt = toAdapt;
	}
	public IConfigurationElement[] getConfigurationElements() {
		return Utils.convertConfigurationElements(toAdapt.getConfigurationElements());
	}
	public String getExtensionPointUniqueIdentifier() {
		return toAdapt.getExtensionPointUniqueIdentifier();
	}
	public String getHostIdentifier() {
		return toAdapt.getDeclaringPluginDescriptor().getUniqueIdentifier();
	}
	public String getLabel() {
		return toAdapt.getLabel();
	}
	public String getSimpleIdentifier() {
		return toAdapt.getSimpleIdentifier();
	}
	public String getUniqueIdentifier() {
		return toAdapt.getUniqueIdentifier();
	}
	public org.eclipse.core.runtime.IExtension getAdapted() {
		return toAdapt;
	}
}
