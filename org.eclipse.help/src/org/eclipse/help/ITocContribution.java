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
 * <p>
 * An <code>ITocContribution</code> represents either a complete or partial
 * table of contents, as well as its metadata.
 * </p>
 * <p>
 * IMPORTANT: This API is still subject to change in 3.3.
 * </p>
 * 
 * @since 3.3
 */
public interface ITocContribution {

	/**
	 * Returns the TOC's category id. Categories are used to organize similar
	 * books in categories.
	 * 
	 * @return the TOC's category id.
	 */
	public String getCategoryId();
	
	/**
	 * Returns the hrefs for any additional documents that are not in this TOC
	 * but are associated with it, and should be indexed for searching. A
	 * return value of <code>null</code> will be treated the same as an empty
	 * array.
	 * 
	 * @return any extra documents associated with the TOC
	 */
	public String[] getExtraDocuments();
	
	/**
	 * Returns a unique identifier for this contribution.
	 * 
	 * @return the contribution's unique identifier
	 */
	public String getId();
	
	/**
	 * Returns the target anchor at which to insert this contribution. The
	 * format is: [contribution_id]#[anchor_id]. For example, to insert the toc
	 * at the "reference_troubleshooting" anchor in the
	 * "/my.plugin.id/path/toc.xml" contribution, the target will be
	 * "/my.plugin.id/path/toc.xml#reference_troubleshooting". If not specified,
	 * returns null.
	 * 
	 * @return the target destination for this contribution, or null if none
	 */
	public String getLinkTo();
	
	/**
	 * Returns the locale for this contribution.
	 * 
	 * @return the contribution's locale
	 */
	public String getLocale();
	
	/**
	 * Returns the TOC data for this contribution.
	 * 
	 * @return the TOC data for this contribution
	 */
	public IToc getToc();
	
	/**
	 * Returns whether or not this is a top-level contribution (a book).
	 * 
	 * @return whether the contribution is top-level book
	 */
	public boolean isPrimary();
}
