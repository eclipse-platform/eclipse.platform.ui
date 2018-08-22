/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 220700, 271148, 278550
 *******************************************************************************/

package org.eclipse.core.databinding;

import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.internal.databinding.BindingStatus;
import org.eclipse.core.internal.databinding.Util;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 1.0
 *
 */
class ValueBinding<M, T> extends Binding {
	private final UpdateValueStrategy<? super T, ? extends M> targetToModel;
	private final UpdateValueStrategy<? super M, ? extends T> modelToTarget;
	private WritableValue<IStatus> validationStatusObservable;
	private IObservableValue<T> target;
	private IObservableValue<M> model;

	private boolean updatingTarget;
	private boolean updatingModel;
	private IValueChangeListener<T> targetChangeListener = new IValueChangeListener<T>() {
		@Override
		public void handleValueChange(ValueChangeEvent<? extends T> event) {
			if (!updatingTarget && !Util.equals(event.diff.getOldValue(), event.diff.getNewValue())) {
				doUpdate(target, model, targetToModel, false, false);
			}
		}
	};

	private IValueChangeListener<M> modelChangeListener = new IValueChangeListener<M>() {
		@Override
		public void handleValueChange(ValueChangeEvent<? extends M> event) {
			if (!updatingModel && !Util.equals(event.diff.getOldValue(), event.diff.getNewValue())) {
				doUpdate(model, target, modelToTarget, false, false);
			}
		}
	};

	/**
	 * @param targetObservableValue
	 * @param modelObservableValue
	 * @param targetToModel
	 * @param modelToTarget
	 */
	public ValueBinding(IObservableValue<T> targetObservableValue, IObservableValue<M> modelObservableValue,
			UpdateValueStrategy<? super T, ? extends M> targetToModel,
			UpdateValueStrategy<? super M, ? extends T> modelToTarget) {
		super(targetObservableValue, modelObservableValue);
		this.target = targetObservableValue;
		this.model = modelObservableValue;
		this.targetToModel = targetToModel;
		this.modelToTarget = modelToTarget;
	}

	@Override
	protected void preInit() {
		ObservableTracker.setIgnore(true);
		try {
			validationStatusObservable = new WritableValue<>(
					context.getValidationRealm(), Status.OK_STATUS, IStatus.class);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	protected void postInit() {
		if (modelToTarget.getUpdatePolicy() == UpdateValueStrategy.POLICY_UPDATE) {
			model.addValueChangeListener(modelChangeListener);
			updateModelToTarget();
		} else if (modelToTarget.getUpdatePolicy() == UpdateValueStrategy.POLICY_CONVERT) {
			model.addValueChangeListener(modelChangeListener);
			validateModelToTarget();
		} else {
			modelChangeListener = null;
		}

		if (targetToModel.getUpdatePolicy() == UpdateValueStrategy.POLICY_UPDATE) {
			target.addValueChangeListener(targetChangeListener);
			if (modelToTarget.getUpdatePolicy() == UpdateValueStrategy.POLICY_NEVER) {
				updateTargetToModel();
			} else {
				validateTargetToModel();
			}
		} else if (targetToModel.getUpdatePolicy() == UpdateValueStrategy.POLICY_CONVERT) {
			target.addValueChangeListener(targetChangeListener);
			validateTargetToModel();
		} else {
			targetChangeListener = null;
		}
	}

	@Override
	public IObservableValue<IStatus> getValidationStatus() {
		return validationStatusObservable;
	}

	@Override
	public void updateTargetToModel() {
		doUpdate(target, model, targetToModel, true, false);
	}

	@Override
	public void updateModelToTarget() {
		doUpdate(model, target, modelToTarget, true, false);
	}

	/**
	 * Incorporates the provided <code>newStats</code> into the
	 * <code>multieStatus</code>.
	 *
	 * @param multiStatus
	 * @param newStatus
	 * @return <code>true</code> if the update should proceed
	 */
	/* package */boolean mergeStatus(MultiStatus multiStatus, IStatus newStatus) {
		if (!newStatus.isOK()) {
			multiStatus.add(newStatus);
			return multiStatus.getSeverity() < IStatus.ERROR;
		}
		return true;
	}

	/*
	 * This method may be moved to UpdateValueStrategy in the future if clients
	 * need more control over how the source value is copied to the destination
	 * observable.
	 */
	private <S, D1, D2 extends D1> void doUpdate(final IObservableValue<S> source,
			final IObservableValue<D1> destination,
			final UpdateValueStrategy<? super S, D2> updateValueStrategy,
			final boolean explicit, final boolean validateOnly) {

		final int policy = updateValueStrategy.getUpdatePolicy();
		if (policy == UpdateValueStrategy.POLICY_NEVER)
			return;
		if (policy == UpdateValueStrategy.POLICY_ON_REQUEST && !explicit)
			return;

		source.getRealm().exec(() -> {
			boolean destinationRealmReached = false;
			final MultiStatus multiStatus = BindingStatus.ok();
			try {
				// Get value
				S value = source.getValue();

				// Validate after get
				IStatus status = updateValueStrategy.validateAfterGet(value);
				if (!mergeStatus(multiStatus, status))
					return;

				// Convert value
				final D2 convertedValue = updateValueStrategy.convert(value);

				// Validate after convert
				status = updateValueStrategy.validateAfterConvert(convertedValue);
				if (!mergeStatus(multiStatus, status))
					return;
				if (policy == UpdateValueStrategy.POLICY_CONVERT && !explicit)
					return;

				// Validate before set
				status = updateValueStrategy.validateBeforeSet(convertedValue);
				if (!mergeStatus(multiStatus, status))
					return;
				if (validateOnly)
					return;

				// Set value
				destinationRealmReached = true;
				destination.getRealm().exec(() -> {
					if (destination == target) {
						updatingTarget = true;
					} else {
						updatingModel = true;
					}
					try {
						IStatus setterStatus = updateValueStrategy.doSet(destination, convertedValue);

						mergeStatus(multiStatus, setterStatus);
					} finally {
						if (destination == target) {
							updatingTarget = false;
						} else {
							updatingModel = false;
						}
						setValidationStatus(multiStatus);
					}
				});
			} catch (Exception ex) {
				// This check is necessary as in 3.2.2 Status
				// doesn't accept a null message (bug 177264).
				String message = (ex.getMessage() != null) ? ex.getMessage() : ""; //$NON-NLS-1$

				mergeStatus(multiStatus,
						new Status(IStatus.ERROR, Policy.JFACE_DATABINDING, IStatus.ERROR, message, ex));
			} finally {
				if (!destinationRealmReached) {
					setValidationStatus(multiStatus);
				}

			}
		});
	}

	@Override
	public void validateModelToTarget() {
		doUpdate(model, target, modelToTarget, true, true);
	}

	@Override
	public void validateTargetToModel() {
		doUpdate(target, model, targetToModel, true, true);
	}

	private void setValidationStatus(final IStatus status) {
		validationStatusObservable.getRealm().exec(() -> validationStatusObservable.setValue(status));
	}

	@Override
	public void dispose() {
		if (targetChangeListener != null) {
			target.removeValueChangeListener(targetChangeListener);
			targetChangeListener = null;
		}
		if (modelChangeListener != null) {
			model.removeValueChangeListener(modelChangeListener);
			modelChangeListener = null;
		}
		target = null;
		model = null;
		super.dispose();
	}

}
