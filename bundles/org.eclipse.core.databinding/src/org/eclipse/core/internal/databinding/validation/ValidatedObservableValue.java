/*******************************************************************************
 * Copyright (c) 2008, 2017 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 218269)
 ******************************************************************************/

package org.eclipse.core.internal.databinding.validation;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.IVetoableValue;
import org.eclipse.core.databinding.observable.value.ValueChangingEvent;
import org.eclipse.core.internal.databinding.Util;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

/**
 * An {@link IObservableValue} wrapper that stays in sync with the target
 * observable as long as a given validation status is valid.
 * <ul>
 * <li>While status is valid, ValidatedObservableValue stays in sync with its
 * target.
 * <li>When status becomes invalid, ValidatedObservableValue will retain the
 * last valid value of its target.
 * <li>While status is invalid, changes in the target observable cause
 * ValidatedObservableValue to fire a stale event, to indicate that changes are
 * pending.
 * <li>When status becomes valid, pending value changes are performed (if any)
 * and synchronization resumes.
 * </ul>
 * <p>
 * Note:
 * <ul>
 * <li>By default, a status is valid if its {@link IStatus#getSeverity()
 * severity} is {@link IStatus#OK OK}, {@link IStatus#INFO INFO}, or
 * {@link IStatus#WARNING WARNING}
 * <li>Calls to {@link #setValue(Object)} on the validated observable changes
 * the value regardless of the validation status.
 * <li>This class will not forward {@link ValueChangingEvent} events from a
 * wrapped {@link IVetoableValue}.
 * </ul>
 *
 * @param <T>
 *            The type of the value.
 *
 * @since 1.2
 */
public class ValidatedObservableValue<T> extends AbstractObservableValue<T> {
	private IObservableValue<T> target;
	private IObservableValue<IStatus> validationStatus;

	private T cachedValue;
	private boolean stale;
	private boolean updatingTarget = false;

	private IValueChangeListener<T> targetChangeListener = event -> {
		if (updatingTarget)
			return;
		IStatus status = validationStatus.getValue();
		if (isValid(status))
			internalSetValue(event.diff.getNewValue(), false);
		else
			makeStale();
	};

	private static boolean isValid(IStatus status) {
		return status.isOK() || status.matches(IStatus.INFO | IStatus.WARNING);
	}

	private IStaleListener targetStaleListener = staleEvent -> fireStale();

	private IValueChangeListener<IStatus> validationStatusChangeListener = event -> {
		IStatus oldStatus = event.diff.getOldValue();
		IStatus newStatus = event.diff.getNewValue();
		if (stale && !isValid(oldStatus) && isValid(newStatus)) {
			internalSetValue(target.getValue(), false);
		}
	};

	/**
	 * Constructs an observable value
	 *
	 * @param target
	 *            the observable value to be wrapped
	 * @param validationStatus
	 *            an observable value of type {@link IStatus}.class which
	 *            contains the current validation status
	 */
	public ValidatedObservableValue(IObservableValue<T> target, IObservableValue<IStatus> validationStatus) {
		super(target.getRealm());
		Assert.isNotNull(validationStatus,
				"Validation status observable cannot be null"); //$NON-NLS-1$
		Assert.isTrue(target.getRealm().equals(validationStatus.getRealm()),
				"Target and validation status observables must be on the same realm"); //$NON-NLS-1$
		this.target = target;
		this.validationStatus = validationStatus;
		this.cachedValue = target.getValue();

		target.addValueChangeListener(targetChangeListener);
		target.addStaleListener(targetStaleListener);
		validationStatus.addValueChangeListener(validationStatusChangeListener);
	}

	private void makeStale() {
		if (!stale) {
			stale = true;
			fireStale();
		}
	}

	@Override
	public boolean isStale() {
		ObservableTracker.getterCalled(this);
		return stale || target.isStale();
	}

	@Override
	protected T doGetValue() {
		return cachedValue;
	}

	private void internalSetValue(T value, boolean updateTarget) {
		T oldValue = cachedValue;
		cachedValue = value;
		if (updateTarget) {
			updatingTarget = true;
			try {
				target.setValue(value);
				cachedValue = target.getValue();
			} finally {
				updatingTarget = false;
			}
		}
		stale = false;
		if (!Util.equals(oldValue, cachedValue))
			fireValueChange(Diffs.createValueDiff(oldValue, cachedValue));
	}

	@Override
	protected void doSetValue(T value) {
		internalSetValue(value, true);
	}

	@Override
	public Object getValueType() {
		return target.getValueType();
	}

	@Override
	public synchronized void dispose() {
		target.removeValueChangeListener(targetChangeListener);
		target.removeStaleListener(targetStaleListener);
		validationStatus
				.removeValueChangeListener(validationStatusChangeListener);
		super.dispose();
	}
}
