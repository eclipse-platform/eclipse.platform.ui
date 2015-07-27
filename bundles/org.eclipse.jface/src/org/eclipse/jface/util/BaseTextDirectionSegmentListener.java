/*******************************************************************************
 * Copyright (c) 2012, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SegmentEvent;
import org.eclipse.swt.events.SegmentListener;
import org.eclipse.swt.widgets.Control;

/**
 * Defines the segment listener that enforces Base Text Direction (BTD) support.
 *
 * @since 3.9
 */
/*package*/ class BaseTextDirectionSegmentListener implements SegmentListener {

	private String textDirection;

	/**
	 * Creates a new segment listener that enforces Base Text Direction (BTD) support.
	 *
	 * @param textDir the text direction
	 * Possible values are:
	 * <ul>
	 * <li> {@link BidiUtils#LEFT_TO_RIGHT}
	 * <li> {@link BidiUtils#RIGHT_TO_LEFT}
	 * <li> {@link BidiUtils#AUTO}
	 * </ul>
	 */
	public BaseTextDirectionSegmentListener(String textDir) {
		super();
		textDirection = textDir;
	}

	@Override
	public void getSegments(SegmentEvent event) {
		int length = event.lineText.length();
		if (length > 0) {
			boolean isRTL = isRTLValue(event.lineText);
			if (event.widget instanceof Control && Util.isWindows()) {
				if (isRTL) {
					((Control) event.widget).setOrientation(SWT.RIGHT_TO_LEFT);
				} else {
					((Control) event.widget).setOrientation(SWT.LEFT_TO_RIGHT);
				}
			} else {
				event.segments = new int[2];
				event.segments[0] = 0;
				event.segments[1] = length;
				event.segmentsChars = new char[2];
				event.segmentsChars[0] = isRTL ? BidiUtils.RLE : BidiUtils.LRE;
				event.segmentsChars[1] = BidiUtils.PDF;
			}
		}
	}

	protected boolean isRTLValue(String stringValue) {
		if (stringValue == null || stringValue.length() == 0 || BidiUtils.LEFT_TO_RIGHT.equals(textDirection))
			return false;

		if (BidiUtils.RIGHT_TO_LEFT.equals(textDirection))
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
