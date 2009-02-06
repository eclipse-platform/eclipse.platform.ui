/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 263691)
 ******************************************************************************/

package org.eclipse.core.databinding.observable.value;

import org.eclipse.core.databinding.observable.Diffs;

/**
 * An {@link IVetoableValue} decorator for an observable value.
 * 
 * @since 1.2
 */
public class DecoratingVetoableValue extends DecoratingObservableValue
		implements IVetoableValue {
	/**
	 * @param decorated
	 * @param disposeDecoratedOnDispose
	 */
	public DecoratingVetoableValue(IObservableValue decorated,
			boolean disposeDecoratedOnDispose) {
		super(decorated, disposeDecoratedOnDispose);
	}

	public void setValue(Object value) {
		checkRealm();
		Object currentValue = getValue();
		ValueDiff diff = Diffs.createValueDiff(currentValue, value);
		boolean okToProceed = fireValueChanging(diff);
		if (!okToProceed) {
			throw new ChangeVetoException("Change not permitted"); //$NON-NLS-1$
		}
		super.setValue(value);
	}

	public synchronized void addValueChangingListener(
			IValueChangingListener listener) {
		addListener(ValueChangingEvent.TYPE, listener);
	}

	public synchronized void removeValueChangingListener(
			IValueChangingListener listener) {
		removeListener(ValueChangingEvent.TYPE, listener);
	}

	/**
	 * Notifies listeners about a pending change, and returns true if no
	 * listener vetoed the change.
	 * 
	 * @param diff
	 * @return false if the change was vetoed, true otherwise
	 */
	protected boolean fireValueChanging(ValueDiff diff) {
		checkRealm();

		ValueChangingEvent event = new ValueChangingEvent(this, diff);
		fireEvent(event);
		return !event.veto;
	}
}
