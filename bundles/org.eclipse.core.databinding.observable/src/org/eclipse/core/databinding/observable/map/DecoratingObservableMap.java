/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 237718)
 *     Matthew Hall - but 246626, 226289
 ******************************************************************************/

package org.eclipse.core.databinding.observable.map;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.DecoratingObservable;

/**
 * An observable map which decorates another observable map.
 *
 * @since 1.2
 */
public class DecoratingObservableMap extends DecoratingObservable implements
		IObservableMap {
	private IObservableMap decorated;

	private IMapChangeListener mapChangeListener;

	/**
	 * Constructs a DecoratingObservableMap which decorates the given
	 * observable.
	 *
	 * @param decorated
	 *            the observable map being decorated
	 * @param disposeDecoratedOnDispose
	 */
	public DecoratingObservableMap(IObservableMap decorated,
			boolean disposeDecoratedOnDispose) {
		super(decorated, disposeDecoratedOnDispose);
		this.decorated = decorated;
	}

	@Override
	public synchronized void addMapChangeListener(IMapChangeListener listener) {
		addListener(MapChangeEvent.TYPE, listener);
	}

	@Override
	public synchronized void removeMapChangeListener(IMapChangeListener listener) {
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

	protected void fireMapChange(MapDiff diff) {
		// fire general change event first
		super.fireChange();
		fireEvent(new MapChangeEvent(this, diff));
	}

	@Override
	protected void fireChange() {
		throw new RuntimeException(
				"fireChange should not be called, use fireListChange() instead"); //$NON-NLS-1$
	}

	@Override
	protected void firstListenerAdded() {
		if (mapChangeListener == null) {
			mapChangeListener = new IMapChangeListener() {
				@Override
				public void handleMapChange(MapChangeEvent event) {
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
	protected void handleMapChange(final MapChangeEvent event) {
		fireMapChange(event.diff);
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

	private class BackedCollection implements Collection {
		private Collection collection;

		BackedCollection(Collection set) {
			this.collection = set;
		}

		@Override
		public boolean add(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection arg0) {
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
		public boolean containsAll(Collection c) {
			getterCalled();
			return collection.containsAll(c);
		}

		@Override
		public boolean isEmpty() {
			getterCalled();
			return collection.isEmpty();
		}

		@Override
		public Iterator iterator() {
			final Iterator iterator = collection.iterator();
			return new Iterator() {
				@Override
				public boolean hasNext() {
					getterCalled();
					return iterator.hasNext();
				}

				@Override
				public Object next() {
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
		public boolean removeAll(Collection c) {
			getterCalled();
			return collection.removeAll(c);
		}

		@Override
		public boolean retainAll(Collection c) {
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
		public Object[] toArray(Object[] array) {
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

	private class BackedSet extends BackedCollection implements Set {
		BackedSet(Set set) {
			super(set);
		}
	}

	Set entrySet = null;

	@Override
	public Set entrySet() {
		getterCalled();
		if (entrySet == null) {
			entrySet = new BackedSet(decorated.entrySet());
		}
		return entrySet;
	}

	@Override
	public Object get(Object key) {
		getterCalled();
		return decorated.get(key);
	}

	@Override
	public boolean isEmpty() {
		getterCalled();
		return decorated.isEmpty();
	}

	Set keySet = null;

	@Override
	public Set keySet() {
		getterCalled();
		if (keySet == null) {
			keySet = new BackedSet(decorated.keySet());
		}
		return keySet;
	}

	@Override
	public Object put(Object key, Object value) {
		checkRealm();
		return decorated.put(key, value);
	}

	@Override
	public void putAll(Map m) {
		checkRealm();
		decorated.putAll(m);
	}

	@Override
	public Object remove(Object key) {
		checkRealm();
		return decorated.remove(key);
	}

	@Override
	public int size() {
		getterCalled();
		return decorated.size();
	}

	Collection values;

	@Override
	public Collection values() {
		getterCalled();
		if (values == null) {
			values = new BackedCollection(decorated.values());
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
