/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 262269, 266754, 265561, 262287, 268688
 *     Ovidio Mallo - bug 299619
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.value;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.map.ComputedObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.core.internal.databinding.identity.IdentityMap;
import org.eclipse.core.internal.databinding.identity.IdentitySet;
import org.eclipse.core.internal.databinding.property.Util;

/**
 * @since 1.2
 */
public class SetSimpleValueObservableMap extends ComputedObservableMap
		implements IPropertyObservable {
	private SimpleValueProperty detailProperty;

	private INativePropertyListener listener;

	private Map cachedValues;
	private Set staleKeys;

	private boolean updating;

	/**
	 * @param keySet
	 * @param valueProperty
	 */
	public SetSimpleValueObservableMap(IObservableSet keySet,
			SimpleValueProperty valueProperty) {
		super(keySet, valueProperty.getValueType());
		this.detailProperty = valueProperty;
	}

	protected void firstListenerAdded() {
		if (listener == null) {
			listener = detailProperty
					.adaptListener(new ISimplePropertyListener() {
						public void handleEvent(final SimplePropertyEvent event) {
							if (!isDisposed() && !updating) {
								getRealm().exec(new Runnable() {
									public void run() {
										if (event.type == SimplePropertyEvent.CHANGE) {
											notifyIfChanged(event.getSource());
										} else if (event.type == SimplePropertyEvent.STALE) {
											boolean wasStale = !staleKeys
													.isEmpty();
											staleKeys.add(event.getSource());
											if (!wasStale)
												fireStale();
										}
									}
								});
							}
						}
					});
		}
		cachedValues = new IdentityMap();
		staleKeys = new IdentitySet();
		super.firstListenerAdded();
	}

	protected void lastListenerRemoved() {
		super.lastListenerRemoved();
		cachedValues.clear();
		cachedValues = null;
		staleKeys.clear();
		staleKeys = null;
	}

	protected void hookListener(Object addedKey) {
		if (cachedValues != null) {
			cachedValues.put(addedKey, detailProperty.getValue(addedKey));
			if (listener != null)
				listener.addTo(addedKey);
		}
	}

	protected void unhookListener(Object removedKey) {
		if (cachedValues != null) {
			if (listener != null)
				listener.removeFrom(removedKey);
			cachedValues.remove(removedKey);
			staleKeys.remove(removedKey);
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
			if (!Util.equals(oldValue, newValue) || staleKeys.contains(key)) {
				cachedValues.put(key, newValue);
				staleKeys.remove(key);
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

	public boolean isStale() {
		return super.isStale() || staleKeys != null && !staleKeys.isEmpty();
	}

	public synchronized void dispose() {
		if (cachedValues != null) {
			cachedValues.clear();
			cachedValues = null;
		}

		listener = null;
		detailProperty = null;
		cachedValues = null;
		staleKeys = null;

		super.dispose();
	}
}
