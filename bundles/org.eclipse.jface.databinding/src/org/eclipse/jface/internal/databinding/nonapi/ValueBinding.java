/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.nonapi;

import org.eclipse.jface.internal.databinding.api.BindingEvent;
import org.eclipse.jface.internal.databinding.api.BindingException;
import org.eclipse.jface.internal.databinding.api.IBindSpec;
import org.eclipse.jface.internal.databinding.api.conversion.IConverter;
import org.eclipse.jface.internal.databinding.api.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.api.observable.value.IValueChangeListener;
import org.eclipse.jface.internal.databinding.api.observable.value.IValueChangingListener;
import org.eclipse.jface.internal.databinding.api.observable.value.IValueDiff;
import org.eclipse.jface.internal.databinding.api.observable.value.IVetoableValue;
import org.eclipse.jface.internal.databinding.api.validation.IDomainValidator;
import org.eclipse.jface.internal.databinding.api.validation.IValidator;
import org.eclipse.jface.internal.databinding.api.validation.ValidationError;

/**
 * @since 3.2
 * 
 */
public class ValueBinding extends Binding {

	private final IObservableValue target;

	private final IObservableValue model;

	private IValidator targetValidator;

	private IConverter targetToModelConverter;

	private IConverter modelToTargetConverter;

	private IDomainValidator domainValidator;

	private boolean updating = false;

	/**
	 * @param context
	 * @param target
	 * @param model
	 * @param bindSpec
	 */
	public ValueBinding(DataBindingContext context, IObservableValue target,
			IObservableValue model, IBindSpec bindSpec) {
		super(context);
		this.target = target;
		this.model = model;
		targetToModelConverter = bindSpec.getTargetToModelConverter();
		modelToTargetConverter = bindSpec.getModelToTargetConverter();
		if (targetToModelConverter == null) {
			throw new BindingException(
					"Missing target to model converter from " + target.getValueType() + " to " + model.getValueType()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (modelToTargetConverter == null) {
			throw new BindingException(
					"Missing model to target converter from " + model.getValueType() + " to " + target.getValueType()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (!context.isAssignableFromTo(model.getValueType(),
				modelToTargetConverter.getFromType())) {
			throw new BindingException(
					"Converter does not apply to model type. Expected: " + model.getValueType() + ", actual: " + modelToTargetConverter.getFromType()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (!context.isAssignableFromTo(modelToTargetConverter.getToType(),
				target.getValueType())) {
			throw new BindingException(
					"Converter does not apply to target type. Expected: " + modelToTargetConverter.getToType() + ", actual: " + target.getValueType()); //$NON-NLS-1$ //$NON-NLS-2$
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
		model.addValueChangeListener(modelChangeListener);
	}

	private final IValueChangingListener targetChangingListener = new IValueChangingListener() {
		public boolean handleValueChanging(IVetoableValue source,
				IValueDiff diff) {
			if (updating)
				return true;
			// we are notified of a pending change, do validation
			// and veto the change if it is not valid
			Object value = diff.getNewValue();
			ValidationError partialValidationError = targetValidator
					.isPartiallyValid(value);
			context.updatePartialValidationError(ValueBinding.this,
					partialValidationError);
			return partialValidationError == null;
		}
	};

	private final IValueChangeListener targetChangeListener = new IValueChangeListener() {
		public void handleValueChange(IObservableValue source, IValueDiff diff) {
			if (updating)
				return;
			// the target (usually a widget) has changed, validate
			// the value and update the source
			updateModelFromTarget(diff);
		}
	};

	private IValueChangeListener modelChangeListener = new IValueChangeListener() {
		public void handleValueChange(IObservableValue source, IValueDiff diff) {
			if (updating)
				return;
			// The model has changed so we must update the target
			doUpdateTargetFromModel(diff);
		}
	};

	/**
	 * This also does validation.
	 * 
	 * @param changeEvent
	 *            TODO
	 */
	public void updateModelFromTarget(IValueDiff diff) {
		BindingEvent e = new BindingEvent(model, target, diff,
				BindingEvent.EVENT_COPY_TO_MODEL,
				BindingEvent.PIPELINE_AFTER_GET);
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
			context.updateValidationError(this, ValidationError.error(BindingMessages
					.getString("ValueBinding_ErrorWhileSettingValue"))); //$NON-NLS-1$
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
		ValidationError validationError = domainValidator.isValid(convertedValue);
		return errMsg(validationError);
	}

	private ValidationError doValidate(Object value) {
		ValidationError validationError = targetValidator.isValid(value);
		return errMsg(validationError);
	}

	private ValidationError errMsg(ValidationError validationError) {
		context.updatePartialValidationError(this, null);
		context.updateValidationError(this, validationError);
		return validationError;
	}

	private boolean failure(ValidationError errorMessage) {
		// FIXME: Need to fire a BindingEvent here
		if (errorMessage != null && errorMessage.status == ValidationError.ERROR) {
			return true;
		}
		return false;
	}

	public void updateTargetFromModel() {
		doUpdateTargetFromModel(null);
	}

	/**
	 * @param diff
	 */
	public void doUpdateTargetFromModel(IValueDiff diff) {
		try {
			updating = true;
			BindingEvent e = new BindingEvent(model, target, diff,
					BindingEvent.EVENT_COPY_TO_TARGET,
					BindingEvent.PIPELINE_AFTER_GET);
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

			doValidate(target.getValue());
			e.pipelinePosition = BindingEvent.PIPELINE_AFTER_VALIDATE;
			fireBindingEvent(e);
		} finally {
			updating = false;
		}
	}
}