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
package org.eclipse.ui.internal.presentation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;


/**
 * @since 3.0
 */
public final class ColorUtils {
	
	/**
	 * @param value the SWT constant <code>String</code>.
	 * @return the value of the SWT constant, or <code>SWT.COLOR_BLACK</code> if it could 
	 * not be determined.
	 */
	private static String extractSWTConstant(String value) {
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

		if (!isDirectValue(rawValue)) {
			return extractSWTConstant(rawValue);
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
		return rawValue == null ? true : rawValue.matches("\\d+,\\d+,\\d+"); //$NON-NLS-1$
	}
    
    /**
     * Not intended to be instantiated.
     */
    private ColorUtils() {
        //no-op
    }
}
