/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.css.swt;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.IntStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

/**
 * Selects the installed face for CSS font weights other than regular and bold.
 */
public final class FontWeights {

	/** Indexed by weight / 100 - 1. */
	private static final String[][] FACE_NAMES = { //
			{ "Thin", "Hairline" }, //
			{ "ExtraLight", "Extra Light", "UltraLight", "Ultra Light" }, //
			{ "Light", "SemiLight", "Semi Light" }, //
			{ "Regular" }, //
			{ "Medium" }, //
			{ "SemiBold", "Semi Bold", "DemiBold", "Demi Bold" }, //
			{ "Bold" }, //
			{ "ExtraBold", "Extra Bold", "UltraBold", "Ultra Bold" }, //
			{ "Black", "Heavy" } };

	/** Indexed by weight / 100 - 1. */
	private static final String[] PANGO_WEIGHTS = { "Thin", "Ultra-Light", "Light", "Regular", "Medium", "Semi-Bold",
			"Bold", "Ultra-Bold", "Heavy" };

	private static final int REGULAR_STEP = 4;

	private static final int MEDIUM_STEP = 5;

	private static final int BOLD_STEP = 7;

	private static final Field FACE_FIELD = faceField();

	private FontWeights() {
	}

	/**
	 * Makes <code>fontData</code> use the installed face closest to
	 * <code>weight</code>. Call it after name, height and style are set.
	 */
	public static void apply(FontData fontData, int weight) {
		int step = Math.clamp(Math.round(weight / 100f), 1, FACE_NAMES.length);
		if (step == REGULAR_STEP || step == BOLD_STEP) {
			return;
		}
		try {
			String platform = SWT.getPlatform();
			if ("gtk".equals(platform)) {
				applyPangoWeight(fontData, step);
			} else if ("win32".equals(platform)) {
				applyLogFontWeight(fontData, step);
			} else if ("cocoa".equals(platform)) {
				applyFace(fontData, step);
			}
		} catch (ReflectiveOperationException | RuntimeException e) {
			// keep the plain style
		}
	}

	/**
	 * The weight steps (weight / 100) to look for in turn when a face of
	 * <code>step</code> is wanted, nearest first as CSS font matching orders
	 * them. It stops before regular or bold, which every family has.
	 */
	public static int[] stepsToTry(int step) {
		IntStream.Builder steps = IntStream.builder();
		int direction = step <= MEDIUM_STEP ? -1 : 1;
		for (int candidate = step; candidate >= 1 && candidate <= FACE_NAMES.length; candidate += direction) {
			if (candidate == REGULAR_STEP || candidate == BOLD_STEP) {
				return steps.build().toArray();
			}
			steps.add(candidate);
		}
		for (int candidate = step - direction; candidate != REGULAR_STEP
				&& candidate != BOLD_STEP; candidate -= direction) {
			steps.add(candidate);
		}
		return steps.build().toArray();
	}

	/**
	 * Whether two font data use the same face, which {@link FontData#equals}
	 * ignores on GTK and macOS.
	 */
	public static boolean isSameFace(FontData fontData, FontData other) {
		if (FACE_FIELD == null) {
			return true;
		}
		try {
			return Objects.deepEquals(FACE_FIELD.get(fontData), FACE_FIELD.get(other));
		} catch (IllegalAccessException e) {
			return true;
		}
	}

	private static Field faceField() {
		try {
			return switch (SWT.getPlatform()) {
			case "gtk" -> FontData.class.getField("string");
			case "cocoa" -> FontData.class.getField("nsName");
			default -> null;
			};
		} catch (NoSuchFieldException e) {
			return null;
		}
	}

	private static void applyPangoWeight(FontData fontData, int step) throws IllegalAccessException {
		StringBuilder description = new StringBuilder(fontData.getName()).append(", ");
		if ((fontData.getStyle() & SWT.ITALIC) != 0) {
			description.append("Italic ");
		}
		description.append(PANGO_WEIGHTS[step - 1]).append(' ').append(fontData.getHeight()).append('\0');
		FACE_FIELD.set(fontData, description.toString().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * GDI ships other weights as families of their own, like "Segoe UI Semibold".
	 * Without such a face the regular or bold of the style is kept, since GDI
	 * would embolden the regular face for a weight like 600.
	 */
	private static void applyLogFontWeight(FontData fontData, int step) throws ReflectiveOperationException {
		Display display = Display.getCurrent();
		if (display == null) {
			return;
		}
		for (int candidate : stepsToTry(step)) {
			for (String face : FACE_NAMES[candidate - 1]) {
				FontData[] family = display.getFontList(fontData.getName() + ' ' + face, true);
				if (family.length > 0) {
					// GDI matches case-insensitively, take the installed spelling
					fontData.setName(family[0].getName());
					setLogFontWeight(fontData, candidate * 100);
					return;
				}
			}
		}
	}

	private static void setLogFontWeight(FontData fontData, int weight) throws ReflectiveOperationException {
		Object logFont = FontData.class.getField("data").get(fontData);
		logFont.getClass().getField("lfWeight").setInt(logFont, weight);
	}

	private static void applyFace(FontData fontData, int step) throws IllegalAccessException {
		Display display = Display.getCurrent();
		if (display == null) {
			return;
		}
		boolean italic = (fontData.getStyle() & SWT.ITALIC) != 0;
		FontData[] faces = display.getFontList(fontData.getName(), true);
		for (int candidate : stepsToTry(step)) {
			for (FontData face : faces) {
				String nsName = (String) FACE_FIELD.get(face);
				if (((face.getStyle() & SWT.ITALIC) != 0) == italic && nsName != null && isFace(nsName, candidate)) {
					// setStyle clears nsName, and bold would embolden the face again
					fontData.setStyle(fontData.getStyle() & ~SWT.BOLD);
					FACE_FIELD.set(fontData, nsName);
					return;
				}
			}
		}
	}

	private static boolean isFace(String postScriptName, int step) {
		String suffix = postScriptName.substring(postScriptName.lastIndexOf('-') + 1).toLowerCase(Locale.ENGLISH);
		suffix = suffix.replace("italic", "").replace("oblique", "");
		for (String face : FACE_NAMES[step - 1]) {
			if (suffix.equals(face.replace(" ", "").toLowerCase(Locale.ENGLISH))) {
				return true;
			}
		}
		return false;
	}
}
