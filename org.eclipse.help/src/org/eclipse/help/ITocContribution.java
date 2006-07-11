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
 * An <code>ITocContribution</code> represents a complete top-level TOC (a book)
 * and its metadata.
 * 
 * This interface is intended to be implemented by clients.
 * 
 * @since 3.3
 */
public interface ITocContribution {

	/**
	 * Returns the TOC data for this contribution. This will be called only
	 * once per session.
	 * 
	 * @return the TOC data for this contribution
	 */
	public IToc getToc();
	
	/**
	 * Returns a unique identifier for this contribution.
	 * 
	 * @return the contribution's unique identifier
	 */
	public String getId();
	
	/**
	 * Returns the TOC's category id. Categories are used to organize similar
	 * books in categories.
	 * 
	 * @return the TOC's category id.
	 */
	public String getCategoryId();
	
	/**
	 * Returns the locale for this contribution.
	 * 
	 * @return the contribution's locale
	 */
	public String getLocale();
}
