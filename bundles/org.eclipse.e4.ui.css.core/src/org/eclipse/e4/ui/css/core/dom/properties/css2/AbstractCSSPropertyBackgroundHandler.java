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
		if ("background".equals(property)) {
			applyCSSPropertyBackground(element, value, pseudo, engine);
		}
		if ("background-attachment".equals(property)) {
			applyCSSPropertyBackgroundAttachment(element, value, pseudo, engine);
		}
		if ("background-color".equals(property)) {
			applyCSSPropertyBackgroundColor(element, value, pseudo, engine);
		}
		if ("background-image".equals(property)) {
			applyCSSPropertyBackgroundImage(element, value, pseudo, engine);
		}
		if ("background-position".equals(property)) {
			applyCSSPropertyBackgroundPosition(element, value, pseudo, engine);
		}
		if ("background-repeat".equals(property)) {
			applyCSSPropertyBackgroundRepeat(element, value, pseudo, engine);
		}
		return false;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		if ("background-attachment".equals(property)) {
			return retrieveCSSPropertyBackgroundAttachment(element, pseudo,
					engine);
		}
		if ("background-color".equals(property)) {
			return retrieveCSSPropertyBackgroundColor(element, pseudo, engine);
		}
		if ("background-image".equals(property)) {
			return retrieveCSSPropertyBackgroundImage(element, pseudo, engine);
		}
		if ("background-position".equals(property)) {
			return retrieveCSSPropertyBackgroundPosition(element, pseudo,
					engine);
		}
		if ("background-repeat".equals(property)) {
			return retrieveCSSPropertyBackgroundRepeat(element, pseudo, engine);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.css.core.dom.properties.css2.ICSSPropertyBackgroundHandler#applyCSSPropertyBackground(java.lang.Object,
	 *      org.w3c.dom.css.CSSValue, java.lang.String,
	 *      org.eclipse.e4.css.core.engine.CSSEngine)
	 */
	@Override
	public void applyCSSPropertyBackground(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		super.applyCSSPropertyComposite(element, "background", value, pseudo,
				engine);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.css.core.dom.properties.css2.ICSSPropertyBackgroundHandler#applyCSSPropertyBackgroundAttachment(java.lang.Object,
	 *      org.w3c.dom.css.CSSValue, java.lang.String,
	 *      org.eclipse.e4.css.core.engine.CSSEngine)
	 */
	@Override
	public void applyCSSPropertyBackgroundAttachment(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("background-attachment");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.css.core.dom.properties.css2.ICSSPropertyBackgroundHandler#applyCSSPropertyBackgroundColor(java.lang.Object,
	 *      org.w3c.dom.css.CSSValue, java.lang.String,
	 *      org.eclipse.e4.css.core.engine.CSSEngine)
	 */
	@Override
	public void applyCSSPropertyBackgroundColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("background-color");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.css.core.dom.properties.css2.ICSSPropertyBackgroundHandler#applyCSSPropertyBackgroundImage(java.lang.Object,
	 *      org.w3c.dom.css.CSSValue, java.lang.String,
	 *      org.eclipse.e4.css.core.engine.CSSEngine)
	 */
	@Override
	public void applyCSSPropertyBackgroundImage(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("background-image");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.css.core.dom.properties.css2.ICSSPropertyBackgroundHandler#applyCSSPropertyBackgroundPosition(java.lang.Object,
	 *      org.w3c.dom.css.CSSValue, java.lang.String,
	 *      org.eclipse.e4.css.core.engine.CSSEngine)
	 */
	@Override
	public void applyCSSPropertyBackgroundPosition(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("background-position");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.css.core.dom.properties.css2.ICSSPropertyBackgroundHandler#applyCSSPropertyBackgroundRepeat(java.lang.Object,
	 *      org.w3c.dom.css.CSSValue, java.lang.String,
	 *      org.eclipse.e4.css.core.engine.CSSEngine)
	 */
	@Override
	public void applyCSSPropertyBackgroundRepeat(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("background-repeat");
	}
}
