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
	public void multipleDisplayDispose_noDisposeOtherThreadFonts() {
		assumeTrue(OS.isWindows(), "multiple Display instance only allowed on Windows");

		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
		Font mainFont = fontRegistry.get("myfont");
		testMultipleDisplayDispose(fontRegistry::defaultFont);
		assertFalse(mainFont.isDisposed(), "FontRegistry should not dispose fonts on other displays");
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
	public void multipleDisplayDispose_italicFont() {
		assumeTrue(OS.isWindows(), "multiple Display instance only allowed on Windows");

		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.get(JFaceResources.DEFAULT_FONT);
		testMultipleDisplayDispose(() -> fontRegistry.getItalic(JFaceResources.DEFAULT_FONT));
	}

	@Test
	public void multipleDisplay_reusesMainDisplayFont_whenStyleAlreadyCached() {
		assumeTrue(OS.isWindows(), "multiple Display instance only allowed on Windows");

		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
		Font mainFont = fontRegistry.get("myfont");

		Display secondDisplay = initializeDisplayInSeparateThread();
		try {
			Font fontFromSecondDisplayThread = secondDisplay.syncCall(() -> fontRegistry.get("myfont"));
			assertEquals(mainFont, fontFromSecondDisplayThread,
					"a font already realized on the main display should be reused from any other display's thread");
			assertEquals(Display.getCurrent(), fontFromSecondDisplayThread.getDevice(),
					"the reused font is still owned by the main display");
		} finally {
			secondDisplay.syncExec(secondDisplay::dispose);
		}
	}

	@Test
	public void multipleDisplay_createsOwnFont_whenStyleNotYetCachedOnMainDisplay() {
		assumeTrue(OS.isWindows(), "multiple Display instance only allowed on Windows");

		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
		fontRegistry.get("myfont"); // only realizes the NORMAL style on the main display

		Display secondDisplay = initializeDisplayInSeparateThread();
		Font boldFontOnSecondDisplay;
		try {
			boldFontOnSecondDisplay = secondDisplay.syncCall(() -> fontRegistry.getBold("myfont"));

			assertEquals(secondDisplay, boldFontOnSecondDisplay.getDevice(),
					"bold style is not yet cached on the main display, so it must be created on the requesting display");
		} finally {
			secondDisplay.syncExec(secondDisplay::dispose);
		}
		assertTrue(boldFontOnSecondDisplay.isDisposed(),
				"fonts created for the second display must be disposed together with it");
	}

	@Test
	public void multipleDisplay_reusesMainDisplayFont_onceStyleBecomesCachedThere() {
		assumeTrue(OS.isWindows(), "multiple Display instance only allowed on Windows");

		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
		fontRegistry.get("myfont"); // only realizes the NORMAL style on the main display

		Display firstSecondDisplay = initializeDisplayInSeparateThread();
		Font boldFontOnFirstSecondDisplay = firstSecondDisplay.syncCall(() -> fontRegistry.getBold("myfont"));
		assertEquals(firstSecondDisplay, boldFontOnFirstSecondDisplay.getDevice(),
				"bold style is not yet cached on the main display, so it must be created on the requesting display");
		firstSecondDisplay.syncExec(firstSecondDisplay::dispose);

		// the main display now also realizes the bold style
		Font boldFontOnMainDisplay = fontRegistry.getBold("myfont");

		Display secondSecondDisplay = initializeDisplayInSeparateThread();
		try {
			Font boldFontOnSecondSecondDisplay = secondSecondDisplay.syncCall(() -> fontRegistry.getBold("myfont"));
			assertEquals(boldFontOnMainDisplay, boldFontOnSecondSecondDisplay,
					"once the main display has realized the requested style, later lookups from any display must reuse it");
		} finally {
			secondSecondDisplay.syncExec(secondSecondDisplay::dispose);
		}
	}

	@Test
	public void multipleDisplay_keepsOwnFont_whenMainDisplayRealizesItLater() {
		assumeTrue(OS.isWindows(), "multiple Display instance only allowed on Windows");

		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });

		Display secondDisplay = initializeDisplayInSeparateThread();
		try {
			// the second display realizes the font before the main display has one to reuse
			Font fontOnSecondDisplay = secondDisplay.syncCall(() -> fontRegistry.get("myfont"));
			assertEquals(secondDisplay, fontOnSecondDisplay.getDevice(),
					"nothing to reuse yet, so the second display must realize a font of its own");

			// the main display realizing the same font afterwards must not change what
			// the second display gets, or it would silently switch instances mid-flight
			fontRegistry.get("myfont");

			Font fontOnSecondDisplayAgain = secondDisplay.syncCall(() -> fontRegistry.get("myfont"));
			assertSame(fontOnSecondDisplay, fontOnSecondDisplayAgain,
					"a display that realized a font itself must keep getting that same instance");
		} finally {
			secondDisplay.syncExec(secondDisplay::dispose);
		}
	}

	@Test
	public void put_invalidatesCachedFont_onAllDisplays() {
		assumeTrue(OS.isWindows(), "multiple Display instance only allowed on Windows");

		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
		fontRegistry.get("myfont"); // only realizes the NORMAL style on the main display

		Display secondDisplay = initializeDisplayInSeparateThread();
		try {
			// a style the main display has not realized, so the second display is
			// guaranteed to hold a font record of its own; only then does this
			// actually cover invalidation across displays
			Font boldFontOnSecondDisplay = secondDisplay.syncCall(() -> fontRegistry.getBold("myfont"));
			assertEquals(secondDisplay, boldFontOnSecondDisplay.getDevice(),
					"the second display must have realized a font of its own");

			// changing the font data must invalidate the cached font on every display, not just the main one
			fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 20, SWT.NORMAL) });

			Font updatedBoldFontOnSecondDisplay = secondDisplay.syncCall(() -> fontRegistry.getBold("myfont"));
			assertNotEquals(boldFontOnSecondDisplay, updatedBoldFontOnSecondDisplay,
					"put() must invalidate the cached font on the second display too");
			assertEquals(20, updatedBoldFontOnSecondDisplay.getFontData()[0].getHeight());
		} finally {
			secondDisplay.syncExec(secondDisplay::dispose);
		}
	}

	@Test
	public void cleanOnDisplayDisposalFalse_cachesFontAcrossRepeatedCalls() {
		FontRegistry fontRegistry = new FontRegistry(Display.getCurrent(), false);
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });

		Font first = fontRegistry.get("myfont");
		Font second = fontRegistry.get("myfont");

		assertEquals(first, second,
				"repeated get() calls must return the cached font, not a new one, when cleanOnDisplayDisposal is false");
	}

	@Test
	public void cleanOnDisplayDisposalFalse_doesNotAutoDisposeFontsOnSecondDisplay() {
		assumeTrue(OS.isWindows(), "multiple Display instance only allowed on Windows");

		FontRegistry fontRegistry = new FontRegistry(Display.getCurrent(), false);
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });

		Display secondDisplay = initializeDisplayInSeparateThread();
		Font fontOnSecondDisplay = secondDisplay.syncCall(() -> fontRegistry.get("myfont"));
		Font sameFontOnSecondDisplay = secondDisplay.syncCall(() -> fontRegistry.get("myfont"));
		assertEquals(fontOnSecondDisplay, sameFontOnSecondDisplay,
				"font must be cached per display even when cleanOnDisplayDisposal is false");

		secondDisplay.syncExec(secondDisplay::dispose);
		assertFalse(fontOnSecondDisplay.isDisposed(),
				"fonts must not be disposed automatically when cleanOnDisplayDisposal is false");
	}

	private static void testMultipleDisplayDispose(Supplier<Font> fontSupplier) {
		assumeTrue(OS.isWindows(), "multiple Display instance only allowed on Windows");

		Display secondDisplay = initializeDisplayInSeparateThread();
		// the second display is asked first on purpose: the requested font is not
		// realized on the main display yet, so there is nothing to reuse and the
		// second display has to realize (and own) a font of its own
		Font fontOnSecondDisplay = secondDisplay.syncCall(fontSupplier::get);

		Font fontOnThisDisplayBeforeSecondDisplayDispose = fontSupplier.get();
		Device displayOfFontOnSecondDisplay = fontOnSecondDisplay.getDevice();
		assertEquals(secondDisplay, displayOfFontOnSecondDisplay);
		assertNotEquals(fontOnThisDisplayBeforeSecondDisplayDispose, fontOnSecondDisplay);

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

		Font fontFromNonUIThread = callOnNonUIThread(() -> fontRegistry.get("myfont"));

		assertSame(defaultFont, fontFromNonUIThread);
		assertSame(defaultFont, fontRegistry.get(JFaceResources.DEFAULT_FONT));
	}

	@Test
	public void getBold_fromNonUIThreadFallback_reusesMainDisplaysBoldDefaultFont() throws Throwable {
		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
		// a thread without a display of its own can neither create a font nor scope a
		// lookup, so the main display's default font record is all it can fall back to
		Font defaultBold = fontRegistry.getBold(JFaceResources.DEFAULT_FONT);

		Font boldFromNonUIThread = callOnNonUIThread(() -> fontRegistry.getBold("myfont"));

		assertSame(defaultBold, boldFromNonUIThread);
		assertSame(defaultBold, fontRegistry.getBold(JFaceResources.DEFAULT_FONT));
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
	public void put_notifiesListenersOnlyAfterTheNewFontIsInEffect() {
		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
		Font originalFont = fontRegistry.get("myfont");

		AtomicReference<Font> fontSeenByListener = new AtomicReference<>();
		fontRegistry.addListener(event -> fontSeenByListener.set(fontRegistry.get("myfont")));

		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 18, SWT.NORMAL) });

		assertNotEquals(originalFont, fontSeenByListener.get(),
				"a listener must not still see the replaced font when it is notified");
		assertEquals(18, fontSeenByListener.get().getFontData()[0].getHeight());
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


	@Test
	public void getBold_fromNonUIThread_realizesBoldFontOnTheRegistrysDisplay() throws Throwable {
		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
		// only the plain default font is realized, so the bold variant still has to be created
		Font defaultFont = fontRegistry.get(JFaceResources.DEFAULT_FONT);

		Font boldFont = callOnNonUIThread(() -> fontRegistry.getBold("myfont"));

		assertEquals(SWT.BOLD, boldFont.getFontData()[0].getStyle() & SWT.BOLD,
				"a caller without a display of its own must still get a bold font");
		assertEquals(defaultFont.getDevice(), boldFont.getDevice(),
				"the bold font must be realized on the display the fallback font belongs to");
	}

	@Test
	public void getItalic_fromNonUIThread_realizesItalicFontOnTheRegistrysDisplay() throws Throwable {
		FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("myfont", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
		// only the plain default font is realized, so the italic variant still has to be created
		Font defaultFont = fontRegistry.get(JFaceResources.DEFAULT_FONT);

		Font italicFont = callOnNonUIThread(() -> fontRegistry.getItalic("myfont"));

		assertEquals(SWT.ITALIC, italicFont.getFontData()[0].getStyle() & SWT.ITALIC,
				"a caller without a display of its own must still get an italic font");
		assertEquals(defaultFont.getDevice(), italicFont.getDevice(),
				"the italic font must be realized on the display the fallback font belongs to");
	}

	private static Font callOnNonUIThread(Supplier<Font> fontSupplier) throws Throwable {
		AtomicReference<Font> font = new AtomicReference<>();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread nonUiThread = new Thread(() -> {
			try {
				font.set(fontSupplier.get());
			} catch (Throwable t) {
				failure.set(t);
			}
		}, "non-UI thread font lookup");
		nonUiThread.start();
		nonUiThread.join();

		if (failure.get() != null) {
			throw failure.get();
		}
		return font.get();
	}

}
