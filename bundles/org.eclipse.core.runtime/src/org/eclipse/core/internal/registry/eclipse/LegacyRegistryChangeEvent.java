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
package org.eclipse.core.internal.registry.eclipse;

import java.util.Map;
import org.eclipse.core.internal.registry.RegistryChangeEvent;
import org.eclipse.core.runtime.*;

/**
 * A registry change event implementation provided for backward compatibility.
 * For general use consider RegistryChangeEvent.
 *
 * @since org.eclipse.core.runtime 3.2 
 */
public final class LegacyRegistryChangeEvent implements IRegistryChangeEvent {

	private org.eclipse.equinox.registry.IRegistryChangeEvent target;

	public LegacyRegistryChangeEvent(org.eclipse.equinox.registry.IRegistryChangeEvent event) {
		target = event;
	}

	public LegacyRegistryChangeEvent(Map deltas, String filter) {
		target = new RegistryChangeEvent(deltas, filter);
	}

	public IExtensionDelta[] getExtensionDeltas() {
		return LegacyRegistryConverter.convert(target.getExtensionDeltas());
	}

	public IExtensionDelta[] getExtensionDeltas(String hostName) {
		return LegacyRegistryConverter.convert(target.getExtensionDeltas(hostName));
	}

	public IExtensionDelta[] getExtensionDeltas(String hostName, String extensionPoint) {
		return LegacyRegistryConverter.convert(target.getExtensionDeltas(hostName, extensionPoint));
	}

	public IExtensionDelta getExtensionDelta(String hostName, String extensionPoint, String extension) {
		return LegacyRegistryConverter.convert(target.getExtensionDelta(hostName, extensionPoint, extension));
	}

	/**
	 * Unwraps handle to obtain underlying Equinox handle form Eclipse handle
	 * @return - Equinox handle 
	 */
	public org.eclipse.equinox.registry.IRegistryChangeEvent toEquinox() {
		return target;
	}
}
