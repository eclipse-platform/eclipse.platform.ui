/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;


/**
 * Useful color utilities.
 * 
 * @since 3.0
 */
public final class ColorUtils {
	
	/**
	 * The blend(c1-c2) pattern.  
	 */
	private static Pattern BLEND = Pattern.compile("blend\\((.*)-(.*)\\)"); //$NON-NLS-1$
	
	/**
	 * @param value the SWT constant <code>String</code>.
	 * @return the value of the SWT constant, or <code>SWT.COLOR_BLACK</code> if it could 
	 * not be determined.
	 */
	private static String process(String value) {
		Matcher matcher = BLEND.matcher(value);
		if (matcher.matches()) {
			return blend(matcher.group(1), matcher.group(2));
		}
		else {
			try {
				Class clazz = SWT.class; //$NON-NLS-1$
				Field[] fields = clazz.getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					Field field = fields[i];
					if (field.getType() == Integer.TYPE
						&& Modifier.isStatic(field.getModifiers())
						&& Modifier.isPublic(field.getModifiers())
						&& Modifier.isFinal(field.getModifiers())) {
						if (value.equals(field.getName())) {
							return formatSystemColor(field.getInt(null));
						}
					}
				}
			} catch (IllegalArgumentException e) {
				// no op - shouldnt happen.  We check for static before calling getInt(null)
			} catch (IllegalAccessException e) {		    
				// no op - shouldnt happen.  We check for public before calling getInt(null)
			}
			return formatSystemColor(SWT.COLOR_BLACK);
		}
	}	

	/**
	 * Blend the two color values returning a value that is halfway between them.
	 * 
	 * @param val1 the first value
	 * @param val2 the second value
	 * @return the blended color string
	 */
	private static String blend(String val1, String val2) {
		RGB rgb1 = StringConverter.asRGB(getColorValue(val1));		
		RGB rgb2 = StringConverter.asRGB(getColorValue(val2));		
		return StringConverter.asString(blend(rgb1, rgb2));
	}

	/**
	 * Blend the two color values returning a value that is halfway between them.
	 * 
	 * @param val1 the first value
	 * @param val2 the second value
	 * @return the blended color
	 */
	public static RGB blend(RGB val1, RGB val2) {	
		int red = blend(val1.red, val2.red);
		int green = blend(val1.green, val2.green);
		int blue = blend(val1.blue, val2.blue);		
		return new RGB(red, green, blue);
	}
	
	/**
	 * Blend the two color values returning a value that is halfway between them.
	 * 
	 * @param temp1 the first value
	 * @param temp2 the second value
	 * @return the blended int value
	 */
	private static int blend(int temp1, int temp2) {
		return (Math.abs(temp1 - temp2) / 2) + Math.min(temp1,temp2);
	}
	

	/**
	 * @param colorId the system color identifier.
	 * @return the rrr,ggg,bbb <code>String</code> value of the supplied system 
	 *		color.
	 */
	private static String formatSystemColor(int colorId) {
		Color color = Display.getCurrent().getSystemColor(colorId);
		return color.getRed() + "," + color.getGreen() + "," + color.getBlue(); //$NON-NLS-1$ //$NON-NLS-2$
	}	

	/**
	 * Get the RGB value for a given color.
     * 
     * @param rawValue the raw value, either an RGB triple or an SWT constant.
     * @return Returns the RGB value.
	 */
    public static String getColorValue(String rawValue) {
		if (rawValue == null)
			return null;
		
		rawValue = rawValue.trim();

		if (!isDirectValue(rawValue)) {
			return process(rawValue);
		}

		return rawValue;        
    }

    /**
	 * Get the RGB values for a given color array.
     * 
     * @param rawValue the raw values, either RGB triple or an SWT constant.
     * @return Returns the RGB values.
	 */
    public static String [] getColorValues(String[] rawValues) {
    	String [] values = new String[rawValues.length];
    	for (int i = 0; i < rawValues.length; i++) {
            values[i] = getColorValue(rawValues[i]);
        }
    	return values;
    }

    /** 
	 * @return whether the value returned by <code>getValue()</code> is already 
	 * in RGB form. 
	 */
	private static boolean isDirectValue(String rawValue) { 
		return rawValue == null ? true : rawValue.matches("[0-2]\\d\\d,[0-2]\\d\\d,[0-2]\\d\\d"); //$NON-NLS-1$
	}
    
    /**
     * Not intended to be instantiated.
     */
    private ColorUtils() {
        //no-op
    }
}
