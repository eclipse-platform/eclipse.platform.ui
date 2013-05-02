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
	public short getType() {
		return CSSRule.FONT_FACE_RULE;
	}
	
	// W3C CSSFontFaceRule API methods
	
	/**
	 * @see org.w3c.dom.css.CSSFontFaceRule.getStyle()
	 */
	public CSSStyleDeclaration getStyle() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	// Additional methods
	
	public void setStyle(CSSStyleDeclarationImpl decl) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

}