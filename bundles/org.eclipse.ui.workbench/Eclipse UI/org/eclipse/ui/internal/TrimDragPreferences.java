/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

/**
 * Static class to contain the preferences used to manage the GUI during
 * trim dragging.
 * <p><b>
 * NOTE: this is a test harness at this time. This class may be removed
 * before the release of 3.2.
 * </b></p>
 * 
 * @since 3.2
 *
 */
public class TrimDragPreferences {

	/**
	 * How close to a caret the cursor has to be to be 'valid'
	 */

	private static int thresholdPref = 50;
	
	/**
	 * 'true' means that each trim element can have a different 'height'
	 */
	private static boolean raggedTrim = true;

	/*
	 * Accessor Methods
	 */
	/**
	 * @return Returns the threshold.
	 */
	public static int getThreshold() {
		return thresholdPref;
	}

	/**
	 * @param threshold The threshold to set.
	 */
	public static void setThreshold(int threshold) {
		thresholdPref = threshold;
	}

	/**
	 * @return Returns the raggedTrim.
	 */
	public static boolean showRaggedTrim() {
		return raggedTrim;
	}

	/**
	 * @param raggedTrim The raggedTrim to set.
	 */
	public static void setRaggedTrim(boolean raggedTrim) {
		TrimDragPreferences.raggedTrim = raggedTrim;
	}
}
