/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyMaximizeVisibleSWTHandler extends AbstractCSSPropertySWTHandler {

	@Override
	public void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine) throws Exception{
		boolean isMaxVisible = (Boolean) engine.convert(value, Boolean.class, null);
		if (control instanceof CTabFolder folder) {
			folder.setMaximizeVisible(isMaxVisible);
		}
	}

	@Override
	public String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine) {
		if (control instanceof CTabFolder folder) {
			return Boolean.toString(folder.getMaximizeVisible());
		}
		return null;
	}
}
