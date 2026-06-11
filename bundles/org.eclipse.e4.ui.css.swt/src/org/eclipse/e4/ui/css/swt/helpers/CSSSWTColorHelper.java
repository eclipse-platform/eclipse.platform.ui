/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *     Kai Toedter - added radial gradient support
 *     Robin Stocker - Bug 420035 - [CSS] Support SWT color constants in gradients
 *     Stefan Winkler <stefan@winklerweb.net> - Bug 459961
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

import static org.eclipse.e4.ui.css.swt.helpers.ThemeElementDefinitionHelper.normalizeId;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.eclipse.e4.ui.css.core.css2.CSS2ColorHelper;
import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.internal.css.swt.ColorAndFontUtil;
import org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssColor;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssList;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssNumber;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssNumeric;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssPrimitive;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssText;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssUnit;
import org.w3c.dom.css.CSSValue;

public class CSSSWTColorHelper {
	public static final String COLOR_DEFINITION_MARKER = "#";

	private static final Pattern HEX_COLOR_VALUE_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

	private static Field[] cachedFields;

	/*--------------- SWT Color Helper -----------------*/

	public static Color getSWTColor(CssColor rgbColor) {
		RGBA rgb = getRGBA(rgbColor);
		return new Color(rgb);
	}

	public static Color getSWTColor(CSSValue value, Display display) {
		if (!(value instanceof CssPrimitive primitive)) {
			return null;
		}
		Color color = display.getSystemColor(SWT.COLOR_BLACK);
		RGBA rgba = getRGBA(primitive, display);
		if (rgba != null) {
			color = new Color(rgba.rgb.red, rgba.rgb.green, rgba.rgb.blue, rgba.alpha);
		}
		return color;
	}

	private static RGBA getRGBA(CssPrimitive value, Display display) {
		RGBA rgba = getRGBA(value);
		if (rgba == null && display != null && value instanceof CssText text) {
			String name = text.value();
			if (hasColorDefinitionAsValue(name)) {
				rgba = findColorByDefinition(name);
			} else if (name.contains("-")) {
				name = name.replace('-', '_');
				rgba = process(display, name);
				if (rgba == null) {
					rgba = display.getSystemColor(SWT.COLOR_BLACK).getRGBA();
				}
			}
		}
		return rgba;
	}

	public static boolean hasColorDefinitionAsValue(CSSValue value) {
		if (value instanceof CssText text && text.kind() == CssText.Kind.STRING) {
			return hasColorDefinitionAsValue(text.value());
		}
		return false;
	}

	public static boolean hasColorDefinitionAsValue(String name) {
		if (name.startsWith(COLOR_DEFINITION_MARKER)) {
			return !HEX_COLOR_VALUE_PATTERN.matcher(name).matches();
		}
		return false;
	}

	/**
	 * Process the given string and return a corresponding RGBA object.
	 *
	 * @param value the SWT constant <code>String</code>
	 * @return the value of the SWT constant, or <code>null</code> if it could not
	 *         be determined
	 */
	private static RGBA process(Display display, String value) {
		Field [] fields = getFields();
		try {
			for (Field field : fields) {
				if (field.getName().equals(value)) {
					return display.getSystemColor(field.getInt(null)).getRGBA();
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// no op - shouldnt happen. We check for public before calling
			// getInt(null)
		}
		return null;
	}

	/**
	 * Get the SWT constant fields.
	 *
	 * @return the fields
	 * @since 3.3
	 */
	private static Field[] getFields() {
		if (cachedFields == null) {
			Class<?> clazz = SWT.class;
			Field[] allFields = clazz.getDeclaredFields();
			ArrayList<Field> applicableFields = new ArrayList<>(
					allFields.length);

			for (Field field : allFields) {
				if (field.getType() == Integer.TYPE
						&& Modifier.isStatic(field.getModifiers())
						&& Modifier.isPublic(field.getModifiers())
						&& Modifier.isFinal(field.getModifiers())
						&& field.getName().startsWith("COLOR")) { //$NON-NLS-1$

					applicableFields.add(field);
				}
			}
			cachedFields = applicableFields.toArray(new Field [applicableFields.size()]);
		}
		return cachedFields;
	}

	public static RGBA getRGBA(String name) {
		CssColor color = CSS2ColorHelper.getRGBColor(name);
		if (color != null) {
			return getRGBA(color);
		}
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		if (display != null) {
			name = name.replace('-', '_');
			if (name.startsWith("#")) { //$NON-NLS-1$
				name = name.substring(1);
			}
			return process(display, name);
		}
		return null;
	}

	public static RGBA getRGBA(CssColor color) {
		// for now, we only support solid RGB colors in CSS - our CSS model
		// as of now does not have an element for RGBAColor.
		return new RGBA((int) color.red().value(), (int) color.green().value(), (int) color.blue().value(), 255);
	}

	public static RGBA getRGBA(CSSValue value) {
		if (!(value instanceof CssPrimitive primitive)) {
			return null;
		}
		return getRGBA(primitive);
	}

	public static RGBA getRGBA(CssPrimitive value) {
		if (value instanceof CssText text
				&& (text.kind() == CssText.Kind.IDENT || text.kind() == CssText.Kind.STRING)) {
			return getRGBA(text.value());
		}
		if (value instanceof CssColor color) {
			return getRGBA(color);
		}
		return null;
	}

	public static Integer getPercent(CssPrimitive value) {
		int percent = 0;
		if (value instanceof CssNumeric numeric && numeric.unit() == CssUnit.PERCENT) {
			percent = (int) numeric.value();
		}
		return Integer.valueOf(percent);
	}

	public static Gradient getGradient(CssList list, Display display) {
		Gradient gradient = new Gradient();
		for (CSSValue value : list.values()) {
			if (!(value instanceof CssPrimitive primitive)) {
				continue;
			}
			boolean isIdent = primitive instanceof CssText text && text.kind() == CssText.Kind.IDENT;
			if (isIdent) {
				switch (value.getCssText()) {
				case "gradient":
					// Skip the keyword "gradient"
					continue;
				case "linear":
					gradient.setLinear(true);
					continue;
				case "radial":
					gradient.setLinear(false);
					continue;
				default:
					break;
				}
			}

			if (isIdent || primitive instanceof CssColor
					|| (primitive instanceof CssText text && text.kind() == CssText.Kind.STRING)) {
				RGBA rgba = getRGBA(primitive, display);
				if (rgba != null) {
					// note that in this call we lose the RGBA alpha
					// component - we do currently not support alpha
					// gradients
					gradient.addRGB(rgba, primitive);
				} else {
					// check for vertical gradient
					gradient.setVertical(!value.getCssText().equals("false"));
				}
			} else if (primitive instanceof CssNumeric numeric && numeric.unit() == CssUnit.PERCENT) {
				gradient.addPercent(getPercent(numeric));
			}
		}
		return gradient;
	}

	public static Color[] getSWTColors(Gradient grad, Display display,
			CSSEngine engine) throws Exception {
		List<CssPrimitive> values = grad.getValues();
		Color[] colors = new Color[values.size()];

		for (int i = 0; i < values.size(); i++) {
			CssPrimitive value = values.get(i);
			//We rely on the fact that when a gradient is created, it's colors are converted and in the registry
			//TODO see bug #278077
			Color color = (Color) engine.convert(value, Color.class, display);
			colors[i] = color;
		}
		return colors;
	}

	public static int[] getPercents(Gradient grad) {
		// There should be exactly one more RGBs. than percent,
		// in which case just return the percents as array
		if (grad.getRGBs().size() == grad.getPercents().size() + 1) {
			int[] percents = new int[grad.getPercents().size()];
			for (int i = 0; i < percents.length; i++) {
				int value = (grad.getPercents().get(i)).intValue();
				if (value < 0 || value > 100) {
					// TODO this should be an exception because bad source
					// format
					return getDefaultPercents(grad);
				}
				percents[i] = value;
			}
			return percents;
		} else {
			// We can get here if either:
			// A: the percents are empty (legal) or
			// B: size mismatches (error)
			// TODO this should be an exception because bad source format

			return getDefaultPercents(grad);
		}
	}

	/*
	 * Compute and return a default array of percentages based on number of
	 * colors o If two colors, {100} o if three colors, {50, 100} o if four
	 * colors, {33, 67, 100}
	 */
	private static int[] getDefaultPercents(Gradient grad) {
		// Needed to avoid /0 in increment calc

		if (grad.getRGBs().size() <= 1) {
			return new int[0];
		}

		int[] percents = new int[grad.getRGBs().size() - 1];
		float increment = 100f / (grad.getRGBs().size() - 1);

		for (int i = 0; i < percents.length; i++) {
			percents[i] = Math.round((i + 1) * increment);
		}
		return percents;
	}

	public static CssColor getRGBColor(Color color) {
		return new CssColor(new CssNumber(color.getRed(), true), new CssNumber(color.getGreen(), true),
				new CssNumber(color.getBlue(), true));
	}

	public static CssColor getRGBColor(RGB color) {
		return new CssColor(new CssNumber(color.red, true), new CssNumber(color.green, true),
				new CssNumber(color.blue, true));
	}

	private static RGBA findColorByDefinition(String name) {
		IColorAndFontProvider provider = ColorAndFontUtil.getColorAndFontProvider();
		if (provider != null) {
			RGB rgb = provider.getColor(normalizeId(name.substring(1)));
			if (rgb != null) {
				return new RGBA(rgb.red, rgb.green, rgb.blue, 255);
			}
		}
		return null;
	}

	/** Helper function to avoid setting colors unnecessarily */
	public static void setForeground(Control control, Color newColor) {
		if (!Objects.equals(control.getForeground(), newColor)) {
			control.setForeground(newColor);
		}
	}

	/** Helper function to avoid setting colors unnecessarily */
	public static void setBackground(Control control, Color newColor) {
		if (!Objects.equals(control.getBackground(), newColor)) {
			control.setBackground(newColor);
		}
	}


	/** Helper function to avoid setting colors unnecessarily */
	public static void setSelectionForeground(CTabFolder folder, Color newColor) {
		if (!Objects.equals(folder.getSelectionForeground(), newColor)) {
			folder.setSelectionForeground(newColor);
		}
	}

	/** Helper function to avoid setting colors unnecessarily */
	public static void setSelectionBackground(CTabFolder folder, Color newColor) {
		if (!Objects.equals(folder.getSelectionBackground(), newColor)) {
			folder.setSelectionBackground(newColor);
		}
	}
}
