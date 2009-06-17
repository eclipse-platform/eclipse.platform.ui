package org.eclipse.e4.ui.css.core.dom.properties.css2;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSSValue;

public abstract class AbstractCSSPropertyMarginHandler implements ICSSPropertyMarginHandler {
	
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if ("margin".equals(property))
			applyCSSPropertyMargin(element, value, pseudo, engine);
		else if ("margin-top".equals(property))
			applyCSSPropertyMarginTop(element, value, pseudo, engine);
		else if ("margin-right".equals(property))
			applyCSSPropertyMarginRight(element, value, pseudo, engine);
		else if ("margin-bottom".equals(property))
			applyCSSPropertyMarginBottom(element, value, pseudo, engine);
		else if ("margin-left".equals(property))
			applyCSSPropertyMarginLeft(element, value, pseudo, engine);
		return false;
	}
	
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		if ("margin".equals(property)) {
			return retrieveCSSPropertyMargin(element, pseudo, engine);
		}
		if ("margin-top".equals(property)) {
			return retrieveCSSPropertyMarginTop(element, pseudo, engine);
		}
		if ("margin-right".equals(property)) {
			return retrieveCSSPropertyMarginRight(element, pseudo, engine);
		}
		if ("margin-bottom".equals(property)) {
			return retrieveCSSPropertyMarginBottom(element, pseudo, engine);
		}
		if ("margin-left".equals(property)) {
			return retrieveCSSPropertyMarginLeft(element, pseudo, engine);
		}
		return null;
	}
}
