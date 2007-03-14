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

package org.eclipse.core.databinding;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 3.3
 * 
 */
class ValueBinding extends Binding {

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

	/**
	 * @param targetObservableValue
	 * @param modelObservableValue
	 * @param targetToModel
	 * @param modelToTarget
	 */
	public ValueBinding(IObservableValue targetObservableValue,
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
		if ((modelToTarget.getUpdatePolicy() & (UpdateValueStrategy.POLICY_CONVERT | UpdateValueStrategy.POLICY_UPDATE)) != 0) {
			model.addValueChangeListener(modelChangeListener);
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
						try {
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
																	IStatus setterStatus = updateValueStrategy.doSet(destination, convertedValue);
																	if (!setterStatus.isOK()) {
																		statusHolder[0] = setterStatus;
																	}
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
						} catch (Exception ex) {
							//This check is necessary as in 3.2.2 Status doesn't accept a null message (bug 177264).
							String message = (ex.getMessage() != null) ? ex.getMessage() : ""; //$NON-NLS-1$
							
							statusHolder[0] = new Status(IStatus.ERROR,
									Policy.JFACE_DATABINDING, IStatus.ERROR, message, ex);
						} finally {
							if (!destinationRealmReached) {
								setValidationStatus(statusHolder[0]);
							}

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
