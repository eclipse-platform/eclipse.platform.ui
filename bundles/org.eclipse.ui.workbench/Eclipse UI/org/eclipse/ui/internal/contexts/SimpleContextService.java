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

package org.eclipse.ui.internal.contexts;

/**
 * <p>
 * TODO javadoc
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public final class SimpleContextService extends AbstractContextService {
	
	private String[] activeContextIds;

	/**
	 * TODO javadoc
	 */	
	public SimpleContextService() {
		super();
		this.activeContextIds = new String[0];
	}

	/**
	 * Returns the active context ids.
	 *
	 * @return the active context ids.
	 */
	public String[] getActiveContextIds() {
		return (String[]) activeContextIds.clone();
	}

	/**
	 * Sets the active context ids.
	 *
	 * @param activeContextIds the active context ids.
	 * @throws IllegalArgumentException
	 */	
	public void setActiveContextIds(String[] activeContextIds)
		throws IllegalArgumentException {
		if (activeContextIds == null)
			throw new IllegalArgumentException();
		
		activeContextIds = (String[]) activeContextIds.clone();

		for (int i = 0; i < activeContextIds.length; i++)
			if (activeContextIds[i] == null)
				throw new IllegalArgumentException();
	
		this.activeContextIds = activeContextIds;
		fireContextServiceChanged(); 
	}
}
