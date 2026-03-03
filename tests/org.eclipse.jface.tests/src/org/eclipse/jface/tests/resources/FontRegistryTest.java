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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.core.runtime.Platform.OS;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.Test;

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
		assumeTrue(OS.isWindows(), "multiple Display instance only allowed on Windows");

		FontRegistry fontRegistry = new FontRegistry();
		testMultipleDisplayDispose(fontRegistry::defaultFont);
	}

	@Test
	public void multipleDisplayDispose_boldFont() {
		assumeTrue(OS.isWindows(), "multiple Display instance only allowed on Windows");

		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.get(JFaceResources.DEFAULT_FONT);
		testMultipleDisplayDispose(() -> fontRegistry.getBold(JFaceResources.DEFAULT_FONT));
	}

	@Test
	public void multipleDisplay_italicFont() {
		assumeTrue(OS.isWindows(), "multiple Display instance only allowed on Windows");

		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.get(JFaceResources.DEFAULT_FONT);
		testMultipleDisplayDispose(() -> fontRegistry.getItalic(JFaceResources.DEFAULT_FONT));
	}

	private static void testMultipleDisplayDispose(Supplier<Font> fontSupplier) {
		assumeTrue(OS.isWindows(), "multiple Display instance only allowed on Windows");

		Display secondDisplay = initializeDisplayInSeparateThread();
		Font fontOnSecondDisplay = secondDisplay.syncCall(fontSupplier::get);

		Font fontOnThisDisplayBeforeSecondDisplayDispose = fontSupplier.get();
		Device displayOfFontOnSecondDisplay = fontOnSecondDisplay.getDevice();
		// font registry returns same font for every display
		assertEquals(secondDisplay, displayOfFontOnSecondDisplay);
		assertEquals(fontOnThisDisplayBeforeSecondDisplayDispose, fontOnSecondDisplay);

		// after disposing font's display, registry should reinitialize the font
		secondDisplay.syncExec(secondDisplay::dispose);
		assertTrue(fontOnSecondDisplay.isDisposed());
		Font fontOnThisDisplayAfterSecondDisplayDispose = fontSupplier.get();
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
			assertFalse(Instant.now().isAfter(maximumEndTime), "display was not instantiated in time");
			Thread.yield();
		}
	}

}
