/*******************************************************************************
 * Copyright (c) 2008, 2019 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyTextHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyTextHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyTextSWTHandler extends AbstractCSSPropertyTextHandler {

	public static final ICSSPropertyTextHandler INSTANCE = new CSSPropertyTextSWTHandler();

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Widget widget = SWTElementHelpers.getWidget(element);
		if (widget != null) {
			return super.applyCSSProperty(widget, property, value, pseudo, engine);
		}
		return false;

	}

	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		Widget widget = SWTElementHelpers.getWidget(element);
		if (widget != null) {
			return super.retrieveCSSProperty(widget, property, pseudo, engine);
		}
		return null;
	}

	@Override
	public void applyCSSPropertyColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		Widget widget = (Widget) element;
		Color newColor = (Color) engine.convert(value, Color.class, widget.getDisplay());

		if (newColor != null && newColor.isDisposed() || value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			return;
		}

		if (widget instanceof CTabItem) {
			CTabFolder folder = ((CTabItem) widget).getParent();
			if ("selected".equals(pseudo)) {
				CSSSWTColorHelper.setSelectionForeground(folder, newColor);
			} else {
				CSSSWTColorHelper.setForeground(folder, newColor);
			}
		} else if (widget instanceof ToolItem) {
			// ToolItem prevents itself from repaints if the same color is set
			((ToolItem) widget).setForeground(newColor);
		} else if (widget instanceof Control) {
			CSSSWTColorHelper.setForeground((Control) widget, newColor);
		}
	}

	@Override
	public void applyCSSPropertyTextTransform(Object element,
			final CSSValue value, String pseudo, CSSEngine engine)
					throws Exception {
		Widget widget = (Widget) element;
		String defaultText = (String) widget.getData(CSSSWTConstants.TEXT_KEY);
		if (element instanceof Text) {
			final Text text = (Text) widget;
			String oldText = text.getText();
			String newText = getTextTransform(text.getText(), value,
					defaultText);
			if (!oldText.equals(newText)) {
				text.setText(newText);
			}
		}
		if (element instanceof Label) {
			Label label = (Label) element;
			label
			.setText(getTextTransform(label.getText(), value,
					defaultText));
			return;
		}
		if (element instanceof Button) {
			Button button = (Button) element;
			button.setText(getTextTransform(button.getText(), value,
					defaultText));
			return;
		}
	}

	@Override
	public String retrieveCSSPropertyColor(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		Widget widget = (Widget) element;
		Color color = null;
		if (widget instanceof CTabItem) {
			if ("selected".equals(pseudo)) {
				color = ((CTabItem) widget).getParent().getSelectionForeground();
			} else {
				color = ((CTabItem) widget).getParent().getForeground();
			}
		} else if (widget instanceof Control) {
			color = ((Control) widget).getForeground();
		}
		return engine.convert(color, Color.class, null);
	}

	@Override
	public String retrieveCSSPropertyTextTransform(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		String text = null;
		Widget widget = (Widget) element;
		// if (control instanceof Text) {
		// final Text controlText = ((Text) element);
		// text = controlText.getText();
		// } else {
		if (widget instanceof Label) {
			text = ((Label) element).getText();
			if (text != null) {
				widget.setData(CSSSWTConstants.TEXT_KEY, text);
			}
		} else if (widget instanceof Button) {
			text = ((Button) element).getText();
			if (text != null) {
				widget.setData(CSSSWTConstants.TEXT_KEY, text);
			}
		}
		// }
		return "none";
	}

}
