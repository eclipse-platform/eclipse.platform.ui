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
	
	private String[] contextIds;

	/**
	 * TODO javadoc
	 */	
	public SimpleContextService() {
		super();
		this.contextIds = new String[0];
	}

	/**
	 * Returns the context ids.
	 *
	 * @return the context ids.
	 */
	public String[] getContextIds() {
		return (String[]) contextIds.clone();
	}

	/**
	 * Sets the context ids.
	 *
	 * @param contextIds the context ids.
	 * @throws IllegalArgumentException
	 */	
	public void setContextIds(String[] contextIds)
		throws IllegalArgumentException {
		if (contextIds == null)
			throw new IllegalArgumentException();
		
		contextIds = (String[]) contextIds.clone();

		for (int i = 0; i < contextIds.length; i++)
			if (contextIds[i] == null)
				throw new IllegalArgumentException();
	
		this.contextIds = contextIds;
		fireContextServiceChanged(); 
	}
}
