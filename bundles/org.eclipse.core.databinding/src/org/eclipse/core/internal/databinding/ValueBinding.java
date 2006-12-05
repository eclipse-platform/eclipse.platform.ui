/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 152543
 *******************************************************************************/
package org.eclipse.core.internal.databinding;

import org.eclipse.core.databinding.BindSpec;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.BindingEvent;
import org.eclipse.core.databinding.BindingException;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.IValueChangingListener;
import org.eclipse.core.databinding.observable.value.IVetoableValue;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 1.0
 * 
 */
public class ValueBinding extends Binding {

	private final IObservableValue target;

	private final IObservableValue model;

	private IValidator targetValidator;

	private IConverter targetToModelConverter;

	private IConverter modelToTargetConverter;

	private boolean updatingTarget = false;
	private boolean updatingModel = false;

	private WritableValue partialValidationErrorObservable;

	private WritableValue validationErrorObservable;

	private IValidator domainValidator;

	/**
	 * @param context
	 * @param target
	 * @param model
	 * @param bindSpec
	 */
	public ValueBinding(DataBindingContext context, IObservableValue target,
			IObservableValue model, BindSpec bindSpec) {
		super(context);
		this.target = target;
		this.model = model;
		validationErrorObservable = new WritableValue(context
				.getValidationRealm(), IStatus.class, null);
		partialValidationErrorObservable = new WritableValue(context
				.getValidationRealm(), IStatus.class, null);
		if (bindSpec.isUpdateTarget()) {
			modelToTargetConverter = bindSpec.getModelToTargetConverter();
			if (modelToTargetConverter == null) {
				throw new BindingException(
						"Missing model to target converter from " + model.getValueType() + " to " + target.getValueType()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (!context.isAssignableFromTo(model.getValueType(),
					modelToTargetConverter.getFromType())) {
				throw new BindingException(
						"model to target converter does not convert from model type. Expected: " + model.getValueType() + ", actual: " + modelToTargetConverter.getFromType()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (!context.isAssignableFromTo(modelToTargetConverter.getToType(),
					target.getValueType())) {
				throw new BindingException(
						"model to target converter does convert to target type. Expected: " + target.getValueType() + ", actual: " + modelToTargetConverter.getToType()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			model.addValueChangeListener(modelChangeListener);
		}
		if (bindSpec.isUpdateModel()) {
			targetToModelConverter = bindSpec.getTargetToModelConverter();
			if (targetToModelConverter == null) {
				throw new BindingException(
						"Missing target to model converter from " + target.getValueType() + " to " + model.getValueType()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (!context.isAssignableFromTo(target.getValueType(),
					targetToModelConverter.getFromType())) {
				throw new BindingException(
						"target to model converter does not convert from target type. Expected: " + target.getValueType() + ", actual: " + targetToModelConverter.getFromType()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (!context.isAssignableFromTo(targetToModelConverter.getToType(),
					model.getValueType())) {
				throw new BindingException(
						"target to model converter does convert to model type. Expected: " + model.getValueType() + ", actual: " + targetToModelConverter.getToType()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			targetValidator = bindSpec.getTypeConversionValidator();
			if (targetValidator == null) {
				throw new BindingException("Missing validator"); //$NON-NLS-1$
			}
			domainValidator = bindSpec.getDomainValidator();
			target.addValueChangeListener(targetChangeListener);
			if (target instanceof IVetoableValue) {
				((IVetoableValue) target)
						.addValueChangingListener(targetChangingListener);
			}
		}
		if (bindSpec.isUpdateTarget()) {
			updateTargetFromModel();
		}
	}

	public void dispose() {
		target.removeValueChangeListener(targetChangeListener);
		if (target instanceof IVetoableValue) {
			((IVetoableValue) target)
					.removeValueChangingListener(targetChangingListener);
		}
		model.removeValueChangeListener(modelChangeListener);
		super.dispose();
	}

	private final IValueChangingListener targetChangingListener = new IValueChangingListener() {
		public boolean handleValueChanging(IVetoableValue source, ValueDiff diff) {
			if (updatingTarget)
				return true;
			// we are notified of a pending change, do validation
			// and veto the change if it is not valid
			Object value = diff.getNewValue();
			IStatus partialValidationError = targetValidator
					.validatePartial(value);
			partialValidationErrorObservable.setValue(partialValidationError);
			return partialValidationError.isOK();
		}
	};

	private final IValueChangeListener targetChangeListener = new IValueChangeListener() {
		public void handleValueChange(IObservableValue source,
				final ValueDiff diff) {
			if (updatingTarget)
				return;
			// the target (usually a widget) has changed, validate
			// the value and update the source
			model.getRealm().exec(new Runnable() {
				public void run() {
					doUpdateModelFromTarget(diff);
				}
			});
		}
	};

	private IValueChangeListener modelChangeListener = new IValueChangeListener() {
		public void handleValueChange(IObservableValue source,
				final ValueDiff diff) {
			if (updatingModel)
				return;
			// The model has changed so we must update the target
			model.getRealm().exec(new Runnable() {
				public void run() {
					doUpdateTargetFromModel(diff);
				}
			});
		}
	};

	/**
	 * This also does validation.
	 * 
	 * @param diff
	 * 
	 * @param changeEvent
	 *            TODO
	 */
	private void doUpdateModelFromTarget(ValueDiff diff) {
		Assert.isTrue(model.getRealm().isCurrent());
		BindingEvent e = new BindingEvent(model, target, diff,
				BindingEvent.EVENT_COPY_TO_MODEL,
				BindingEvent.PIPELINE_AFTER_GET) {
		};
		e.originalValue = diff.getNewValue();
		if (failure(errMsg(fireBindingEvent(e)))) {
			return;
		}

		IStatus validationStatus = doValidate(e.originalValue);
		if (!validationStatus.isOK()) {
			return;
		}
		e.pipelinePosition = BindingEvent.PIPELINE_AFTER_VALIDATE;
		if (failure(errMsg(fireBindingEvent(e)))) {
			return;
		}

		try {
			updatingModel = true;

			e.convertedValue = targetToModelConverter.convert(e.originalValue);
			e.pipelinePosition = BindingEvent.PIPELINE_AFTER_CONVERT;
			if (failure(errMsg(fireBindingEvent(e)))) {
				return;
			}

			validationStatus = doDomainValidation(e.convertedValue);
			if (!validationStatus.isOK()) {
				return;
			}
			e.pipelinePosition = BindingEvent.PIPELINE_AFTER_BUSINESS_VALIDATE;
			if (failure(errMsg(fireBindingEvent(e)))) {
				return;
			}

			model.setValue(e.convertedValue);
			e.pipelinePosition = BindingEvent.PIPELINE_AFTER_CHANGE;
			fireBindingEvent(e);
		} catch (Exception ex) {
			IStatus error = ValidationStatus.error(BindingMessages
					.getString("ValueBinding_ErrorWhileSettingValue"), //$NON-NLS-1$
					ex);
			validationErrorObservable.setValue(error);
		} finally {
			updatingModel = false;
		}
	}

	/**
	 * @param convertedValue
	 * @return String
	 */
	private IStatus doDomainValidation(Object convertedValue) {
		if (domainValidator == null) {
			return Status.OK_STATUS;
		}
		IStatus validationStatus = domainValidator.validate(convertedValue);
		return errMsg(validationStatus);
	}

	private IStatus doValidate(Object value) {
		if (targetValidator == null)
			return Status.OK_STATUS;
		IStatus validationStatus = targetValidator.validate(value);
		return errMsg(validationStatus);
	}

	private IStatus errMsg(final IStatus validationStatus) {
		Assert.isTrue(partialValidationErrorObservable.getRealm().equals(
				validationErrorObservable.getRealm()));
		partialValidationErrorObservable.getRealm().exec(new Runnable() {
			public void run() {
				partialValidationErrorObservable.setValue(Status.OK_STATUS);
				validationErrorObservable.setValue(validationStatus);
			}
		});
		return validationStatus;
	}

	private boolean failure(IStatus errorStatus) {
		// FIXME: Need to fire a BindingEvent here
		return !errorStatus.isOK();
	}

	/**
	 * Can be called from any thread/realm.
	 */
	public void updateTargetFromModel() {
		model.getRealm().exec(new Runnable() {
			public void run() {
				final ValueDiff valueDiff = Diffs.createValueDiff(null, model
						.getValue());
				target.getRealm().exec(new Runnable() {
					public void run() {
						doUpdateTargetFromModel(valueDiff);
					}
				});
			}
		});
	}

	/**
	 * @param diff
	 */
	private void doUpdateTargetFromModel(ValueDiff diff) {
		Assert.isTrue(target.getRealm().isCurrent());
		try {
			updatingTarget = true;
			BindingEvent e = new BindingEvent(model, target, diff,
					BindingEvent.EVENT_COPY_TO_TARGET,
					BindingEvent.PIPELINE_AFTER_GET) {
			};
			e.originalValue = diff.getNewValue();
			if (failure(errMsg(fireBindingEvent(e)))) {
				return;
			}

			e.convertedValue = modelToTargetConverter.convert(e.originalValue);
			e.pipelinePosition = BindingEvent.PIPELINE_AFTER_CONVERT;
			if (failure(errMsg(fireBindingEvent(e)))) {
				return;
			}

			target.setValue(e.convertedValue);
			e.pipelinePosition = BindingEvent.PIPELINE_AFTER_CHANGE;
			if (failure(errMsg(fireBindingEvent(e)))) {
				return;
			}

			// FIXME ValueBinding Needs Separate modelValidator to perform model
			// to target validation.
			doValidate(target.getValue());
			e.pipelinePosition = BindingEvent.PIPELINE_AFTER_VALIDATE;
			fireBindingEvent(e);
		} catch (Exception ex) {
			final IStatus error = ValidationStatus.error(BindingMessages
					.getString("ValueBinding_ErrorWhileSettingValue"), //$NON-NLS-1$
					ex);
			validationErrorObservable.getRealm().exec(new Runnable() {
				public void run() {
					validationErrorObservable.setValue(error);
				}
			});
		} finally {
			updatingTarget = false;
		}
	}

	public IObservableValue getValidationStatus() {
		return validationErrorObservable;
	}

	public IObservableValue getPartialValidationStatus() {
		return partialValidationErrorObservable;
	}

	/**
	 * Can be called from any thread/realm.
	 */
	public void updateModelFromTarget() {
		target.getRealm().exec(new Runnable() {
			public void run() {
				final ValueDiff valueDiff = Diffs.createValueDiff(target
						.getValue(), target.getValue());
				model.getRealm().exec(new Runnable() {
					public void run() {
						doUpdateModelFromTarget(valueDiff);
					}
				});
			}
		});
	}
}
