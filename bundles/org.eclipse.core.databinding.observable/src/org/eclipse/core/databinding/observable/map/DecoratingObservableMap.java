/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 237718)
 *     Matthew Hall - but 246626, 226289
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *     Stefan Xenos <sxenos@gmail.com> - Bug 474065
 ******************************************************************************/

package org.eclipse.core.databinding.observable.map;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.DecoratingObservable;
import org.eclipse.core.databinding.observable.Diffs;

/**
 * An observable map which decorates another observable map.
 *
 * @param <K>
 *            type of the keys to the map
 * @param <V>
 *            type of the values in the map
 *
 * @since 1.2
 */
public class DecoratingObservableMap<K, V> extends DecoratingObservable
		implements IObservableMap<K, V> {
	private IObservableMap<K, V> decorated;

	private IMapChangeListener<K, V> mapChangeListener;

	/**
	 * Constructs a DecoratingObservableMap which decorates the given
	 * observable.
	 *
	 * @param decorated
	 *            the observable map being decorated
	 * @param disposeDecoratedOnDispose
	 */
	public DecoratingObservableMap(IObservableMap<K, V> decorated,
			boolean disposeDecoratedOnDispose) {
		super(decorated, disposeDecoratedOnDispose);
		this.decorated = decorated;
	}

	@Override
	public synchronized void addMapChangeListener(IMapChangeListener<? super K, ? super V> listener) {
		addListener(MapChangeEvent.TYPE, listener);
	}

	@Override
	public synchronized void removeMapChangeListener(IMapChangeListener<? super K, ? super V> listener) {
		removeListener(MapChangeEvent.TYPE, listener);
	}

	@Override
	public Object getKeyType() {
		return decorated.getKeyType();
	}

	@Override
	public Object getValueType() {
		return decorated.getValueType();
	}

	protected void fireMapChange(MapDiff<K, V> diff) {
		// fire general change event first
		super.fireChange();
		fireEvent(new MapChangeEvent<>(this, diff));
	}

	@Override
	protected void fireChange() {
		throw new RuntimeException(
				"fireChange should not be called, use fireListChange() instead"); //$NON-NLS-1$
	}

	@Override
	protected void firstListenerAdded() {
		if (mapChangeListener == null) {
			mapChangeListener = new IMapChangeListener<K, V>() {
				@Override
				public void handleMapChange(MapChangeEvent<? extends K, ? extends V> event) {
					DecoratingObservableMap.this.handleMapChange(event);
				}
			};
		}
		decorated.addMapChangeListener(mapChangeListener);
		super.firstListenerAdded();
	}

	@Override
	protected void lastListenerRemoved() {
		super.lastListenerRemoved();
		if (mapChangeListener != null) {
			decorated.removeMapChangeListener(mapChangeListener);
			mapChangeListener = null;
		}
	}

	/**
	 * Called whenever a MapChangeEvent is received from the decorated
	 * observable. By default, this method fires the map change event again,
	 * with the decorating observable as the event source. Subclasses may
	 * override to provide different behavior.
	 *
	 * @param event
	 *            the change event received from the decorated observable
	 */
	protected void handleMapChange(final MapChangeEvent<? extends K, ? extends V> event) {
		fireMapChange(Diffs.unmodifiableDiff(event.diff));
	}

	@Override
	public void clear() {
		checkRealm();
		decorated.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		getterCalled();
		return decorated.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		getterCalled();
		return decorated.containsValue(value);
	}

	private class BackedCollection<E> implements Collection<E> {
		private Collection<E> collection;

		BackedCollection(Collection<E> set) {
			this.collection = set;
		}

		@Override
		public boolean add(E o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends E> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			checkRealm();
			collection.clear();
		}

		@Override
		public boolean contains(Object o) {
			getterCalled();
			return collection.contains(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			getterCalled();
			return collection.containsAll(c);
		}

		@Override
		public boolean isEmpty() {
			getterCalled();
			return collection.isEmpty();
		}

		@Override
		public Iterator<E> iterator() {
			final Iterator<E> iterator = collection.iterator();
			return new Iterator<E>() {
				@Override
				public boolean hasNext() {
					getterCalled();
					return iterator.hasNext();
				}

				@Override
				public E next() {
					getterCalled();
					return iterator.next();
				}

				@Override
				public void remove() {
					checkRealm();
					iterator.remove();
				}
			};
		}

		@Override
		public boolean remove(Object o) {
			getterCalled();
			return collection.remove(o);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			getterCalled();
			return collection.removeAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			getterCalled();
			return collection.retainAll(c);
		}

		@Override
		public int size() {
			getterCalled();
			return collection.size();
		}

		@Override
		public Object[] toArray() {
			getterCalled();
			return collection.toArray();
		}

		@Override
		public <T> T[] toArray(T[] array) {
			getterCalled();
			return collection.toArray(array);
		}

		@Override
		public boolean equals(Object obj) {
			getterCalled();
			return collection.equals(obj);
		}

		@Override
		public int hashCode() {
			getterCalled();
			return collection.hashCode();
		}

		@Override
		public String toString() {
			getterCalled();
			return collection.toString();
		}
	}

	private class BackedSet<E> extends BackedCollection<E> implements Set<E> {
		BackedSet(Set<E> set) {
			super(set);
		}
	}

	Set<Entry<K, V>> entrySet = null;

	@Override
	public Set<Entry<K, V>> entrySet() {
		getterCalled();
		if (entrySet == null) {
			entrySet = new BackedSet<>(decorated.entrySet());
		}
		return entrySet;
	}

	@Override
	public V get(Object key) {
		getterCalled();
		return decorated.get(key);
	}

	@Override
	public boolean isEmpty() {
		getterCalled();
		return decorated.isEmpty();
	}

	Set<K> keySet = null;

	@Override
	public Set<K> keySet() {
		getterCalled();
		if (keySet == null) {
			keySet = new BackedSet<>(decorated.keySet());
		}
		return keySet;
	}

	@Override
	public V put(K key, V value) {
		checkRealm();
		return decorated.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		checkRealm();
		decorated.putAll(m);
	}

	@Override
	public V remove(Object key) {
		checkRealm();
		return decorated.remove(key);
	}

	@Override
	public int size() {
		getterCalled();
		return decorated.size();
	}

	Collection<V> values;

	@Override
	public Collection<V> values() {
		getterCalled();
		if (values == null) {
			values = new BackedCollection<>(decorated.values());
		}
		return values;
	}

	@Override
	public boolean equals(Object obj) {
		getterCalled();
		if (this == obj) {
			return true;
		}
		return decorated.equals(obj);
	}

	@Override
	public int hashCode() {
		getterCalled();
		return decorated.hashCode();
	}

	@Override
	public String toString() {
		getterCalled();
		return decorated.toString();
	}

	@Override
	public synchronized void dispose() {
		if (decorated != null && mapChangeListener != null) {
			decorated.removeMapChangeListener(mapChangeListener);
		}
		decorated = null;
		mapChangeListener = null;
		super.dispose();
	}
}
