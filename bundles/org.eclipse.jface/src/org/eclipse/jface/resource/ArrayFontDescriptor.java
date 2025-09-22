/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
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
package org.eclipse.jface.resource;

import java.util.Arrays;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/**
 * Describes a Font using an array of FontData
 *
 * @since 3.1
 */
final class ArrayFontDescriptor extends FontDescriptor {

	private final FontData[] data;
	private Font originalFont = null;

	/**
	 * Creates a font descriptor for a font with the given name, height,
	 * and style. These arguments are passed directly to the constructor
	 * of Font.
	 *
	 * @param data FontData describing the font to create
	 *
	 * @see org.eclipse.swt.graphics.Font#Font(org.eclipse.swt.graphics.Device, org.eclipse.swt.graphics.FontData)
	 * @since 3.1
	 */
	public ArrayFontDescriptor(FontData[] data) {
		this.data = data;
	}

	/**
	 * Creates a font descriptor that describes the given font.
	 *
	 * @param originalFont font to be described
	 *
	 * @see FontDescriptor#createFrom(org.eclipse.swt.graphics.Font)
	 * @since 3.1
	 */
	public ArrayFontDescriptor(Font originalFont) {
		this(originalFont.getFontData());
		this.originalFont = originalFont;
	}

	@Override
	public FontData[] getFontData() {
		// Copy the original array to ensure that callers will not modify it
		return copy(data);
	}


	@Override
	public Font createFont(Device device) {

		// If this descriptor is an existing font, then we can return the original font
		// if this is the same device.
		if (originalFont != null) {
			// If we're allocating on the same device as the original font, return the original.
			if (originalFont.getDevice() == device) {
				return originalFont;
			}
		}

		return new Font(device, data);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ArrayFontDescriptor descr) {
			if (descr.originalFont != originalFont) {
				return false;
			}

			if (originalFont != null) {
				return true;
			}

			if (!Arrays.equals(data, descr.data)) {
				return false;
			}

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		if (originalFont != null) {
			return originalFont.hashCode();
		}
		return Arrays.hashCode(data);
	}

	@Override
	public void destroyFont(Font previouslyCreatedFont) {
		if (previouslyCreatedFont == originalFont) {
			return;
		}
		previouslyCreatedFont.dispose();
	}

}
