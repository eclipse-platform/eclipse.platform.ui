/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
package org.eclipse.jface.preference;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * The <code>ColorSelector</code> is a wrapper for a button that displays a
 * swatch of the selected color and allows the user to change the selection
 * using the operating system's native color chooser dialog.
 */
public class ColorSelector extends EventManager {
	/**
	 * Property name that signifies the selected color of this
	 * <code>ColorSelector</code> has changed.
	 *
	 * @since 3.0
	 */
	public static final String PROP_COLORCHANGE = "colorValue"; //$NON-NLS-1$

	private Button fButton;

	private RGB fColorValue;

	private Point fExtent;

	private Image fImage;

	/**
	 * Create a new instance of the receiver and the button that it wrappers in
	 * the supplied parent <code>Composite</code>.
	 *
	 * @param parent
	 *            The parent of the button.
	 */
	public ColorSelector(Composite parent) {
		fButton = new Button(parent, SWT.PUSH);
		fExtent = computeImageSize(parent);
		updateColorImage();
		fButton.addSelectionListener(widgetSelectedAdapter(event -> open()));
		fButton.addDisposeListener(event -> {
			if (fImage != null) {
				fImage.dispose();
				fImage = null;
			}
		});
		fButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = JFaceResources.getString("ColorSelector.Name"); //$NON-NLS-1$
			}
		});
	}

	/**
	 * Adds a property change listener to this <code>ColorSelector</code>.
	 * Events are fired when the color in the control changes via the user
	 * clicking an selecting a new one in the color dialog. No event is fired in
	 * the case where <code>setColorValue(RGB)</code> is invoked.
	 *
	 * @param listener
	 *            a property change listener
	 * @since 3.0
	 */
	public void addListener(IPropertyChangeListener listener) {
		addListenerObject(listener);
	}

	/**
	 * Compute the size of the image to be displayed.
	 *
	 * @param window -
	 *            the window used to calculate
	 * @return <code>Point</code>
	 */
	private Point computeImageSize(Control window) {
		GC gc = new GC(window);
		Font f = JFaceResources.getFontRegistry().get(
				JFaceResources.DIALOG_FONT);
		gc.setFont(f);
		int height = gc.getFontMetrics().getHeight();
		gc.dispose();
		return new Point(height * 3 - 6, height);
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
	 * Removes the given listener from this <code>ColorSelector</code>. Has
	 * no effect if the listener is not registered.
	 *
	 * @param listener
	 *            a property change listener
	 * @since 3.0
	 */
	public void removeListener(IPropertyChangeListener listener) {
		removeListenerObject(listener);
	}

	/**
	 * Set the current color value and update the control.
	 *
	 * @param rgb
	 *            The new color.
	 */
	public void setColorValue(RGB rgb) {
		fColorValue = rgb;
		updateColorImage();
	}

	/**
	 * Set whether or not the button is enabled.
	 *
	 * @param state
	 *            the enabled state.
	 */
	public void setEnabled(boolean state) {
		getButton().setEnabled(state);
	}

	/**
	 * Update the image being displayed on the button using the current color
	 * setting.
	 */
	protected void updateColorImage() {
		if (fImage != null) {
			fImage.dispose();
		}

		final Display display = fButton.getDisplay();

		fImage = new Image(display, new ImageDataProvider() {

			@Override
			public ImageData getImageData(int zoom) {
				Image image = new Image(display, fExtent.x, fExtent.y);
				GC gc = new GC(image);

				RGB color = getColorValue();
				gc.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_BORDER));
				if (color != null) {
					gc.setBackground(new Color(display, color));
					gc.fillRectangle(image.getBounds());
				}
				gc.setLineWidth(2);
				gc.drawRectangle(image.getBounds());
				gc.dispose();

				ImageData data = image.getImageData(zoom);
				image.dispose();
				return data;
			}
		});
		fButton.setImage(fImage);
	}

	/**
	 * Activate the editor for this selector. This causes the color selection
	 * dialog to appear and wait for user input.
	 *
	 * @since 3.2
	 */
	public void open() {
		ColorDialog colorDialog = new ColorDialog(fButton.getShell());
		colorDialog.setRGB(fColorValue);
		RGB newColor = colorDialog.open();
		if (newColor != null) {
			RGB oldValue = fColorValue;
			fColorValue = newColor;
			final Object[] finalListeners = getListeners();
			if (finalListeners.length > 0) {
				PropertyChangeEvent pEvent = new PropertyChangeEvent(
						this, PROP_COLORCHANGE, oldValue, newColor);
				for (Object finalListener : finalListeners) {
					IPropertyChangeListener listener = (IPropertyChangeListener) finalListener;
					listener.propertyChange(pEvent);
				}
			}
			updateColorImage();
		}
	}
}
