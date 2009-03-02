/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 262269
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.value;

import java.util.Map;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.map.ComputedObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.core.internal.databinding.IdentityMap;
import org.eclipse.core.internal.databinding.Util;

/**
 * @since 1.2
 */
public class SetSimpleValueObservableMap extends ComputedObservableMap
		implements IPropertyObservable {
	private SimpleValueProperty detailProperty;

	private INativePropertyListener listener;

	private Map cachedValues;

	private boolean updating;

	/**
	 * @param keySet
	 * @param valueProperty
	 */
	public SetSimpleValueObservableMap(IObservableSet keySet,
			SimpleValueProperty valueProperty) {
		super(keySet);
		this.detailProperty = valueProperty;
	}

	protected void firstListenerAdded() {
		cachedValues = new IdentityMap();
		if (listener == null) {
			listener = detailProperty
					.adaptListener(new ISimplePropertyListener() {
						public void handlePropertyChange(
								final SimplePropertyEvent event) {
							if (!isDisposed() && !updating) {
								getRealm().exec(new Runnable() {
									public void run() {
										notifyIfChanged(event.getSource());
									}
								});
							}
						}
					});
		}
		super.firstListenerAdded();
	}

	protected void lastListenerRemoved() {
		super.lastListenerRemoved();
		cachedValues.clear();
		cachedValues = null;
	}

	protected void hookListener(Object addedKey) {
		if (cachedValues != null) {
			cachedValues.put(addedKey, detailProperty.getValue(addedKey));
			detailProperty.addListener(addedKey, listener);
		}
	}

	protected void unhookListener(Object removedKey) {
		if (cachedValues != null) {
			detailProperty.removeListener(removedKey, listener);
			cachedValues.remove(removedKey);
		}
	}

	protected Object doGet(Object key) {
		return detailProperty.getValue(key);
	}

	protected Object doPut(Object key, Object value) {
		Object oldValue = detailProperty.getValue(key);

		updating = true;
		try {
			detailProperty.setValue(key, value);
		} finally {
			updating = false;
		}

		notifyIfChanged(key);

		return oldValue;
	}

	private void notifyIfChanged(Object key) {
		if (cachedValues != null) {
			Object oldValue = cachedValues.get(key);
			Object newValue = detailProperty.getValue(key);
			if (!Util.equals(oldValue, newValue)) {
				cachedValues.put(key, newValue);
				fireMapChange(Diffs.createMapDiffSingleChange(key, oldValue,
						newValue));
			}
		}
	}

	public Object getObserved() {
		return keySet();
	}

	public IProperty getProperty() {
		return detailProperty;
	}

	public synchronized void dispose() {
		if (cachedValues != null) {
			cachedValues.clear();
			cachedValues = null;
		}
		listener = null;
		detailProperty = null;

		super.dispose();
	}
}
