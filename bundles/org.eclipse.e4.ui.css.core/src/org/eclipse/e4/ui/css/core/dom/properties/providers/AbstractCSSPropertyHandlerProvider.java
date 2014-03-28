/*******************************************************************************
 * Copyright (c) 2008, 2012 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom.properties.providers;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandlerProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * Abstract CSS Property handler.
 */
public abstract class AbstractCSSPropertyHandlerProvider implements
		ICSSPropertyHandlerProvider {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandlerProvider#getDefaultCSSStyleDeclaration(org.eclipse.e4.ui.css.core.engine.CSSEngine,
	 *      java.lang.Object, org.w3c.dom.css.CSSStyleDeclaration)
	 */
	@Override
	public CSSStyleDeclaration getDefaultCSSStyleDeclaration(CSSEngine engine,
			Object widget, CSSStyleDeclaration newStyle, String pseudoE)
			throws Exception {
		Element elt = engine.getElement(widget);
		if (elt != null) {
			if (elt instanceof CSSStylableElement) {
				CSSStylableElement stylableElement = (CSSStylableElement) elt;
				return getDefaultCSSStyleDeclaration(engine, stylableElement,
						newStyle, pseudoE);
			}
		}
		return null;
	}

	/**
	 * Return the CSS property from the CSS <code>propertyName</code> of the
	 * <code>stylableElement</code>.
	 *
	 * @param engine
	 * @param stylableElement
	 * @param propertyName
	 * @return
	 */
	protected String getCSSPropertyStyle(CSSEngine engine,
			CSSStylableElement stylableElement, String propertyName,
			String pseudo) {
		String propertyValue = engine.retrieveCSSProperty(stylableElement,
				propertyName, pseudo);
		if (propertyValue == null) {
			return null;
		}
		StringBuffer style = new StringBuffer();
		style.append(propertyName);
		style.append(":");
		style.append(propertyValue);
		style.append(";");
		return style.toString();
	}

	/**
	 * Return the default CSS style declaration of the
	 * {@link CSSStylableElement} <code>stylableElement</code> before apply
	 * the <code>newStyle</code> {@link CSSStyleDeclaration}.
	 *
	 * @param engine
	 * @param stylableElement
	 * @param newStyle
	 * @return
	 * @throws Exception
	 */
	protected abstract CSSStyleDeclaration getDefaultCSSStyleDeclaration(
			CSSEngine engine, CSSStylableElement stylableElement,
			CSSStyleDeclaration newStyle, String pseudoE) throws Exception;
}
