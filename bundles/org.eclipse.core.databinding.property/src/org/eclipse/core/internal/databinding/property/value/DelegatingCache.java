/*******************************************************************************
 * Copyright (c) 2009, 2017 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 262269, 281727, 278550
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.value;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.value.DelegatingValueProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.internal.databinding.identity.IdentityMap;
import org.eclipse.core.internal.databinding.identity.IdentityObservableSet;

/**
 * @since 3.3
 *
 */
abstract class DelegatingCache<S, K extends S, V> {
	private Realm realm;
	private DelegatingValueProperty<S, V> detailProperty;
	private IObservableSet<K> elements;
	private Map<IValueProperty<S, V>, DelegateCache> delegateCaches;

	private class DelegateCache implements IMapChangeListener<K, V> {
		private final IValueProperty<S, V> delegate;
		private final IObservableSet<K> masterElements;
		private final IObservableMap<K, V> masterElementValues;
		private final Map<K, V> cachedValues;

		DelegateCache(IValueProperty<S, V> delegate) {
			this.delegate = delegate;
			ObservableTracker.setIgnore(true);
			try {
				this.masterElements = new IdentityObservableSet<>(realm, elements.getElementType());
				this.masterElementValues = delegate.observeDetail(masterElements);
			} finally {
				ObservableTracker.setIgnore(false);
			}
			this.cachedValues = new IdentityMap<>();

			masterElementValues.addMapChangeListener(this);
		}

		void add(K masterElement) {
			boolean wasEmpty = masterElements.isEmpty();

			masterElements.add(masterElement);
			cachedValues.put(masterElement, masterElementValues.get(masterElement));

			if (wasEmpty)
				delegateCaches.put(delegate, this);
		}

		void remove(Object masterElement) {
			cachedValues.remove(masterElement);
			masterElements.remove(masterElement);
			if (cachedValues.isEmpty())
				dispose();
		}

		V get(Object masterElement) {
			return cachedValues.get(masterElement);
		}

		V put(K masterElement, V detailValue) {
			V oldValue = masterElementValues.put(masterElement, detailValue);
			notifyIfChanged(masterElement);
			return oldValue;
		}

		boolean containsValue(Object detailValue) {
			return cachedValues.containsValue(detailValue);
		}

		@Override
		public void handleMapChange(MapChangeEvent<? extends K, ? extends V> event) {
			Set<? extends K> changedKeys = event.diff.getChangedKeys();
			for (K next : changedKeys) {
				notifyIfChanged(next);
			}
		}

		private void notifyIfChanged(K masterElement) {
			V oldValue = cachedValues.get(masterElement);
			V newValue = masterElementValues.get(masterElement);
			if (oldValue != newValue) {
				cachedValues.put(masterElement, newValue);
				handleValueChange(masterElement, oldValue, newValue);
			}
		}

		void handleValueChange(K masterElement, V oldValue, V newValue) {
			DelegatingCache.this.handleValueChange(masterElement, oldValue, newValue);
		}

		void dispose() {
			delegateCaches.remove(delegate);
			masterElementValues.dispose();
			masterElements.dispose();
			cachedValues.clear();
		}
	}

	DelegatingCache(Realm realm, DelegatingValueProperty<S, V> detailProperty) {
		this.realm = realm;
		this.detailProperty = detailProperty;

		ObservableTracker.setIgnore(true);
		try {
			this.elements = new IdentityObservableSet<>(realm, null);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		this.delegateCaches = new IdentityMap<>();

		elements.addSetChangeListener(event -> {
			for (K element1 : event.diff.getRemovals()) {
				getCache(element1).remove(element1);

			}
			for (K element2 : event.diff.getAdditions()) {
				getCache(element2).add(element2);
			}
		});
	}

	private DelegateCache getCache(Object masterElement) {
		// NOTE: This is unsafe. This method can be invoked with something other
		// than an element of type S, in which case getDelegate(...) will most
		// likely throw a ClassCastException. This should really be redesigned
		// such that getDelegete will never be invoked for an element which
		// isn't actually part of the cache.
		@SuppressWarnings("unchecked")
		IValueProperty<S, V> delegate = detailProperty.getDelegate((S) masterElement);
		if (delegateCaches.containsKey(delegate)) {
			return delegateCaches.get(delegate);
		}
		return new DelegateCache(delegate);
	}

	V get(Object element) {
		return getCache(element).get(element);
	}

	V put(K element, V value) {
		return getCache(element).put(element, value);
	}

	boolean containsValue(Object value) {
		for (DelegateCache cache : delegateCaches.values()) {
			if (cache.containsValue(value))
				return true;
		}
		return false;
	}

	void addAll(Collection<? extends K> elements) {
		this.elements.addAll(elements);
	}

	void retainAll(Collection<?> elements) {
		this.elements.retainAll(elements);
	}

	abstract void handleValueChange(K masterElement, V oldValue, V newValue);

	void dispose() {
		if (elements != null) {
			elements.clear(); // clears caches
			elements.dispose();
			elements = null;
		}

		if (delegateCaches != null) {
			for (DelegatingCache<S, K, V>.DelegateCache cache : delegateCaches.values()) {
				cache.dispose();
			}
			delegateCaches.clear();
			delegateCaches = null;
		}
	}
}
