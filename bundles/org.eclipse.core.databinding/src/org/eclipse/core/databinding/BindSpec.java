/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Data binding has three concerns, the target, the model, and the data flow
 * between the target and model. BindSpec contains values and settings that
 * influence how data binding manages this data flow between the target and the
 * model.
 * 
 * @since 1.0
 * 
 */
public class BindSpec {

	/**
	 * @since 3.3
	 * 
	 */
	static class DefaultValidator implements IValidator {
		public IStatus validate(Object value) {
			return Status.OK_STATUS;
		}
	}

	/**
	 * @since 3.3
	 * 
	 */
	static class DefaultConverter implements IConverter {
		/**
		 * 
		 */
		private final Object toType;
		/**
		 * 
		 */
		private final Object fromType;

		/**
		 * @param fromType
		 * @param toType
		 */
		DefaultConverter(Object fromType, Object toType) {
			this.toType = toType;
			this.fromType = fromType;
		}

		public Object convert(Object fromObject) {
			return fromObject;
		}

		public Object getFromType() {
			return fromType;
		}

		public Object getToType() {
			return toType;
		}
	}

	private IValidator domainValidator;

	private IConverter modelToTargetConverter;

	private Integer modelUpdatePolicy;

	private IValidator partialTargetValidator;

	private IConverter targetToModelConverter;

	private Integer targetUpdatePolicy;

	private IValidator targetValidator;

	private boolean updateModel = true;

	private boolean updateTarget = true;

	private Integer validatePolicy;

	/**
	 * Default constructor that initializes all objects to their defaults.
	 */
	public BindSpec() {
	}

	/**
	 * Returns the validator to be used, or <code>null</code> if a default
	 * validator should be used.
	 * 
	 * @return the validator, or <code>null</code>
	 */
	public IValidator getDomainValidator() {
		return domainValidator;
	}

	/**
	 * Returns the converter to be used, or <code>null</code> if a default
	 * converter should be used.
	 * 
	 * @return the converter, or <code>null</code>
	 */
	public IConverter getModelToTargetConverter() {
		return modelToTargetConverter;
	}

	/**
	 * Returns the update policy to be used for updating the model when the
	 * target has changed
	 * 
	 * @return the update policy, or <code>null</code> if unspecified
	 * 
	 * @see DataBindingContext#POLICY_AUTOMATIC
	 * @see DataBindingContext#POLICY_EXPLICIT
	 */
	public Integer getModelUpdatePolicy() {
		return modelUpdatePolicy;
	}

	/**
	 * Returns the converter to be used, or <code>null</code> if a default
	 * converter should be used.
	 * 
	 * @return the converter, or <code>null</code>
	 */
	public IConverter getTargetToModelConverter() {
		return targetToModelConverter;
	}

	/**
	 * Returns the update policy to be used for updating the target when the
	 * model has changed
	 * 
	 * @return the update policy, or <code>null</code> if unspecified
	 * 
	 * @see DataBindingContext#POLICY_AUTOMATIC
	 * @see DataBindingContext#POLICY_EXPLICIT
	 */
	public Integer getTargetUpdatePolicy() {
		return targetUpdatePolicy;
	}

	/**
	 * @return a validator for validation of partial target values
	 */
	public IValidator getPartialTargetValidator() {
		return partialTargetValidator;
	}

	/**
	 * Returns the validator to be used, or <code>null</code> if a default
	 * validator should be used.
	 * 
	 * @return the validator, or <code>null</code>
	 */
	public IValidator getTargetValidator() {
		return targetValidator;
	}

	/**
	 * Returns the validate policy to be used for validating changes to the
	 * target
	 * 
	 * @return the update policy, or <code>null</code> if unspecified
	 * 
	 * @see DataBindingContext#POLICY_AUTOMATIC
	 * @see DataBindingContext#POLICY_EXPLICIT
	 */
	public Integer getValidatePolicy() {
		return validatePolicy;
	}

	/**
	 * @return true if the model should be updated by the binding
	 */
	public boolean isUpdateModel() {
		return updateModel;
	}

	/**
	 * @return true if the target should be updated by the binding
	 */
	public boolean isUpdateTarget() {
		return updateTarget;
	}

	/**
	 * Sets the domain validator.
	 * 
	 * @param validator
	 *            <code>null</code> allowed
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setDomainValidator(IValidator validator) {
		this.domainValidator = validator;
		return this;
	}

	/**
	 * Sets the model to target converter.
	 * 
	 * @param converter
	 *            <code>null</code> allowed and will remove all existing
	 *            converters.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setModelToTargetConverter(IConverter converter) {
		modelToTargetConverter = converter;
		return this;
	}

	/**
	 * @param modelUpdatePolicy
	 *            The modelUpdatePolicy to set.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setModelUpdatePolicy(Integer modelUpdatePolicy) {
		this.modelUpdatePolicy = modelUpdatePolicy;
		return this;
	}

	/**
	 * Sets the validator for validating partial target values.
	 * 
	 * @param validator
	 *            the validator, or <code>null</code> for no validation.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setPartialTargetValidator(IValidator validator) {
		partialTargetValidator = validator;
		return this;
	}

	/**
	 * Sets the target to model converter.
	 * 
	 * @param converter
	 *            <code>null</code> allowed and will remove all existing
	 *            converters.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setTargetToModelConverter(IConverter converter) {
		targetToModelConverter = converter;
		return this;
	}

	/**
	 * @param targetUpdatePolicy
	 *            The targetUpdatePolicy to set.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setTargetUpdatePolicy(Integer targetUpdatePolicy) {
		this.targetUpdatePolicy = targetUpdatePolicy;
		return this;
	}

	/**
	 * @param updateModel
	 *            The updateModel to set.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setUpdateModel(boolean updateModel) {
		this.updateModel = updateModel;
		return this;
	}

	/**
	 * @param updateTarget
	 *            The updateTarget to set.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setUpdateTarget(boolean updateTarget) {
		this.updateTarget = updateTarget;
		return this;
	}

	/**
	 * @param validatePolicy
	 *            The validatePolicy to set.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setValidatePolicy(Integer validatePolicy) {
		this.validatePolicy = validatePolicy;
		return this;
	}

	/**
	 * Sets the validator.
	 * 
	 * @param validator
	 *            <code>null</code> allowed and will remove all existing
	 *            validators.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setTargetValidator(IValidator validator) {
		targetValidator = validator;
		return this;
	}

	/**
	 * Fills any values not explicitly set with defaults. This implementation of
	 * {@link #fillBindSpecDefaults(IObservableValue, IObservableValue)} creates
	 * validators that always return {@link Status#OK_STATUS}, and converters
	 * that perform no conversion.
	 * 
	 * @param target
	 * @param model
	 */
	protected void fillBindSpecDefaults(IObservable target, IObservable model) {
		if (getTargetValidator() == null) {
			setTargetValidator(new DefaultValidator());
		}
		if (getPartialTargetValidator() == null) {
			setPartialTargetValidator(new DefaultValidator());
		}
		if (getDomainValidator() == null) {
			setDomainValidator(new DefaultValidator());
		}
		if (getModelToTargetConverter() == null) {
			setModelToTargetConverter(new DefaultConverter(Object.class,
					Object.class));
		}
		if (getTargetToModelConverter() == null) {
			setTargetToModelConverter(new DefaultConverter(Object.class,
					Object.class));
		}
	}

}
