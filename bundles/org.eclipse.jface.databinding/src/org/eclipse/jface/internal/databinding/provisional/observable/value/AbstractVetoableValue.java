/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.provisional.observable.value;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.internal.databinding.provisional.observable.Diffs;

/**
 * @since 1.0
 * 
 */
public abstract class AbstractVetoableValue extends AbstractObservableValue
		implements IVetoableValue {

	public void setValue(Object value) {
		Object currentValue = doGetValue();
		ValueDiff diff = Diffs.createValueDiff(currentValue, value);
		boolean okToProceed = fireValueChanging(diff);
		if (!okToProceed) {
			throw new ChangeVetoException("Change not permitted"); //$NON-NLS-1$
		}
		doSetValue(value);
		fireValueChange(diff);
	}

	private Collection valueChangingListeners = null;

	public void addValueChangingListener(IValueChangingListener listener) {
		if (valueChangingListeners == null) {
			boolean hadListeners = hasListeners();
			valueChangingListeners = new ArrayList();
			valueChangingListeners.add(listener);
			if (!hadListeners) {
				firstListenerAdded();
			}
		} else {
			valueChangingListeners.add(listener);
		}
	}

	public void removeValueChangingListener(IValueChangingListener listener) {
		if (valueChangingListeners == null) {
			return;
		}
		valueChangingListeners.remove(listener);
		if (valueChangingListeners.isEmpty()) {
			valueChangingListeners = null;
		}
		if (!hasListeners()) {
			lastListenerRemoved();
		}
	}

	/**
	 * Notifies listeners about a pending change, and returns true if no
	 * listener vetoed the change.
	 * 
	 * @param diff
	 * @return false if the change was vetoed, true otherwise
	 */
	protected boolean fireValueChanging(ValueDiff diff) {
		if (valueChangingListeners != null) {
			IValueChangingListener[] listeners = (IValueChangingListener[]) valueChangingListeners
					.toArray(new IValueChangingListener[valueChangingListeners
							.size()]);
			for (int i = 0; i < listeners.length; i++) {
				boolean okToProceed = listeners[i].handleValueChanging(this,
						diff);
				if (!okToProceed) {
					return false;
				}
			}
		}
		return true;
	}

	protected abstract void doSetValue(Object value);

	protected boolean hasListeners() {
		return super.hasListeners();
	}

	public void dispose() {
		super.dispose();
	}

}
