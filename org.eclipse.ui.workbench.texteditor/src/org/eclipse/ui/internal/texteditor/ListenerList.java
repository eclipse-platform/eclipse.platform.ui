package org.eclipse.ui.internal.texteditor;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Allows multiple registration, guarantees stable notification order, removal and addition do
 * not affect a currently running notification. Thread safe.
 * 
 * @since 3.1
 */
final class ListenerList/*<L> implements Iterable<L>*/ {
	/**
	 * A notifier is dispatches an event to a number of listeners. See
	 * {@link ListenerList#notifyListeners(INotifier)}.
	 */
	public static interface INotifier/*<L>*/ {
		/**
		 * Asks the notifier to notify the passed listener. This method is called for all
		 * registered listeners when the receiver is passed to
		 * {@link ListenerList#notifyListeners(INotifier)}.
		 * 
		 * @param listener the listener to notify
		 */
		void notifyListener(Object/*L*/ listener);
	}
	
	/**
	 * Iterator over the registered listeners. The list it is based on is copied-on-write if the
	 * underlying listener list is modified.
	 */
	private final class ListenerIterator/*<L>*/ implements Iterator/*<L>*/ {
		/** The list of listeners. */
		List/*<L>*/ fList;
		/** The iterator this instance is based on, <code>null</code> if not yet created. */
		Iterator/*<L>*/ fIterator;
		/** The index of the next element to be returned. */
		int fIndex;
		
		public ListenerIterator(List/*<L>*/ listeners) {
			fList= listeners;
			fIndex= 0;
		}
		
		/*
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			synchronized (ListenerList.this) {
				boolean hasNext= getIterator().hasNext();
				if (!hasNext) {
					fIterator= null;
					fIndex= 0;
					fList= Collections.EMPTY_LIST;
				}
				return hasNext;
			}
		}
		
		/*
		 * @see java.util.Iterator#next()
		 */
		public Object/*L*/ next() {
			synchronized (ListenerList.this) {
				fIndex++;
				return getIterator().next();
			}
		}
		
		/*
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		/**
		 * Detach this iterator from the original list of listeners as it is being modified.
		 */
		void detach() {
			// only called synchronized from ListenerList
			fIterator= null;
			if (fIndex < fList.size()) {
				fList= Arrays.asList(fList.toArray());
			} else {
				fList= Collections.EMPTY_LIST;
				fIndex= 0;
			}
		}
		
		Iterator/*<L>*/ getIterator() {
			if (fIterator == null)
				fIterator= fList.listIterator(fIndex);
			return fIterator;
		}
	}
	
	private final List fListeners= new LinkedList();
	private final List fIterators= new LinkedList();
	
	/**
	 * Adds a listener to this list.
	 * 
	 * @param listener the new listener
	 */
	public synchronized void addListener(Object/*L*/ listener) {
		if (listener == null)
			throw new NullPointerException("listener"); //$NON-NLS-1$
		detachIterators();
		fListeners.add(listener);
	}
	
	/**
	 * Removes a listener from this list.
	 * 
	 * @param listener the listener to be removed
	 */
	public synchronized void removeListener(Object/*L*/ listener) {
		if (listener == null)
			throw new NullPointerException("listener"); //$NON-NLS-1$
		detachIterators();
		fListeners.remove(listener);
	}
	
	private void detachIterators() {
		for (Iterator it= fIterators.iterator(); it.hasNext();) {
			Reference ref= (Reference) it.next();
			it.remove();
			ListenerIterator iterator= (ListenerIterator) ref.get();
			if (iterator != null)
				iterator.detach();
		}
	}

	/**
	 * Returns an iterator over the listeners registered at the moment that this method is
	 * called. Adding or removing listeners does not impact the returned iterator.
	 * 
	 * @return an iterator over the registered listeners
	 */
	public synchronized Iterator/*<L>*/ iterator() {
		ListenerIterator iterator= new ListenerIterator/*<L>*/(fListeners);
		fIterators.add(new WeakReference(iterator));
		return iterator;
	}
	
	/**
	 * Asks the passed notifier to notify all registered listeners.
	 * 
	 * @param notifier the notifier that will be passed the registered listeners
	 */
	public void notifyListeners(INotifier notifier) {
		Iterator it= iterator();
		while(it.hasNext())
			notifier.notifyListener(it.next());

		synchronized (this) {
			for (Iterator it2= fIterators.iterator(); it2.hasNext();) {
				Reference ref= (Reference) it2.next();
				ListenerIterator iterator= (ListenerIterator) ref.get();
				if (iterator == it) {
					it2.remove();
					break;
				}
			}
		}
	}
}