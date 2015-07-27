/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import org.eclipse.e4.ui.css.core.exceptions.DOMExceptionImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleSheet;

public abstract class CSSRuleImpl extends AbstractCSSNode implements CSSRule {

	private CSSStyleSheet parentStyleSheet = null;  // null allowed
	private CSSRule parentRule = null;  // null allowed
	private boolean readOnly;

	//TODO who sets readOnly?  Seems should be ViewCSSImpl.getComputedStyle(Element,String)

	public CSSRuleImpl(CSSStyleSheet parentStyleSheet, CSSRule parentRule) {
		super();
		this.parentStyleSheet = parentStyleSheet;
		this.parentRule = parentRule;
	}

	// W3C CSSRule API methods

	@Override
	public String getCssText() {
		// TODO Auto-generated constructor stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	@Override
	public CSSStyleSheet getParentStyleSheet() {
		return parentStyleSheet;
	}

	@Override
	public CSSRule getParentRule() {
		return parentRule;
	}

	@Override
	abstract public short getType();

	@Override
	public void setCssText(String cssText) throws DOMException {
		if(readOnly)
			throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR, DOMExceptionImpl.NO_MODIFICATION_ALLOWED_ERROR);
		// TODO Auto-generated method stub
		// TODO throws SYNTAX_ERR if cssText is unparsable
		// TODO throws INVALID_MODIFICATION_ERR: Raised if the specified CSS string value represents a different type of rule than the current one.
		// TODO throws HIERARCHY_REQUEST_ERR: Raised if the rule cannot be inserted at this point in the style sheet.
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}
}
