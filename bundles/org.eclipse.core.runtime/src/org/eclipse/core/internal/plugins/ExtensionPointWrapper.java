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

import org.eclipse.core.runtime.model.ExtensionModel;
import org.eclipse.core.runtime.registry.IExtension;
import org.eclipse.core.runtime.registry.IExtensionPoint;

public class ExtensionPointWrapper implements IExtensionPoint {
	private org.eclipse.core.runtime.IExtensionPoint toAdapt;
	public ExtensionPointWrapper(org.eclipse.core.runtime.IExtensionPoint toAdapt) {
		this.toAdapt = toAdapt;
	}
	public IExtension getExtension(String extensionId) {
		return new ExtensionWrapper(toAdapt.getExtension(extensionId));
	}
	public IExtension[] getExtensions() {
		return Utils.convertExtensions(toAdapt.getExtensions());
	}
	public String getParentIdentifier() {
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
	public org.eclipse.core.runtime.IExtensionPoint getAdapted() {
		return toAdapt;
	}
	public void setExtensions(IExtension[] newExtensions) {
		ExtensionModel[] newExtensionModels = null;
		if (newExtensions != null && newExtensions.length > 0) {
			newExtensionModels = new ExtensionModel[newExtensions.length];
			System.arraycopy(Utils.convertExtensions(newExtensions),0,newExtensionModels,0,newExtensions.length);
		}			
		((ExtensionPoint)toAdapt).setDeclaredExtensions(newExtensionModels);
	}
}
