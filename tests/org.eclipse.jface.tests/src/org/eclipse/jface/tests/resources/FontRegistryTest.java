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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.core.runtime.Platform.OS;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
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

	@Test
	public void defaultFont_isStableAcrossLookupsOfOtherNames() {
		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });

		Font defaultFont = fontRegistry.get(JFaceResources.DEFAULT_FONT);
		fontRegistry.get("myfont");
		fontRegistry.get("neverRegisteredName");

		assertSame(defaultFont, fontRegistry.get(JFaceResources.DEFAULT_FONT));
	}

	@Test
	public void put_onNameOnlyResolvedViaDefaultFallback_doesNotStaleDefaultFontsBoldAndItalic() {
		FontRegistry fontRegistry = new FontRegistry();
		Font defaultBold = fontRegistry.getBold(JFaceResources.DEFAULT_FONT);
		Font defaultItalic = fontRegistry.getItalic(JFaceResources.DEFAULT_FONT);

		// never explicitly registered, so this only ever resolves via the default-font fallback
		fontRegistry.get("neverRegisteredName");

		// registering data for that name must not disturb the still-live default font record
		fontRegistry.put("neverRegisteredName", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });

		assertSame(defaultBold, fontRegistry.getBold(JFaceResources.DEFAULT_FONT));
		assertSame(defaultItalic, fontRegistry.getItalic(JFaceResources.DEFAULT_FONT));
	}

	@Test
	public void get_fontFromNonUIThreadFallback_doesNotOverwriteDefaultFont() throws Throwable {
		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
		Font defaultFont = fontRegistry.get(JFaceResources.DEFAULT_FONT);

		AtomicReference<Font> fontFromNonUIThread = new AtomicReference<>();
		AtomicReference<Throwable> failureFromNonUIThread = new AtomicReference<>();
		Thread nonUiThread = new Thread(() -> {
			try {
				fontFromNonUIThread.set(fontRegistry.get("myfont"));
			} catch (Throwable t) {
				failureFromNonUIThread.set(t);
			}
		});
		nonUiThread.start();
		nonUiThread.join();

		if (failureFromNonUIThread.get() != null) {
			throw failureFromNonUIThread.get();
		}
		assertSame(defaultFont, fontFromNonUIThread.get());
		assertSame(defaultFont, fontRegistry.get(JFaceResources.DEFAULT_FONT));
	}

	@Test
	public void get_returnsSameFontInstanceOnRepeatedCalls() {
		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });

		Font first = fontRegistry.get("myfont");
		Font second = fontRegistry.get("myfont");

		assertSame(first, second);
	}

	@Test
	public void get_forNameThatWasNeverRegistered_returnsDefaultFontAndIsStableAfterwards() {
		FontRegistry fontRegistry = new FontRegistry();

		Font first = fontRegistry.get("neverRegisteredName");
		Font second = fontRegistry.get("neverRegisteredName");

		assertSame(fontRegistry.get(JFaceResources.DEFAULT_FONT), first);
		assertSame(first, second);
	}

	@Test
	public void get_forNameRegisteredWithoutAnyFontData_returnsDefaultFont() {
		FontRegistry fontRegistry = new FontRegistry();
		// an empty array leaves nothing to filter, so filterData() yields no usable data at all
		fontRegistry.put("fontWithoutData", new FontData[0]);

		Font first = fontRegistry.get("fontWithoutData");
		Font second = fontRegistry.get("fontWithoutData");

		assertSame(fontRegistry.get(JFaceResources.DEFAULT_FONT), first,
				"a name that cannot be resolved to any font data must fall back to the default font");
		assertSame(first, second);
	}

	@Test
	public void getBoldAndGetItalic_returnSameInstanceOnRepeatedCalls() {
		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });

		assertSame(fontRegistry.getBold("myfont"), fontRegistry.getBold("myfont"));
		assertSame(fontRegistry.getItalic("myfont"), fontRegistry.getItalic("myfont"));
	}

	@Test
	public void put_firesPropertyChangeOnlyWhenDataActuallyChanges() {
		FontRegistry fontRegistry = new FontRegistry();
		List<PropertyChangeEvent> events = new ArrayList<>();
		fontRegistry.addListener(events::add);

		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
		assertEquals(1, events.size());
		assertEquals("myfont", events.get(0).getProperty());

		// re-putting the same data must not fire a change
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
		assertEquals(1, events.size());

		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 18, SWT.NORMAL) });
		assertEquals(2, events.size());
	}

	@Test
	public void put_withNewData_disposesOldFontOnlyOnDisplayDispose() {
		assumeTrue(OS.isWindows(), "multiple Display instance only allowed on Windows");

		FontRegistry fontRegistry = new FontRegistry();
		Display secondDisplay = initializeDisplayInSeparateThread();
		try {
			Font original = secondDisplay.syncCall(() -> {
				fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
				return fontRegistry.get("myfont");
			});

			secondDisplay
					.syncExec(() -> fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 18, SWT.BOLD) }));
			// the font may still be in use elsewhere, so it must not be disposed right away
			assertFalse(original.isDisposed(), "previous font must stay usable until its display is disposed");

			secondDisplay.syncExec(secondDisplay::dispose);
			assertTrue(original.isDisposed(), "stale font must be disposed once its display is disposed");
		} finally {
			if (!secondDisplay.isDisposed()) {
				secondDisplay.syncExec(secondDisplay::dispose);
			}
		}
	}

	@Test
	public void put_withNewData_invalidatesCachedBoldAndItalicFonts() {
		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
		Font originalBold = fontRegistry.getBold("myfont");
		Font originalItalic = fontRegistry.getItalic("myfont");

		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 18, SWT.NORMAL) });

		Font updatedBold = fontRegistry.getBold("myfont");
		Font updatedItalic = fontRegistry.getItalic("myfont");

		assertNotEquals(originalBold, updatedBold);
		assertNotEquals(originalItalic, updatedItalic);
		assertEquals(18, updatedBold.getFontData()[0].getHeight());
		assertEquals(18, updatedItalic.getFontData()[0].getHeight());
	}

	@Test
	public void put_withNewData_invalidatesPreviouslyCachedFont() {
		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
		Font original = fontRegistry.get("myfont");

		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 18, SWT.BOLD) });
		Font updated = fontRegistry.get("myfont");

		assertNotEquals(original, updated);
		assertEquals(18, updated.getFontData()[0].getHeight());
	}

	@Test
	public void put_replacingRealizedFont_doesNotRegisterDefaultFontAsSideEffect() {
		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
		fontRegistry.get("myfont"); // realize it, so replacing it has fonts to retire

		// retiring those fonts compares them against the default font, which must not
		// realize and register a default font that nobody has asked for yet
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 18, SWT.NORMAL) });

		assertFalse(fontRegistry.hasValueFor(JFaceResources.DEFAULT_FONT),
				"replacing an unrelated font must not register default font data as a side effect");
		assertFalse(fontRegistry.getKeySet().contains(JFaceResources.DEFAULT_FONT),
				"replacing an unrelated font must not add the default font to the key set");
	}

	@Test
	public void hasValueForAndGetKeySet_reflectOnlyRegisteredNames() {
		FontRegistry fontRegistry = new FontRegistry();
		assertFalse(fontRegistry.hasValueFor("myfont"));
		assertFalse(fontRegistry.getKeySet().contains("myfont"));

		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
		assertTrue(fontRegistry.hasValueFor("myfont"));
		assertTrue(fontRegistry.getKeySet().contains("myfont"));

		// falling back to the default for an unregistered name must not register it
		fontRegistry.get("neverRegisteredName");
		assertFalse(fontRegistry.hasValueFor("neverRegisteredName"));
		assertFalse(fontRegistry.getKeySet().contains("neverRegisteredName"));
	}

	@Test
	public void getFontDataAndGetDescriptor_fallBackToDefaultForUnregisteredName() {
		FontRegistry fontRegistry = new FontRegistry();

		assertArrayEquals(fontRegistry.getFontData(JFaceResources.DEFAULT_FONT),
				fontRegistry.getFontData("neverRegisteredName"));

		FontDescriptor defaultDescriptor = fontRegistry.getDescriptor(JFaceResources.DEFAULT_FONT);
		FontDescriptor fallbackDescriptor = fontRegistry.getDescriptor("neverRegisteredName");
		assertEquals(defaultDescriptor, fallbackDescriptor);
	}

}
