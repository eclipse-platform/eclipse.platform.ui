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
public class EclipseRegistryAdaptor {

	public final static IExtension adapt(org.eclipse.equinox.registry.IExtension theExtension) {
		if (theExtension != null)
			return new EclipseExtensionHandle(theExtension);
		return null;
	}

	public final static IExtensionPoint adapt(org.eclipse.equinox.registry.IExtensionPoint theExtensionPoint) {
		if (theExtensionPoint != null)
			return new EclipseExtensionPointHandle(theExtensionPoint);
		return null;
	}

	public final static IConfigurationElement adapt(org.eclipse.equinox.registry.IConfigurationElement theConfigElement) {
		if (theConfigElement != null)
			return new EclipseConfigurationElementHandle(theConfigElement);
		return null;
	}

	public final static IExtensionDelta adapt(org.eclipse.equinox.registry.IExtensionDelta delta) {
		if (delta != null)
			return new EclipseExtensionDelta(delta);
		return null;
	}

	public final static IConfigurationElement[] adapt(org.eclipse.equinox.registry.IConfigurationElement[] equinoxes) {
		if (equinoxes == null)
			return null;
		IConfigurationElement[] result = new IConfigurationElement[equinoxes.length];
		for (int i = 0; i < equinoxes.length; i++)
			result[i] = adapt(equinoxes[i]);
		return result;
	}

	public final static IExtensionPoint[] adapt(org.eclipse.equinox.registry.IExtensionPoint[] equinoxes) {
		if (equinoxes == null)
			return null;
		IExtensionPoint[] result = new IExtensionPoint[equinoxes.length];
		for (int i = 0; i < equinoxes.length; i++)
			result[i] = adapt(equinoxes[i]);
		return result;
	}

	public final static IExtension[] adapt(org.eclipse.equinox.registry.IExtension[] equinoxes) {
		if (equinoxes == null)
			return null;
		IExtension[] result = new IExtension[equinoxes.length];
		for (int i = 0; i < equinoxes.length; i++)
			result[i] = adapt(equinoxes[i]);
		return result;
	}

	public final static IExtensionDelta[] adapt(org.eclipse.equinox.registry.IExtensionDelta[] equinoxDeltas) {
		IExtensionDelta[] result = new IExtensionDelta[equinoxDeltas.length];
		for (int i = 0; i < equinoxDeltas.length; i++)
			result[i] = adapt(equinoxDeltas[i]);
		return result;
	}

}
