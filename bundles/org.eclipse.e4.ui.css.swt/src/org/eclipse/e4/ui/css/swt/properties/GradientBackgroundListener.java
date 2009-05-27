/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class GradientBackgroundListener implements Listener {
	private Gradient grad;
	private Control control;
	private static Map handlers = new HashMap();

	private GradientBackgroundListener(Control control, Gradient grad) {
		this.grad = grad;
		this.control = control;
		control.addListener(SWT.Resize, this);
	}

	public static void handle(Control control, Gradient grad) {
		GradientBackgroundListener handler = (GradientBackgroundListener) handlers
				.get(control);
		if (handler == null) {
			handler = new GradientBackgroundListener(control, grad);
			handlers.put(control, handler);
		}
		else {
			handler.grad = grad;
			handler.handleEvent(null);
		}
	}

	public void handleEvent(Event event) {
		Point size = control.getSize();
		if (size.x <= 0 || size.y <= 0)
			return;
		/*
		 * Dispose the old background image.
		 */
		Image oldImage = control.getBackgroundImage();
		if (oldImage != null && !oldImage.isDisposed()) {
			oldImage.dispose();
			oldImage = null;
		}
		/*
		 * Draw the new background.
		 */
		Image newImage = new Image(control.getDisplay(), size.x, size.y);
		GC gc = new GC(newImage);
		List colors = new ArrayList();
		for (Iterator iterator = grad.getRGBs().iterator(); iterator.hasNext();) {
			RGB rgb = (RGB) iterator.next();
			Color color = new Color(control.getDisplay(), rgb.red, rgb.green,
					rgb.blue);
			colors.add(color);

		}
		fillGradient(gc, new Rectangle(0, 0, size.x, size.y), colors, CSSSWTColorHelper.getPercents(grad), true);
		gc.dispose();
		for (Iterator iterator = colors.iterator(); iterator.hasNext();) {
			Color c = (Color) iterator.next();
			c.dispose(); // Dispose colors too.
		}
		/*
		 * Set the new background.
		 */
		control.setBackgroundImage(newImage);
	}

	/*
	 * Fills a gradient rectangle in the specified gc with the specified colors
	 * and percentages.
	 * 
	 * @param gc @param rect @param gradientColors @param gradientPercents
	 * @param gradientVertical
	 */
	private static void fillGradient(GC gc, Rectangle rect,
			List gradientColors, int[] gradientPercents, boolean gradientVertical) {
		Color background = (Color) gradientColors
				.get(gradientColors.size() - 1);
		if (gradientColors.size() == 1) {
			if (gradientColors.get(0) != null)
				gc.setBackground((Color) gradientColors.get(0));
			gc.fillRectangle(rect.x, rect.y, rect.width, rect.height);
		} else {
			Color lastColor = (Color) gradientColors.get(0);
			int pos = (gradientVertical) ? rect.y : rect.x;
			int loopCount = Math.min(gradientColors.size() - 1,
					gradientPercents.length);
			for (int i = 0; i < loopCount; ++i) {
				gc.setForeground(lastColor);
				lastColor = (Color) gradientColors.get(i + 1);
				if (lastColor == null)
					lastColor = background;
				gc.setBackground(lastColor);
				int grpercent = ((Integer) gradientPercents[i]).intValue();
				if (gradientVertical) {
					final int gradientHeight = (grpercent * rect.height / 100)
							- (pos - rect.y);
					gc.fillGradientRectangle(rect.x, pos, rect.width,
							gradientHeight, true);
					pos += gradientHeight;
				} else {
					final int gradientWidth = (grpercent * rect.width / 100)
							- (pos - rect.x);
					gc.fillGradientRectangle(pos, rect.y, gradientWidth,
							rect.height, false);
					pos += gradientWidth;
				}
			}
			if (gradientVertical && pos < rect.height) {
				gc.setBackground(background);
				gc.fillRectangle(rect.x, pos, rect.width, rect.height - pos);
			}
			if (!gradientVertical && pos < rect.width) {
				gc.setBackground(background);
				gc.fillRectangle(pos, rect.y, rect.width - pos, rect.height);
			}
		}
	}
}
