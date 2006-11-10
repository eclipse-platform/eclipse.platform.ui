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
package org.eclipse.core.databinding.validation;


/**
 * A validator.  This validator is responsible for telling clients if its associated
 * type conversion function will succeed or fail.  For example, a String2IntValidator would
 * only accept source Strings that can successfully be converted to an integer value.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 1.0
 *
 */
public interface IValidator {

	/**
	 * Determines if the given value is partially valid. This method is used to
	 * determine, for example, if keystrokes can still be applied to the value.
	 * 
	 * @param value
	 *            the value to validate
	 * @return the ValidationError, or </code>null</code> if the value is
	 *         partially valid.
	 */
	public ValidationError isPartiallyValid(Object value);

	/**
	 * Determines if the given value is valid, that is if it can successfully
	 * be converted to the target data type.
	 * 
	 * @param value
	 *            the value to validate
	 * @return the ValidationError, or </code>null</code> if the value is valid.
	 */
	public ValidationError isValid(Object value);

}
