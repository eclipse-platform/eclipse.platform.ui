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
package org.eclipse.team.tests.ccvs.ui;


/**
 * Generic object filter mechanism.
 */
public interface ICriteria {
	/**
	 * Returns true if the candidate object satisfies the specified
	 * criteria value according to a particular algorithm.
	 */
	public boolean test(Object candidate, Object value);
}
