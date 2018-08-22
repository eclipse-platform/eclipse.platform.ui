/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
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

import org.w3c.dom.css.CSSFontFaceRule;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;

public class CSSFontFaceRuleImpl extends CSSRuleImpl implements CSSFontFaceRule {

	public CSSFontFaceRuleImpl(CSSStyleSheet parentStyleSheet, CSSRule parentRule) {
		super(parentStyleSheet, parentRule);
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	/**
	 * @see org.w3c.dom.css.CSSRule.getType()
	 */
	@Override
	public short getType() {
		return CSSRule.FONT_FACE_RULE;
	}

	// W3C CSSFontFaceRule API methods

	/**
	 * @see org.w3c.dom.css.CSSFontFaceRule.getStyle()
	 */
	@Override
	public CSSStyleDeclaration getStyle() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	// Additional methods

	public void setStyle(CSSStyleDeclaration decl) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

}