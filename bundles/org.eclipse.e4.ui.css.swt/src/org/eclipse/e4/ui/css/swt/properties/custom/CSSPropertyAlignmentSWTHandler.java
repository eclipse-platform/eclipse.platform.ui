/*******************************************************************************
 *  Copyright (c) 2009, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *      Remy Chi Jian Suen <remy.suen@gmail.com> - bug 137650
 *******************************************************************************/

package org.eclipse.e4.ui.css.swt.properties.custom;

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

	@Override
	public void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (control instanceof Button) {
			Button button = (Button)control;
			switch (value.getCssText().toLowerCase()) {
			case "left":
				button.setAlignment(SWT.LEFT);
				break;
			case "lead":
				button.setAlignment(SWT.LEAD);
				break;
			case "right":
				button.setAlignment(SWT.RIGHT);
				break;
			case "trail":
				button.setAlignment(SWT.TRAIL);
				break;
			case "center":
				button.setAlignment(SWT.CENTER);
				break;
			case "up":
				button.setAlignment(SWT.UP);
				break;
			case "down":
				button.setAlignment(SWT.DOWN);
				break;
			case "inherit":
				// todo
				break;
			default:
				break;
			}
		}
		else if (control instanceof Label) {
			Label label = (Label)control;
			switch (value.getCssText().toLowerCase()) {
			case "left":
				label.setAlignment(SWT.LEFT);
				break;
			case "lead":
				label.setAlignment(SWT.LEAD);
				break;
			case "right":
				label.setAlignment(SWT.RIGHT);
				break;
			case "trail":
				label.setAlignment(SWT.TRAIL);
				break;
			case "center":
				label.setAlignment(SWT.CENTER);
				break;
			case "inherit":
				// todo
				break;
			default:
				break;
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
