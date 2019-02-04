/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.resources;

import static org.junit.Assert.assertArrayEquals;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.junit.Test;

public class FontRegistryTest {

	@Test
	public void testBug544026() {
		FontData[] fontData = JFaceResources.getDefaultFont().getFontData();
		// Resize default font data
		fontData[0].setHeight(fontData[0].getHeight() + 1);

		// Create a temporary font to get accurate dimensions in font data
		Font temp = new Font(Display.getCurrent(), fontData);
		fontData = temp.getFontData();
		temp.dispose();

		// Replace default font in FontRegistry
		JFaceResources.getFontRegistry().put(JFaceResources.DEFAULT_FONT, fontData);

		// Ensure JFaceResources.getDefaultFont() returns resized font data
		assertArrayEquals(fontData, JFaceResources.getDefaultFont().getFontData());
	}

}
