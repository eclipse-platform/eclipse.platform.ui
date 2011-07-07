/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.workbench.addons.perspectiveswitcher;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.e4.ui.model.application.ui.menu.impl.ToolControlImpl;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

public class PerspectiveSwitcherCSSHandler extends AbstractCSSPropertySWTHandler {

	public static final ICSSPropertyHandler INSTANCE = new PerspectiveSwitcherCSSHandler();

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {

		Object obj = control.getData("modelElement"); //$NON-NLS-1$
		if (obj instanceof ToolControlImpl) {
			Object bar = ((ToolControlImpl) obj).getObject();
			if (bar != null && bar instanceof PerspectiveSwitcher) {
				Color borderColor = null;
				Color curveColor = null;
				if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
					borderColor = (Color) engine.convert(value, Color.class, control.getDisplay());
					((PerspectiveSwitcher) bar).setKeylineColor(borderColor, borderColor);
				} else if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
					CSSValueList list = (CSSValueList) value;
					if (list.getLength() == 2) {
						CSSValue border = list.item(0);
						CSSValue curve = list.item(1);
						borderColor = (Color) engine.convert(border, Color.class,
								control.getDisplay());
						curveColor = (Color) engine.convert(curve, Color.class,
								control.getDisplay());
						((PerspectiveSwitcher) bar).setKeylineColor(borderColor, curveColor);
					}
				}

			}
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
