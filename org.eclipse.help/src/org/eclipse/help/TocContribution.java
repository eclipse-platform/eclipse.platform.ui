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

import org.eclipse.help.internal.toc.Toc;

/**
 * A <code>TocContribution</code> represents either a complete or partial
 * table of contents, as well as its metadata.
 * 
 * @since 3.3
 */
public class TocContribution extends Node {

	private static final String NAME = "tocContribution"; //$NON-NLS-1$
	private static final String ELEMENT_EXTRA_DOC = "extraDoc"; //$NON-NLS-1$
	private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	private static final String ATTRIBUTE_CATEGORY_ID = "categoryId"; //$NON-NLS-1$
	private static final String ATTRIBUTE_CONTRIBUTOR_ID = "contributorId"; //$NON-NLS-1$
	private static final String ATTRIBUTE_LOCALE = "locale"; //$NON-NLS-1$
	private static final String ATTRIBUTE_IS_PRIMARY = "isPrimary"; //$NON-NLS-1$
	private static final String ATTRIBUTE_HREF = "href"; //$NON-NLS-1$

	/**
	 * Constructs a new empty <code>TocContribution</code>. Use the
	 * <code>set</code> methods to set the content. 
	 */
	public TocContribution() {
		super();
		setNodeName(NAME);
	}
	
	/**
	 * Returns the contribution's category id. Contributions with the same
	 * category id will be grouped together.
	 * 
	 * @return the contribution's category id.
	 */
	public String getCategoryId() {
		return getAttribute(ATTRIBUTE_CATEGORY_ID);
	}
	
	/**
	 * Returns the symbolic name of the bundle that made this contribution,
	 * e.g. "org.eclipse.help"
	 * 
	 * @return the contributor id, e.g. "org.eclipse.help"
	 */
	public String getContributorId() {
		return getAttribute(ATTRIBUTE_CONTRIBUTOR_ID);
	}
	
	/**
	 * Returns the hrefs for any additional documents that are not in this TOC
	 * but are associated with it, and should be indexed for searching.
	 * 
	 * @return any extra documents associated with the TOC
	 */
	public String[] getExtraDocuments() {
		Node[] children = getChildNodes();
		if (children.length > 0) {
			boolean hasToc = Toc.NAME.equals(children[0].getNodeName());
			String[] extraDocuments = new String[hasToc ? children.length - 1 : children.length];
			for (int i=0;i<extraDocuments.length;++i) {
				extraDocuments[i] = children[hasToc ? i + 1 : i].getAttribute(ATTRIBUTE_HREF);
			}
			return extraDocuments;
		}
		return new String[0];
	}
	
	/**
	 * Returns a unique identifier for this contribution.
	 * 
	 * @return the contribution's unique identifier
	 */
	public String getId() {
		return getAttribute(ATTRIBUTE_ID);
	}
	
	/**
	 * Returns the locale for this contribution.
	 * 
	 * @return the contribution's locale
	 */
	public String getLocale() {
		return getAttribute(ATTRIBUTE_LOCALE);
	}
	
	/**
	 * Returns the TOC data for this contribution. The format is the same as
	 * TOC XML file contributions, except the content is returned as
	 * <code>Node</code>s that mirror the XML structure.
	 * 
	 * @return the TOC data for this contribution
	 */
	public Node getToc() {
		Node[] children = getChildNodes();
		if (children.length > 0 && Toc.NAME.equals(children[0].getNodeName())) {
			return children[0];
		}
		return null;
	}
	
	/**
	 * Returns whether or not this is a top-level contribution (a book).
	 * 
	 * @return whether the contribution is top-level book
	 */
	public boolean isPrimary() {
		return String.valueOf(true).equalsIgnoreCase(getAttribute(ATTRIBUTE_IS_PRIMARY));
	}
	
	/**
	 * Sets the contribution's category id. Contributions with the same
	 * category id will be grouped together.
	 * 
	 * param categoryId the contribution's category id.
	 */
	public void setCategoryId(String categoryId) {
		setAttribute(ATTRIBUTE_CATEGORY_ID, categoryId);
	}
	
	/**
	 * Sets the symbolic name of the bundle that made this contribution,
	 * e.g. "org.eclipse.help"
	 * 
	 * @param contributorId the contributor id, e.g. "org.eclipse.help"
	 */
	public void setContributorId(String contributorId) {
		setAttribute(ATTRIBUTE_CONTRIBUTOR_ID, contributorId);
	}
	
	/**
	 * Sets the hrefs for any additional documents that are not in this TOC
	 * but are associated with it, and should be indexed for searching.
	 * 
	 * @param extraDocuments any extra documents associated with the TOC
	 */
	public void setExtraDocuments(String[] extraDocuments) {
		// remove existing extra documents, if exists
		Node[] children = getChildNodes();
		if (children.length > 0) {
			boolean hasToc = Toc.NAME.equals(children[0].getNodeName());
			for (int i=hasToc?1:0;i<children.length;++i) {
				removeChild(children[i]);
			}
		}
		// add new nodes
		for (int i=0;i<extraDocuments.length;++i) {
			Node extraDoc = new Node();
			extraDoc.setNodeName(ELEMENT_EXTRA_DOC);
			extraDoc.setAttribute(ATTRIBUTE_HREF, extraDocuments[i]);
			appendChild(extraDoc);
		}
	}
	
	/**
	 * Assigns a unique identifier to this contribution.
	 * 
	 * @param id the contribution's unique identifier
	 */
	public void setId(String id) {
		setAttribute(ATTRIBUTE_ID, id);
	}
	
	/**
	 * Sets the locale for this contribution.
	 * 
	 * @param locale the contribution's locale
	 */
	public void setLocale(String locale) {
		setAttribute(ATTRIBUTE_LOCALE, locale);
	}
	
	/**
	 * Sets whether or not this is a top-level contribution (a book).
	 * 
	 * @param isPrimary whether the contribution is top-level book
	 */
	public void setPrimary(boolean isPrimary) {
		setAttribute(ATTRIBUTE_IS_PRIMARY, String.valueOf(isPrimary));
	}
	
	/**
	 * Sets the TOC data for this contribution. The format is the same as
	 * TOC XML file contributions, except the content is returned as
	 * <code>Node</code>s that mirror the XML structure.
	 * 
	 * @param toc the TOC data for this contribution
	 */
	public void setToc(Node toc) {
		Node[] children = getChildNodes();
		if (children.length > 0) {
			insertBefore(toc, children[0]);
			if (Toc.NAME.equals(children[0].getNodeName())) {
				removeChild(children[0]);
			}
		}
		else {
			appendChild(toc);
		}
	}
}
