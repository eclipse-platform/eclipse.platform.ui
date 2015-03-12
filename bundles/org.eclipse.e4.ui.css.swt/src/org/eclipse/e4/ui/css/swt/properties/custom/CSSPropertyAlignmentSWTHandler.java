/*******************************************************************************
 *  Copyright (c) 2009, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *      Remy Chi Jian Suen <remy.suen@gmail.com> - bug 137650
 *******************************************************************************/

package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.w3c.dom.css.CSSValue;

/**
 * We support some additional SWT-specific values
 */
public class CSSPropertyAlignmentSWTHandler extends AbstractCSSPropertySWTHandler{

	public static final ICSSPropertyHandler INSTANCE = new CSSPropertyAlignmentSWTHandler();

	@Override
	public void applyCSSProperty(Control control, String property,
		    CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (control instanceof Button) {
			Button button = (Button)control;
			String stringValue = value.getCssText().toLowerCase();
			if ("left".equals(stringValue)){
				button.setAlignment(SWT.LEFT);
			} else if ("lead".equals(stringValue)){
				button.setAlignment(SWT.LEAD);
			} else if ("right".equals(stringValue)){
				button.setAlignment(SWT.RIGHT);
			} else if ("trail".equals(stringValue)){
				button.setAlignment(SWT.TRAIL);
			} else if ("center".equals(stringValue)){
				button.setAlignment(SWT.CENTER);
			} else if ("up".equals(stringValue)){
				button.setAlignment(SWT.UP);
			} else if ("down".equals(stringValue)){
				button.setAlignment(SWT.DOWN);
			} else if ("inherit".equals(stringValue)) {
				// todo
			}

		}
		else if (control instanceof Label) {
			Label label = (Label)control;
			String stringValue = value.getCssText().toLowerCase();
			if ("left".equals(stringValue)){
				label.setAlignment(SWT.LEFT);
			} else if ("lead".equals(stringValue)){
				label.setAlignment(SWT.LEAD);
			} else if ("right".equals(stringValue)){
				label.setAlignment(SWT.RIGHT);
			} else if ("trail".equals(stringValue)){
				label.setAlignment(SWT.TRAIL);
			} else if ("center".equals(stringValue)){
				label.setAlignment(SWT.CENTER);
			} else if ("inherit".equals(stringValue)) {
				// todo
			}
		}

	}

	@Override
	public String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		if (control instanceof Button) {
			Button button = (Button)control;
			switch(button.getAlignment()){
			case SWT.RIGHT: return "right";  //Note same value as SWT.TRAIL
			case SWT.LEFT: return "left";  //Note same value as SWT.LEAD
			case SWT.CENTER: return "center";
			case SWT.UP: return "up";
			case SWT.DOWN: return "down";
			}
		}
		else if (control instanceof Label) {
			Label label = (Label)control;
			switch(label.getAlignment()){
			case SWT.RIGHT: return "right";  //Note same value as SWT.TRAIL
			case SWT.LEFT: return "left";  //Note same value as SWT.LEAD
			case SWT.CENTER: return "center";
			}
		}
		return null;
	}
}
