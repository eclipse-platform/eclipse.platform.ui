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

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;

/**
 * A registry change event implementation provided for backward compatibility.
 * 
 * For general use consider ExtensionDelta.
 */
public class EclipseExtensionDelta implements org.eclipse.core.runtime.IExtensionDelta {

	private org.eclipse.equinox.registry.IExtensionDelta theEquinoxHandle;

	public EclipseExtensionDelta(org.eclipse.equinox.registry.IExtensionDelta delta) {
		theEquinoxHandle = delta;
	}

	public IExtensionPoint getExtensionPoint() {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getExtensionPoint());
	}

	public int getKind() {
		return theEquinoxHandle.getKind();
	}

	public IExtension getExtension() {
		return EclipseRegistryAdaptor.adapt(theEquinoxHandle.getExtension());
	}

	/**
	 * Unwraps handle to obtain underlying Equinox handle form Eclipse handle
	 * @return - Equinox handle 
	 */
	public org.eclipse.equinox.registry.IExtensionDelta getInternalHandle() {
		return theEquinoxHandle;
	}

}
