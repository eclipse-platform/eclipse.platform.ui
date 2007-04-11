/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.databinding.validation;

import org.eclipse.core.runtime.IStatus;

/**
 * A validator. This validator is responsible for determining if a given value
 * is valid. Validators can be used on target or model values. For example, a
 * String2IntValidator would only accept source Strings that can successfully be
 * converted to an integer value, and a PositiveIntegerValidator would only
 * accept positive integers.
 * 
 * @since 1.0
 * 
 */
public interface IValidator {

	/**
	 * Determines if the given value is valid.
	 * 
	 * @param value
	 *            the value to validate
	 * @return a status object indicating whether the validation succeeded
	 *         {@link IStatus#isOK()} or not. Never null.
	 */
	public IStatus validate(Object value);
	
}
