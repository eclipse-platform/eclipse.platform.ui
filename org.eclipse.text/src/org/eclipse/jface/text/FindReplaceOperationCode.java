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

package org.eclipse.jface.text;


/**
 * Enumeration type declaring the available findReplace operation codes.
 * 
 * @see org.eclipse.jface.text.FindReplaceDocumentAdapter#findReplace(FindReplaceOperationCode, int, String, String, boolean, boolean, boolean, boolean)
 * @since 3.0
 */
public final class FindReplaceOperationCode {

	/**
	 * The operation code's name.
	 */
	private final String fName;

	/**
	 * Creates and returns a new findReplace operation code.
	 * 
	 * @param operationName the name of this operation
	 */
	private FindReplaceOperationCode(String operationName) {
		Assert.isNotNull(operationName);
		fName= operationName;
	}

	/*
	 * @see Object#toString()
	 */
	public String toString() {
		return fName;
	}

	/**
	 * findReplace operation code used to find the first match.
	 */
	public static final FindReplaceOperationCode FIND_FIRST= new FindReplaceOperationCode("findFirst"); //$NON-NLS-1$

	/**
	 * findReplace operation code to find the next match.
	 */
	public static final FindReplaceOperationCode FIND_NEXT= new FindReplaceOperationCode("findNext"); //$NON-NLS-1$

	/**
	 * findReplace operation code to replace the current match.
	 * This operation must be preceded by a <code>FIND_FIRST</code> or <code>FIND_NEXT</code> operation.
	 */
	public static final FindReplaceOperationCode REPLACE= new FindReplaceOperationCode("replace"); //$NON-NLS-1$

	/**
	 * findReplace operation code to replace the current match and find the next one.
	 * This operation must be preceded by a <code>FIND_FIRST</code> or <code>FIND_NEXT</code> operation.
	 */
	public static final FindReplaceOperationCode REPLACE_FIND_NEXT= new FindReplaceOperationCode("replaceAndFindNext"); //$NON-NLS-1$
}