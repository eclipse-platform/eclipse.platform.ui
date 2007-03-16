/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import java.util.Set;

import org.eclipse.core.databinding.observable.set.AbstractObservableSet;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;

/**
 * Wraps an observable set. This object acts like an exact copy of the original
 * set, and tracks all the changes in the original. The only difference is that
 * disposing the wrapper will not dispose the original. You can use this whenever
 * you need to return an IObservableSet from a method that expects the caller
 * to dispose the set, but you have an IObservableSet that you don't want disposed.
 */
public final class ProxyObservableSet extends AbstractObservableSet {

	private IObservableSet toDelegateTo;
	private ISetChangeListener listener = new ISetChangeListener() {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.ISetChangeListener#handleSetChange(org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet, org.eclipse.jface.internal.databinding.provisional.observable.set.SetDiff)
		 */
		public void handleSetChange(SetChangeEvent event) {
			fireSetChange(event.diff);
		}
	};
	
	/**
	 * Constructs a DelegatingObservableSet that tracks the state of the given set.
	 * 
	 * @param toDelegate
	 */
	public ProxyObservableSet(IObservableSet toDelegate) {
		super(toDelegate.getRealm());
		this.toDelegateTo = toDelegate;
		toDelegate.addSetChangeListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.AbstractObservableSet#getWrappedSet()
	 */
	protected Set getWrappedSet() {
		return toDelegateTo;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#getElementType()
	 */
	public Object getElementType() {
		return toDelegateTo.getElementType();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.AbstractObservableSet#dispose()
	 */
	public void dispose() {
		toDelegateTo.removeSetChangeListener(listener);
		super.dispose();
	}
}
