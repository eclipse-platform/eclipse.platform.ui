/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.preference;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * The <code>ColorSelector</code> is a wrapper for a button that displays
 * a selected <code>Color</code> and allows the user to change the selection.
 */
public class ColorSelector {
	private Button fButton;
	private Color fColor;
	private RGB fColorValue;
	private Point fExtent;
	private Image fImage;

	/**
	 * Create a new instance of the reciever and the
	 * button that it wrappers in the supplied parent <code>Composite</code>.
	 * 
	 * @param parent. The parent of the button.
	 */
	public ColorSelector(Composite parent) {

		fButton = new Button(parent, SWT.PUSH);
		fExtent = computeImageSize(parent);
		fImage = new Image(parent.getDisplay(), fExtent.x, fExtent.y);

		GC gc = new GC(fImage);
		gc.setBackground(fButton.getBackground());
		gc.fillRectangle(0, 0, fExtent.x, fExtent.y);
		gc.dispose();

		fButton.setImage(fImage);
		fButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				ColorDialog colorDialog = new ColorDialog(fButton.getShell());
				colorDialog.setRGB(fColorValue);
				RGB newColor = colorDialog.open();
				if (newColor != null) {
					fColorValue = newColor;
					updateColorImage();
				}
			}
		});

		fButton.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				if (fImage != null) {
					fImage.dispose();
					fImage = null;
				}
				if (fColor != null) {
					fColor.dispose();
					fColor = null;
				}
			}
		});

		fButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.accessibility.AccessibleAdapter#getName(org.eclipse.swt.accessibility.AccessibleEvent)
			 */
			public void getName(AccessibleEvent e) {
				e.result = JFaceResources.getString("ColorSelector.Name"); //$NON-NLS-1$
			}
		});
	}

	/**
	 * Compute the size of the image to be displayed.
	 * 
	 * @param window - the window used to calculate
	 * @return <code>Point</code>
	 */

	private Point computeImageSize(Control window) {
		GC gc = new GC(window);
		Font f =
			JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT);
		gc.setFont(f);
		int height = gc.getFontMetrics().getHeight();
		gc.dispose();
		Point p = new Point(height * 3 - 6, height);
		return p;
	}

	/**
	 * Get the button control being wrappered by the selector.
	 * 
	 * @return <code>Button</code>
	 */
	public Button getButton() {
		return fButton;
	}

	/**
	 * Return the currently displayed color.
	 * 
	 * @return <code>RGB</code>
	 */
	public RGB getColorValue() {
		return fColorValue;
	}

	/**
	 * Set the current color value and update the control.
	 * 
	 * @param rgb. The new color.
	 */
	public void setColorValue(RGB rgb) {
		fColorValue = rgb;
		updateColorImage();
	}

	/**
	 * Set whether or not the button is enabled.
	 * 
	 * @param state the enabled state.
	 */

	public void setEnabled(boolean state) {
		getButton().setEnabled(state);
	}

	/**
	 * Update the image being displayed on the button using
	 * the current color setting,
	 */

	protected void updateColorImage() {

		Display display = fButton.getDisplay();

		GC gc = new GC(fImage);
		gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
		gc.drawRectangle(0, 2, fExtent.x - 1, fExtent.y - 4);

		if (fColor != null)
			fColor.dispose();

		fColor = new Color(display, fColorValue);
		gc.setBackground(fColor);
		gc.fillRectangle(1, 3, fExtent.x - 2, fExtent.y - 5);
		gc.dispose();

		fButton.setImage(fImage);
	}
}
