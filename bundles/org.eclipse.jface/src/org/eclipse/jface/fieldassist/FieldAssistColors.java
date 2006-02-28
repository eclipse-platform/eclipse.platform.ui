/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.fieldassist;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;

/**
 * FieldAssistColors defines protocol for retrieving colors that can be used to
 * provide visual cues with fields. For consistency with JFace dialogs and
 * wizards, it is recommended that FieldAssistColors is used when colors are
 * used to annotate fields.
 * <p>
 * Color resources that are returned using methods in this class are maintained
 * in the JFace color registries, or by SWT. Users of any color resources
 * provided by this class are not responsible for the lifecycle of the color.
 * Colors provided by this class should never be disposed by clients. In some
 * cases, clients are provided information, such as RGB values, in order to
 * create their own color resources. In these cases, the client should manage
 * the lifecycle of any created resource.
 * 
 * @since 3.2
 */
public class FieldAssistColors {
	/**
	 * Compute the RGB of the color that should be used for the background of a
	 * control to indicate that the control has an error. Because the color
	 * suitable for indicating an error depends on the colors set into the
	 * control, this color is always computed dynamically and provided as an RGB
	 * value. Clients who use this RGB to create a Color resource are
	 * responsible for managing the life cycle of the color.
	 * <p>
	 * This color is computed dynamically each time that it is queried. Clients
	 * should typically call this method once, create a color from the RGB
	 * provided, and dispose of the color when finished using it.
	 * 
	 * @param control
	 *            the control for which the background color should be computed.
	 * @return the RGB value indicating a background color appropriate for
	 *         indicating an error in the control.
	 */
	public static RGB computeErrorFieldBackgroundRGB(Control control) {
		/*
		 * There is probably a much more elegant way to do this. Suggestions
		 * welcome. The current computation uses the JFace error text color to
		 * affect the color of the control background. The error color is
		 * examined and the computation takes into account both the magnitude of
		 * each color component in the error color and significant variation of
		 * any particular R,G,B value from the average magnitude. The result is
		 * that a color skewed heavily toward a R,G,B affects the control
		 * background much more obviously than a color where the RGB values are
		 * similar.
		 */
		Color background = control.getBackground();
		Color error = JFaceColors.getErrorText(control.getDisplay());

		// Compute the average magnitude.
		int average = (error.getRed() + error.getBlue() + error.getGreen()) / 3;
		// RGB values are increased by a combination of the magnitude of each
		// color component and its difference from the average.
		int rMore = (int) (0.08 * error.getRed())
				+ (int) (0.05 * (error.getRed() - average));
		int gMore = (int) (0.08 * error.getGreen())
				+ (int) (0.05 * (error.getGreen() - average));
		int bMore = (int) (0.08 * error.getBlue())
				+ (int) (0.05 * (error.getBlue() - average));

		// Now compute the RGB components by either increasing each
		// component, or if they are at full magnitude, decreasing the
		// other components.
		int r = background.getRed();
		int g = background.getGreen();
		int b = background.getBlue();
		if (r <= 255 - rMore) {
			r += rMore;
		} else {
			g -= rMore;
			b -= rMore;
		}
		if (g <= 255 - gMore) {
			g += gMore;
		} else {
			r -= gMore;
			b -= gMore;
		}
		if (b <= 255 - bMore) {
			b += bMore;
		} else {
			r -= bMore;
			g -= bMore;
		}
		r = Math.max(0, Math.min(255, r));
		g = Math.max(0, Math.min(255, g));
		b = Math.max(0, Math.min(255, b));
		return new RGB(r, g, b);
	}

	/**
	 * Return the color that should be used for the background of a control to
	 * indicate that the control is a required field and does not have content.
	 * <p>
	 * This color is managed by FieldAssistResources and should never be
	 * disposed by clients.
	 * 
	 * @param control
	 *            the control on which the background color will be used.
	 * @return the color used to indicate that a field is required.
	 */
	public static Color getRequiredFieldBackgroundColor(Control control) {
		return control.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);

	}

}
