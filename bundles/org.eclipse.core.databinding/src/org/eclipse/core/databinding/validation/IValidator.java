/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * @param <T>
 *            type of object being validated
 * @since 1.0
 */
@FunctionalInterface
public interface IValidator<T> {

	/**
	 * Determines if the given value is valid.
	 *
	 * @param value
	 *            the value to validate
	 * @return a status object indicating whether the validation succeeded
	 *         {@link IStatus#isOK()} or not. Never null.
	 */
	public IStatus validate(T value);

}
