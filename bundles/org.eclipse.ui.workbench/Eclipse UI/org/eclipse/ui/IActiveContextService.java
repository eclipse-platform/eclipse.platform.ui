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

package org.eclipse.ui;

/**
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * @since 3.0
 */
public interface IActiveContextService {

	/**
	 * Returns the active contexts.
	 * 
	 * @return the active contexts.
	 */
	String[] getActiveContexts();
	
	/**
	 * Sets the active contexts.
	 *
	 * @param ids the active contexts.
	 */	
	void setActiveContexts(String[] activeContexts)
		throws IllegalArgumentException;
}
