/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *     Matthew Hall - bug 263691
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *******************************************************************************/
package org.eclipse.core.databinding.observable.value;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.internal.databinding.observable.Util;

/**
 *
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 *
 * @param <T>
 *            the type of value being observed
 * @since 1.0
 *
 */
public abstract class AbstractVetoableValue<T> extends
		AbstractObservableValue<T> implements IVetoableValue<T> {

	/**
	 * Creates a new vetoable value.
	 */
	public AbstractVetoableValue() {
		this(Realm.getDefault());
	}

	/**
	 * @param realm
	 */
	public AbstractVetoableValue(Realm realm) {
		super(realm);
	}

	@Override
	final protected void doSetValue(T value) {
		T currentValue = doGetValue();
		ValueDiff<T> diff = Diffs.createValueDiff(currentValue, value);
		boolean okToProceed = fireValueChanging(diff);
		if (!okToProceed) {
			throw new ChangeVetoException("Change not permitted"); //$NON-NLS-1$
		}
		doSetApprovedValue(value);

		if (!Util.equals(diff.getOldValue(), diff.getNewValue())) {
			fireValueChange(diff);
		}
	}

	/**
	 * Sets the value. Invoked after performing veto checks. Should not fire
	 * change events.
	 *
	 * @param value
	 */
	protected abstract void doSetApprovedValue(T value);

	@Override
	public synchronized void addValueChangingListener(
			IValueChangingListener<T> listener) {
		addListener(ValueChangingEvent.TYPE, listener);
	}

	@Override
	public synchronized void removeValueChangingListener(
			IValueChangingListener<T> listener) {
		removeListener(ValueChangingEvent.TYPE, listener);
	}

	/**
	 * Notifies listeners about a pending change, and returns true if no
	 * listener vetoed the change.
	 *
	 * @param diff
	 * @return false if the change was vetoed, true otherwise
	 */
	protected boolean fireValueChanging(ValueDiff<T> diff) {
		checkRealm();

		ValueChangingEvent<T> event = new ValueChangingEvent<>(this, diff);
		fireEvent(event);
		return !event.veto;
	}
}
