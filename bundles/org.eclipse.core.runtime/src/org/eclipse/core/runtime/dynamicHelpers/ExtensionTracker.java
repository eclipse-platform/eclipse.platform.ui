/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.dynamicHelpers;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.internal.runtime.ListenerList;
import org.eclipse.core.runtime.*;

/**
 * Implementation of the IExtensionTracker. This API is EXPERIMENTAL and
 * provided as early access.
 * 
 * @since 3.1
 */
public class ExtensionTracker implements IExtensionTracker, IRegistryChangeListener {
	//Map keeping the association between extensions and a set of objects. Key: IExtension, value: ReferenceHashSet.
	private Map extensionToObjects = new HashMap();

	//The handlers
	private ListenerList additionHandlers = new ListenerList();
	private ListenerList removalHandlers = new ListenerList();

	private static final Object[] EMPTY_ARRAY = new Object[0];

	public ExtensionTracker() {
		Platform.getExtensionRegistry().addRegistryChangeListener(this);
	}

	public void registerAdditionHandler(IExtensionAdditionHandler handler) {
		synchronized (additionHandlers) {
			additionHandlers.add(handler);
		}
	}

	public void unregisterAdditionHandler(IExtensionAdditionHandler handler) {
		synchronized (additionHandlers) {
			additionHandlers.remove(handler);
		}
	}

	public void registerRemovalHandler(IExtensionRemovalHandler handler) {
		synchronized (removalHandlers) {
			removalHandlers.add(handler);
		}
	}

	public void unregisterRemovalHandler(IExtensionRemovalHandler handler) {
		synchronized (removalHandlers) {
			removalHandlers.remove(handler);
		}
	}

	public void registerObject(IExtension element, Object object, int referenceType) {
		if (element == null || object == null)
			return;

		synchronized (extensionToObjects) {
			ReferenceHashSet associatedObjects = (ReferenceHashSet) extensionToObjects.get(element);
			if (associatedObjects == null) {
				associatedObjects = new ReferenceHashSet();
				extensionToObjects.put(element, associatedObjects);
			}

			associatedObjects.add(object, referenceType);
		}
	}

	public void registryChanged(IRegistryChangeEvent event) {
		IExtensionDelta delta[] = event.getExtensionDeltas();
		int len = delta.length;
		for (int i = 0; i < len; i++)
			switch (delta[i].getKind()) {
				case IExtensionDelta.ADDED :
					doAdd(delta[i]);
					break;
				case IExtensionDelta.REMOVED :
					doRemove(delta[i]);
					break;
				default :
					break;
			}
	}

	private void doAdd(IExtensionDelta delta) {
		// Get a copy of the handlers for safe notification
		Object[] handlersCopy = null;
		synchronized (additionHandlers) {
			if (additionHandlers == null || additionHandlers.isEmpty())
				return;
			handlersCopy = additionHandlers.getListeners();
		}

		for (int i = 0; i < handlersCopy.length; i++) {
			IExtensionAdditionHandler handler = (IExtensionAdditionHandler) handlersCopy[i];
			if (handler.getExtensionPointFilter() == null || handler.getExtensionPointFilter().equals(delta.getExtensionPoint())) {
				applyAdd(handler, delta.getExtension());
			}
		}
	}

	protected void applyAdd(IExtensionAdditionHandler handler, IExtension extension) {
		handler.addInstance(this, extension);
	}

	private void doRemove(IExtensionDelta delta) {
		IExtension removedExtension = delta.getExtension();
		Object[] removedObjects = null;
		synchronized (extensionToObjects) {
			ReferenceHashSet associatedObjects = (ReferenceHashSet) extensionToObjects.remove(removedExtension);
			if (associatedObjects == null)
				return;
			//Copy the objects early so we don't hold the lock too long
			removedObjects = associatedObjects.toArray();
		}

		// Get a copy of the handlers for safe notification
		Object[] handlersCopy = null;
		synchronized (removalHandlers) {
			// No one is listening. Simply remove the objects
			if (removalHandlers == null || removalHandlers.isEmpty())
				return;
			handlersCopy = removalHandlers.getListeners();
		}

		// Find the objects that have not been gc'ed, and notify the handlers
		for (int i = 0; i < removedObjects.length; i++) {
			applyRemove((IExtensionRemovalHandler) handlersCopy[i], removedExtension, removedObjects);
		}
	}

	protected void applyRemove(IExtensionRemovalHandler handler, IExtension removedExtension, Object[] removedObjects) {
		handler.removeInstance(removedExtension, removedObjects);
	}

	public Object[] getObjects(IExtension element) {
		synchronized (extensionToObjects) {
			ReferenceHashSet objectSet = (ReferenceHashSet) extensionToObjects.get(element);
			if (objectSet == null)
				return EMPTY_ARRAY;

			return objectSet.toArray();
		}
	}

	public void close() {
		Platform.getExtensionRegistry().removeRegistryChangeListener(this);
		extensionToObjects = null;
		additionHandlers = null;
		removalHandlers = null;
	}

	public void unregisterObject(IExtension extension, Object object) {
		synchronized (extensionToObjects) {
			ReferenceHashSet associatedObjects = (ReferenceHashSet) extensionToObjects.get(extension);
			if (associatedObjects != null)
				associatedObjects.remove(object);
		}
	}

	public Object[] unregisterObject(IExtension extension) {
		synchronized (extensionToObjects) {
			ReferenceHashSet associatedObjects = (ReferenceHashSet) extensionToObjects.remove(extension);
			if (associatedObjects == null)
				return EMPTY_ARRAY;
			return associatedObjects.toArray();
		}
	}
}
