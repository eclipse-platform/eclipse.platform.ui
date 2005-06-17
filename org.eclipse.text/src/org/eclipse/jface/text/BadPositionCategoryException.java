/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;



/**
 * Indicates the attempt to access a non-existing position
 * category in a document.
 * <p>
 * This class is not intended to be serialized.
 * </p>
 *
 * @see org.eclipse.jface.text.IDocument
 */
public class BadPositionCategoryException extends Exception {

	/**
	 * Serial version UID for this class.
	 * <p>
	 * Note: This class is not intended to be serialized.
	 * </p>
	 * @since 3.1
	 */
	private static final long serialVersionUID= 3761405300745713206L;

	/**
	 * Creates a new bad position category exception.
	 */
	public BadPositionCategoryException() {
		super();
	}

	/**
	 * Creates a new bad position category exception.
	 *
	 * @param message the exception's message
	 */
	public BadPositionCategoryException(String message) {
		super(message);
	}
}
