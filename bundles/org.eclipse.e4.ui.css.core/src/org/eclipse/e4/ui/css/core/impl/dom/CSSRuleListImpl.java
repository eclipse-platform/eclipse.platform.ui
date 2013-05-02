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

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;

public class CSSRuleListImpl implements CSSRuleList {

	private List<CSSRule> ruleList;
	
	public CSSRuleListImpl() {
		super();
		this.ruleList = new ArrayList<CSSRule>();
	}
	
	// W3C CSSRuleList API methods
	
	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.CSSRuleList#getLength()
	 */
	public int getLength() {
		return ruleList.size();
	}

	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.CSSRuleList#item(int)
	 */
	public CSSRule item(int position) {
		return ruleList.get(position);
	}

	//Additional
	
	/**
	 * @throws IndexOutOfBoundsException
	 */	
	public void add(CSSRule rule) {
		ruleList.add(rule);
	}

	/**
	 * @throws IndexOutOfBoundsException
	 */	
	public void remove(int position) {
		ruleList.remove(position);
	}
}
