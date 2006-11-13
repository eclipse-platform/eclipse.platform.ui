/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.window;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 * Default implementation of ToolTip that provides an iconofied label with font
 * and color controls by subclass.
 * 
 * @since 3.3 <strong>EXPERIMENTAL</strong> This class or interface has been
 *        added as part of a work in progress. This API may change at any given
 *        time. Please do not use this API without consulting with the
 *        Platform/UI team.
 */
public abstract class DefaultToolTip extends ToolTip {

	/**
	 * @param control
	 */
	public DefaultToolTip(Control control) {
		super(control);
	}

	/**
	 * Creates the content are of the the tooltip. By default this creates a
	 * CLabel to display text. To customize the text Subclasses may override the
	 * following methods
	 * <ul>
	 * <li>{@link #getStyle(Event)}</li>
	 * <li>{@link #getBackgroundColor(Event)}</li>
	 * <li>{@link #getForegroundColor(Event)}</li>
	 * <li>{@link #getFont(Event)}</li>
	 * <li>{@link #getImage(Event)}</li>
	 * <li>{@link #getText(Event)}</li>
	 * <li>{@link #getBackgroundImage(Event)}</li>
	 * </ul>
	 * 
	 * @param event
	 *            the event that triggered the activation of the tooltip
	 * @param parent
	 *            the parent of the content area
	 * @return the content area created
	 */
	protected Composite createToolTipContentArea(Event event, Composite parent) {
		Image image = getImage(event);
		Image bgImage = getBackgroundImage(event);
		String text = getText(event);
		Color fgColor = getForegroundColor(event);
		Color bgColor = getBackgroundColor(event);
		Font font = getFont(event);

		CLabel label = new CLabel(parent, getStyle(event));
		if (text != null) {
			label.setText(text);
		}

		if (image != null) {
			label.setImage(image);
		}

		if (fgColor != null) {
			label.setForeground(fgColor);
		}

		if (bgColor != null) {
			label.setBackground(bgColor);
		}

		if (bgImage != null) {
			label.setBackgroundImage(image);
		}

		if (font != null) {
			label.setFont(font);
		}

		return label;
	}

	/**
	 * The style used to create the {@link CLabel} in the default implementation
	 * 
	 * @param event
	 *            the event triggered the popup of the tooltip
	 * @return the style
	 */
	protected int getStyle(Event event) {
		return SWT.SHADOW_NONE;
	}

	/**
	 * The {@link Image} displayed in the {@link CLabel} in the default
	 * implementation implementation
	 * 
	 * @param event
	 *            the event triggered the popup of the tooltip
	 * @return the {@link Image} or <code>null</code> if no image should be
	 *         displayed
	 */
	protected Image getImage(Event event) {
		return null;
	}

	/**
	 * The foreground {@link Color} used by {@link CLabel} in the default
	 * implementation
	 * 
	 * @param event
	 *            the event triggered the popup of the tooltip
	 * @return the {@link Color} or <code>null</code> if default foreground
	 *         color should be used
	 */
	protected Color getForegroundColor(Event event) {
		return event.widget.getDisplay().getSystemColor(
				SWT.COLOR_INFO_FOREGROUND);
	}

	/**
	 * The background {@link Color} used by {@link CLabel} in the default
	 * implementation
	 * 
	 * @param event
	 *            the event triggered the popup of the tooltip
	 * @return the {@link Color} or <code>null</code> if default background
	 *         color should be used
	 */
	protected Color getBackgroundColor(Event event) {
		return event.widget.getDisplay().getSystemColor(
				SWT.COLOR_INFO_BACKGROUND);
	}

	/**
	 * The background {@link Image} used by {@link CLabel} in the default
	 * implementation
	 * 
	 * @param event
	 *            the event triggered the popup of the tooltip
	 * @return the {@link Image} or <code>null</code> if no image should be
	 *         displayed in the background
	 */
	protected Image getBackgroundImage(Event event) {
		return null;
	}

	/**
	 * The {@link Font} used by {@link CLabel} in the default implementation
	 * 
	 * @param event
	 *            the event triggered the popup of the tooltip
	 * @return the {@link Font} or <code>null</code> if the default font
	 *         should be used
	 */
	protected Font getFont(Event event) {
		return null;
	}

	/**
	 * The text displayed in the {@link CLabel} in the default implementation
	 * 
	 * @param event
	 *            the event triggered the popup of the tooltip
	 * @return the text or <code>null</code> if no text has to be displayed
	 */
	protected String getText(Event event) {
		return null;
	}
}
