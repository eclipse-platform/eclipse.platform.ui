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
package org.eclipse.ltk.core.refactoring.participants;

/**
 * A generic super class of all refactoring specific argument
 * classes.
 * <p>
 * The main purpose of this class is to not use <code>Object</code>
 * in signatures. This helps to distinguish between the element to
 * be refactored and the arguments needed to carry out the refactoring.
 * </p>
 * 
 * @since 3.0
 */
public abstract class RefactoringArguments {

	/**
	 * Creates new refactoring arguments.
	 */
	protected RefactoringArguments() {
	}
}
