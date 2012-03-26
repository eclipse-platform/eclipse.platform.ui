package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSBackgroundModePropertyHandler extends
		AbstractCSSPropertySWTHandler {

	@Override
	protected void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			return;
		}
		if (control instanceof Composite) {
			Composite composite = (Composite) control;
			String stringValue = value.getCssText().toLowerCase();
			if ("default".equalsIgnoreCase(stringValue)) {
				composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
			} else if ("force".equalsIgnoreCase(stringValue)) {
				composite.setBackgroundMode(SWT.INHERIT_FORCE);
			} else if ("none".equalsIgnoreCase(stringValue)) {
				composite.setBackgroundMode(SWT.INHERIT_NONE);
			}
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		if (control instanceof Composite) {
			Composite composite = (Composite) control;
			switch (composite.getBackgroundMode()) {
			case SWT.INHERIT_DEFAULT:
				return "default";
			case SWT.INHERIT_FORCE:
				return "force";
			case SWT.INHERIT_NONE:
				return "none";
			}
		}
		return null;
	}

}
