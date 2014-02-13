/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyBackgroundHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyBackgroundHandler;
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
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyBackgroundSWTHandler extends
AbstractCSSPropertyBackgroundHandler {
	public final static ICSSPropertyBackgroundHandler INSTANCE = new CSSPropertyBackgroundSWTHandler();

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Widget widget = SWTElementHelpers.getWidget(element);
		if (widget != null) {
			// super.applyCSSProperty(widget, property, value, pseudo, engine);
			super.applyCSSProperty(element, property, value, pseudo, engine);
			return true;
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

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.ui.core.css.dom.properties.css2.
	 * AbstractCSSPropertyBackgroundHandler
	 * #applyCSSPropertyBackgroundColor(java.lang.Object,
	 * org.w3c.dom.css.CSSValue, java.lang.String,
	 * org.eclipse.e4.ui.core.css.engine.CSSEngine)
	 */
	@Override
	public void applyCSSPropertyBackgroundColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		Widget widget = (Widget) ((WidgetElement) element).getNativeWidget();
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			Color newColor = (Color) engine.convert(value, Color.class, widget
					.getDisplay());
			if (widget instanceof CTabItem) {
				CTabFolder folder = ((CTabItem) widget).getParent();
				if ("selected".equals(pseudo)) {
					// tab folder selection manages gradients
					folder.setSelectionBackground(newColor);
				} else {
					folder.setBackground(newColor);
				}
			} else if (widget instanceof Control) {
				GradientBackgroundListener.remove((Control) widget);
				((Control) widget).setBackground(newColor);
				CompositeElement.setBackgroundOverriddenByCSSMarker(widget);
			}
		} else if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			Gradient grad = (Gradient) engine.convert(value, Gradient.class,
					widget.getDisplay());
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
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.ui.core.css.dom.properties.css2.
	 * AbstractCSSPropertyBackgroundHandler
	 * #applyCSSPropertyBackgroundImage(java.lang.Object,
	 * org.w3c.dom.css.CSSValue, java.lang.String,
	 * org.eclipse.e4.ui.core.css.engine.CSSEngine)
	 */
	@Override
	public void applyCSSPropertyBackgroundImage(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		// Widget control = (Widget) element;
		Widget control = (Widget) ((WidgetElement) element).getNativeWidget();
		Image image = (Image) engine.convert(value, Image.class, control
				.getDisplay());
		if (control instanceof CTabFolder && "selected".equals(pseudo)) {
			((CTabFolder) control).setSelectionBackground(image);
		} else if (control instanceof Button) {
			Button button = ((Button) control);
			// Image oldImage = button.getImage();
			// if (oldImage != null)
			// oldImage.dispose();
			CSSSWTImageHelper.storeDefaultImage(button);
			button.setImage(image);

		} else {
			try {
				if (control instanceof Control) {
					((Control) control).setBackgroundImage(image);
				}
			} catch (Throwable e) {
				//TODO replace with eclipse logging
				// if (logger.isWarnEnabled())
				// logger
				// .warn("Impossible to manage backround-image, This SWT version doesn't support control.setBackgroundImage(Image image) Method");
			}
		}
	}

	@Override
	public String retrieveCSSPropertyBackgroundAttachment(Object widget,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
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
		} else if (widget instanceof Control) {
			color = ((Control) widget).getBackground();
		}
		return engine.convert(color, Color.class, null);
	}

	@Override
	public String retrieveCSSPropertyBackgroundImage(Object widget,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO : manage path of Image.
		return "none";
	}

	@Override
	public String retrieveCSSPropertyBackgroundPosition(Object widget,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBackgroundRepeat(Object widget,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}
}
