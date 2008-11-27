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
package org.eclipse.e4.ui.css.swt.properties.css2.lazy.background;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyBackgroundImageHandler extends
		AbstractCSSPropertySWTHandler {

	public void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Image image = (Image) engine.convert(value, Image.class, control
				.getDisplay());
		if (control instanceof CTabFolder) {
			((CTabFolder) control).setSelectionBackground(image);
		} else if (control instanceof Button) {
			Button button = ((Button) control);
			Image oldImage = button.getImage();
			if (oldImage != null)
				oldImage.dispose();
			button.setImage(image);
		} else
			control.setBackgroundImage(image);
	}

	public String retrieveCSSProperty(Control control, String property, String pseudo, 
			CSSEngine engine) throws Exception {
		return "none";
	}
}
