/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

import org.eclipse.core.internal.registry.eclipse.*;

/**
 * Use this class to convert registry elements into the legacy form. 
 * <p>
 * This class cannot be instantiated or subclassed by clients; all functionality is provided 
 * by static methods.
 * </p>
 * 
 * @see IExtension#toEquinox()
 * @see IExtensionPoint#toEquinox()
 * @see IConfigurationElement#toEquinox()
 * @see IExtensionDelta#toEquinox()
 * 
 * @since org.eclipse.core.runtime 3.2
 */
public final class LegacyRegistryConverter {

	/**
	 * Converts the given Equinox extension object into a legacy runtime 
	 * extension object. Return <code>null</code> if the given extension is 
	 * <code>null</code>.
	 *
	 * @param extension the extension in the Equinox registry format
	 * @return the extension converted to the legacy registry format
	 * @see org.eclipse.core.runtime.IExtension#toEquinox()
	 * @see org.eclipse.equinox.registry.IExtension
	 */
	public static IExtension convert(org.eclipse.equinox.registry.IExtension extension) {
		return extension == null ? null : new LegacyExtensionHandle(extension);
	}

	/**
	 * Converts the Equinox extension point object into an equivalent legacy runtime 
	 * extension point object. Return <code>null</code> if the given extension 
	 * point is <code>null</code>.
	 *
	 * @param extensionPoint extension point in the Equinox registry format
	 * @return the extension point converted to the legacy registry format
	 * @see org.eclipse.core.runtime.IExtensionPoint#toEquinox()
	 * @see org.eclipse.equinox.registry.IExtensionPoint
	 */
	public static IExtensionPoint convert(org.eclipse.equinox.registry.IExtensionPoint extensionPoint) {
		return extensionPoint == null ? null : new LegacyExtensionPointHandle(extensionPoint);
	}

	/**
	 * Converts the given Equinox configuration element object to an equivalent
	 * legacy runtime configuration element object. Returns <code>null</code> if
	 * the given configuration element is <code>null</code>.
	 *
	 * @param configurationElement the configuration element in the Equinox registry format
	 * @return the configuration element converted to the legacy registry format
	 * @see org.eclipse.core.runtime.IConfigurationElement#toEquinox()
	 * @see org.eclipse.equinox.registry.IConfigurationElement
	 */
	public static IConfigurationElement convert(org.eclipse.equinox.registry.IConfigurationElement configurationElement) {
		return configurationElement == null ? null : new LegacyConfigurationElementHandle(configurationElement);
	}

	/**
	 * Converts the given Equinox extension registry delta object into an equivalent
	 * legacy runtime extension registry delta object. Returns <code>null</code> if
	 * the given delta is <code>null</code>.
	 *
	 * @param delta extension delta in the Equinox registry format
	 * @return the extension delta converted to the legacy registry format
	 * @see org.eclipse.core.runtime.IExtensionDelta#toEquinox()
	 * @see org.eclipse.equinox.registry.IExtensionDelta
	 */
	public static IExtensionDelta convert(org.eclipse.equinox.registry.IExtensionDelta delta) {
		return delta == null ? null : new LegacyExtensionDelta(delta);
	}

	/**
	 * Converts the given array of Equinox registry configuration elements into an 
	 * array of legacy runtime configuration elements.
	 *
	 * @param elements array of configuration elements in the Equinox registry format
	 * @return the array of configuration elements converted to the legacy registry format
	 * @see org.eclipse.core.runtime.IConfigurationElement#toEquinox()
	 * @see org.eclipse.equinox.registry.IConfigurationElement
	 */
	public static IConfigurationElement[] convert(org.eclipse.equinox.registry.IConfigurationElement[] elements) {
		if (elements == null)
			return null;
		IConfigurationElement[] result = new IConfigurationElement[elements.length];
		for (int i = 0; i < elements.length; i++)
			result[i] = convert(elements[i]);
		return result;
	}

	/**
	 * Converts the given array of Equinox extension point objects to equivalent 
	 * legacy runtime extension point objects.
	 *
	 * @param extensionPoints array of extension points in the Equinox registry format
	 * @return the array of extension points converted to the legacy registry format
	 * @see org.eclipse.core.runtime.IExtensionPoint#toEquinox()
	 * @see org.eclipse.equinox.registry.IExtensionPoint
	 */
	public static IExtensionPoint[] convert(org.eclipse.equinox.registry.IExtensionPoint[] extensionPoints) {
		if (extensionPoints == null)
			return null;
		IExtensionPoint[] result = new IExtensionPoint[extensionPoints.length];
		for (int i = 0; i < extensionPoints.length; i++)
			result[i] = convert(extensionPoints[i]);
		return result;
	}

	/**
	 * Converts the given array of Equinox extension objects into an equivalent 
	 * array of legacy runtime extension objects.
	 *
	 * @param extensions array of extensions in the Equinox registry format
	 * @return the array of extensions converted to the legacy registry format
	 * @see org.eclipse.core.runtime.IExtension#toEquinox()
	 * @see org.eclipse.equinox.registry#IExtension
	 */
	public static IExtension[] convert(org.eclipse.equinox.registry.IExtension[] extensions) {
		if (extensions == null)
			return null;
		IExtension[] result = new IExtension[extensions.length];
		for (int i = 0; i < extensions.length; i++)
			result[i] = convert(extensions[i]);
		return result;
	}

	/**
	 * Converts the given array of Equinox extension delta objects to an 
	 * equivalent array of legacy runtime extention delta objects.
	 *
	 * @param equinoxDeltas array of extension deltas in the Equinox registry format
	 * @return the array of extension deltas converted to the legacy registry format
	 * @see org.eclipse.core.runtime.IExtensionDelta#toEquinox()
	 * @see org.eclipse.equinox.registry.IExtensionDelta
	 */
	public static IExtensionDelta[] convert(org.eclipse.equinox.registry.IExtensionDelta[] equinoxDeltas) {
		IExtensionDelta[] result = new IExtensionDelta[equinoxDeltas.length];
		for (int i = 0; i < equinoxDeltas.length; i++)
			result[i] = convert(equinoxDeltas[i]);
		return result;
	}

	/**
	 * Converts the given Equinox registry exception to an equivalent legacy runtime exception.
	 * @param exception the exception in the Equinox registry format
	 * @return the exception converted to the legacy registry format
	 */
	public final static InvalidRegistryObjectException convert(org.eclipse.equinox.registry.InvalidRegistryObjectException exception) {
		InvalidRegistryObjectException wrappedException = new InvalidRegistryObjectException();
		wrappedException.setStackTrace(exception.getStackTrace());
		return wrappedException;
	}
}
