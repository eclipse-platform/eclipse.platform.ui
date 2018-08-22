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
package org.eclipse.core.databinding.observable.value;

/**
 * @since 1.0
 *
 */
public class ChangeVetoException extends RuntimeException {

	/**
	 * @param string
	 */
	public ChangeVetoException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 1L;

}
