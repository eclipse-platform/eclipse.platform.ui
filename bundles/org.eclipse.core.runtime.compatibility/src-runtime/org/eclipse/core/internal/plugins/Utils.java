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

public class Utils {

	public static org.eclipse.core.runtime.IConfigurationElement[] convertConfigurationElements(org.eclipse.core.runtime.registry.IConfigurationElement[] ces) {
		org.eclipse.core.runtime.IConfigurationElement[] oldConfigElt = new org.eclipse.core.runtime.IConfigurationElement[ces.length];
		for (int i = 0; i < ces.length; i++) {
			oldConfigElt[i] = new org.eclipse.core.internal.plugins.ConfigurationElement(ces[i]);
		}
		return oldConfigElt;
	}

	public static org.eclipse.core.runtime.IExtensionPoint[] convertExtensionPoints(org.eclipse.core.runtime.registry.IExtensionPoint[] xps) {
		org.eclipse.core.runtime.IExtensionPoint[] oldExtensionPoints = new org.eclipse.core.runtime.IExtensionPoint[xps.length];
		for (int i = 0; i < xps.length; i++) {
			oldExtensionPoints[i] = new org.eclipse.core.internal.plugins.ExtensionPoint(xps[i]);
		}
		return oldExtensionPoints;
	}

	public static org.eclipse.core.runtime.IExtension[] convertExtensions(org.eclipse.core.runtime.registry.IExtension[] exts) {
		org.eclipse.core.runtime.IExtension[] oldExtension = new org.eclipse.core.runtime.IExtension[exts.length];
		for (int i = 0; i < exts.length; i++) {
			oldExtension[i] = new org.eclipse.core.internal.plugins.Extension(exts[i]);
		}
		return oldExtension;
	}

}
