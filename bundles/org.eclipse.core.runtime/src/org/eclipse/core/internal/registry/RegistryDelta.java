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

import java.util.*;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;

/*
 * Basic implementation for now...
 * Aggregates extension deltas related to extension points declared by a specific host.
 */
public class RegistryDelta {
	private Set extensionDeltas = new HashSet();
	private String hostName;

	RegistryDelta(String hostName) {
		this.hostName = hostName;
	}

	public int getExtensionDeltasCount() {
		return extensionDeltas.size();
	}

	public IExtensionDelta[] getExtensionDeltas() {
		return (IExtensionDelta[]) extensionDeltas.toArray(new IExtensionDelta[extensionDeltas.size()]);
	}

	public IExtensionDelta[] getExtensionDeltas(String extensionPoint) {
		Collection selectedExtDeltas = new LinkedList();
		for (Iterator extDeltasIter = extensionDeltas.iterator(); extDeltasIter.hasNext();) {
			IExtensionDelta extensionDelta = (IExtensionDelta) extDeltasIter.next();
			if (extensionDelta.getExtension().getExtensionPointUniqueIdentifier().equals(extensionPoint))
				selectedExtDeltas.add(extensionDelta);
		}
		return (IExtensionDelta[]) selectedExtDeltas.toArray(new IExtensionDelta[selectedExtDeltas.size()]);
	}

	/**
	 * @param extensionPointId
	 * @param extensionId must not be null
	 */
	public IExtensionDelta getExtensionDelta(String extensionPointId, String extensionId) {
		for (Iterator extDeltasIter = extensionDeltas.iterator(); extDeltasIter.hasNext();) {
			IExtensionDelta extensionDelta = (IExtensionDelta) extDeltasIter.next();
			IExtension extension = extensionDelta.getExtension();
			if (extension.getExtensionPointUniqueIdentifier().equals(extensionPointId) && extension.getUniqueIdentifier() != null && extension.getUniqueIdentifier().equals(extensionId))
				return extensionDelta;
		}
		return null;
	}

	void addExtensionDelta(IExtensionDelta extensionDelta) {
		this.extensionDeltas.add(extensionDelta);
	}

	public String toString() {
		return "\n\tHost " + hostName + ": " + extensionDeltas; //$NON-NLS-1$//$NON-NLS-2$
	}

}