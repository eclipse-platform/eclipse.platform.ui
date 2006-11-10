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

package org.eclipse.core.databinding.observable.value;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;

/**
 * @since 1.0
 * 
 */
abstract public class AbstractObservableValue extends AbstractObservable
		implements IObservableValue {

	/**
	 * @param realm
	 */
	public AbstractObservableValue() {
		this(Realm.getDefault());
	}

	/**
	 * @param realm
	 */
	public AbstractObservableValue(Realm realm) {
		super(realm);
	}
	
	private Collection valueChangeListeners = null;

	public void addValueChangeListener(IValueChangeListener listener) {
		if (valueChangeListeners == null) {
			boolean hadListeners = hasListeners();
			valueChangeListeners = new ArrayList();
			valueChangeListeners.add(listener);
			if (!hadListeners) {
				firstListenerAdded();
			}
		} else {
			valueChangeListeners.add(listener);
		}
	}

	public void removeValueChangeListener(IValueChangeListener listener) {
		if (valueChangeListeners == null) {
			return;
		}
		valueChangeListeners.remove(listener);
		if (valueChangeListeners.isEmpty()) {
			valueChangeListeners = null;
		}
		if (!hasListeners()) {
			lastListenerRemoved();
		}
	}

	public void setValue(Object value) {
		throw new UnsupportedOperationException();
	}

	protected void fireValueChange(ValueDiff diff) {
		// fire general change event first
		super.fireChange();
		if (valueChangeListeners != null) {
			IValueChangeListener[] listeners = (IValueChangeListener[]) valueChangeListeners
					.toArray(new IValueChangeListener[valueChangeListeners
							.size()]);
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].handleValueChange(this, diff);
			}
		}
	}

	public final Object getValue() {
		ObservableTracker.getterCalled(this);
		return doGetValue();
	}

	abstract protected Object doGetValue();

	public boolean isStale() {
		return false;
	}

	protected boolean hasListeners() {
		return super.hasListeners() || valueChangeListeners != null;
	}

	protected void fireChange() {
		throw new RuntimeException(
				"fireChange should not be called, use fireValueChange() instead"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.provisional.databinding.observable.AbstractObservable#dispose()
	 */
	public void dispose() {
		valueChangeListeners = null;
		super.dispose();
	}
}
