/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.databinding.observable.AbstractObservable;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyListElementProvider;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyDeleteEvent;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertEvent;

/**
 * @since 3.3
 *
 */
public class LazyDataRequestor extends AbstractObservable implements ILazyDataRequestor {

	List elementProviders = new ArrayList();
	List insertDeleteProviders = new ArrayList();
	List listChangeListeners = new ArrayList();
	List changeListeners = new ArrayList();
	List staleListeners = new ArrayList();
	List windowData = new ArrayList();
	int size;

	int windowSize;
	int topRow = 0;
	
	public LazyDataRequestor (int windowSize) {
		this.windowSize = windowSize;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#add(int, java.lang.Object)
	 */
	public void add(int position, Object element) {
		size++;
		if (position < windowSize) {
			windowData.add(position, element);
			windowData.remove(windowData.size()-1);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#remove(int)
	 */
	public void remove(int position) {
		size--;
		if (position < windowSize) {
			windowData.remove(position);
			Object refreshed = fireElementProviders(windowData.size());
			windowData.add(refreshed);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#addElementProvider(org.eclipse.jface.internal.databinding.provisional.observable.ILazyListElementProvider)
	 */
	public void addElementProvider(ILazyListElementProvider p) {
		elementProviders.add(p);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#addInsertDeleteProvider(org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider)
	 */
	public void addInsertDeleteProvider(LazyInsertDeleteProvider p) {
		insertDeleteProviders.add(p);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#removeElementProvider(org.eclipse.jface.internal.databinding.provisional.observable.ILazyListElementProvider)
	 */
	public void removeElementProvider(ILazyListElementProvider p) {
		elementProviders.remove(p);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#removeInsertDeleteProvider(org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider)
	 */
	public void removeInsertDeleteProvider(LazyInsertDeleteProvider p) {
		insertDeleteProviders.remove(p);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#setSize(int)
	 */
	public void setSize(int size) {
		this.size = size;
		refresh();
	}

	private void refresh() {
		windowData = new ArrayList();
		for (int i = topRow; i <= windowSize - 1; ++i) {
			windowData.add(fireElementProviders(i));
		}
	}

	private Object fireElementProviders(int index) {
		for (Iterator epIter = elementProviders.iterator(); epIter.hasNext();) {
			ILazyListElementProvider p = (ILazyListElementProvider) epIter.next();
			Object result = p.get(index);
			if (result != null) {
				return result;
			}
		}
		throw new IndexOutOfBoundsException("Request for a nonexistent element");
	}
	
	private NewObject fireInsert(Object initializationData) {
		for (Iterator iter = insertDeleteProviders.iterator(); iter.hasNext();) {
			LazyInsertDeleteProvider p = (LazyInsertDeleteProvider) iter.next();
			NewObject result = p.insertElementAt(new LazyInsertEvent(0, initializationData));
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
	private boolean fireDelete(int position) {
		for (Iterator iter = insertDeleteProviders.iterator(); iter.hasNext();) {
			LazyInsertDeleteProvider p = (LazyInsertDeleteProvider) iter.next();
			boolean result = p.canDeleteElementAt(new LazyDeleteEvent(position));
			if (result) {
				p.deleteElementAt(new LazyDeleteEvent(position));
				return true;
			}
		}
		return false;
	}
	
	public void requestInsert(Object initializationData) {
		NewObject result = fireInsert(initializationData);
		if (result.position < windowSize) {
			refresh();
		}
	}
	
	public void requestDelete(int position) {
		if (fireDelete(position) && position < windowSize) {
			refresh();
		}
	}
	
	/**
	 * @return Returns the windowData.
	 */
	public List getWindowData() {
		return windowData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#isStale()
	 */
	public boolean isStale() {
		return false;
	}

}
