/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.util;

import org.eclipse.swt.events.SegmentListener;
import org.eclipse.swt.events.SegmentEvent;
/**
 * This class defines the segment listener in order to enforce Base Text Direction (BTD) support
 *
 * @since 3.9
 */
public final class BaseTextDirectionSegmentListener implements SegmentListener {
	
	private String textDirection;

	/**
	 *  Creates a new segment listener instance in order to enforce Base Text Direction (BTD) support
	 * 
	 * @param textDir the text direction
	 * Possible values are:
	 * <ul>
	 * <li> {@link BidiUtils#LEFT_TO_RIGHT}
	 * <li> {@link BidiUtils#RIGHT_TO_LEFT}
	 * <li> {@link BidiUtils#AUTO}
	 * </ul>
	 */ 
	protected BaseTextDirectionSegmentListener(String textDir) {
		super();
		textDirection = textDir;
	}

	public void getSegments(SegmentEvent event) {
		if (BidiUtils.LEFT_TO_RIGHT.equalsIgnoreCase(textDirection)
				|| BidiUtils.RIGHT_TO_LEFT.equalsIgnoreCase(textDirection)
				|| BidiUtils.AUTO.equalsIgnoreCase(textDirection)) {

			int length = event.lineText.length();
			if (length > 0) {
				event.segments = new int[2];
				event.segments[0] = 0;
				event.segments[1] = length;
				event.segmentsChars = new char[2];
				event.segmentsChars[0] = isRTLValue(event.lineText) ? BidiUtils.RLE : BidiUtils.LRE;
				event.segmentsChars[1] = BidiUtils.PDF;				
			}
		}
	}
	
	private boolean isRTLValue(String stringValue){
    	
    	if (stringValue == null || stringValue.length() == 0 || BidiUtils.LEFT_TO_RIGHT.equalsIgnoreCase(textDirection))
			return false;
    	
    	else if (BidiUtils.RIGHT_TO_LEFT.equalsIgnoreCase(textDirection))
    		return true;

		for (int i = 0; i < stringValue.length(); i++) {
			if (Character.getDirectionality(stringValue.charAt(i)) == Character.DIRECTIONALITY_RIGHT_TO_LEFT
					|| Character.getDirectionality(stringValue.charAt(i)) == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
					|| Character.getDirectionality(stringValue.charAt(i)) == Character.DIRECTIONALITY_ARABIC_NUMBER)
				return true;
			else if (Character.getDirectionality(stringValue.charAt(i)) == Character.DIRECTIONALITY_LEFT_TO_RIGHT) {
				return false;
			}
		}
		return false;
    }
}
