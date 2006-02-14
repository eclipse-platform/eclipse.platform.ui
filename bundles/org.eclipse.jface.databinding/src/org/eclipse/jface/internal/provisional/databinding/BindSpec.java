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
package org.eclipse.jface.internal.provisional.databinding;

import org.eclipse.jface.internal.provisional.databinding.converter.IConverter;
import org.eclipse.jface.internal.provisional.databinding.validator.IDomainValidator;
import org.eclipse.jface.internal.provisional.databinding.validator.IValidator;

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

	private IConverter converter;

	private IValidator validator;

	private final Integer modelUpdatePolicy;

	private final Integer validatePolicy;

	private final Integer targetUpdatePolicy;

	private IDomainValidator domainValidator;

	/**
	 * Creates a bind spec with the given converter, validator, and update
	 * policies.
	 * 
	 * @param converter
	 * @param validator
	 * @param domainValidator 
	 * @param modelUpdatePolicy
	 * @param validatePolicy
	 * @param targetUpdatePolicy
	 */
	public BindSpec(IConverter converter, IValidator validator, IDomainValidator domainValidator,
			Integer modelUpdatePolicy, Integer validatePolicy,
			Integer targetUpdatePolicy) {
		this.converter = converter;
		this.validator = validator;
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
	public BindSpec(IConverter converter, IValidator validator) {
		this(converter, validator, null, null, null, null);
	}
	
	/**
	 * Creates a bind spec with the given converter and validators. The update
	 * policies are set to <code>IBindSpec.POLICY_CONTEXT</code>.
	 * 
	 * @param converter
	 * @param validator
	 * @param domainValidator 
	 */
	public BindSpec(IConverter converter, IValidator validator, IDomainValidator domainValidator) {
		this(converter, validator, domainValidator, null, null, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.provisional.databinding.IBindSpec#getConverter()
	 */
	public IConverter getConverter() {
		return converter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.provisional.databinding.IBindSpec#getModelUpdatePolicy()
	 */
	public Integer getModelUpdatePolicy() {
		return modelUpdatePolicy;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.provisional.databinding.IBindSpec#getTargetUpdatePolicy()
	 */
	public Integer getTargetUpdatePolicy() {
		return targetUpdatePolicy;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.provisional.databinding.IBindSpec#getValidatePolicy()
	 */
	public Integer getValidatePolicy() {
		return validatePolicy;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.provisional.databinding.IBindSpec#getValidator()
	 */
	public IValidator getValidator() {
		return validator;
	}

	/** (non-api)
	 * Set the validator
	 * @param validator 
	 */
	public void setValidator(IValidator validator) {
		this.validator = validator;
	}

	/** (non-api)
	 * Set the converter
	 * @param converter 
	 */
	public void setConverter(IConverter converter) {
		this.converter = converter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.provisional.databinding.IBindSpec#getDomainValidator()
	 */
	public IDomainValidator getDomainValidator() {
		return domainValidator;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.provisional.databinding.IBindSpec#setDomainValidator(org.eclipse.jface.internal.provisional.databinding.validator.IDomainValidator)
	 */
	public void setDomainValidator(IDomainValidator domainValidator) {
		this.domainValidator = domainValidator;
	}

}
