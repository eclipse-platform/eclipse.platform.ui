/*******************************************************************************
 *  Copyright (c) 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.e4.ui.widgets.ETabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyTabKeylineColorHandler extends AbstractCSSPropertySWTHandler {

	public static final ICSSPropertyHandler INSTANCE = new CSSPropertyTabKeylineColorHandler();

	public void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {

			if (control instanceof ETabFolder) {
				Color newColor = (Color) engine.convert(value, Color.class, control
						.getDisplay());
				
				((ETabFolder) control).setTabKeylineColor(newColor);
			}
		}
	}

	public String retrieveCSSProperty(Control control, String property, String pseudo, 
			CSSEngine engine) throws Exception {
		Color color = ((ETabFolder) control).getTabKeylineColor();
		return engine.convert(color, Color.class, null);
	}

}
