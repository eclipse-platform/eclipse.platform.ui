/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import java.util.Arrays;
import java.util.Map;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IRegistryChangeEvent;

/**
 * A registry change event implementation. A filter can be specified. In that case, only 
 * deltas for the selected host will be available to clients.
 */
public final class RegistryChangeEvent implements IRegistryChangeEvent {
	private String filter;
	private Map deltas;

	RegistryChangeEvent(Map deltas, String filter) {
		this.deltas = deltas;
		this.filter = filter;
	}

	private RegistryDelta[] getHostDeltas() {
		// if there is a filter, return only the delta for the selected plug-in
		if (filter != null) {
			RegistryDelta singleDelta = getHostDelta(filter);
			return singleDelta == null ? new RegistryDelta[0] : new RegistryDelta[] {singleDelta};
		}
		// there is no filter - return all deltas
		return (RegistryDelta[]) deltas.values().toArray(new RegistryDelta[deltas.size()]);
	}

	private RegistryDelta getHostDelta(String pluginId) {
		if (filter != null && !pluginId.equals(filter))
			return null;
		return (RegistryDelta) deltas.get(pluginId);
	}

	public IExtensionDelta[] getExtensionDeltas() {
		RegistryDelta[] hostDeltas = getHostDeltas();
		if (hostDeltas.length == 0)
			return new IExtensionDelta[0];
		int extensionDeltasSize = 0;
		for (int i = 0; i < hostDeltas.length; i++)
			extensionDeltasSize += hostDeltas[i].getExtensionDeltasCount();
		IExtensionDelta[] extensionDeltas = new IExtensionDelta[extensionDeltasSize];
		for (int i = 0, offset = 0; i < hostDeltas.length; i++) {
			IExtensionDelta[] hostExtDeltas = hostDeltas[i].getExtensionDeltas();
			System.arraycopy(hostExtDeltas, 0, extensionDeltas, offset, hostExtDeltas.length);
			offset += hostExtDeltas.length;
		}
		return extensionDeltas;
	}

	public IExtensionDelta[] getExtensionDeltas(String hostName) {
		RegistryDelta hostDelta = getHostDelta(hostName);
		if (hostDelta == null)
			return new IExtensionDelta[0];
		return hostDelta.getExtensionDeltas();
	}

	public IExtensionDelta[] getExtensionDeltas(String hostName, String extensionPoint) {
		RegistryDelta hostDelta = getHostDelta(hostName);
		if (hostDelta == null)
			return new IExtensionDelta[0];
		return hostDelta.getExtensionDeltas(hostName + '.' + extensionPoint);
	}

	public IExtensionDelta getExtensionDelta(String hostName, String extensionPoint, String extension) {
		RegistryDelta hostDelta = getHostDelta(hostName);
		if (hostDelta == null)
			return null;
		return hostDelta.getExtensionDelta(hostName + '.' + extensionPoint, extension);
	}

	public String toString() {
		return "RegistryChangeEvent:  " + Arrays.asList(getHostDeltas()); //$NON-NLS-1$
	}
}