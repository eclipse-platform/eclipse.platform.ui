/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding.observable.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;

/**
 * 
 * Abstract implementation of {@link IObservableList}, based on an underlying regular list. 
 * 
 * @since 1.0
 * 
 */
public abstract class ObservableList extends AbstractObservable implements
		IObservableList {

	protected List wrappedList;

	private boolean stale = false;

	private Object listChangeListeners;

	private Object elementType;

	protected ObservableList(List wrappedList, Object elementType) {
		this(Realm.getDefault(), wrappedList, elementType);
	}

	protected ObservableList(Realm realm, List wrappedList, Object elementType) {
		super(realm);
		this.wrappedList = wrappedList;
		this.elementType = elementType;
	}
	
	public void addListChangeListener(IListChangeListener listener) {
		if (listChangeListeners == null) {
			boolean hadListeners = hasListeners();
			listChangeListeners = listener;
			if (!hadListeners) {
				firstListenerAdded();
			}
			return;
		}

		Collection listenerList;
		if (listChangeListeners instanceof Collection) {
			listenerList = (Collection) listChangeListeners;
		} else {
			IListChangeListener l = (IListChangeListener) listChangeListeners;
			
			listenerList = new ArrayList();
			listenerList.add(l);
			listChangeListeners = listenerList;
		}

		listenerList.add(listener);
	}

	public void removeListChangeListener(IListChangeListener listener) {

		if (listChangeListeners == listener) {
			listChangeListeners = null;
			if (!hasListeners()) {
				lastListenerRemoved();
			}
			return;
		}

		if (listChangeListeners instanceof Collection) {
			Collection listenerList = (Collection) listChangeListeners;
			listenerList.remove(listener);
			if (listenerList.isEmpty()) {
				listChangeListeners = null;
				if (!hasListeners()) {
					lastListenerRemoved();
				}
			}
		}
	}

	protected boolean hasListeners() {
		return super.hasListeners() || listChangeListeners!=null;
	}

	protected void fireListChange(ListDiff diff) {
		// fire general change event first
		super.fireChange();

		if (listChangeListeners == null) {
			return;
		}
		
		if (listChangeListeners instanceof IListChangeListener) {
			((IListChangeListener) listChangeListeners).handleListChange(this, diff);
			return;
		}
		
		Collection changeListenerCollection = (Collection) listChangeListeners;
		
		IListChangeListener[] listeners = (IListChangeListener[]) (changeListenerCollection)
		.toArray(new IListChangeListener[changeListenerCollection.size()]);
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].handleListChange(this, diff);
		}
	}
	
	public boolean contains(Object o) {
		getterCalled();
		return wrappedList.contains(o);
	}

	public boolean containsAll(Collection c) {
		getterCalled();
		return wrappedList.containsAll(c);
	}

	public boolean equals(Object o) {
		getterCalled();
		return wrappedList.equals(o);
	}

	public int hashCode() {
		getterCalled();
		return wrappedList.hashCode();
	}

	public boolean isEmpty() {
		getterCalled();
		return wrappedList.isEmpty();
	}

	public Iterator iterator() {
		final Iterator wrappedIterator = wrappedList.iterator();
		return new Iterator() {

			public void remove() {
				throw new UnsupportedOperationException();
			}

			public boolean hasNext() {
				ObservableTracker.getterCalled(ObservableList.this);
				return wrappedIterator.hasNext();
			}

			public Object next() {
				ObservableTracker.getterCalled(ObservableList.this);
				return wrappedIterator.next();
			}
		};
	}

	public int size() {
		getterCalled();
		return wrappedList.size();
	}

	public Object[] toArray() {
		getterCalled();
		return wrappedList.toArray();
	}

	public Object[] toArray(Object[] a) {
		getterCalled();
		return wrappedList.toArray(a);
	}

	public String toString() {
		getterCalled();
		return wrappedList.toString();
	}
	
	/**
	 * @TrackedGetter
	 */
    public Object get(int index) {
    	getterCalled();
    	return wrappedList.get(index);
    }

	/**
	 * @TrackedGetter
	 */
    public int indexOf(Object o) {
    	getterCalled();
    	return wrappedList.indexOf(o);
    }

	/**
	 * @TrackedGetter
	 */
    public int lastIndexOf(Object o) {
    	getterCalled();
    	return wrappedList.lastIndexOf(o);
    }

    // List Iterators

	/**
	 * @TrackedGetter
	 */
    public ListIterator listIterator() {
    	return listIterator(0);
    }

	/**
	 * @TrackedGetter
	 */
    public ListIterator listIterator(int index) {
		final ListIterator wrappedIterator = wrappedList.listIterator(index);
		return new ListIterator() {

			public int nextIndex() {
				getterCalled();
				return wrappedIterator.nextIndex();
			}

			public int previousIndex() {
				getterCalled();
				return wrappedIterator.previousIndex();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			public boolean hasNext() {
				getterCalled();
				return wrappedIterator.hasNext();
			}

			public boolean hasPrevious() {
				getterCalled();
				return wrappedIterator.hasPrevious();
			}

			public Object next() {
				getterCalled();
				return wrappedIterator.next();
			}

			public Object previous() {
				getterCalled();
				return wrappedIterator.previous();
			}

			public void add(Object o) {
				throw new UnsupportedOperationException();
			}

			public void set(Object o) {
				throw new UnsupportedOperationException();
			}
		};
    }


    public List subList(int fromIndex, int toIndex) {
    	getterCalled();
    	return wrappedList.subList(fromIndex, toIndex);
    }

	protected void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

    public Object set(int index, Object element) {
    	throw new UnsupportedOperationException();
    }

    public Object remove(int index) {
    	throw new UnsupportedOperationException();
    }

	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	public void add(int index, Object element) {
		throw new UnsupportedOperationException();
	}
	
	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException();
	}

    public boolean addAll(int index, Collection c) {
    	throw new UnsupportedOperationException();
    }

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return Returns the stale state.
	 */
	public boolean isStale() {
		return stale;
	}

	/**
	 * @param stale
	 *            The stale state to list. This will fire a stale event if the
	 *            given boolean is true and this observable list was not already
	 *            stale.
	 */
	public void setStale(boolean stale) {
		boolean wasStale = this.stale;
		this.stale = stale;
		if (!wasStale && stale) {
			fireStale();
		}
	}

	protected void fireChange() {
		throw new RuntimeException("fireChange should not be called, use fireListChange() instead"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.provisional.databinding.observable.AbstractObservable#dispose()
	 */
	public void dispose() {
		listChangeListeners = null;
		super.dispose();
	}
	
	public Object getElementType() {
		return elementType;
	}

	protected void updateWrappedList(List newList) {
		// TODO this is a naive list diff algorithm, we need a
		// smarter one
		List oldList = wrappedList;
		ListDiff listDiff = Diffs.computeListDiff(oldList, newList);
		wrappedList = newList;
		fireListChange(listDiff);
	}

}
