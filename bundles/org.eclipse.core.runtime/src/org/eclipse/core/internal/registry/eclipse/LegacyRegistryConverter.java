/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry.eclipse;

import org.eclipse.core.runtime.*;

/**
 * Helper class to deal with element conversion between Equinox registry elements
 * and Eclipse registry elements.
 */
public class LegacyRegistryConverter {

	public final static IExtension convert(org.eclipse.equinox.registry.IExtension theExtension) {
		if (theExtension != null)
			return new LegacyExtensionHandle(theExtension);
		return null;
	}

	public final static IExtensionPoint convert(org.eclipse.equinox.registry.IExtensionPoint theExtensionPoint) {
		if (theExtensionPoint != null)
			return new LegacyExtensionPointHandle(theExtensionPoint);
		return null;
	}

	public final static IConfigurationElement convert(org.eclipse.equinox.registry.IConfigurationElement theConfigElement) {
		if (theConfigElement != null)
			return new LegacyConfigurationElementHandle(theConfigElement);
		return null;
	}

	public final static IExtensionDelta convert(org.eclipse.equinox.registry.IExtensionDelta delta) {
		if (delta != null)
			return new LegacyExtensionDelta(delta);
		return null;
	}

	public final static IConfigurationElement[] convert(org.eclipse.equinox.registry.IConfigurationElement[] equinoxes) {
		if (equinoxes == null)
			return null;
		IConfigurationElement[] result = new IConfigurationElement[equinoxes.length];
		for (int i = 0; i < equinoxes.length; i++)
			result[i] = convert(equinoxes[i]);
		return result;
	}

	public final static IExtensionPoint[] convert(org.eclipse.equinox.registry.IExtensionPoint[] equinoxes) {
		if (equinoxes == null)
			return null;
		IExtensionPoint[] result = new IExtensionPoint[equinoxes.length];
		for (int i = 0; i < equinoxes.length; i++)
			result[i] = convert(equinoxes[i]);
		return result;
	}

	public final static IExtension[] convert(org.eclipse.equinox.registry.IExtension[] equinoxes) {
		if (equinoxes == null)
			return null;
		IExtension[] result = new IExtension[equinoxes.length];
		for (int i = 0; i < equinoxes.length; i++)
			result[i] = convert(equinoxes[i]);
		return result;
	}

	public final static IExtensionDelta[] convert(org.eclipse.equinox.registry.IExtensionDelta[] equinoxDeltas) {
		IExtensionDelta[] result = new IExtensionDelta[equinoxDeltas.length];
		for (int i = 0; i < equinoxDeltas.length; i++)
			result[i] = convert(equinoxDeltas[i]);
		return result;
	}

}
