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
package org.eclipse.e4.ui.css.xml.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyBackgroundHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyBackgroundHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

/**
 * 
 */
public class CSSPropertyBackgroundXMLHandler extends
		AbstractCSSPropertyBackgroundHandler {

	public final static ICSSPropertyBackgroundHandler INSTANCE = new CSSPropertyBackgroundXMLHandler();

	public boolean applyCSSProperty(Object node, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (node instanceof Element) {
			super.applyCSSProperty((Element) node, property, value, pseudo,
					engine);
			return true;
		}
		return false;
	}

	public String retrieveCSSPropertyBackgroundAttachment(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String retrieveCSSPropertyBackgroundColor(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String retrieveCSSPropertyBackgroundImage(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String retrieveCSSPropertyBackgroundPosition(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String retrieveCSSPropertyBackgroundRepeat(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
