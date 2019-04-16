/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
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

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPageRule;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;

public class CSSPageRuleImpl extends CSSRuleImpl implements CSSPageRule {

	private static final String NOT_YET_IMPLEMENTED = "NOT YET IMPLEMENTED"; //$NON-NLS-1$

	public CSSPageRuleImpl(CSSStyleSheet parentStyleSheet, CSSRule parentRule,
			String name, String pseudo_page) {
		super(parentStyleSheet, parentRule);
	}

	@Override
	public short getType() {
		return CSSRule.PAGE_RULE;
	}

	// W3C CSSPageRule API methods

	@Override
	public String getSelectorText() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public CSSStyleDeclaration getStyle() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public void setSelectorText(String arg0) throws DOMException {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	// Additional methods

	public void setStyle(CSSStyleDeclaration decl) {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}
}