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

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IConfigurationElement;

public class Utils {
	
public static org.eclipse.core.runtime.registry.IConfigurationElement[] convertConfigurationElements(IConfigurationElement[] ces) {
	org.eclipse.core.runtime.registry.IConfigurationElement[] oldConfigElt = new org.eclipse.core.runtime.registry.IConfigurationElement[ces.length];
	for (int i = 0; i < ces.length; i++) {
		oldConfigElt[i] = new ConfigurationElementWrapper(ces[i]);	
	}
	return oldConfigElt;
}
public static IConfigurationElement[] convertConfigurationElements(org.eclipse.core.runtime.registry.IConfigurationElement[] ces) {
	IConfigurationElement[] oldConfigElt = new IConfigurationElement[ces.length];
	for (int i = 0; i < ces.length; i++) {
		oldConfigElt[i] = ((ConfigurationElementWrapper)ces[i]).getAdapted();	
	}
	return oldConfigElt;
}
public static org.eclipse.core.runtime.registry.IExtensionPoint[] convertExtensionPoints(IExtensionPoint[] xps) {
	org.eclipse.core.runtime.registry.IExtensionPoint[] oldExtensionPoints = new org.eclipse.core.runtime.registry.IExtensionPoint[xps.length];
	for (int i = 0; i < xps.length; i++) {
		oldExtensionPoints[i] = new ExtensionPointWrapper(xps[i]);
	}
	return oldExtensionPoints;
}
public static IExtensionPoint[] convertExtensionPoints(org.eclipse.core.runtime.registry.IExtensionPoint[] xps) {
	IExtensionPoint[] oldExtensionPoints = new IExtensionPoint[xps.length];
	for (int i = 0; i < xps.length; i++) {
		oldExtensionPoints[i] = ((ExtensionPointWrapper)xps[i]).getAdapted();
	}
	return oldExtensionPoints;
}

public static org.eclipse.core.runtime.registry.IExtension[] convertExtensions(IExtension[] exts) {
	org.eclipse.core.runtime.registry.IExtension[] oldExtension = new org.eclipse.core.runtime.registry.IExtension[exts.length];
	for (int i = 0; i < exts.length; i++) {
		oldExtension[i] = new ExtensionWrapper(exts[i]);
	}
	return oldExtension;
}
public static IExtension[] convertExtensions(org.eclipse.core.runtime.registry.IExtension[] exts) {
	IExtension[] oldExtension = new IExtension[exts.length];
	for (int i = 0; i < exts.length; i++) {
		oldExtension[i] = ((ExtensionWrapper)exts[i]).getAdapted();
	}
	return oldExtension;
}
}