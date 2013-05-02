/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import org.w3c.dom.css.CSSImportRule;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.stylesheets.MediaList;

public class CSSImportRuleImpl extends CSSRuleImpl implements CSSImportRule {
	
	String uri;
	MediaListImpl mediaList;
	
	public CSSImportRuleImpl(CSSStyleSheet parentStyleSheet, CSSRule parentRule,
			String uri, MediaListImpl mediaListImpl) {
		super(parentStyleSheet, parentRule);
		this.uri = uri;
		this.mediaList = mediaListImpl;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.CSSRule#getType()
	 */
	public short getType() {
		return CSSRule.IMPORT_RULE;
	}
	
	// W3C CSSImportRule API methods

	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.CSSImportRule#getHref()
	 */
	public String getHref() {
		return uri;
	}

	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.CSSImportRule#getMedia()
	 */
	public MediaList getMedia() {
		return mediaList;
	}

	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.CSSImportRule#getStyleSheet()
	 */
	public CSSStyleSheet getStyleSheet() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}
}