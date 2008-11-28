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
package org.eclipse.e4.ui.css.swt.properties.css2.lazy.border;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler2;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler2Delegate;
import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyBorderCompositeHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyBorderHandler extends
		AbstractCSSPropertyBorderCompositeHandler implements
		ICSSPropertyHandler2Delegate {

	public boolean applyCSSProperty(Object element, String property, 
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Control control = SWTElementHelpers.getControl(element);
		if (control != null) {
			super.applyCSSPropertyComposite(element, property, value, pseudo,
					engine);
			return true;
		}
		return false;
	}

	public String retrieveCSSProperty(Object widget, String property, String pseudo, 
			CSSEngine engine) throws Exception {
		return null;
	}

	public ICSSPropertyHandler2 getCSSPropertyHandler2() {
		return CSSPropertyBorderSWTHandler2.INSTANCE;
	}
}
