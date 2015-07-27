/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
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
import org.eclipse.e4.ui.css.core.exceptions.UnsupportedPropertyException;
import org.w3c.dom.css.CSSValue;

public abstract class AbstractCSSPropertyClassificationHandler implements
		ICSSPropertyClassificationHandler {

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if ("clear".equals(property)) {
			applyCSSPropertyClear(element, value, pseudo, engine);
		}
		if ("cursor".equals(property)) {
			applyCSSPropertyCursor(element, value, pseudo, engine);
		}
		if ("display".equals(property)) {
			applyCSSPropertyDisplay(element, value, pseudo, engine);
		}
		if ("float".equals(property)) {
			applyCSSPropertyFloat(element, value, pseudo, engine);
		}
		if ("position".equals(property)) {
			applyCSSPropertyPosition(element, value, pseudo, engine);
		}
		if ("visibility".equals(property)) {
			applyCSSPropertyVisibility(element, value, pseudo, engine);
		}
		return false;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		if ("clear".equals(property)) {
			return retrieveCSSPropertyClear(element, pseudo, engine);
		}
		if ("cursor".equals(property)) {
			return retrieveCSSPropertyCursor(element, pseudo, engine);
		}
		if ("display".equals(property)) {
			return retrieveCSSPropertyDisplay(element, pseudo, engine);
		}
		if ("float".equals(property)) {
			return retrieveCSSPropertyFloat(element, pseudo, engine);
		}
		if ("position".equals(property)) {
			return retrieveCSSPropertyPosition(element, pseudo, engine);
		}
		if ("visibility".equals(property)) {
			return retrieveCSSPropertyVisibility(element, pseudo, engine);
		}
		return null;
	}

	@Override
	public void applyCSSPropertyClear(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("clear");
	}

	@Override
	public void applyCSSPropertyCursor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("cursor");
	}

	@Override
	public void applyCSSPropertyDisplay(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("display");
	}

	@Override
	public void applyCSSPropertyFloat(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("float");
	}

	@Override
	public void applyCSSPropertyPosition(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("position");
	}

	@Override
	public void applyCSSPropertyVisibility(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("visibility");
	}

	@Override
	public String retrieveCSSPropertyClear(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyCursor(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyDisplay(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyFloat(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyPosition(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyVisibility(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

}
