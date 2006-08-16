/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help;

/**
 * An <code>IIndexContribution</code> represents a set of index keywords and
 * their metadata.
 * 
 * @since 3.3
 */
public interface IIndexContribution {

	/**
	 * Returns a unique identifier for this contribution.
	 * 
	 * @return the contribution's unique identifier
	 */
	public String getId();
	
	/**
	 * Returns the locale for this contribution.
	 * 
	 * @return the contribution's locale
	 */
	public String getLocale();
	
	/**
	 * Returns the keyword index data for this contribution.
	 * 
	 * @return the contribution's keyword index data
	 */
	public IIndex getIndex();
}
