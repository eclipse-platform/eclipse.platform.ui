/*******************************************************************************
 * Copyright (c) 2017 Fabian Pfaff and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabian Pfaff <fabian.pfaff@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.TableElement;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Table;
import org.w3c.dom.css.CSSValue;

/**
 * A handler which allows the styling of table headers.
 */
public class CSSPropertyTableHeaderHandler implements ICSSPropertyHandler {

	private static final String SWT_TABLE_HEADER_COLOR = "swt-table-header-color"; //$NON-NLS-1$
	private static final String SWT_TABLE_HEADER_BACKGROUND_COLOR = "swt-table-header-background-color"; //$NON-NLS-1$

	@Override
	public boolean applyCSSProperty(Object element, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (!(element instanceof TableElement)) {
			return false;
		}
		TableElement tableElement = (TableElement) element;
		Table table = (Table) tableElement.getNativeWidget();
		if (SWT_TABLE_HEADER_COLOR.equals(property)
				&& value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			Color newColor = (Color) engine.convert(value, Color.class, table.getDisplay());
			tableElement.setTableHeaderColor(newColor);
		} else if (SWT_TABLE_HEADER_BACKGROUND_COLOR.equals(property)
				&& value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			Color newColor = (Color) engine.convert(value, Color.class, table.getDisplay());
			tableElement.setTableHeaderBackgroundColor(newColor);
		} else {
			return false;
		}
		return true;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property, String pseudo, CSSEngine engine)
			throws Exception {
		return null;
	}

}
