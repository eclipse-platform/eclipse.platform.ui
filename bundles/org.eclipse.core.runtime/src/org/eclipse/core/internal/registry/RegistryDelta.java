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

/**
 * The extension deltas are grouped by namespace. There is one registry delta by namespace.
 */
public class RegistryDelta {
	private Set extensionDeltas = new HashSet(); //the extension deltas (each element indicate the type of the delta)
	private IObjectManager objectManager;	//The object manager from which all the objects contained in the deltas will be found.
	
	RegistryDelta() {
		//Nothing to do
	}

	public int getExtensionDeltasCount() {
		return extensionDeltas.size();
	}

	public IExtensionDelta[] getExtensionDeltas() {
		return (IExtensionDelta[]) extensionDeltas.toArray(new ExtensionDelta[extensionDeltas.size()]);
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
		((ExtensionDelta) extensionDelta).setContainingDelta(this);
	}

	public String toString() {
		return "\n\tHost " +  ": " + extensionDeltas; //$NON-NLS-1$//$NON-NLS-2$
	}

	void setObjectManager(IObjectManager objectManager) {
		this.objectManager = objectManager;
		//TODO May want to add into the existing one here... if it is possible to have batching 
	}
	
	IObjectManager getObjectManager() {
		return objectManager;
	}
}