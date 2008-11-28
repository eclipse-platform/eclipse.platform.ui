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
package org.eclipse.e4.ui.css.swt.properties.css2.lazy.font;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper;
import org.eclipse.swt.widgets.Control;

public class CSSPropertyFontFamilyHandler extends
		AbstractCSSPropertyFontSWTHandler {

	public String retrieveCSSProperty(Control control, String property, String pseudo, 
			CSSEngine engine) throws Exception {
		return CSSSWTFontHelper.getFontFamily(control);
	}

}
