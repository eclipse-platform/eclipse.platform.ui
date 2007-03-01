/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.databinding;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.IValueChangingListener;
import org.eclipse.core.databinding.observable.value.IVetoableValue;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.ValueChangingEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 3.3
 * 
 */
public class ValueBinding2 extends Binding {

	private final UpdateValueStrategy targetToModel;
	private final UpdateValueStrategy modelToTarget;
	private WritableValue validationStatusObservable;
	private IObservableValue target;
	private IObservableValue model;

	private boolean updatingTarget;
	private boolean updatingModel;
	private IValueChangeListener targetChangeListener = new IValueChangeListener() {
		public void handleValueChange(ValueChangeEvent event) {
			if (!updatingTarget) {
				doUpdate(target, model, targetToModel, false, false);
			}
		}
	};
	private IValueChangeListener modelChangeListener = new IValueChangeListener() {
		public void handleValueChange(ValueChangeEvent event) {
			if (!updatingModel) {
				doUpdate(model, target, modelToTarget, false, false);
			}
		}
	};
	
	private IValueChangingListener targetChangingListener = new IValueChangingListener(){
		public void handleValueChanging(ValueChangingEvent event) {
			if (!updatingTarget) {
				IStatus status = targetToModel.validateBeforeChange(event.diff);
				if (!status.isOK()) {
					setValidationStatus(status);
					event.veto = true;
				}
			}
		}};

		private IValueChangingListener modelChangingListener = new IValueChangingListener(){
			public void handleValueChanging(ValueChangingEvent event) {
				if (!updatingModel) {
					IStatus status = modelToTarget.validateBeforeChange(event.diff);
					if (!status.isOK()) {
						setValidationStatus(status);
						event.veto = true;
					}
				}
			}};
			
	/**
	 * @param targetObservableValue
	 * @param modelObservableValue
	 * @param targetToModel
	 * @param modelToTarget
	 */
	public ValueBinding2(IObservableValue targetObservableValue,
			IObservableValue modelObservableValue,
			UpdateValueStrategy targetToModel, UpdateValueStrategy modelToTarget) {
		super(targetObservableValue, modelObservableValue);
		this.target = targetObservableValue;
		this.model = modelObservableValue;
		this.targetToModel = targetToModel;
		this.modelToTarget = modelToTarget;
		if ((targetToModel.getUpdatePolicy() & (UpdateValueStrategy.POLICY_CONVERT | UpdateValueStrategy.POLICY_UPDATE)) != 0) {
			target.addValueChangeListener(targetChangeListener);
		}
		if ((targetToModel.getUpdatePolicy() & UpdateValueStrategy.VALIDATE_BEFORE_CHANGE) != 0 && targetObservableValue instanceof IVetoableValue) {
			((IVetoableValue)targetObservableValue).addValueChangingListener(targetChangingListener);
		}
		if ((modelToTarget.getUpdatePolicy() & (UpdateValueStrategy.POLICY_CONVERT | UpdateValueStrategy.POLICY_UPDATE)) != 0) {
			model.addValueChangeListener(modelChangeListener);
		}
		if ((modelToTarget.getUpdatePolicy() & UpdateValueStrategy.VALIDATE_BEFORE_CHANGE) != 0 && modelObservableValue instanceof IVetoableValue) {
			((IVetoableValue)modelObservableValue).addValueChangingListener(modelChangingListener);
		}
	}

	protected void preInit() {
		validationStatusObservable = new WritableValue(context
				.getValidationRealm(), Status.OK_STATUS, IStatus.class);
	}

	protected void postInit() {
		if (modelToTarget.getUpdatePolicy() == UpdateValueStrategy.POLICY_UPDATE) {
			updateModelToTarget();
		}
		if (targetToModel.getUpdatePolicy() != UpdateValueStrategy.POLICY_NEVER) {
			validateTargetToModel();
		}
	}

	public IObservableValue getValidationStatus() {
		return validationStatusObservable;
	}

	public void updateTargetToModel() {
		doUpdate(target, model, targetToModel, true, false);
	}

	public void updateModelToTarget() {
		doUpdate(model, target, modelToTarget, true, false);
	}

	/*
	 * This method may be moved to UpdateValueStrategy in the future if clients
	 * need more control over how the source value is copied to the destination
	 * observable.
	 */
	private void doUpdate(final IObservableValue source,
			final IObservableValue destination,
			final UpdateValueStrategy updateValueStrategy,
			final boolean explicit, final boolean validateOnly) {
		final int policy = updateValueStrategy.getUpdatePolicy();
		final IStatus[] statusHolder = { Status.OK_STATUS };
		if (policy != UpdateValueStrategy.POLICY_NEVER) {
			if (policy != UpdateValueStrategy.POLICY_ON_REQUEST || explicit) {
				source.getRealm().exec(new Runnable() {
					public void run() {
						boolean destinationRealmReached = false;
						Object value = source.getValue();
						IStatus status = updateValueStrategy
								.validateAfterGet(value);
						if (!status.isOK()) {
							statusHolder[0] = status;
						} else {
							final Object convertedValue = updateValueStrategy
									.convert(value);
							status = updateValueStrategy
									.validateAfterConvert(convertedValue);
							if (!status.isOK()) {
								statusHolder[0] = status;
							} else {
								if (policy == UpdateValueStrategy.POLICY_CONVERT
										&& !explicit) {
								} else {
									status = updateValueStrategy
											.validateBeforeSet(convertedValue);
									if (!status.isOK()) {
										statusHolder[0] = status;
									} else {
										if (!validateOnly) {
											destinationRealmReached = true;
											destination.getRealm().exec(
													new Runnable() {
														public void run() {
															if (destination == target) {
																updatingTarget = true;
															} else {
																updatingModel = true;
															}
															try {
																destination
																		.setValue(convertedValue);
															} catch (Exception ex) {
																statusHolder[0] = ValidationStatus
																		.error(
																				BindingMessages
																						.getString("ValueBinding_ErrorWhileSettingValue"), //$NON-NLS-1$
																				ex);
															} finally {
																if (destination == target) {
																	updatingTarget = false;
																} else {
																	updatingModel = false;
																}
																setValidationStatus(statusHolder[0]);
															}
														}
													});
										}
									}
								}
							}
						}
						if (!destinationRealmReached) {
							setValidationStatus(statusHolder[0]);
						}
					}
				});
			}
		}
	}

	public void validateModelToTarget() {
		doUpdate(model, target, modelToTarget, true, true);
	}

	public void validateTargetToModel() {
		doUpdate(target, model, targetToModel, true, true);
	}

	private void setValidationStatus(final IStatus status) {
		validationStatusObservable.getRealm().exec(new Runnable() {
			public void run() {
				validationStatusObservable.setValue(status);
			}
		});
	}

}
