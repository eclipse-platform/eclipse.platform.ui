/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *******************************************************************************/
package org.eclipse.core.databinding.observable.value;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;

/**
 * @since 1.0
 * 
 */
public abstract class AbstractVetoableValue extends AbstractObservableValue
		implements IVetoableValue {

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

	final protected void doSetValue(Object value) {
		Object currentValue = doGetValue();
		ValueDiff diff = Diffs.createValueDiff(currentValue, value);
		boolean okToProceed = fireValueChanging(diff);
		if (!okToProceed) {
			throw new ChangeVetoException("Change not permitted"); //$NON-NLS-1$
		}
		doSetApprovedValue(value);
		fireValueChange(diff);
	}

	/**
	 * Sets the value. Invoked after performing veto checks.
	 * 
	 * @param value
	 */
	protected abstract void doSetApprovedValue(Object value);

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

	public void dispose() {
		super.dispose();
	}

}
