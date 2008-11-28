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
package org.eclipse.e4.ui.css.swt.properties.css2.lazy.text;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractConvertedCSSPropertySWTHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;

public class CSSPropertyColorHandler extends
		AbstractConvertedCSSPropertySWTHandler {

	protected void applyCSSPropertyValue(Control control, String property,
			Object value, String pseudo, CSSEngine engine) throws Exception {
		if (value instanceof Color) {
			Color color = (Color) value;
			control.setForeground(color);
		}
	}

	public String retrieveCSSProperty(Control control, String property, String pseudo, 
			CSSEngine engine) throws Exception {
		Color color = control.getForeground();
		return retrieveCSSProperty(color, pseudo, engine);
	}

	protected Object getToType(Object value) {
		return Color.class;
	}
}
