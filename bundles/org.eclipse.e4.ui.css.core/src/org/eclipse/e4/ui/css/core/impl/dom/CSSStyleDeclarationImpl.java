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

import org.eclipse.e4.ui.css.core.dom.CSSProperty;
import org.eclipse.e4.ui.css.core.dom.CSSPropertyList;
import org.eclipse.e4.ui.css.core.exceptions.DOMExceptionImpl;
import org.w3c.css.sac.Selector;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;

public class CSSStyleDeclarationImpl extends AbstractCSSNode implements CSSStyleDeclaration, Serializable {

	protected boolean readOnly;

	public CSSStyleDeclarationImpl(Object object) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	// W3C CSSStyleDeclaration API methods
	
	/**
	 * @see org.w3c.dom.css.CSSStyleDeclaration.getCSSText()
	 */
	public String getCssText() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	/**
	 * @see org.w3c.dom.css.CSSStyleDeclaration.getLength()
	 */
	public int getLength() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	/**
	 * @see org.w3c.dom.css.CSSStyleDeclaration.getParentRule()
	 */
	public CSSRule getParentRule() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	/**
	 * @see org.w3c.dom.css.CSSStyleDeclaration.getPropertyCSSValue(String)
	 */
	public CSSValue getPropertyCSSValue(String propertyName) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	/**
	 * @see org.w3c.dom.css.CSSStyleDeclaration.getPropertyPriority(String)
	 */
	public String getPropertyPriority(String propertyName) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	/**
	 * @see org.w3c.dom.css.CSSStyleDeclaration.getPropertyValue(String)
	 */
	public String getPropertyValue(String propertyName) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	/**
	 * @see org.w3c.dom.css.CSSStyleDeclaration.item(int)
	 */
	public String item(int index) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	/**
	 * @see org.w3c.dom.css.CSSStyleDeclaration.removeProperty(String)
	 */
	public String removeProperty(String propertyName) throws DOMException {
		if(readOnly)
			throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR, DOMExceptionImpl.NO_MODIFICATION_ALLOWED_ERROR);
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	/**
	 * @see org.w3c.dom.css.CSSStyleDeclaration.setCssText(String)
	 */
	public void setCssText(String cssText) throws DOMException {
		if(readOnly)
			throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR, DOMExceptionImpl.NO_MODIFICATION_ALLOWED_ERROR);
		// TODO Auto-generated method stub
		// TODO throws SYNTAX_ERR if cssText is unparsable
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");		
	}

	/**
	 * @see org.w3c.dom.css.CSSStyleDeclaration.setProperty(String, String, String)
	 */
	public void setProperty(String propertyName, String value, String priority) throws DOMException {
		if(readOnly)
			throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR, DOMExceptionImpl.NO_MODIFICATION_ALLOWED_ERROR);
		// TODO Auto-generated method stub
		// TODO throws SYNTAX_ERR if value is unparsable
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	
	// Additional
	
	public void addProperty(CSSProperty property) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	public CSSPropertyList getCSSPropertyList() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	public Selector getSelector() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}	
	
	protected void setReadOnly(boolean readOnly) {
		//TODO ViewCSS.getComputedStyle() should provide a read only access to the computed values
		this.readOnly = readOnly;
	}

}
