/*******************************************************************************
 * Copyright (c) 2015 Fabio Zadrozny and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.dom.ElementAdapter;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.ISelectionBackgroundCustomizationElement;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.css.CSSValue;

/**
 * A handler which will set the selection/hot background and border colors.
 */
public class CSSPropertySelectionBackgroundHandler implements ICSSPropertyHandler {

	private static final String SWT_SELECTION_FOREGROUND_COLOR = "swt-selection-foreground-color"; //$NON-NLS-1$

	private static final String SWT_SELECTION_BACKGROUND_COLOR = "swt-selection-background-color"; //$NON-NLS-1$
	private static final String SWT_SELECTION_BORDER_COLOR = "swt-selection-border-color"; //$NON-NLS-1$

	private static final String SWT_HOT_BACKGROUND_COLOR = "swt-hot-background-color"; //$NON-NLS-1$
	private static final String SWT_HOT_BORDER_COLOR = "swt-hot-border-color"; //$NON-NLS-1$

	@Override
	public boolean applyCSSProperty(Object element, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {

		if (!(element instanceof ISelectionBackgroundCustomizationElement && element instanceof ElementAdapter)
				|| !(((ElementAdapter) element).getNativeWidget() instanceof Widget) || property == null
				|| value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			return false;
		}

		Widget widget = (Widget) ((ElementAdapter) element).getNativeWidget();
		ISelectionBackgroundCustomizationElement treeElement = (ISelectionBackgroundCustomizationElement) element;
		Color newColor = (Color) engine.convert(value, Color.class, widget.getDisplay());

		switch (property) {
		case SWT_SELECTION_FOREGROUND_COLOR:
			treeElement.setSelectionForegroundColor(newColor);
			break;
		case SWT_SELECTION_BACKGROUND_COLOR:
			treeElement.setSelectionBackgroundColor(newColor);
			break;
		case SWT_SELECTION_BORDER_COLOR:
			treeElement.setSelectionBorderColor(newColor);
			break;
		case SWT_HOT_BACKGROUND_COLOR:
			treeElement.setHotBackgroundColor(newColor);
			break;
		case SWT_HOT_BORDER_COLOR:
			treeElement.setHotBorderColor(newColor);
			break;
		default:
			return false;
		}

		return true;
	}

}
