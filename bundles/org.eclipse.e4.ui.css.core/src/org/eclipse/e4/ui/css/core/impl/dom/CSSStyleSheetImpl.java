/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import java.io.Serializable;

import org.eclipse.e4.ui.css.core.exceptions.DOMExceptionImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.stylesheets.MediaList;
import org.w3c.dom.stylesheets.StyleSheet;

public class CSSStyleSheetImpl extends AbstractCSSNode implements CSSStyleSheet, Serializable {

	private CSSRuleList rules = null;
	
	// CSSStyleSheet API methods
	
	/**
	 * @see org.w3c.dom.css.CSSStyleSheet.deleteRule(int)
	 */
	public void deleteRule(int position) throws DOMException {
		try {
			((CSSRuleListImpl) rules).remove(position);			
		} catch (IndexOutOfBoundsException ex) {
			throw new DOMExceptionImpl(DOMException.INDEX_SIZE_ERR, DOMExceptionImpl.ARRAY_OUT_OF_BOUNDS, ex.getMessage());
		}
	}

	/**
	 * @see org.w3c.dom.css.CSSStyleSheet.getCssRules()
	 */
	public CSSRuleList getCssRules() {
		return rules;
	}

	/**
	 * @see org.w3c.dom.css.CSSStyleSheet.getOwnerRule()
	 */
	public CSSRule getOwnerRule() {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	/**
	 * @see org.w3c.dom.css.CSSStyleSheet.insertRule(String, int)
	 */
	public int insertRule(String arg0, int arg1) throws DOMException {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	// Additional
	
	public boolean getDisabled() {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	public String getHref() {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	public MediaList getMedia() {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	public Node getOwnerNode() {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	public StyleSheet getParentStyleSheet() {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	public String getTitle() {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	public String getType() {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	public void setDisabled(boolean arg0) {
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");				
	}

	public void setRuleList(CSSRuleList rules) {
		this.rules = rules;
	}

}
