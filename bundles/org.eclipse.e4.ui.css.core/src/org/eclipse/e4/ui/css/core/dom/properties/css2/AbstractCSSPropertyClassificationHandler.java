/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
		if (property == null) {
			return false;
		}

		switch (property) {
		case "clear":
			applyCSSPropertyClear(element, value, pseudo, engine);
			break;
		case "cursor":
			applyCSSPropertyCursor(element, value, pseudo, engine);
			break;
		case "display":
			applyCSSPropertyDisplay(element, value, pseudo, engine);
			break;
		case "float":
			applyCSSPropertyFloat(element, value, pseudo, engine);
			break;
		case "position":
			applyCSSPropertyPosition(element, value, pseudo, engine);
			break;
		case "visibility":
			applyCSSPropertyVisibility(element, value, pseudo, engine);
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		if (property == null) {
			return null;
		}

		switch (property) {
		case "clear":
			return retrieveCSSPropertyClear(element, pseudo, engine);
		case "cursor":
			return retrieveCSSPropertyCursor(element, pseudo, engine);
		case "display":
			return retrieveCSSPropertyDisplay(element, pseudo, engine);
		case "float":
			return retrieveCSSPropertyFloat(element, pseudo, engine);
		case "position":
			return retrieveCSSPropertyPosition(element, pseudo, engine);
		case "visibility":
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
