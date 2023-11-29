/*******************************************************************************
 * Copyright (c) 2004, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @since 3.1
 */
public final class PropertyListenerList {
	private Map<String, List<IPropertyMapListener>> listeners;
	private List<IPropertyMapListener> globalListeners;
	private static String[] singlePropertyDelta;
	private static Object mutex = new Object();

	public PropertyListenerList() {
	}

	public void firePropertyChange(String prefId) {
		String[] delta;

		// Optimization: as long as we're not being called recursively,
		// we can reuse the same delta object to avoid repeated memory
		// allocation.
		synchronized (mutex) {
			if (singlePropertyDelta != null) {
				delta = singlePropertyDelta;
				singlePropertyDelta = null;
			} else {
				delta = new String[] { prefId };
			}
		}

		delta[0] = prefId;

		firePropertyChange(delta);

		// Optimization: allow this same delta object to be reused at a later
		// time
		if (singlePropertyDelta == null) {
			synchronized (mutex) {
				singlePropertyDelta = delta;
			}
		}
	}

	public void firePropertyChange(String[] propertyIds) {
		if (globalListeners != null) {
			for (IPropertyMapListener next : globalListeners) {
				next.propertyChanged(propertyIds);
			}
		}

		if (listeners != null) {

			// To avoid temporary memory allocation, we try to simply move the
			// result pointer around if possible. We only allocate a HashSet
			// to compute which listeners we care about
			Collection<IPropertyMapListener> result = Collections.emptySet();
			HashSet<IPropertyMapListener> union = null;

			for (String property : propertyIds) {
				List<IPropertyMapListener> existingListeners = listeners.get(property);

				if (existingListeners != null) {
					if (result.isEmpty()) {
						result = existingListeners;
					} else {
						if (union == null) {
							union = new HashSet<>();
							union.addAll(result);
							result = union;
						}

						union.addAll(existingListeners);
					}
				}
			}

			for (IPropertyMapListener next : result) {
				next.propertyChanged(propertyIds);
			}
		}
	}

	public void add(IPropertyMapListener newListener) {
		if (globalListeners == null) {
			globalListeners = new ArrayList<>();
		}

		globalListeners.add(newListener);
		newListener.listenerAttached();
	}

	/**
	 * Adds a listener which will be notified when the given property changes
	 *
	 * @since 3.1
	 */
	private void addInternal(String propertyId, IPropertyMapListener newListener) {
		if (listeners == null) {
			listeners = new HashMap<>();
		}

		List<IPropertyMapListener> listenerList = listeners.get(propertyId);

		if (listenerList == null) {
			listenerList = new ArrayList<>(1);
			listeners.put(propertyId, listenerList);
		}

		if (!listenerList.contains(newListener)) {
			listenerList.add(newListener);
		}
	}

	public void add(String[] propertyIds, IPropertyMapListener newListener) {
		for (String id : propertyIds) {
			addInternal(id, newListener);
		}
		newListener.listenerAttached();
	}

	public void remove(String propertyId, IPropertyMapListener toRemove) {
		if (listeners == null) {
			return;
		}
		List<IPropertyMapListener> listenerList = listeners.get(propertyId);

		if (listenerList != null) {
			listenerList.remove(toRemove);

			if (listenerList.isEmpty()) {
				listeners.remove(propertyId);

				if (listeners.isEmpty()) {
					listeners = null;
				}
			}
		}
	}

	public void removeAll() {
		globalListeners = null;
		listeners = null;
	}

	public void remove(IPropertyMapListener toRemove) {
		if (globalListeners != null) {
			globalListeners.remove(toRemove);
			if (globalListeners.isEmpty()) {
				globalListeners = null;
			}
		}

		if (listeners != null) {
			for (String key : listeners.keySet()) {
				remove(key, toRemove);
			}
		}
	}

	public boolean isEmpty() {
		return globalListeners == null && listeners == null;
	}
}
