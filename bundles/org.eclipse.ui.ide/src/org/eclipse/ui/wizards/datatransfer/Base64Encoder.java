/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.wizards.datatransfer;


/**
 *	This utility class converts a passed byte array into a Base 64 encoded
 *	String according to the specification in RFC1521 section 5.2
 */
/*package*/ class Base64Encoder {
	private static final String mappings = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";//$NON-NLS-1$
	private static final String filler = "=";//$NON-NLS-1$
/**
 *	Answer a string representing the Base 64 encoded form of the passed
 *	byte array
 *
 *	@return java.lang.String
 *	@param contents byte[]
 */
public static String encode(byte[] contents) {
	StringBuffer result = new StringBuffer();

	for (int i = 0; i < contents.length; i = i + 3) {
		if (result.length() == 76)
			result.append("\n\r");//$NON-NLS-1$
		
		// output character 1
		result.append(mappings.charAt((contents[i] & 0xFC) >> 2));

		// output character 2
		int c2 = (contents[i] & 0x03) << 4;
		if (i + 1 >= contents.length) {
			result.append(mappings.charAt(c2));
			result.append(filler);
			result.append(filler);
			return result.toString();
		}
		
		c2 |= ((contents[i + 1] & 0xF0) >> 4);
		result.append(mappings.charAt(c2));

		// output character 3
		int c3 = (contents[i + 1] & 0x0F) << 2;
		if (i + 2 >= contents.length) {
			result.append(mappings.charAt(c3));
			result.append(filler);
			return result.toString();
		}
		
		c3 |= ((contents[i + 2] & 0xC0) >> 6);
		result.append(mappings.charAt(c3));

		// output character 4
		result.append(mappings.charAt(contents[i + 2] & 0x3F));
	}
		
	return result.toString();
}
}
