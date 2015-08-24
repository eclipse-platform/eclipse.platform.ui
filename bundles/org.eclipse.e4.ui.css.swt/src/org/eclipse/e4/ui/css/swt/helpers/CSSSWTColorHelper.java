/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.e4.ui.css.core.css2.CSS2ColorHelper;
import org.eclipse.e4.ui.css.core.css2.CSS2RGBColorImpl;
import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.internal.css.swt.CSSActivator;
import org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;
import org.w3c.dom.css.RGBColor;

public class CSSSWTColorHelper {
	public static final String COLOR_DEFINITION_MARKER = "#";

	private static final String HEX_COLOR_VALUE_PATTERN = "#[a-fA-F0-9]{6}";

	private static Field[] cachedFields;

	/*--------------- SWT Color Helper -----------------*/

	public static Color getSWTColor(RGBColor rgbColor, Display display) {
		RGBA rgb = getRGBA(rgbColor);
		return new Color(display, rgb);
	}

	public static Color getSWTColor(CSSValue value, Display display) {
		if (value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			return null;
		}
		Color color = display.getSystemColor(SWT.COLOR_BLACK);
		RGBA rgba = getRGBA((CSSPrimitiveValue) value, display);
		if (rgba != null) {
			color = new Color(display, rgba.rgb.red, rgba.rgb.green, rgba.rgb.blue, rgba.alpha);
		}
		return color;
	}

	private static RGBA getRGBA(CSSPrimitiveValue value, Display display) {
		RGBA rgba = getRGBA(value);
		if (rgba == null && display != null) {
			String name = value.getStringValue();
			if (hasColorDefinitionAsValue(name)) {
				rgba = findColorByDefinition(name);
			} else if (name.contains("-")) {
				name = name.replace('-', '_');
				rgba = process(display, name);
			}
		}
		return rgba;
	}

	public static boolean hasColorDefinitionAsValue(CSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value;
			if (primitiveValue.getPrimitiveType() == CSSPrimitiveValue.CSS_STRING) {
				return hasColorDefinitionAsValue(primitiveValue
						.getStringValue());
			}
		}
		return false;
	}

	public static boolean hasColorDefinitionAsValue(String name) {
		if (name.startsWith(COLOR_DEFINITION_MARKER)) {
			return !name.matches(HEX_COLOR_VALUE_PATTERN);
		}
		return false;
	}

	/**
	 * Process the given string and return a corresponding RGBA object.
	 *
	 * @param value
	 *            the SWT constant <code>String</code>
	 * @return the value of the SWT constant, or <code>SWT.COLOR_BLACK</code> if
	 *         it could not be determined
	 */
	private static RGBA process(Display display, String value) {
		Field [] fields = getFields();
		try {
			for (Field field : fields) {
				if (field.getName().equals(value)) {
					return display.getSystemColor(field.getInt(null)).getRGBA();
				}
			}
		} catch (IllegalArgumentException e) {
			// no op - shouldnt happen. We check for static before calling
			// getInt(null)
		} catch (IllegalAccessException e) {
			// no op - shouldnt happen. We check for public before calling
			// getInt(null)
		}
		return  display.getSystemColor(SWT.COLOR_BLACK).getRGBA();
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
			ArrayList<Field> applicableFields = new ArrayList<Field>(
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
		RGBColor color = CSS2ColorHelper.getRGBColor(name);
		if (color != null) {
			return getRGBA(color);
		}
		return null;
	}

	public static RGBA getRGBA(RGBColor color) {
		return new RGBA((int) color.getRed().getFloatValue(
				CSSPrimitiveValue.CSS_NUMBER), (int) color.getGreen()
				.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), (int) color
				.getBlue().getFloatValue(CSSPrimitiveValue.CSS_NUMBER),
				// for now, we only support solid RGB colors in CSS - our CSS model
				// as of now does not have an element for RGBAColor.
				255);
	}

	public static RGBA getRGBA(CSSValue value) {
		if (value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			return null;
		}
		return getRGBA((CSSPrimitiveValue) value);
	}

	public static RGBA getRGBA(CSSPrimitiveValue value) {
		RGBA rgba = null;
		switch (value.getPrimitiveType()) {
		case CSSPrimitiveValue.CSS_IDENT:
		case CSSPrimitiveValue.CSS_STRING:
			String string = value.getStringValue();
			rgba = getRGBA(string);
			break;
		case CSSPrimitiveValue.CSS_RGBCOLOR:
			RGBColor rgbColor = value.getRGBColorValue();
			rgba = getRGBA(rgbColor);
			break;
		}
		return rgba;
	}

	public static Integer getPercent(CSSPrimitiveValue value) {
		int percent = 0;
		switch (value.getPrimitiveType()) {
		case CSSPrimitiveValue.CSS_PERCENTAGE:
			percent = (int) value
			.getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE);
		}
		return new Integer(percent);
	}

	public static Gradient getGradient(CSSValueList list, Display display) {
		Gradient gradient = new Gradient();
		for (int i = 0; i < list.getLength(); i++) {
			CSSValue value = list.item(i);
			if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				short primType = ((CSSPrimitiveValue) value).getPrimitiveType();

				if (primType == CSSPrimitiveValue.CSS_IDENT) {
					if (value.getCssText().equals("gradient")) {
						// Skip the keyword "gradient"
						continue;
					} else if (value.getCssText().equals("linear")) {
						gradient.setLinear(true);
						continue;
					} else if (value.getCssText().equals("radial")) {
						gradient.setLinear(false);
						continue;
					}
				}

				switch (primType) {
				case CSSPrimitiveValue.CSS_IDENT:
				case CSSPrimitiveValue.CSS_STRING:
				case CSSPrimitiveValue.CSS_RGBCOLOR:
					RGBA rgba = getRGBA((CSSPrimitiveValue) value, display);
					if (rgba != null) {
						// note that in this call we lose the RGBA alpha
						// component - we do currently not support alpha
						// gradients
						gradient.addRGB(rgba, (CSSPrimitiveValue) value);
					} else {
						//check for vertical gradient
						gradient.setVertical(!value.getCssText().equals("false"));
					}
					break;
				case CSSPrimitiveValue.CSS_PERCENTAGE:
					gradient.addPercent(getPercent((CSSPrimitiveValue) value));
					break;
				}
			}
		}
		return gradient;
	}

	@SuppressWarnings("rawtypes")
	public static Color[] getSWTColors(Gradient grad, Display display,
			CSSEngine engine) throws Exception {
		List values = grad.getValues();
		Color[] colors = new Color[values.size()];

		for (int i = 0; i < values.size(); i++) {
			CSSPrimitiveValue value = (CSSPrimitiveValue) values.get(i);
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

	public static RGBColor getRGBColor(Color color) {
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		return new CSS2RGBColorImpl(red, green, blue);
	}

	public static RGBColor getRGBColor(RGB color) {
		int red = color.red;
		int green = color.green;
		int blue = color.blue;
		return new CSS2RGBColorImpl(red, green, blue);
	}

	private static RGBA findColorByDefinition(String name) {
		IColorAndFontProvider provider = CSSActivator.getDefault().getColorAndFontProvider();
		if (provider != null) {
			RGB rgb = provider.getColor(normalizeId(name.substring(1)));
			return new RGBA(rgb.red, rgb.green, rgb.blue, 255);
		}
		return null;
	}

	/** Simplify testing for color equality */
	public static boolean equals(Color c1, Color c2) {
		if (c1 == c2) {
			return true;
		}
		if (c1 == null || c2 == null) {
			return false;
		}
		return c1.equals(c2);
	}

	/** Helper function to avoid setting colors unnecessarily */
	public static void setForeground(Control control, Color newColor) {
		if (!equals(control.getForeground(), newColor)) {
			control.setForeground(newColor);
		}
	}

	/** Helper function to avoid setting colors unnecessarily */
	public static void setBackground(Control control, Color newColor) {
		if (!equals(control.getBackground(), newColor)) {
			control.setBackground(newColor);
		}
	}

	/** Helper function to avoid setting colors unnecessarily */
	public static void setSelectionForeground(CTabFolder folder, Color newColor) {
		if (!equals(folder.getSelectionForeground(), newColor)) {
			folder.setSelectionForeground(newColor);
		}
	}

	/** Helper function to avoid setting colors unnecessarily */
	public static void setSelectionBackground(CTabFolder folder, Color newColor) {
		if (!equals(folder.getSelectionBackground(), newColor)) {
			folder.setSelectionBackground(newColor);
		}
	}
}
