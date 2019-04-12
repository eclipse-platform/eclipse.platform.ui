/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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

/**
 * @since 3.1
 */
public abstract class PropertyMapAdapter implements IDynamicPropertyMap {

	private PropertyListenerList listeners;
	private int ignoreCount = 0;
	private ArrayList queuedEvents = new ArrayList();

	@Override
	public final void addListener(IPropertyMapListener listener) {
		if (listeners == null) {
			listeners = new PropertyListenerList();
			attachListener();
		}
		listeners.add(listener);
	}

	@Override
	public final void removeListener(IPropertyMapListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
			if (listeners.isEmpty()) {
				detachListener();
				listeners = null;
			}
		}
	}

	@Override
	public final boolean isCommonProperty(String propertyId) {
		return true;
	}

	/**
	 * Detaches all listeners which have been registered with other objects
	 *
	 * @since 3.1
	 */
	public void dispose() {
		if (listeners != null) {
			detachListener();
			listeners = null;
		}
	}

	protected final void firePropertyChange(String prefId) {
		if (ignoreCount > 0) {
			queuedEvents.add(prefId);
			return;
		}

		if (listeners != null) {
			listeners.firePropertyChange(prefId);
		}
	}

	@Override
	public final void addListener(String[] eventsOfInterest, IPropertyMapListener listener) {
		if (listeners == null) {
			listeners = new PropertyListenerList();
			attachListener();
		}
		listeners.add(eventsOfInterest, listener);
	}

	protected final void firePropertyChange(String[] prefIds) {
		if (ignoreCount > 0) {
			for (String prefId : prefIds) {
				queuedEvents.add(prefId);
			}
			return;
		}

		if (listeners != null) {
			listeners.firePropertyChange(prefIds);
		}
	}

	public final void startTransaction() {
		ignoreCount++;
	}

	public final void endTransaction() {
		ignoreCount--;
		if (ignoreCount == 0 && !queuedEvents.isEmpty()) {
			if (listeners != null) {
				listeners.firePropertyChange((String[]) queuedEvents.toArray(new String[queuedEvents.size()]));
			}
			queuedEvents.clear();
		}
	}

	@Override
	public boolean equals(Object toCompare) {
		return toCompare instanceof IPropertyMap && PropertyUtil.isEqual(this, (IPropertyMap) toCompare);
	}

	protected abstract void attachListener();

	protected abstract void detachListener();
}
