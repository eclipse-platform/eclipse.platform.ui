/*******************************************************************************
 * Copyright (c) 2008, 2026 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import org.eclipse.e4.ui.css.core.dom.CSSPropertyList;
import org.eclipse.e4.ui.css.core.dom.ExtendedCSSRule;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

public class CSSStyleRuleImpl extends CSSRuleImpl implements CSSStyleRule, ExtendedCSSRule {

	private final Selectors.SelectorList selectors;
	private CSSStyleDeclaration styleDeclaration;

	public CSSStyleRuleImpl(CSSStyleSheet parentStyleSheet, CSSRule parentRule, Selectors.SelectorList selectors) {
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
		return selectors.text();
	}

	@Override
	public CSSStyleDeclaration getStyle() {
		return styleDeclaration;
	}

	@Override
	public void setSelectorText(String selectorText) throws DOMException {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}


	//----------------------------------------
	// Additional methods

	@Override
	public Selectors.SelectorList getSelectorList() {
		return selectors;
	}


	@Override
	public CSSPropertyList getCSSPropertyList() {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	public void setStyle(CSSStyleDeclaration styleDeclaration) {
		this.styleDeclaration = styleDeclaration;
	}

	@Override
	public String toString() {
		return getSelectorText();
	}
}
