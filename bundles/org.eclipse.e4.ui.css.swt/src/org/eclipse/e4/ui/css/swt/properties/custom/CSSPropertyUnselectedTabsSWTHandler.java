/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import java.lang.reflect.Method;
import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.graphics.Color;
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
			CTabFolder folder = ((CTabFolder) control);
			Color[] colors = CSSSWTColorHelper.getSWTColors(grad, folder.getDisplay(), engine);
			int[] percents = CSSSWTColorHelper.getPercents(grad);
			folder.setBackground(colors, percents, true);
			
			CTabFolderRenderer renderer = ((CTabFolder) control).getRenderer();
			if (renderer == null) return;
			try {
				if (pseudo != null && pseudo.equals("selected")) {
					Method m = renderer.getClass().getMethod("setActiveToolbarGradient",  new Class[]{Color[].class, int[].class});
					m.invoke(renderer, colors, percents);
				} else {
					Method m = renderer.getClass().getMethod("setInactiveToolbarGradient",  new Class[]{Color[].class, int[].class});
					m.invoke(renderer, colors, percents);
				}
			} catch(NoSuchMethodException e) {/*IGNORED*/}
		}
	}
	

	@Override
	protected String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
