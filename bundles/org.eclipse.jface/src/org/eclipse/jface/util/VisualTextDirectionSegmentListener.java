/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.util;

import org.eclipse.swt.events.SegmentEvent;
import org.eclipse.swt.events.SegmentListener;

/**
 * Defines the segment listener that enforces Visual Text Direction support.
 *
 * @since 3.11
 */
/* package */class VisualTextDirectionSegmentListener implements SegmentListener {

	private String textDirection;

	/**
	 * Creates a new segment listener that enforces Visual Text Direction
	 * support.
	 *
	 * @param textDir
	 *            the text direction Possible values are:
	 *            <ul>
	 *            <li> {@link BidiUtils#VISUAL_LEFT_TO_RIGHT}
	 *            <li> {@link BidiUtils#VISUAL_RIGHT_TO_LEFT}
	 *            </ul>
	 */
	public VisualTextDirectionSegmentListener(String textDir) {
		textDirection = textDir;
	}

	@Override
	public void getSegments(SegmentEvent event) {
		int length = event.lineText.length();
		if (length > 0) {
			event.segments = new int[2];
			event.segments[0] = 0;
			event.segments[1] = length;
			event.segmentsChars = new char[2];
			event.segmentsChars[0] = BidiUtils.VISUAL_RIGHT_TO_LEFT.equals(textDirection)
					? BidiUtils.RLO
					: BidiUtils.LRO;
			event.segmentsChars[1] = BidiUtils.PDF;
		}
	}
}
