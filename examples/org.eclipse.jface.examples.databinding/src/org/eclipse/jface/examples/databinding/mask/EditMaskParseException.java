/*******************************************************************************
 * Copyright (c) 2006, 2014 The Pampered Chef and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.mask;

/**
 * Indicates a parse error while parsing an edit mask.
 *
 * @since 3.3
 */
public class EditMaskParseException extends RuntimeException {

	private static final long serialVersionUID = 8142999683999681500L;

	/**
	 * Construct a MaskParseException
	 */
	public EditMaskParseException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public EditMaskParseException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public EditMaskParseException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public EditMaskParseException(Throwable cause) {
		super(cause);
	}
}
