package org.eclipse.ui.internal.forms.css.properties.css2;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyExpandableCompositeHandler extends AbstractCSSPropertySWTHandler {

	private static final String TITLE_BAR_FOREGROUND = "swt-titlebar-color"; //$NON-NLS-1$

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (!(control instanceof ExpandableComposite)) {
			return;
		}
		if (TITLE_BAR_FOREGROUND.equalsIgnoreCase(property)) {
			if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				Color newColor = (Color) engine.convert(value, Color.class, control.getDisplay());
				((ExpandableComposite) control).setTitleBarForeground(newColor);
			}
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {
		return null;
	}

}
