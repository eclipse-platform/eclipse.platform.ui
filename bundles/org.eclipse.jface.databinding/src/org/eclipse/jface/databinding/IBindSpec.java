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
package org.eclipse.jface.databinding;

import org.eclipse.jface.databinding.converter.IConverter;
import org.eclipse.jface.databinding.validator.IValidator;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 *
 */
public interface IBindSpec {
	
	/**
	 * Returns the converter to be used, or <code>null</code> if a default
	 * converter should be used.
	 * 
	 * @return the converter, or <code>null</code>
	 */
	public IConverter getConverter();
	
	/**
	 * Returns the validator to be used, or <code>null</code> if a default
	 * validator should be used.
	 * 
	 * @return the validator, or <code>null</code>
	 */
	public IValidator getValidator();
	
	/**
	 * Returns the update policy to be used for updating the model when the target has changed
	 * @return the update policy, or <code>null</code> if unspecified
	 * 
	 * @see IDataBindingContext#POLICY_AUTOMATIC
	 * @see IDataBindingContext#POLICY_EXPLICIT
	 */
	public Integer getModelUpdatePolicy();
	
	/**
	 * Returns the validate policy to be used for validating changes to the target
	 * @return the update policy, or <code>null</code> if unspecified
	 * 
	 * @see IDataBindingContext#POLICY_AUTOMATIC
	 * @see IDataBindingContext#POLICY_EXPLICIT
	 */
	public Integer getValidatePolicy();
	
	/**
	 * Returns the update policy to be used for updating the target when the model has changed
	 * @return the update policy, or <code>null</code> if unspecified
	 * 
	 * @see IDataBindingContext#POLICY_AUTOMATIC
	 * @see IDataBindingContext#POLICY_EXPLICIT
	 */
	public Integer getTargetUpdatePolicy();
	
}
