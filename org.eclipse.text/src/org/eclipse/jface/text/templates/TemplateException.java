/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
 * 
 * @since 3.0
 */
public class TemplateException extends Exception {

	/**
	 * Creates a new template exception.
	 */
	public TemplateException() {
		super();
	}
	
	/**
	 * Creates a new template exception.
	 * 
	 * @param message the message describing the problem that arised
	 */
	public TemplateException(String message) {
		super(message);
	}
	
	/**
	 * Creates a new template exception.
	 * 
	 * @param message the message describing the problem that arised
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
