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

package org.eclipse.jface.text.templates;


/**
 * Thrown when a template cannot be validated.
 * <p>
 * Clients may instantiate this class.
 * </p>
 * <p>
 * This class is not intended to be serialized.
 * </p>
 *
 * @since 3.0
 */
public class TemplateException extends Exception {

	/**
	 * Serial version UID for this class.
	 * <p>
	 * Note: This class is not intended to be serialized.
	 * </p>
	 * @since 3.1
	 */
	private static final long serialVersionUID= 3906362710416699442L;

	/**
	 * Creates a new template exception.
	 */
	public TemplateException() {
		super();
	}

	/**
	 * Creates a new template exception.
	 *
	 * @param message the message describing the problem that arose
	 */
	public TemplateException(String message) {
		super(message);
	}

	/**
	 * Creates a new template exception.
	 *
	 * @param message the message describing the problem that arose
	 * @param cause the original exception
	 */
	public TemplateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new template exception.
	 *
	 * @param cause the original exception
	 */
	public TemplateException(Throwable cause) {
		super(cause);
	}
}
