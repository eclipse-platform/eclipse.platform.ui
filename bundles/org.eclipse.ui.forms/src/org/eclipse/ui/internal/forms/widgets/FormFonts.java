/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.forms.widgets;

import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class FormFonts {
	private static FormFonts instance;

	public static FormFonts getInstance() {
		if (instance == null)
			instance = new FormFonts();
		return instance;
	}

	private ResourceManagerManger manager = new ResourceManagerManger();
	private HashMap<Font, BoldFontDescriptor> descriptors;

	private FormFonts() {
	}

	private static class BoldFontDescriptor extends FontDescriptor {
		private FontData[] fFontData;

		BoldFontDescriptor (Font font) {
			fFontData = font.getFontData();
			for (FontData element : fFontData) {
				element.setStyle(element.getStyle() | SWT.BOLD);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof BoldFontDescriptor) {
				BoldFontDescriptor desc = (BoldFontDescriptor)obj;
				return Arrays.equals(fFontData, desc.fFontData);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(fFontData);
		}

		@Override
		public Font createFont(Device device) throws DeviceResourceException {
			return new Font(device, fFontData);
		}

		@Override
		public void destroyFont(Font previouslyCreatedFont) {
			previouslyCreatedFont.dispose();
		}
	}

	public Font getBoldFont(Display display, Font font) {
		checkHashMaps();
		BoldFontDescriptor desc = new BoldFontDescriptor(font);
		Font result = manager.getResourceManager(display).create(desc);
		descriptors.put(result, desc);
		return result;
	}

	public boolean markFinished(Font boldFont, Display display) {
		checkHashMaps();
		BoldFontDescriptor desc = descriptors.get(boldFont);
		if (desc != null) {
			LocalResourceManager resourceManager = manager.getResourceManager(display);
			resourceManager.destroy(desc);
			if (resourceManager.find(desc) == null) {
				descriptors.remove(boldFont);
				validateHashMaps();
			}
			return true;

		}
		// if the image was not found, dispose of it for the caller
		boldFont.dispose();
		return false;
	}

	private void checkHashMaps() {
		if (descriptors == null)
			descriptors = new HashMap<>();
	}

	private void validateHashMaps() {
		if (descriptors.isEmpty())
			descriptors = null;
	}
}
