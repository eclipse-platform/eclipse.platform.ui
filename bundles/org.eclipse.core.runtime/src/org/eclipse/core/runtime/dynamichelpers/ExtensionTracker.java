/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.dynamichelpers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.internal.runtime.ListenerList;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;

/**
 * Implementation of the IExtensionTracker. 
 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionTracker 
 * @since 3.1
 */
public class ExtensionTracker implements IExtensionTracker, IRegistryChangeListener {
	//Map keeping the association between extensions and a set of objects. Key: IExtension, value: ReferenceHashSet.
	private Map extensionToObjects = new HashMap();
	private ListenerList handlers = new ListenerList();
	private final Object lock = new Object();
	private boolean closed = false;

	private static final Object[] EMPTY_ARRAY = new Object[0];

	public ExtensionTracker() {
		Platform.getExtensionRegistry().addRegistryChangeListener(this);
	}

	public void registerHandler(IExtensionChangeHandler handler, IFilter filter) {
		synchronized (lock) {
			if (closed)
				return;
			// TODO need to store the filter with the handler
			handlers.add(new HandlerWrapper(handler, filter));
		}
	}

	public void unregisterHandler(IExtensionChangeHandler handler) {
		synchronized (lock) {
			if (closed)
				return;
			handlers.remove(new HandlerWrapper(handler, null));
		}
	}

	public void registerObject(IExtension element, Object object, int referenceType) {
		if (element == null || object == null)
			return;

		synchronized (lock) {
			if (closed)
				return;

			ReferenceHashSet associatedObjects = (ReferenceHashSet) extensionToObjects.get(element);
			if (associatedObjects == null) {
				associatedObjects = new ReferenceHashSet();
				extensionToObjects.put(element, associatedObjects);
			}
			associatedObjects.add(object, referenceType);
		}
	}

	/**
	 * Implementation of IRegistryChangeListener interface.  This method should not
	 * be called by clients.
	 */
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

	/**
	 * Notify all handlers whose filter matches that the given delta occured
	 * If the list of objects is not null then this is a removal and the handlers
	 * will be given a chance to process the list.  If it is null then the notification is 
	 * an addition.
	 * @param delta the change to broadcast
	 * @param objects the objects to pass to the handlers on removals
	 */
	private void notify(IExtensionDelta delta, Object[] objects) {
		// Get a copy of the handlers for safe notification
		Object[] handlersCopy = null;
		synchronized (lock) {
			if (closed)
				return;

			if (handlers == null || handlers.isEmpty())
				return;
			handlersCopy = handlers.getListeners();
		}

		for (int i = 0; i < handlersCopy.length; i++) {
			HandlerWrapper wrapper = (HandlerWrapper) handlersCopy[i];
			if (wrapper.filter == null || wrapper.filter.matches(delta.getExtensionPoint())) {
				if (objects == null)
					applyAdd(wrapper.handler, delta.getExtension());
				else
					applyRemove(wrapper.handler, delta.getExtension(), objects);
			}
		}
	}

	protected void applyAdd(IExtensionChangeHandler handler, IExtension extension) {
		handler.addExtension(this, extension);
	}

	private void doAdd(IExtensionDelta delta) {
		notify(delta, null);
	}

	private void doRemove(IExtensionDelta delta) {
		Object[] removedObjects = null;
		synchronized (lock) {
			if (closed)
				return;

			ReferenceHashSet associatedObjects = (ReferenceHashSet) extensionToObjects.remove(delta.getExtension());
			if (associatedObjects == null)
				return;
			//Copy the objects early so we don't hold the lock too long
			removedObjects = associatedObjects.toArray();
		}
		notify(delta, removedObjects);
	}

	protected void applyRemove(IExtensionChangeHandler handler, IExtension removedExtension, Object[] removedObjects) {
		handler.removeExtension(removedExtension, removedObjects);
	}

	public Object[] getObjects(IExtension element) {
		synchronized (lock) {
			if (closed)
				return EMPTY_ARRAY;
			ReferenceHashSet objectSet = (ReferenceHashSet) extensionToObjects.get(element);
			if (objectSet == null)
				return EMPTY_ARRAY;

			return objectSet.toArray();
		}
	}

	public void close() {
		synchronized (lock) {
			if (closed)
				return;

			Platform.getExtensionRegistry().removeRegistryChangeListener(this);
			extensionToObjects = null;
			handlers = null;

			closed = true;
		}
	}

	public void unregisterObject(IExtension extension, Object object) {
		synchronized (lock) {
			if (closed)
				return;
			ReferenceHashSet associatedObjects = (ReferenceHashSet) extensionToObjects.get(extension);
			if (associatedObjects != null)
				associatedObjects.remove(object);
		}
	}

	public Object[] unregisterObject(IExtension extension) {
		synchronized (lock) {
			if (closed)
				return EMPTY_ARRAY;
			ReferenceHashSet associatedObjects = (ReferenceHashSet) extensionToObjects.remove(extension);
			if (associatedObjects == null)
				return EMPTY_ARRAY;
			return associatedObjects.toArray();
		}
	}
	
    /**
     * Return an instance of filter matching all changes for the given extension point.
     * @param xpt the extension point 
     * @return a filter
     */
	public static IFilter createExtensionPointFilter(final IExtensionPoint xpt) {
		return new IFilter() {
			public boolean matches(IExtensionPoint target) {
				return xpt.equals(target);
			}
		};
	}

	public static IFilter createExtensionPointFilter(final IExtensionPoint[] xpts) {
		return new IFilter() {
			public boolean matches(IExtensionPoint target) {
				for (int i = 0; i < xpts.length; i++)
					if (xpts[i].equals(target))
						return true;
				return false;
			}
		};
	}

	public static IFilter createNamespaceFilter(final String id) {
		return new IFilter() {
			public boolean matches(IExtensionPoint target) {
				return id.equals(target.getNamespace());
			}
		};
	}

	private class HandlerWrapper {
		IExtensionChangeHandler handler;
		IFilter filter;

		public HandlerWrapper(IExtensionChangeHandler handler, IFilter filter) {
			this.handler = handler;
			this.filter = filter;
		}

		public boolean equals(Object target) {
			// much simplified equals as we know the each conditions under which 
			// it will be called.
			return handler.equals(target);
		}

		public int hashCode() {
			return handler.hashCode();
		}
	}

}
