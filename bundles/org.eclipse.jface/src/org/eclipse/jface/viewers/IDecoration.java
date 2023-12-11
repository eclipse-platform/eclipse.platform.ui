/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.pde.api.tools.annotations.NoImplement;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Defines the result of decorating an element.
 *
 * This interface is not meant to be implemented and will be provided to
 * instances of <code>ILightweightLabelDecorator</code>.
 */
@NoImplement
public interface IDecoration {

	/**
	 * Constants for placement of image decorations.
	 */
	int TOP_LEFT = 0;

	/**
	 * Constant for the top right quadrant.
	 */
	int TOP_RIGHT = 1;

	/**
	 * Constant for the bottom left quadrant.
	 */
	int BOTTOM_LEFT = 2;

	/**
	 * Constant for the bottom right quadrant.
	 */
	int BOTTOM_RIGHT = 3;

	/**
	 * Constant for the underlay.
	 */
	int UNDERLAY = 4;

	/**
	 * Constant for replacing the original image. Note that for this to have an
	 * effect on the resulting decorated image, {@link #ENABLE_REPLACE} has to
	 * be set to {@link Boolean#TRUE} in the {@link IDecorationContext} (opt-in
	 * model). If replacement behavior is enabled, the resulting decorated image
	 * will be constructed by first painting the underlay, then the replacement
	 * image, and then the regular quadrant images.
	 *
	 * @since 3.4
	 */
	int REPLACE = 5;

	/**
	 * Constant that is used as the property key on an
	 * {@link IDecorationContext}. To enable image replacement, set to
	 * {@link Boolean#TRUE}.
	 *
	 * @since 3.4
	 * @see IDecorationContext
	 */
	String ENABLE_REPLACE = "org.eclipse.jface.viewers.IDecoration.disableReplace"; //$NON-NLS-1$

	/**
	 * Adds a prefix to the element's label.
	 *
	 * @param prefix
	 *            the prefix
	 */
	void addPrefix(String prefix);

	/**
	 * Adds a suffix to the element's label.
	 *
	 * @param suffix
	 *            the suffix
	 */
	void addSuffix(String suffix);

	/**
	 * Adds an overlay to the element's image.
	 *
	 * @param overlay
	 *            the overlay image descriptor
	 */
	void addOverlay(ImageDescriptor overlay);

	/**
	 * Adds an overlay to the element's image.
	 *
	 * @param overlay
	 *            the overlay image descriptor
	 * @param quadrant
	 *            The constant for the quadrant to draw the image on.
	 */
	void addOverlay(ImageDescriptor overlay, int quadrant);

	/**
	 * Set the foreground color for this decoration.
	 * @param color the color to be set for the foreground
	 *
	 * @since 3.1
	 */
	void setForegroundColor(Color color);

	/**
	 * Set the background color for this decoration.
	 * @param color the color to be set for the background
	 *
	 * @since 3.1
	 */
	void setBackgroundColor(Color color);

	/**
	 * Set the font for this decoration.
	 * @param font the font to use in this decoration
	 *
	 * @since 3.1
	 */
	void setFont(Font font);

	/**
	 * Return the decoration context in which this decoration
	 * will be applied.
	 * @return the decoration context
	 *
	 * @since 3.2
	 */
	IDecorationContext getDecorationContext();
}
