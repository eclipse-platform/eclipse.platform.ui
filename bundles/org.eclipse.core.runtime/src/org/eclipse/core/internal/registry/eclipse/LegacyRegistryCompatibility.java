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

import java.util.EventListener;
import java.util.Map;
import org.eclipse.core.internal.registry.ICompatibilityStrategy;
import org.eclipse.core.runtime.*;

/**
 * The backward compatibility strategy typing "old" Eclipse and "new" Equinox registry.
 */
public class LegacyRegistryCompatibility implements ICompatibilityStrategy {

	/* --- ICompatibilityStrategy - my listeners expect objects in the o.e.c.runtime namespace --- */

	public void invokeListener(EventListener listener, Map deltas, String filter) {
		// Backward compatibility: pass information to Eclipse-style listeners 
		if (listener instanceof IRegistryChangeListener) {
			((IRegistryChangeListener) listener).registryChanged(new LegacyRegistryChangeEvent(deltas, filter));
		}
	}

	public void setInitializationData(Object newClassInstance, org.eclipse.equinox.registry.IConfigurationElement confElement, String propertyName, Object initData) throws CoreException {
		if (newClassInstance instanceof IExecutableExtension) {
			IConfigurationElement eclipseConfElement = LegacyRegistryConverter.convert(confElement);
			((IExecutableExtension) newClassInstance).setInitializationData(eclipseConfElement, propertyName, initData);
		}
	}

	public Object create(Object result) throws CoreException {
		if (result instanceof IExecutableExtensionFactory) {
			return ((IExecutableExtensionFactory) result).create();
		}
		return result;
	}

}
