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
package org.eclipse.jface.databinding;

/**
 * A checked exception indicating a binding problem.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * TODO API review issue: Use CoreException instead?
 * 
 * @since 3.2
 */
public class BindingException extends RuntimeException {

	/*
	 * Needed because all Throwables are Serializable.
	 */
	private static final long serialVersionUID = -4092828452936724217L;

	/**
	 * Creates a new BindingException with the given message.
	 * @param message
	 */
	public BindingException(String message) {
		super(message);
	}
}
