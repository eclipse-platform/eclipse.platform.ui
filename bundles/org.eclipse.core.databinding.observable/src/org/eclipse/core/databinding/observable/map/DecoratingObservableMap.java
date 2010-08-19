/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
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

	public synchronized void addMapChangeListener(IMapChangeListener listener) {
		addListener(MapChangeEvent.TYPE, listener);
	}

	public synchronized void removeMapChangeListener(IMapChangeListener listener) {
		removeListener(MapChangeEvent.TYPE, listener);
	}

	public Object getKeyType() {
		return decorated.getKeyType();
	}

	public Object getValueType() {
		return decorated.getValueType();
	}

	protected void fireMapChange(MapDiff diff) {
		// fire general change event first
		super.fireChange();
		fireEvent(new MapChangeEvent(this, diff));
	}

	protected void fireChange() {
		throw new RuntimeException(
				"fireChange should not be called, use fireListChange() instead"); //$NON-NLS-1$
	}

	protected void firstListenerAdded() {
		if (mapChangeListener == null) {
			mapChangeListener = new IMapChangeListener() {
				public void handleMapChange(MapChangeEvent event) {
					DecoratingObservableMap.this.handleMapChange(event);
				}
			};
		}
		decorated.addMapChangeListener(mapChangeListener);
		super.firstListenerAdded();
	}

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

	public void clear() {
		checkRealm();
		decorated.clear();
	}

	public boolean containsKey(Object key) {
		getterCalled();
		return decorated.containsKey(key);
	}

	public boolean containsValue(Object value) {
		getterCalled();
		return decorated.containsValue(value);
	}

	private class BackedCollection implements Collection {
		private Collection collection;

		BackedCollection(Collection set) {
			this.collection = set;
		}

		public boolean add(Object o) {
			throw new UnsupportedOperationException();
		}

		public boolean addAll(Collection arg0) {
			throw new UnsupportedOperationException();
		}

		public void clear() {
			checkRealm();
			collection.clear();
		}

		public boolean contains(Object o) {
			getterCalled();
			return collection.contains(o);
		}

		public boolean containsAll(Collection c) {
			getterCalled();
			return collection.containsAll(c);
		}

		public boolean isEmpty() {
			getterCalled();
			return collection.isEmpty();
		}

		public Iterator iterator() {
			final Iterator iterator = collection.iterator();
			return new Iterator() {
				public boolean hasNext() {
					getterCalled();
					return iterator.hasNext();
				}

				public Object next() {
					getterCalled();
					return iterator.next();
				}

				public void remove() {
					checkRealm();
					iterator.remove();
				}
			};
		}

		public boolean remove(Object o) {
			getterCalled();
			return collection.remove(o);
		}

		public boolean removeAll(Collection c) {
			getterCalled();
			return collection.removeAll(c);
		}

		public boolean retainAll(Collection c) {
			getterCalled();
			return collection.retainAll(c);
		}

		public int size() {
			getterCalled();
			return collection.size();
		}

		public Object[] toArray() {
			getterCalled();
			return collection.toArray();
		}

		public Object[] toArray(Object[] array) {
			getterCalled();
			return collection.toArray(array);
		}

		public boolean equals(Object obj) {
			getterCalled();
			return collection.equals(obj);
		}

		public int hashCode() {
			getterCalled();
			return collection.hashCode();
		}

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

	public Set entrySet() {
		getterCalled();
		if (entrySet == null) {
			entrySet = new BackedSet(decorated.entrySet());
		}
		return entrySet;
	}

	public Object get(Object key) {
		getterCalled();
		return decorated.get(key);
	}

	public boolean isEmpty() {
		getterCalled();
		return decorated.isEmpty();
	}

	Set keySet = null;

	public Set keySet() {
		getterCalled();
		if (keySet == null) {
			keySet = new BackedSet(decorated.keySet());
		}
		return keySet;
	}

	public Object put(Object key, Object value) {
		checkRealm();
		return decorated.put(key, value);
	}

	public void putAll(Map m) {
		checkRealm();
		decorated.putAll(m);
	}

	public Object remove(Object key) {
		checkRealm();
		return decorated.remove(key);
	}

	public int size() {
		getterCalled();
		return decorated.size();
	}

	Collection values;

	public Collection values() {
		getterCalled();
		if (values == null) {
			values = new BackedCollection(decorated.values());
		}
		return values;
	}

	public boolean equals(Object obj) {
		getterCalled();
		if (this == obj) {
			return true;
		}
		return decorated.equals(obj);
	}

	public int hashCode() {
		getterCalled();
		return decorated.hashCode();
	}

	public String toString() {
		getterCalled();
		return decorated.toString();
	}

	public synchronized void dispose() {
		if (decorated != null && mapChangeListener != null) {
			decorated.removeMapChangeListener(mapChangeListener);
		}
		decorated = null;
		mapChangeListener = null;
		super.dispose();
	}
}
