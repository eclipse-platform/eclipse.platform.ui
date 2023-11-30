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
package org.eclipse.jface.preference;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A field editor for a color type preference.
 */
public class ColorFieldEditor extends FieldEditor {

	/**
	 * The color selector, or <code>null</code> if none.
	 */
	private ColorSelector colorSelector;

	/**
	 * Creates a new color field editor
	 */
	protected ColorFieldEditor() {
		//No default behavior
	}

	/**
	 * Creates a color field editor.
	 *
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 */
	public ColorFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		((GridData) colorSelector.getButton().getLayoutData()).horizontalSpan = numColumns - 1;
	}

	/**
	 * Computes the size of the color image displayed on the button.
	 * <p>
	 * This is an internal method and should not be called by clients.
	 * </p>
	 *
	 * @param window
	 *            the window to create a GC on for calculation.
	 * @return Point The image size
	 */
	protected Point computeImageSize(Control window) {
		// Make the image height as high as a corresponding character. This
		// makes sure that the button has the same size as a "normal" text
		// button.
		GC gc = new GC(window);
		Font f = JFaceResources.getFontRegistry().get(
				JFaceResources.DEFAULT_FONT);
		gc.setFont(f);
		int height = gc.getFontMetrics().getHeight();
		gc.dispose();
		return new Point(height * 3 - 6, height);
	}


	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns - 1;
		control.setLayoutData(gd);

		Button colorButton = getChangeControl(parent);
		colorButton.setLayoutData(new GridData());

	}


	@Override
	protected void doLoad() {
		if (colorSelector == null) {
			return;
		}
		colorSelector.setColorValue(PreferenceConverter.getColor(
				getPreferenceStore(), getPreferenceName()));
	}

	@Override
	protected void doLoadDefault() {
		if (colorSelector == null) {
			return;
		}
		colorSelector.setColorValue(PreferenceConverter.getDefaultColor(
				getPreferenceStore(), getPreferenceName()));
	}

	@Override
	protected void doStore() {
		PreferenceConverter.setValue(getPreferenceStore(), getPreferenceName(),
				colorSelector.getColorValue());
	}

	/**
	 * Get the color selector used by the receiver.
	 *
	 * @return ColorSelector/
	 */
	public ColorSelector getColorSelector() {
		return colorSelector;
	}

	/**
	 * Returns the change button for this field editor.
	 *
	 * @param parent
	 *            The control to create the button in if required.
	 * @return the change button
	 */
	protected Button getChangeControl(Composite parent) {
		if (colorSelector == null) {
			colorSelector = new ColorSelector(parent);
			colorSelector.addListener(event -> {
				ColorFieldEditor.this.fireValueChanged(event.getProperty(),
						event.getOldValue(), event.getNewValue());
				setPresentsDefaultValue(false);
			});

		} else {
			checkParent(colorSelector.getButton(), parent);
		}
		return colorSelector.getButton();
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		getChangeControl(parent).setEnabled(enabled);
	}

}
