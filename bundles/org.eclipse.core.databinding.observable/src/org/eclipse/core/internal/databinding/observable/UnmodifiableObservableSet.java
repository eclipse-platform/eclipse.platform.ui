/*******************************************************************************
 * Copyright (c) 2007, 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 208332)
 *     Brad Reynolds - initial API and implementation
 *         (through UnmodifiableObservableList.java)
 *     Matthew Hall - bug 237718
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.set.DecoratingObservableSet;
import org.eclipse.core.databinding.observable.set.IObservableSet;

/**
 * ObservableList implementation that prevents modification by consumers. Events
 * in the originating wrapped list are propagated and thrown from this instance
 * when appropriate. All mutators throw an UnsupportedOperationException.
 * 
 * @since 1.1
 */
public class UnmodifiableObservableSet extends DecoratingObservableSet {
	private Set unmodifiableSet;

	/**
	 * @param decorated
	 */
	public UnmodifiableObservableSet(IObservableSet decorated) {
		super(decorated, false);

		this.unmodifiableSet = Collections.unmodifiableSet(decorated);
	}

	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public Iterator iterator() {
		getterCalled();
		return unmodifiableSet.iterator();
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

	public synchronized void dispose() {
		unmodifiableSet = null;
		super.dispose();
	}
}
