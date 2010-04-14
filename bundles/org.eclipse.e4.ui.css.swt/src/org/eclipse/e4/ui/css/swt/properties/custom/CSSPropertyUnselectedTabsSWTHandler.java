/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import java.util.Iterator;
import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyUnselectedTabsSWTHandler extends AbstractCSSPropertySWTHandler {

	
	public static final ICSSPropertyHandler INSTANCE = new CSSPropertyUnselectedTabsSWTHandler();
	
	@Override
	protected void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (!(control instanceof CTabFolder)) return;
		if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			Gradient grad = (Gradient) engine.convert(value, Gradient.class, control.getDisplay());

			Color[] colors = new Color[grad.getRGBs().size()];
			int i = 0;
			for (Iterator iterator = grad.getRGBs().iterator(); iterator.hasNext();) {
				RGB rgb = (RGB) iterator.next();
				Color tempColor = new Color(control.getDisplay(), rgb);
				colors[i++] = tempColor;
			}
			int[] percents = new int[grad.getPercents().size()];
			i = 0;
			for (Iterator iterator = grad.getPercents().iterator(); iterator.hasNext();) {
				percents [i++] = ((Integer) iterator.next()).intValue();
			}
			
			((CTabFolder) control).setBackground(colors, percents, true);
			
			for (int j = 0; j < colors.length; j++) {
				//colors[j].dispose();
			}
		}
	}
	

	@Override
	protected String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
