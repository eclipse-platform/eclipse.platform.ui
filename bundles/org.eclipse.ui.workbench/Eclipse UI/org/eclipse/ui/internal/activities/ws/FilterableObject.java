/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

/**
 * Baseclass for objects that wish to respond to filtering states.
 * 
 * @since 3.0
 */
public class FilterableObject {

	/**
	 * Whether this object is filtering.
	 */
	private boolean filtering;

	/**
	 * Create a new instance with the supplied filtering state.
	 * 
	 * @param filtering
	 *            the initial filtering state.
	 */
	public FilterableObject(boolean filtering) {
		setFiltering(filtering);
	}

	/**
     * @return whether this object is currently set to filter its content
     *         based on activity enablement.
     */
	public boolean getFiltering() {
		return filtering;
	}

    /**
     * @param filtering
     *            whether this object should filter its content based on
     *            activity enablement.
     */
    public void setFiltering(boolean filtering) {
		this.filtering = filtering;
	}
}
