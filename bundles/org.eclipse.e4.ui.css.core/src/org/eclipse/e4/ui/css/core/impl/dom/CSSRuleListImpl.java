/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.css.CSSFontFaceRule;
import org.w3c.dom.css.CSSImportRule;
import org.w3c.dom.css.CSSMediaRule;
import org.w3c.dom.css.CSSPageRule;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSUnknownRule;

public class CSSRuleListImpl implements CSSRuleList, Serializable {

	private List<CSSRule> ruleList;
	
	public CSSRuleListImpl() {
		super();
		this.ruleList = new ArrayList<CSSRule>();
	}
	
	// W3C CSSRuleList API methods
	
	/**
	 * @see org.w3c.dom.css.CSSRuleList.getLength()
	 */
	public int getLength() {
		return ruleList.size();
	}

	/**
	 * @see org.w3c.dom.css.CSSRuleList.item(int)
	 */
	public CSSRule item(int position) {
		return ruleList.get(position);
	}

	//Additional
	
	/**
	 * @throws IndexOutOfBoundsException
	 */
	public void remove(int position) {
		ruleList.remove(position);
	}
	
	public void add(CSSUnknownRule ir) {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	public void add(CSSMediaRule mr) {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	public void add(CSSPageRule pageRule) {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	public void add(CSSFontFaceRule fontFaceRule) {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	public void add(CSSImportRule ir) {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	public void add(CSSStyleRule rule) {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}


}
