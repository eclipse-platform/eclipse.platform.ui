/*******************************************************************************
 * Copyright (c) 2020 Pierre-Yves Bigourdan and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Pierre-Yves Bigourdan <pyvesdev@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyLinesVisibleSWTHandler extends AbstractCSSPropertySWTHandler {

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			boolean linesVisible = (Boolean) engine.convert(value, Boolean.class, control.getDisplay());
			if (control instanceof Table) {
				((Table) control).setLinesVisible(linesVisible);
			} else if (control instanceof Tree) {
				((Tree) control).setLinesVisible(linesVisible);
			}
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {
		if (control instanceof Table) {
			return Boolean.toString(((Table) control).getLinesVisible());
		} else if (control instanceof Tree) {
			return Boolean.toString(((Tree) control).getLinesVisible());
		}
		return null;
	}

}
