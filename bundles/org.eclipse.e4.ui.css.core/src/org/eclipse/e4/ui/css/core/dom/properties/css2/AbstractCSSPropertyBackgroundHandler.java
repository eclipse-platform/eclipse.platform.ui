/*******************************************************************************
 * Copyright (c) 2008, 2022 Angelo Zerr and others.
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

/**
 * Abstract CSS property background which is enable to manage
 * apply CSS Property background, background-color, background-image...
 */
public abstract class AbstractCSSPropertyBackgroundHandler extends
AbstractCSSPropertyBackgroundCompositeHandler implements
ICSSPropertyBackgroundHandler {

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (property == null) {
			return false;
		}

		switch (property) {
		case "background":
			applyCSSPropertyBackground(element, value, pseudo, engine);
			break;
		case "background-attachment":
			applyCSSPropertyBackgroundAttachment(element, value, pseudo, engine);
			break;
		case "background-color":
			applyCSSPropertyBackgroundColor(element, value, pseudo, engine);
			break;
		case "background-image":
			applyCSSPropertyBackgroundImage(element, value, pseudo, engine);
			break;
		case "background-position":
			applyCSSPropertyBackgroundPosition(element, value, pseudo, engine);
			break;
		case "background-repeat":
			applyCSSPropertyBackgroundRepeat(element, value, pseudo, engine);
			break;
		default:
			return false;
		}
		return true;
	}

	@SuppressWarnings("removal")
	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		switch (property) {
		case "background-attachment":
			return retrieveCSSPropertyBackgroundAttachment(element, pseudo,
					engine);
		case "background-color":
			return retrieveCSSPropertyBackgroundColor(element, pseudo, engine);
		case "background-image":
			return retrieveCSSPropertyBackgroundImage(element, pseudo, engine);
		case "background-position":
			return retrieveCSSPropertyBackgroundPosition(element, pseudo,
					engine);
		case "background-repeat":
			return retrieveCSSPropertyBackgroundRepeat(element, pseudo, engine);
		default:
			return null;
		}
	}

	@Override
	public void applyCSSPropertyBackground(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		super.applyCSSPropertyComposite(element, "background", value, pseudo,
				engine);
	}

	@Override
	public void applyCSSPropertyBackgroundAttachment(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("background-attachment");
	}

	@Override
	public void applyCSSPropertyBackgroundColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("background-color");
	}

	@Override
	public void applyCSSPropertyBackgroundImage(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("background-image");
	}

	@Override
	public void applyCSSPropertyBackgroundPosition(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("background-position");
	}

	@Override
	public void applyCSSPropertyBackgroundRepeat(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("background-repeat");
	}
}
