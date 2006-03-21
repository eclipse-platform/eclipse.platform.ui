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
package org.eclipse.jface.internal.databinding.provisional;

import org.eclipse.jface.internal.databinding.provisional.conversion.IConverter;
import org.eclipse.jface.internal.databinding.provisional.validation.IDomainValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.IValidator;

/**
 * A concrete implementation of IBindSpec, suitable either for instantiating or
 * subclassing.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 1.0
 */
public class BindSpec {

	private IConverter modelToTargetConverter;

	private IConverter targetToModelConverter;

	private IValidator targetValidator;

	private IDomainValidator domainValidator;

	private Integer modelUpdatePolicy;

	private Integer validatePolicy;

	private Integer targetUpdatePolicy;

	private boolean updateModel = true;

	private boolean updateTarget = true;

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
	 * 
	 */
	public BindSpec(IConverter modelToTargetConverter,
			IConverter targetToModelConverter, IValidator targetValidator,
			IDomainValidator domainValidator, Integer modelUpdatePolicy,
			Integer validatePolicy, Integer targetUpdatePolicy) {
		this.modelToTargetConverter = modelToTargetConverter;
		this.targetToModelConverter = targetToModelConverter;
		this.targetValidator = targetValidator;
		this.domainValidator = domainValidator;
		this.modelUpdatePolicy = modelUpdatePolicy;
		this.validatePolicy = validatePolicy;
		this.targetUpdatePolicy = targetUpdatePolicy;
	}

	/**
	 * Creates a bind spec with the given converter and validator. The update
	 * policies are set to <code>IBindSpec.POLICY_CONTEXT</code>.
	 * 
	 * @param modelToTargetConverter
	 * @param targetToModelConverter
	 * @param targetValidator
	 * @param domainValidator
	 * 
	 */
	public BindSpec(IConverter modelToTargetConverter,
			IConverter targetToModelConverter, IValidator targetValidator,
			IDomainValidator domainValidator) {
		this(modelToTargetConverter, targetToModelConverter, targetValidator,
				domainValidator, null, null, null);
	}

	/**
	 * 
	 */
	public BindSpec() {
		this(null, null, null, null, null, null, null);
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
	 * Returns the converter to be used, or <code>null</code> if a default
	 * converter should be used.
	 * 
	 * @return the converter, or <code>null</code>
	 */
	public IConverter getTargetToModelConverter() {
		return targetToModelConverter;
	}

	/**
	 * Returns the validator to be used, or <code>null</code> if a default
	 * validator should be used.
	 * 
	 * @return the validator, or <code>null</code>
	 */
	public IValidator getTypeConversionValidator() {
		return targetValidator;
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
	 * @param converter
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setModelToTargetConverter(IConverter converter) {
		this.modelToTargetConverter = converter;
		return this;
	}

	/**
	 * @param converter
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setTargetToModelConverter(IConverter converter) {
		this.targetToModelConverter = converter;
		return this;
	}

	/**
	 * @param validator
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setValidator(IValidator validator) {
		this.targetValidator = validator;
		return this;
	}

	/**
	 * @param validator
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setDomainValidator(IDomainValidator validator) {
		this.domainValidator = validator;
		return this;
	}

	/**
	 * @return true if the model should be updated by the binding
	 */
	public boolean updateModel() {
		return updateModel;
	}

	/**
	 * @return true if the target should be updated by the binding
	 */
	public boolean updateTarget() {
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

}
