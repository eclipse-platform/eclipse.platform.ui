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
package org.eclipse.jface.internal.databinding.api;

import org.eclipse.jface.internal.databinding.api.conversion.IConverter;
import org.eclipse.jface.internal.databinding.api.validation.IDomainValidator;
import org.eclipse.jface.internal.databinding.api.validation.IValidator;

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
 * @since 3.2
 */
public class BindSpec implements IBindSpec {

	private IConverter modelToTargetConverter;

	private IConverter targetToModelConverter;

	private IValidator targetValidator;

	private IDomainValidator domainValidator;

	private final Integer modelUpdatePolicy;

	private final Integer validatePolicy;

	private final Integer targetUpdatePolicy;

	/**
	 * Creates a bind spec with the given converters, validators, and update
	 * policies.
	 * 
	 * @param modelToTargetConverter
	 * @param targetValidator
	 * @param modelUpdatePolicy
	 * @param validatePolicy
	 * @param targetUpdatePolicy
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
	 * @param converter
	 * @param validator
	 */
	public BindSpec(IConverter modelToTargetConverter,
			IConverter targetToModelConverter, IValidator targetValidator,
			IDomainValidator domainValidator) {
		this(modelToTargetConverter, targetToModelConverter, targetValidator,
				domainValidator, null, null, null);
	}

	public Integer getModelUpdatePolicy() {
		return modelUpdatePolicy;
	}

	public Integer getTargetUpdatePolicy() {
		return targetUpdatePolicy;
	}

	public Integer getValidatePolicy() {
		return validatePolicy;
	}

	public IValidator getTypeConversionValidator() {
		return targetValidator;
	}

	public IDomainValidator getDomainValidator() {
		return domainValidator;
	}

	public IConverter getModelToTargetConverter() {
		return modelToTargetConverter;
	}

	public IConverter getTargetToModelConverter() {
		return targetToModelConverter;
	}
	
	public void setModelToTargetConverter(IConverter converter) {
		this.modelToTargetConverter = converter;
	}
	
	public void setTargetToModelConverter(IConverter converter) {
		this.targetToModelConverter = converter;
	}

	public void setValidator(IValidator validator) {
		this.targetValidator = validator;
	}
	
	public void setDomainValidator(IDomainValidator validator) {
		this.domainValidator = validator;
	}

}
