/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
