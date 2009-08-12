/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 262269, 281727, 278550
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.value;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.property.value.DelegatingValueProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.internal.databinding.identity.IdentityMap;
import org.eclipse.core.internal.databinding.identity.IdentityObservableSet;

/**
 * @since 3.3
 * 
 */
abstract class DelegatingCache {
	private Realm realm;
	private DelegatingValueProperty detailProperty;
	private IObservableSet elements;
	private Map delegateCaches;

	private class DelegateCache implements IMapChangeListener {
		private final IValueProperty delegate;
		private final IObservableSet masterElements;
		private final IObservableMap masterElementValues;
		private final Map cachedValues;

		DelegateCache(IValueProperty delegate) {
			this.delegate = delegate;
			ObservableTracker.setIgnore(true);
			try {
				this.masterElements = new IdentityObservableSet(realm, elements
						.getElementType());
				this.masterElementValues = delegate
						.observeDetail(masterElements);
			} finally {
				ObservableTracker.setIgnore(false);
			}
			this.cachedValues = new IdentityMap();

			masterElementValues.addMapChangeListener(this);
		}

		void add(Object masterElement) {
			boolean wasEmpty = masterElements.isEmpty();

			masterElements.add(masterElement);
			cachedValues.put(masterElement, masterElementValues
					.get(masterElement));

			if (wasEmpty)
				delegateCaches.put(delegate, this);
		}

		void remove(Object masterElement) {
			cachedValues.remove(masterElement);
			masterElements.remove(masterElement);
			if (cachedValues.isEmpty())
				dispose();
		}

		Object get(Object masterElement) {
			return cachedValues.get(masterElement);
		}

		Object put(Object masterElement, Object detailValue) {
			Object oldValue = masterElementValues.put(masterElement,
					detailValue);
			notifyIfChanged(masterElement);
			return oldValue;
		}

		boolean containsValue(Object detailValue) {
			return cachedValues.containsValue(detailValue);
		}

		public void handleMapChange(MapChangeEvent event) {
			Set changedKeys = event.diff.getChangedKeys();
			for (Iterator it = changedKeys.iterator(); it.hasNext();)
				notifyIfChanged(it.next());
		}

		private void notifyIfChanged(Object masterElement) {
			Object oldValue = cachedValues.get(masterElement);
			Object newValue = masterElementValues.get(masterElement);
			if (oldValue != newValue) {
				cachedValues.put(masterElement, newValue);
				handleValueChange(masterElement, oldValue, newValue);
			}
		}

		void handleValueChange(Object masterElement, Object oldValue,
				Object newValue) {
			DelegatingCache.this.handleValueChange(masterElement, oldValue,
					newValue);
		}

		void dispose() {
			delegateCaches.remove(delegate);
			masterElementValues.dispose();
			masterElements.dispose();
			cachedValues.clear();
		}
	}

	DelegatingCache(Realm realm, DelegatingValueProperty detailProperty) {
		this.realm = realm;
		this.detailProperty = detailProperty;

		ObservableTracker.setIgnore(true);
		try {
			this.elements = new IdentityObservableSet(realm, null);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		this.delegateCaches = new IdentityMap();

		elements.addSetChangeListener(new ISetChangeListener() {
			public void handleSetChange(SetChangeEvent event) {
				for (Iterator it = event.diff.getRemovals().iterator(); it
						.hasNext();) {
					Object element = it.next();
					getCache(element).remove(element);

				}
				for (Iterator it = event.diff.getAdditions().iterator(); it
						.hasNext();) {
					Object element = it.next();
					getCache(element).add(element);
				}
			}
		});
	}

	private DelegateCache getCache(Object masterElement) {
		IValueProperty delegate = detailProperty.getDelegate(masterElement);
		if (delegateCaches.containsKey(delegate)) {
			return (DelegateCache) delegateCaches.get(delegate);
		}
		return new DelegateCache(delegate);
	}

	Object get(Object element) {
		return getCache(element).get(element);
	}

	Object put(Object element, Object value) {
		return getCache(element).put(element, value);
	}

	boolean containsValue(Object value) {
		for (Iterator it = delegateCaches.values().iterator(); it.hasNext();) {
			DelegateCache cache = (DelegateCache) it.next();
			if (cache.containsValue(value))
				return true;
		}
		return false;
	}

	void addAll(Collection elements) {
		this.elements.addAll(elements);
	}

	void retainAll(Collection elements) {
		this.elements.retainAll(elements);
	}

	abstract void handleValueChange(Object masterElement, Object oldValue,
			Object newValue);

	void dispose() {
		if (elements != null) {
			elements.clear(); // clears caches
			elements.dispose();
			elements = null;
		}

		if (delegateCaches != null) {
			for (Iterator it = delegateCaches.values().iterator(); it.hasNext();) {
				DelegateCache cache = (DelegateCache) it.next();
				cache.dispose();
			}
			delegateCaches.clear();
			delegateCaches = null;
		}
	}
}
