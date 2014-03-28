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

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPageRule;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;

public class CSSPageRuleImpl extends CSSRuleImpl implements CSSPageRule {

	public CSSPageRuleImpl(CSSStyleSheet parentStyleSheet, CSSRule parentRule,
			String name, String pseudo_page) {
		super(parentStyleSheet, parentRule);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.CSSRule#getType()
	 */
	@Override
	public short getType() {
		return CSSRule.PAGE_RULE;
	}

	// W3C CSSPageRule API methods

	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.CSSPageRule#getSelectorText()
	 */
	@Override
	public String getSelectorText() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.CSSPageRule#getStyle()
	 */
	@Override
	public CSSStyleDeclaration getStyle() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.CSSPageRule#setSelectorText(String)
	 */
	@Override
	public void setSelectorText(String arg0) throws DOMException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	// Additional methods

	public void setStyle(CSSStyleDeclarationImpl decl) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}
}