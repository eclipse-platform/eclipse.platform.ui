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
package org.eclipse.jface.internal.databinding.internal;

import org.eclipse.jface.databinding.BindSpec;
import org.eclipse.jface.databinding.BindingEvent;
import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.conversion.IConverter;
import org.eclipse.jface.internal.databinding.provisional.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IValueChangeListener;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IValueChangingListener;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IVetoableValue;
import org.eclipse.jface.internal.databinding.provisional.observable.value.ValueDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.value.WritableValue;
import org.eclipse.jface.internal.databinding.provisional.validation.IDomainValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.IValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.ValidationError;

/**
 * @since 1.0
 * 
 * implementation note: this class extends a deprecated class for backwards compatibility
 */
public class ValueBinding extends org.eclipse.jface.internal.databinding.provisional.Binding {

	private final IObservableValue target;

	private final IObservableValue model;

	private IValidator targetValidator;

	private IConverter targetToModelConverter;

	private IConverter modelToTargetConverter;

	private IDomainValidator domainValidator;

	private boolean updating = false;

	private WritableValue partialValidationErrorObservable = new WritableValue(
			ValidationError.class, null);

	private WritableValue validationErrorObservable = new WritableValue(
			ValidationError.class, null);

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
		updateTargetFromModel();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.Binding#dispose()
	 */
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
			if (updating)
				return true;
			// we are notified of a pending change, do validation
			// and veto the change if it is not valid
			Object value = diff.getNewValue();
			ValidationError partialValidationError = targetValidator
					.isPartiallyValid(value);
			partialValidationErrorObservable.setValue(partialValidationError);
			return partialValidationError == null;
		}
	};

	private final IValueChangeListener targetChangeListener = new IValueChangeListener() {
		public void handleValueChange(IObservableValue source, ValueDiff diff) {
			if (updating)
				return;
			// the target (usually a widget) has changed, validate
			// the value and update the source
			updateModelFromTarget(diff);
		}
	};

	private IValueChangeListener modelChangeListener = new IValueChangeListener() {
		public void handleValueChange(IObservableValue source, ValueDiff diff) {
			if (updating)
				return;
			// The model has changed so we must update the target
			doUpdateTargetFromModel(diff);
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
	public void updateModelFromTarget(ValueDiff diff) {
		BindingEvent e = new BindingEvent(model, target, diff,
				BindingEvent.EVENT_COPY_TO_MODEL,
				BindingEvent.PIPELINE_AFTER_GET) {
		};
		e.originalValue = target.getValue();
		if (failure(errMsg(fireBindingEvent(e)))) {
			return;
		}

		ValidationError validationError = doValidate(e.originalValue);
		if (validationError != null) {
			return;
		}
		e.pipelinePosition = BindingEvent.PIPELINE_AFTER_VALIDATE;
		if (failure(errMsg(fireBindingEvent(e)))) {
			return;
		}

		try {
			updating = true;

			e.convertedValue = targetToModelConverter.convert(e.originalValue);
			e.pipelinePosition = BindingEvent.PIPELINE_AFTER_CONVERT;
			if (failure(errMsg(fireBindingEvent(e)))) {
				return;
			}

			validationError = doDomainValidation(e.convertedValue);
			if (validationError != null) {
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
			ValidationError error = ValidationError.error(BindingMessages
					.getString("ValueBinding_ErrorWhileSettingValue")); //$NON-NLS-1$
			validationErrorObservable.setValue(error);
		} finally {
			updating = false;
		}
	}

	/**
	 * @param convertedValue
	 * @return String
	 */
	private ValidationError doDomainValidation(Object convertedValue) {
		if (domainValidator == null) {
			return null;
		}
		ValidationError validationError = domainValidator
				.isValid(convertedValue);
		return errMsg(validationError);
	}

	private ValidationError doValidate(Object value) {
		if (targetValidator == null)
			return null;
		ValidationError validationError = targetValidator.isValid(value);
		return errMsg(validationError);
	}

	private ValidationError errMsg(ValidationError validationError) {
		partialValidationErrorObservable.setValue(null);
		validationErrorObservable.setValue(validationError);
		return validationError;
	}

	private boolean failure(ValidationError errorMessage) {
		// FIXME: Need to fire a BindingEvent here
		if (errorMessage != null
				&& errorMessage.status == ValidationError.ERROR) {
			return true;
		}
		return false;
	}

	public void updateTargetFromModel() {
		doUpdateTargetFromModel(Diffs.createValueDiff(null, model.getValue()));
	}

	/**
	 * @param diff
	 */
	public void doUpdateTargetFromModel(ValueDiff diff) {
		try {
			updating = true;
			BindingEvent e = new BindingEvent(model, target, diff,
					BindingEvent.EVENT_COPY_TO_TARGET,
					BindingEvent.PIPELINE_AFTER_GET) {
			};
			e.originalValue = model.getValue();
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

			//FIXME ValueBinding Needs Separate modelValidator to perform model to target validation.
			doValidate(target.getValue());
			e.pipelinePosition = BindingEvent.PIPELINE_AFTER_VALIDATE;
			fireBindingEvent(e);
		} finally {
			updating = false;
		}
	}

	public IObservableValue getValidationError() {
		return validationErrorObservable;
	}

	public IObservableValue getPartialValidationError() {
		return partialValidationErrorObservable;
	}

	public void updateModelFromTarget() {
		updateModelFromTarget(Diffs.createValueDiff(target.getValue(), target
				.getValue()));
	}
}
