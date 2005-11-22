/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding;

import org.eclipse.jface.databinding.converter.IConverter;
import org.eclipse.jface.databinding.validator.IValidator;

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

	/**
	 * Creates a bind spec with the given converter, validator, and update
	 * policies.
	 * 
	 * @param converter
	 * @param validator
	 * @param modelUpdatePolicy
	 * @param validatePolicy
	 * @param targetUpdatePolicy
	 */
	public BindSpec(IConverter converter, IValidator validator,
			Integer modelUpdatePolicy, Integer validatePolicy,
			Integer targetUpdatePolicy) {
		this.converter = converter;
		this.validator = validator;
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
		this(converter, validator, null, null, null);
	}

	public IConverter getConverter() {
		return converter;
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

	public IValidator getValidator() {
		return validator;
	}

	public void setValidator(IValidator validator) {
		this.validator = validator;
	}

	public void setConverter(IConverter converter) {
		this.converter = converter;
	}

}
