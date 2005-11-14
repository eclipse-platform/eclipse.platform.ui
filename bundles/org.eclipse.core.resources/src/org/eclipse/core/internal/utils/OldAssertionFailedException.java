/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.utils;

/**
 * <code>AssertionFailedException</code> is a runtime exception thrown
 * by some of the methods in <code>Assert</code>.
 * <p>
 * This class is not declared public to prevent some misuses; programs that catch 
 * or otherwise depend on assertion failures are susceptible to unexpected
 * breakage when assertions in the code are added or removed.
 * </p>
 */
class OldAssertionFailedException extends RuntimeException {
	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/** 
	 * Constructs a new exception with the given message.
	 */
	public OldAssertionFailedException(String detail) {
		super(detail);
	}
}
