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
package org.eclipse.jface.binding;

/**
 * @since 3.2
 *
 */
public interface IValidator {

	/**
	 * Determines if the given value is partially valid. This method is used to
	 * determine, for example, if keystrokes can still be applied to the value.
	 * 
	 * @param value
	 *            the value to validate
	 * @return the error message, or </code>null</code> if the value is
	 *         partially valid.
	 */
	public String isPartiallyValid(Object value);

	/**
	 * Determines if the given value is valid.
	 * 
	 * @param value
	 *            the value to validate
	 * @return the error message, or </code>null</code> if the value is valid.
	 */
	public String isValid(Object value);

}
