/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools;

/**
 * A set of utility methods for dealing with byte arrays.
 */
public class ByteUtil {

	/**
	 * Helper method that creates a string representation for a byte array.
	 * 
	 * @param byteArray a byte array to be represented as string
	 * @param max a maximum number of bytes to be considered - if zero, there is 
	 * no maximum. 
	 * @return a byte array string representation
	 */
	public static String byteArrayToString(byte[] byteArray, int max) {
		StringBuffer result = new StringBuffer(byteArray.length * 2 + 2);
		int bytesToBeShown = (max > 0) ? (Math.min(max, byteArray.length)) : byteArray.length;
		result.append('[');
		for (int i = 0; i < bytesToBeShown; i++) {
			result.append(byteArray[i]);
			result.append(',');
		}
		// adds an ellipsis if there is too much bytes to show
		if (max > 0 && max < byteArray.length)
			result.append("..."); //$NON-NLS-1$
		// or remove the trailing comma
		else
			result.deleteCharAt(result.length() - 1);
		result.append(']');
		return result.toString();
	}

	/**
	 * Calls <code>byteArrayToString()</code> with no limit for array lenght.
	 * 
	 * @see #byteArrayToString(byte[], int) 
	 * @param byteArray the array to be converted to string
	 * @return a string representation for the array 
	 */
	public static String byteArrayToString(byte[] byteArray) {
		return byteArrayToString(byteArray, 0);
	}
}