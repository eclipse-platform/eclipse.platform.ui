/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

/**
 * Status codes used by the refactoring core plug-in.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see org.eclipse.core.runtime.Status
 * 
 * @since 3.0
 */
public abstract class IRefactoringCoreStatusCodes {

	private IRefactoringCoreStatusCodes() {
		// no instance 
	}
	
	/**
	 * Status code (value 10000) indicating an internal error.
	 */
	public static final int INTERNAL_ERROR= 10000;

	/** 
	 * Status code (value 10001) indicating that a bad location exception has 
	 * occurred during change execution.
	 * 
	 * @see org.eclipse.jface.text.BadLocationException
	 */ 
	public static final int BAD_LOCATION= 10001;
	
	/**
	 * Status code (value 10002) indicating that an validateEdit call has changed the
	 * content of a file on disk.
	 */
	public static final int VALIDATE_EDIT_CHANGED_CONTENT= 10002;
	
	/**
	 * Status code (value 10003) indicating that a condition checker already exists
	 * in a shared condition checking context.
	 */
	public static final int CHECKER_ALREADY_EXISTS_IN_CONTEXT= 10003;	
}
