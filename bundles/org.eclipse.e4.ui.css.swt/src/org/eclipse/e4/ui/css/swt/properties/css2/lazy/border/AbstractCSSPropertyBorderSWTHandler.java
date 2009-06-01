/*******************************************************************************
 * Copyright (c) 2008, 2009 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.css2.lazy.border;

import org.eclipse.e4.ui.css.core.css2.CSSBorderPropertiesHelpers;
import org.eclipse.e4.ui.css.core.dom.properties.CSSBorderProperties;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler2;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler2Delegate;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.dom.properties.CSSBorderPropertiesImpl;
import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTHelpers;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public abstract class AbstractCSSPropertyBorderSWTHandler extends
		AbstractCSSPropertySWTHandler implements ICSSPropertyHandler2Delegate {

	public void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Composite parent = control.getParent();
		if (parent == null)
			return;
		CSSBorderProperties border = (CSSBorderProperties) control
				.getData(CSSSWTConstants.CONTROL_CSS2BORDER_KEY);
		if (border == null) {
			border = new CSSBorderPropertiesImpl();
			control.setData(CSSSWTConstants.CONTROL_CSS2BORDER_KEY, border);
			parent.addPaintListener(CSSSWTHelpers.createBorderPaintListener(engine, control));
		}
		super.applyCSSProperty(border, property, value, pseudo, engine);
	}

	public void applyCSSProperty(CSSBorderProperties border, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		CSSBorderPropertiesHelpers.updateCSSProperty(border, property, value);
	}

	public ICSSPropertyHandler2 getCSSPropertyHandler2() {
		return CSSPropertyBorderSWTHandler2.INSTANCE;
	}

}
