/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds (bug 135316)
 *******************************************************************************/
package org.eclipse.jface.databinding;

import org.eclipse.jface.internal.databinding.provisional.conversion.IConverter;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider;
import org.eclipse.jface.internal.databinding.provisional.validation.IDomainValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.IValidator;


/**
 * Data binding has three concerns, the target, the model, and the data flow
 * between the target and model. BindSpec contains values and settings that
 * influence how data binding manages this data flow between the target and the
 * model.
 * 
 * @since 1.0
 */
public class BindSpec {

	private IConverter[] modelToTargetConverters;

	private IConverter[] targetToModelConverters;

	private IValidator[] targetValidators;
	
	private LazyInsertDeleteProvider lazyInsertDeleteProvider = new LazyInsertDeleteProvider();

	private IDomainValidator domainValidator;

	private Integer modelUpdatePolicy;

	private Integer validatePolicy;

	private Integer targetUpdatePolicy;

	private boolean updateModel = true;

	private boolean updateTarget = true;

	private static final IValidator[] EMPTY_VALIDATORS = new IValidator[0];

	private static final IConverter[] EMPTY_CONVERTERS = new IConverter[0];

	/**
	 * Default constructor that initializes all objects to their defaults.
	 */
	public BindSpec() {
	}
	
	/**
	 * Creates a bind spec with the given converters, validators, and update
	 * policies.
	 * 
	 * @param modelToTargetConverter
	 * @param targetToModelConverter
	 * @param targetValidator
	 * @param domainValidator
	 * @param modelUpdatePolicy
	 * @param validatePolicy
	 * @param targetUpdatePolicy
	 * @param lazyInsertDeleteProvider 
	 * 
	 */
	protected BindSpec(IConverter[] modelToTargetConverter,
			IConverter[] targetToModelConverter, IValidator[] targetValidator,
			IDomainValidator domainValidator, Integer modelUpdatePolicy,
			Integer validatePolicy, Integer targetUpdatePolicy,
			LazyInsertDeleteProvider lazyInsertDeleteProvider) {
		
		this.modelToTargetConverters = modelToTargetConverter;
		this.targetToModelConverters = targetToModelConverter;
		this.targetValidators = targetValidator;
		this.domainValidator = domainValidator;
		this.modelUpdatePolicy = modelUpdatePolicy;
		this.validatePolicy = validatePolicy;
		this.targetUpdatePolicy = targetUpdatePolicy;
		this.lazyInsertDeleteProvider = lazyInsertDeleteProvider;
	}

	/**
	 * Returns the converter to be used, or <code>null</code> if a default
	 * converter should be used.
	 * 
	 * @return the converter, or <code>null</code>
	 */
	public IConverter getModelToTargetConverter() {
		return (getModelToTargetConverters().length == 0) ? null
				: getModelToTargetConverters()[0];
	}

	/**
	 * Returns the converters to be used, or an empty array if a default
	 * converter should be used.
	 * 
	 * @return the converters, or an empty array if none
	 */
	public IConverter[] getModelToTargetConverters() {
		return (modelToTargetConverters == null) ? EMPTY_CONVERTERS
				: modelToTargetConverters;
	}

	/**
	 * Returns the converter to be used, or <code>null</code> if a default
	 * converter should be used.
	 * 
	 * @return the converter, or <code>null</code>
	 */
	public IConverter getTargetToModelConverter() {
		return (getTargetToModelConverters().length == 0) ? null
				: getTargetToModelConverters()[0];
	}

	/**
	 * Returns the converters to be used, or <code>null</code> if a default
	 * converter should be used.
	 * 
	 * @return the converters, or an empty array if none
	 */
	public IConverter[] getTargetToModelConverters() {
		return (targetToModelConverters == null) ? EMPTY_CONVERTERS
				: targetToModelConverters;
	}

	/**
	 * Returns the validator to be used, or <code>null</code> if a default
	 * validator should be used.
	 * 
	 * @return the validator, or <code>null</code>
	 */
	public IValidator getTypeConversionValidator() {
		return (getTypeConversionValidators().length == 0) ? null
				: getTypeConversionValidators()[0];
	}

	/**
	 * Returns the validators to be used, or an empty array if a default
	 * validator should be used.
	 * 
	 * @return the validators or an empty array if none
	 */
	public IValidator[] getTypeConversionValidators() {
		return (targetValidators == null) ? EMPTY_VALIDATORS : targetValidators;
	}

	/**
	 * Returns the validator to be used, or <code>null</code> if a default
	 * validator should be used.
	 * 
	 * @return the validator, or <code>null</code>
	 */
	public IDomainValidator getDomainValidator() {
		return domainValidator;
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
	 * Sets the model to target converter.
	 * 
	 * @param converter <code>null</code> allowed and will remove all existing converters.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setModelToTargetConverter(IConverter converter) {
		return (converter == null) ? setModelToTargetConverters(null)
				: setModelToTargetConverters(new IConverter[] { converter });
	}

	/**
	 * Sets the model to target converters.
	 * 
	 * @param converters <code>null</code> allowed and will remove all existing converters.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setModelToTargetConverters(IConverter[] converters) {
		this.modelToTargetConverters = converters;
		return this;
	}

	/**
	 * Sets the target to model converter.
	 * 
	 * @param converter <code>null</code> allowed and will remove all existing converters.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setTargetToModelConverter(IConverter converter) {
		return (converter == null) ? setTargetToModelConverters(null)
				: setTargetToModelConverters(new IConverter[] { converter });
	}

	/**
	 * Sets the target to model converters.
	 * 
	 * @param converters <code>null</code> allowed and will remove all existing converters.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setTargetToModelConverters(IConverter[] converters) {
		this.targetToModelConverters = converters;
		return this;
	}

	/**
	 * Sets the validator.
	 * 
	 * @param validator <code>null</code> allowed and will remove all existing validators.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setValidator(IValidator validator) {
		return (validator == null) ? setValidators(null)
				: setValidators(new IValidator[] { validator });
	}

	/**
	 * Sets the validators.
	 * 
	 * @param validators <code>null</code> allowed and will remove all existing validators.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setValidators(IValidator[] validators) {
		this.targetValidators = validators;
		return this;
	}

	/**
	 * Sets the domain validator.
	 * 
	 * @param validator <code>null</code> allowed
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setDomainValidator(IDomainValidator validator) {
		this.domainValidator = validator;
		return this;
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
	 * @param modelUpdatePolicy
	 *            The modelUpdatePolicy to set.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setModelUpdatePolicy(Integer modelUpdatePolicy) {
		this.modelUpdatePolicy = modelUpdatePolicy;
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
	 * @param validatePolicy
	 *            The validatePolicy to set.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setValidatePolicy(Integer validatePolicy) {
		this.validatePolicy = validatePolicy;
		return this;
	}

	
	/**
	 * @return Returns the lazyInsertDeleteProvider.
	 */
	public LazyInsertDeleteProvider getLazyInsertDeleteProvider() {
		return lazyInsertDeleteProvider;
	}
	

	/**
	 * @param lazyInsertDeleteProvider The lazyInsertDeleteProvider to set.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setLazyInsertDeleteProvider(
			LazyInsertDeleteProvider lazyInsertDeleteProvider) {
		this.lazyInsertDeleteProvider = lazyInsertDeleteProvider;
		return this;
	}

}
