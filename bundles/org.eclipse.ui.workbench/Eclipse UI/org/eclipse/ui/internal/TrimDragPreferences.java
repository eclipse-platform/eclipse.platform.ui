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

	private static int thresholdPref = 100;
	/**
	 * This preference determines the look of the insertion carets;
	 * 'true' means to use a 'bar' format
	 * 'false' means to use an 'arrow' format
	 */
	private static boolean useBarsPref = false;
	
	/**
	 * If 'true' only uses the four-way 'move' and 'no drop' cursors
	 */
	private static boolean inhibitCustomCursors = false;
	
	/**
	 * If 'true' then the insertion point for an 'empty' trim is in the middle
	 * If 'false' then the insertion point for an 'empty' trim is at the 'start'
	 */
	private static boolean useMiddleIfEmpty = true;
	
	/**
	 * 'true' means that each trim element can have a different 'height'
	 */
	private static boolean raggedTrim = false;
	
	/**
	 * Determines whether the control is docked instead
	 * of just 'snapping' to the insert point
	 */
	private static boolean autoDock = false;

	/*
	 * Accessor Methods
	 */
	public static void setUseBars(boolean useBars) {
		useBarsPref = useBars;
	}
	
	public static boolean useBars() {
		return useBarsPref;
	}

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
	 * @return Returns the inhibitCustomCursors.
	 */
	public static boolean inhibitCustomCursors() {
		return inhibitCustomCursors;
	}

	/**
	 * @param inhibitCustomCursors The inhibitCustomCursors to set.
	 */
	public static void setInhibitCustomCursors(boolean inhibitCustomCursors) {
		TrimDragPreferences.inhibitCustomCursors = inhibitCustomCursors;
	}

	/**
	 * @return Returns the useMiddleIfEmpty.
	 */
	public static boolean useMiddleIfEmpty() {
		return useMiddleIfEmpty;
	}

	/**
	 * @param useMiddleIfEmpty The useMiddleIfEmpty to set.
	 */
	public static void setUseMiddleIfEmpty(boolean useMiddleIfEmpty) {
		TrimDragPreferences.useMiddleIfEmpty = useMiddleIfEmpty;
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

	/**
	 * @return Returns the autoDock.
	 */
	public static boolean autoDock() {
		return autoDock;
	}

	/**
	 * @param autoDock The autoDock to set.
	 */
	public static void setAutoDock(boolean autoDock) {
		TrimDragPreferences.autoDock = autoDock;
	}
}
