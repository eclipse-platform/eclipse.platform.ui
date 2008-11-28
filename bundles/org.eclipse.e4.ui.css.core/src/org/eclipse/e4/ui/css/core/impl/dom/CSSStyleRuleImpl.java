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

import org.eclipse.e4.ui.css.core.dom.CSSPropertyList;
import org.eclipse.e4.ui.css.core.dom.ExtendedCSSRule;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

public class CSSStyleRuleImpl extends AbstractCSSNode implements CSSStyleRule,
ExtendedCSSRule, Serializable {
	PLACEHOLDER; // this class is a stub, needs to be written

	public String getSelectorText() {
		// TODO Auto-generated method stub
		return null;
	}

	public CSSStyleDeclaration getStyle() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSelectorText(String arg0) throws DOMException {
		// TODO Auto-generated method stub
		
	}

	public String getCssText() {
		// TODO Auto-generated method stub
		return null;
	}

	public CSSRule getParentRule() {
		// TODO Auto-generated method stub
		return null;
	}

	public CSSStyleSheet getParentStyleSheet() {
		// TODO Auto-generated method stub
		return null;
	}

	public short getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setCssText(String arg0) throws DOMException {
		// TODO Auto-generated method stub
		
	}

	public CSSPropertyList getCSSPropertyList() {
		// TODO Auto-generated method stub
		return null;
	}

	public SelectorList getSelectorList() {
		// TODO Auto-generated method stub
		return null;
	}
}
