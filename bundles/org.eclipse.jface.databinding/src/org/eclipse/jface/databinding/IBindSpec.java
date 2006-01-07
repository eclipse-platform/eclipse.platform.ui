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
	 * Policy constant specifying that the context's update or validation policy should be used. 
	 */
	public static final int POLICY_CONTEXT = 0;

	/**
	 * Policy constant specifying that update or validation should occur automatically. 
	 */
	public static final int POLICY_AUTOMATIC = 1;
	
	/**
	 * Policy constant specifying that update or validation should only occur when explicitly requested. 
	 */
	public static final int POLICY_EXPLICIT = 2;
	
	/**
	 * @return the converter
	 */
	public IConverter getConverter();
	
	/**
	 * @return the validator
	 */
	public IValidator getValidator();
	
	/**
	 * Returns the update policy to be used for updating the model when the target has changed
	 * @return the update policy
	 */
	public int getModelUpdatePolicy();
	
	/**
	 * Returns the validate policy to be used for validating changes to the target
	 * @return the update policy
	 */
	public int getValidatePolicy();
	
	/**
	 * Returns the update policy to be used for updating the target when the model has changed
	 * @return the update policy
	 */
	public int getTargetUpdatePolicy();
	
}
