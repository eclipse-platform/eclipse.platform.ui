/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.themes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.eclipse.jface.resource.DataFormatException;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Useful color utilities.
 * 
 * @since 3.0 - initial release
 * @since 3.2 - public API
 */
public final class ColorUtil {

	private static Field[] cachedFields;
	
	/**
	 * Process the given string and return a corresponding RGB object.
	 * 
	 * @param value
	 *            the SWT constant <code>String</code>
	 * @return the value of the SWT constant, or <code>SWT.COLOR_BLACK</code>
	 *         if it could not be determined
	 */
	private static RGB process(String value) {
		Field [] fields = getFields();
		try {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.getName().equals(value)) {
					return getSystemColor(field.getInt(null));
				}
			}
		} catch (IllegalArgumentException e) {
			// no op - shouldnt happen. We check for static before calling
			// getInt(null)
		} catch (IllegalAccessException e) {
			// no op - shouldnt happen. We check for public before calling
			// getInt(null)
		}
		return getSystemColor(SWT.COLOR_BLACK);
	}

	/**
	 * Get the SWT constant fields.
	 * 
	 * @return the fields
	 * @since 3.3
	 */
	private static Field[] getFields() {
		if (cachedFields == null) {
			Class clazz = SWT.class;		
			Field[] allFields = clazz.getDeclaredFields();
			ArrayList applicableFields = new ArrayList(allFields.length);
			
			for (int i = 0; i < allFields.length; i++) {
				Field field = allFields[i];
				if (field.getType() == Integer.TYPE
						&& Modifier.isStatic(field.getModifiers())
						&& Modifier.isPublic(field.getModifiers())
						&& Modifier.isFinal(field.getModifiers())
						&& field.getName().startsWith("COLOR")) { //$NON-NLS-1$
				
					applicableFields.add(field);
				}
			}
			cachedFields = (Field []) applicableFields.toArray(new Field [applicableFields.size()]);
		}
		return cachedFields;
	}

	/**
	 * Blends the two color values according to the provided ratio.
	 * 
	 * @param c1
	 *            first color
	 * @param c2
	 *            second color
	 * @param ratio
	 *            percentage of the first color in the blend (0-100)
	 * @return the RGB value of the blended color
	 * 
	 * @since 3.3
	 */
	public static RGB blend(RGB c1, RGB c2, int ratio) {
		int r = blend(c1.red, c2.red, ratio);
		int g = blend(c1.green, c2.green, ratio);
		int b = blend(c1.blue, c2.blue, ratio);
		return new RGB(r, g, b);
	}

	private static int blend(int v1, int v2, int ratio) {
		int b = (ratio * v1 + (100 - ratio) * v2) / 100;
		return Math.min(255, b);
	}
	
	/**
	 * Blend the two color values returning a value that is halfway between
	 * them.
	 * 
	 * @param val1
	 *            the first value
	 * @param val2
	 *            the second value
	 * @return the blended color
	 */
	public static RGB blend(RGB val1, RGB val2) {
		int red = blend(val1.red, val2.red);
		int green = blend(val1.green, val2.green);
		int blue = blend(val1.blue, val2.blue);
		return new RGB(red, green, blue);
	}

	/**
	 * Blend the two color values returning a value that is halfway between
	 * them.
	 * 
	 * @param temp1
	 *            the first value
	 * @param temp2
	 *            the second value
	 * @return the blended int value
	 */
	private static int blend(int temp1, int temp2) {
		return (Math.abs(temp1 - temp2) / 2) + Math.min(temp1, temp2);
	}

	/**
	 * Return the system color that matches the provided SWT constant value.
	 * 
	 * @param colorId
	 *            the system color identifier
	 * @return the RGB value of the supplied system color
	 */
	private static RGB getSystemColor(int colorId) {
		return Display.getCurrent().getSystemColor(colorId).getRGB();
	}

	/**
	 * Get the RGB value for a given color.
	 * 
	 * @param rawValue
	 *            the raw value, either an RGB triple or an SWT constant name
	 * @return the RGB value
	 * @throws DataFormatException
	 *             thrown if the value cannot be interpreted as a color
	 */
	public static RGB getColorValue(String rawValue) throws DataFormatException {
		if (rawValue == null) {
			return null;
		}

		rawValue = rawValue.trim();

		if (!isDirectValue(rawValue)) {
			return process(rawValue);
		}

		return StringConverter.asRGB(rawValue);
	}

	/**
	 * Get the RGB values for a given color array.
	 * 
	 * @param rawValues
	 *            the raw values, either RGB triple or an SWT constant
	 * @return the RGB values
	 */
	public static RGB[] getColorValues(String[] rawValues) {
		RGB[] values = new RGB[rawValues.length];
		for (int i = 0; i < rawValues.length; i++) {
			values[i] = getColorValue(rawValues[i]);
		}
		return values;
	}

	/**
	 * Return whether the value returned by <code>getValue()</code> is already
	 * in RGB form.
	 * 
	 * @return whether the value returned by <code>getValue()</code> is
	 *         already in RGB form
	 */
	private static boolean isDirectValue(String rawValue) {
		return rawValue.indexOf(',') >= 0;
	}

	/**
	 * Not intended to be instantiated.
	 */
	private ColorUtil() {
		// no-op
	}
}
