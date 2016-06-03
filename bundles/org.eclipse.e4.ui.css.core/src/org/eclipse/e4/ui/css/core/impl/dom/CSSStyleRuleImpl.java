/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
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

import org.eclipse.e4.ui.css.core.dom.CSSPropertyList;
import org.eclipse.e4.ui.css.core.dom.ExtendedCSSRule;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

public class CSSStyleRuleImpl extends CSSRuleImpl implements CSSStyleRule, ExtendedCSSRule {

	private SelectorList selectors;
	private CSSStyleDeclaration styleDeclaration;

	public CSSStyleRuleImpl(CSSStyleSheet parentStyleSheet, CSSRule parentRule,
			SelectorList selectors) {
		super(parentStyleSheet, parentRule);
		this.selectors = selectors;
	}

	//----------------------------------------
	// W3C CSSRule API methods

	@Override
	public short getType() {
		return CSSRule.STYLE_RULE;
	}
	// ----------------------------------------
	// W3C CSSStyleRule API methods

	@Override
	public String getCssText() {
		return getSelectorText() + " { " + getStyle().getCssText() + " }";
	}

	//----------------------------------------
	// W3C CSSStyleRule API methods

	@Override
	public String getSelectorText() {
		StringBuilder sb = new StringBuilder();
		for (int selID = 0; selID < getSelectorList().getLength(); selID++) {
			Selector item = getSelectorList().item(selID);
			sb.append(item.toString());
			sb.append(", ");
		}
		if (getSelectorList().getLength() > 0) {
			sb.delete(sb.length() - 2, sb.length());
		}

		return sb.toString();
	}

	@Override
	public CSSStyleDeclaration getStyle() {
		return styleDeclaration;
	}

	@Override
	public void setSelectorText(String selectorText) throws DOMException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}


	//----------------------------------------
	// Additional methods

	@Override
	public SelectorList getSelectorList() {
		return selectors;
	}


	@Override
	public CSSPropertyList getCSSPropertyList() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	public void setStyle(CSSStyleDeclaration styleDeclaration) {
		this.styleDeclaration = styleDeclaration;
	}
}
