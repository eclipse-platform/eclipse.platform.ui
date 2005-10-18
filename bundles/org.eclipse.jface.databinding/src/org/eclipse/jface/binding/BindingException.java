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

package org.eclipse.jface.binding;

/**
 * @since 3.2
 *
 */
public class BindingException extends Exception {

	private static final long serialVersionUID = -4092828452936724217L;

	/**
	 * @param message
	 */
	public BindingException(String message) {
		super(message);
	}
}
