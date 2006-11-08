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
 * An <code>IndexContribution</code> represents either a complete or partial
 * keyword index, as well as its metadata.
 * 
 * @since 3.3
 */
public class IndexContribution extends Node {

	private static final String NAME = "indexContribution"; //$NON-NLS-1$
	private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	private static final String ATTRIBUTE_LOCALE = "locale"; //$NON-NLS-1$
	
	/**
	 * Constructs a new empty <code>IndexContribution</code>. Use the
	 * <code>set</code> methods to set the content. 
	 */
	public IndexContribution() {
		super();
		setNodeName(NAME);
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
	 * Returns the index data for this contribution. The format is the same as
	 * index XML file contributions, except the content is returned as
	 * <code>Node</code>s that mirror the XML structure.
	 * 
	 * @return the index data for this contribution
	 */
	public Node getIndex() {
		Node[] children = getChildNodes();
		if (children.length > 0) {
			return children[0];
		}
		return null;
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
	 * Assigns a unique identifier to this contribution.
	 * 
	 * @param id the contribution's unique identifier
	 */
	public void setId(String id) {
		setAttribute(ATTRIBUTE_ID, id);
	}

	/**
	 * Sets the index data for this contribution. The format is the same as
	 * index XML file contributions, except the content is returned as
	 * <code>Node</code>s that mirror the XML structure.
	 * 
	 * @param index the index data for this contribution
	 */
	public void setIndex(Node index) {
		Node[] children = getChildNodes();
		for (int i=0;i<children.length;++i) {
			removeChild(children[i]);
		}
		appendChild(index);
	}

	/**
	 * Sets the locale for this contribution.
	 * 
	 * @param locale the contribution's locale
	 */
	public void setLocale(String locale) {
		setAttribute(ATTRIBUTE_LOCALE, locale);
	}
}
