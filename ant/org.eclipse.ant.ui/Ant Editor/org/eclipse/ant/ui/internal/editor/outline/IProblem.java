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
package org.eclipse.ant.ui.internal.editor.outline;

import org.eclipse.jface.text.IRegion;


public interface IProblem extends IRegion {

	/**
	 * Returns the problem code
	 *
	 * @return the problem code
	 */
	String getCode();

	/**
	 * Answer a localized, human-readable message string which describes the problem.
	 *
	 * @return a localized, human-readable message string which describes the problem
	 */
	String getMessage();

	/**
	 * Checks the severity to see if the Error bit is set.
	 *
	 * @return true if the Error bit is set for the severity, false otherwise
	 */
	boolean isError();

	/**
	 * Checks the severity to see if the Error bit is not set.
	 *
	 * @return true if the Error bit is not set for the severity, false otherwise
	 */
	boolean isWarning();

}
