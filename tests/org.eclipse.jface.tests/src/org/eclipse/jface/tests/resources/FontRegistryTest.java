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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Platform.OS;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Device;
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

	@Test
	public void multipleDisplayDispose() {
		assumeTrue("multiple Display instance only allowed on Windows", OS.isWindows());

		FontRegistry fontRegistry = new FontRegistry();
		Display secondDisplay = initializeDisplayInSeparateThread();
		Font fontOnSecondDisplay = secondDisplay.syncCall(fontRegistry::defaultFont);

		Font fontOnThisDisplayBeforeSecondDisplayDispose = fontRegistry.defaultFont();
		Device displayOfFontOnSecondDisplay = fontOnSecondDisplay.getDevice();
		// font registry returns same font for every display
		assertEquals(secondDisplay, displayOfFontOnSecondDisplay);
		assertEquals(fontOnThisDisplayBeforeSecondDisplayDispose, fontOnSecondDisplay);

		// after disposing font's display, registry should reinitialize the font
		secondDisplay.syncExec(secondDisplay::dispose);
		assertTrue(fontOnSecondDisplay.isDisposed());
		Font fontOnThisDisplayAfterSecondDisplayDispose = fontRegistry.defaultFont();
		assertNotEquals(fontOnThisDisplayAfterSecondDisplayDispose, fontOnSecondDisplay);
	}

	private static Display initializeDisplayInSeparateThread() {
		AtomicReference<Display> displayReference = new AtomicReference<>();
		new Thread(() -> {
			Display display = new Display();
			displayReference.set(display);
			while (!display.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		}, "async display creation").start();
		waitForDisplayInstantiation(displayReference);
		return displayReference.get();
	}

	private static void waitForDisplayInstantiation(AtomicReference<Display> displayReference) {
		Instant maximumEndTime = Instant.now().plus(Duration.ofSeconds(10));
		while (displayReference.get() == null) {
			assertFalse("display was not instantiated in time", Instant.now().isAfter(maximumEndTime));
			Thread.yield();
		}
	}

}
