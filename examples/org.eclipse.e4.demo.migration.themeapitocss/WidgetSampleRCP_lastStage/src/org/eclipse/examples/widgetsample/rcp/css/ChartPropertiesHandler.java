/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.examples.widgetsample.rcp.css;
import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.examples.chart.Chart;
import org.w3c.dom.css.CSSValue;


public class ChartPropertiesHandler implements ICSSPropertyHandler {
	@Override
	public boolean applyCSSProperty(Object element, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (element instanceof ChartElement) {
			Chart chart = (Chart) ((ChartElement) element).getNativeWidget();			
		
			if ("color".equals(property) && value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				Color color = (Color) engine.convert(value, Color.class, chart.getDisplay());
				chart.setFontColor(color.getRGB());
				return true;
			}
			
			if ("chart-gradient".equals(property) && value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
				Gradient grad = (Gradient) engine.convert(value, Gradient.class, chart.getDisplay());
				if (grad != null && grad.getRGBs().size() == 2) {
					chart.setChartGradient((RGB[]) grad.getRGBs().toArray(new RGB[2]));
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property, String pseudo, CSSEngine engine)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}	

}
