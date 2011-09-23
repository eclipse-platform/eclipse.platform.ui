/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bugs 164653, 167204
 *     Matthew Hall - bugs 118516, 208858, 208332, 247367, 146397, 249526,
 *                    349038
 *******************************************************************************/

package org.eclipse.core.databinding.observable.list;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.ChangeSupport;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;

/**
 * Subclasses should override at least get(int index) and size().
 * 
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 * 
 * @since 1.0
 * 
 */
public abstract class AbstractObservableList extends AbstractList implements
		IObservableList {
	private final class PrivateChangeSupport extends ChangeSupport {
		private PrivateChangeSupport(Realm realm) {
			super(realm);
		}

		protected void firstListenerAdded() {
			AbstractObservableList.this.firstListenerAdded();
		}

		protected void lastListenerRemoved() {
			AbstractObservableList.this.lastListenerRemoved();
		}

		protected boolean hasListeners() {
			return super.hasListeners();
		}
	}

	private final Realm realm;
	private PrivateChangeSupport changeSupport;
	private volatile boolean disposed = false;

	/**
	 * @param realm 
	 * 
	 */
	public AbstractObservableList(Realm realm) {
		Assert.isNotNull(realm, "Realm cannot be null"); //$NON-NLS-1$
		ObservableTracker.observableCreated(this);
		this.realm = realm;
		changeSupport = new PrivateChangeSupport(realm);
	}

	/**
	 * 
	 */
	public AbstractObservableList() {
		this(Realm.getDefault());
	}
	
	/**
	 * Returns whether this observable list has any registered listeners.
	 * 
	 * @return whether this observable list has any registered listeners.
	 * @since 1.2
	 */
	protected synchronized boolean hasListeners() {
		return !disposed && changeSupport.hasListeners();
	}

	public boolean isStale() {
		getterCalled();
		return false;
	}

	public synchronized void addListChangeListener(IListChangeListener listener) {
		if (!disposed) {
			changeSupport.addListener(ListChangeEvent.TYPE, listener);
		}
	}

	public synchronized void removeListChangeListener(IListChangeListener listener) {
		if (!disposed) {
			changeSupport.removeListener(ListChangeEvent.TYPE, listener);
		}
	}

	protected void fireListChange(ListDiff diff) {
		// fire general change event first
		fireChange();
		changeSupport.fireEvent(new ListChangeEvent(this, diff));
	}

	public synchronized void addChangeListener(IChangeListener listener) {
		if (!disposed) {
			changeSupport.addChangeListener(listener);
		}
	}

	public synchronized void removeChangeListener(IChangeListener listener) {
		if (!disposed) {
			changeSupport.removeChangeListener(listener);
		}
	}

	public synchronized void addStaleListener(IStaleListener listener) {
		if (!disposed) {
			changeSupport.addStaleListener(listener);
		}
	}

	public synchronized void removeStaleListener(IStaleListener listener) {
		if (!disposed) {
			changeSupport.removeStaleListener(listener);
		}
	}

	/**
	 * @since 1.2
	 */
	public synchronized void addDisposeListener(IDisposeListener listener) {
		if (!disposed) {
			changeSupport.addDisposeListener(listener);
		}
	}

	/**
	 * @since 1.2
	 */
	public synchronized void removeDisposeListener(IDisposeListener listener) {
		if (!disposed) {
			changeSupport.removeDisposeListener(listener);
		}
	}

	/**
	 * Fires change event. Must be invoked from the current realm.
	 */
	protected void fireChange() {
		checkRealm();
		changeSupport.fireEvent(new ChangeEvent(this));
	}

	/**
	 * Fires stale event. Must be invoked from the current realm.
	 */
	protected void fireStale() {
		checkRealm();
		changeSupport.fireEvent(new StaleEvent(this));
	}

	/**
	 * 
	 */
	protected void firstListenerAdded() {
	}

	/**
	 * 
	 */
	protected void lastListenerRemoved() {
	}

	/**
	 * @since 1.2
	 */
	public synchronized boolean isDisposed() {
		return disposed;
	}

	/**
	 * 
	 */
	public synchronized void dispose() {
		if (!disposed) {
			disposed = true;
			changeSupport.fireEvent(new DisposeEvent(this));
			changeSupport.dispose();
			changeSupport = null;
			lastListenerRemoved();
		}
	}

	public final int size() {
		getterCalled();
		return doGetSize();
	}

	/**
	 * @return the size
	 */
	protected abstract int doGetSize();

	/**
	 * 
	 */
	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	public boolean isEmpty() {
		getterCalled();
		return super.isEmpty();
	}

	public boolean contains(Object o) {
		getterCalled();
		return super.contains(o);
	}

	public Iterator iterator() {
		getterCalled();
		final Iterator wrappedIterator = super.iterator();
		return new Iterator() {
			public void remove() {
				wrappedIterator.remove();
			}

			public boolean hasNext() {
				return wrappedIterator.hasNext();
			}

			public Object next() {
				return wrappedIterator.next();
			}
		};
	}

	public Object[] toArray() {
		getterCalled();
		return super.toArray();
	}

	public Object[] toArray(Object a[]) {
		getterCalled();
		return super.toArray(a);
	}

	// Modification Operations

	public boolean add(Object o) {
		getterCalled();
		return super.add(o);
	}

	/**
	 * Moves the element located at <code>oldIndex</code> to
	 * <code>newIndex</code>. This method is equivalent to calling
	 * <code>add(newIndex, remove(oldIndex))</code>.
	 * <p>
	 * Subclasses should override this method to deliver list change
	 * notification for the remove and add operations in the same
	 * ListChangeEvent, as this allows {@link ListDiff#accept(ListDiffVisitor)}
	 * to recognize the operation as a move.
	 * 
	 * @param oldIndex
	 *            the element's position before the move. Must be within the
	 *            range <code>0 &lt;= oldIndex &lt; size()</code>.
	 * @param newIndex
	 *            the element's position after the move. Must be within the
	 *            range <code>0 &lt;= newIndex &lt; size()</code>.
	 * @return the element that was moved.
	 * @throws IndexOutOfBoundsException
	 *             if either argument is out of range (<code>0 &lt;= index &lt; size()</code>).
	 * @see ListDiffVisitor#handleMove(int, int, Object)
	 * @see ListDiff#accept(ListDiffVisitor)
	 * @since 1.1
	 */
	public Object move(int oldIndex, int newIndex) {
		checkRealm();
		int size = doGetSize();
		if (oldIndex < 0 || oldIndex >= size)
			throw new IndexOutOfBoundsException(
					"oldIndex: " + oldIndex + ", size:" + size); //$NON-NLS-1$ //$NON-NLS-2$
		if (newIndex < 0 || newIndex >= size)
			throw new IndexOutOfBoundsException(
					"newIndex: " + newIndex + ", size:" + size); //$NON-NLS-1$ //$NON-NLS-2$
		Object element = remove(oldIndex);
		add(newIndex, element);
		return element;
	}

	public boolean remove(Object o) {
		getterCalled();
		return super.remove(o);
	}

	// Bulk Modification Operations

	public boolean containsAll(Collection c) {
		getterCalled();
		return super.containsAll(c);
	}

	public boolean addAll(Collection c) {
		getterCalled();
		return super.addAll(c);
	}

	public boolean addAll(int index, Collection c) {
		getterCalled();
		return super.addAll(c);
	}

	public boolean removeAll(Collection c) {
		getterCalled();
		return super.removeAll(c);
	}

	public boolean retainAll(Collection c) {
		getterCalled();
		return super.retainAll(c);
	}

	// Comparison and hashing

	public boolean equals(Object o) {
		getterCalled();
		return super.equals(o);
	}

	public int hashCode() {
		getterCalled();
		return super.hashCode();
	}

	public int indexOf(Object o) {
		getterCalled();
		return super.indexOf(o);
	}

	public int lastIndexOf(Object o) {
		getterCalled();
		return super.lastIndexOf(o);
	}

	public Realm getRealm() {
		return realm;
	}
	
	/**
	 * Asserts that the realm is the current realm.
	 * 
	 * @see Realm#isCurrent()
	 * @throws AssertionFailedException
	 *             if the realm is not the current realm
	 */
	protected void checkRealm() {
		Assert.isTrue(getRealm().isCurrent(),
				"This operation must be run within the observable's realm"); //$NON-NLS-1$
	}
}
