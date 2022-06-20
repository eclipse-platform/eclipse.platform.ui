/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
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
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyBackgroundHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.CompositeElement;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTImageHelper;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.e4.ui.css.swt.properties.GradientBackgroundListener;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyBackgroundSWTHandler extends AbstractCSSPropertyBackgroundHandler {

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Widget widget = SWTElementHelpers.getWidget(element);
		if (widget != null) {
			return super.applyCSSProperty(element, property, value, pseudo, engine);
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
	public void applyCSSPropertyBackgroundColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		Widget widget = (Widget) ((WidgetElement) element).getNativeWidget();
		switch (value.getCssValueType()) {
		case CSSValue.CSS_PRIMITIVE_VALUE:
			Color newColor = (Color) engine.convert(value, Color.class, widget
					.getDisplay());
			if (widget instanceof CTabItem) {
				CTabFolder folder = ((CTabItem) widget).getParent();
				if ("selected".equals(pseudo)) {
					// tab folder selection manages gradients
					CSSSWTColorHelper.setSelectionBackground(folder, newColor);
				} else {
					CSSSWTColorHelper.setBackground(folder, newColor);
				}
			} else if (widget instanceof ToolItem) {
				// ToolItem prevents itself from repaints if the same color is set
				((ToolItem) widget).setBackground(newColor);
			} else if (widget instanceof Control) {
				GradientBackgroundListener.remove((Control) widget);
				CSSSWTColorHelper.setBackground((Control) widget, newColor);
				CompositeElement.setBackgroundOverriddenByCSSMarker(widget);
			}
			break;
		case CSSValue.CSS_VALUE_LIST:
			Gradient grad = (Gradient) engine.convert(value, Gradient.class,
					widget.getDisplay());
			if (grad == null) {
				return; // warn?
			}
			if (widget instanceof CTabItem) {
				CTabFolder folder = ((CTabItem) widget).getParent();
				Color[] colors = CSSSWTColorHelper.getSWTColors(grad,
						folder.getDisplay(), engine);
				int[] percents = CSSSWTColorHelper.getPercents(grad);

				if ("selected".equals(pseudo)) {
					folder.setSelectionBackground(colors, percents, true);
				} else {
					folder.setBackground(colors, percents, true);
				}

			} else if (widget instanceof Control) {
				GradientBackgroundListener.handle((Control) widget, grad);
				CompositeElement.setBackgroundOverriddenByCSSMarker(widget);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void applyCSSPropertyBackgroundImage(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		// Widget control = (Widget) element;
		Widget widget = (Widget) ((WidgetElement) element).getNativeWidget();
		Image image = (Image) engine.convert(value, Image.class,
				widget.getDisplay());
		if (widget instanceof CTabFolder && "selected".equals(pseudo)) {
			((CTabFolder) widget).setSelectionBackground(image);
		} else if (widget instanceof Button) {
			Button button = ((Button) widget);
			// Image oldImage = button.getImage();
			// if (oldImage != null)
			// oldImage.dispose();
			CSSSWTImageHelper.setImage(button, image);
		} else if (widget instanceof Control) {
			CSSSWTImageHelper.setBackgroundImage((Control) widget, image);
		}
	}

	@Override
	public String retrieveCSSPropertyBackgroundColor(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		Widget widget = (Widget) element;
		Color color = null;
		if (widget instanceof CTabItem) {
			if ("selected".equals(pseudo)) {
				color = ((CTabItem) widget).getParent()
						.getSelectionBackground();
			} else {
				color = ((CTabItem) widget).getParent().getBackground();
			}

		}
		else if (widget instanceof ToolItem) {
			color = ((ToolItem) widget).getBackground();
		}

		else if (widget instanceof Control) {
			color = ((Control) widget).getBackground();
		}
		return engine.convert(color, Color.class, null);
	}


}
