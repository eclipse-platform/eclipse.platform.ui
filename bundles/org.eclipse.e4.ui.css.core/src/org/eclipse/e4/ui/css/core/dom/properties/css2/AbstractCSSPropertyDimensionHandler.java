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
package org.eclipse.e4.ui.css.core.dom.properties.css2;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSSValue;

public abstract class AbstractCSSPropertyDimensionHandler implements ICSSPropertyDimensionHandler  {

	public void applyCSSPropertyHeight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		
	}

	public void applyCSSPropertyLineHeight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void applyCSSPropertyMaxHeight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void applyCSSPropertyMaxWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void applyCSSPropertyMinHeight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void applyCSSPropertyMinWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void applyCSSPropertyWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public String retrieveCSSPropertyHeight(Object widget, String property,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String retrieveCSSPropertyMaxHeight(Object widget, String property,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String retrieveCSSPropertyMinHeight(Object widget, String property,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String retrieveCSSPropertyMinWidth(Object widget, String property,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean applyCSSProperty(Object widget, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	public String retrieveCSSProperty(Object widget, String property,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
}
