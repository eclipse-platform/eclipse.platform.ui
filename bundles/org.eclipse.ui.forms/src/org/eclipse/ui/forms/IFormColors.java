/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms;

/**
 * A place to hold all the color constants used in the forms package.
 * 
 * @since 3.3
 */

public interface IFormColors {
	/**
	 * A prefix for all the keys.
	 */
	String PREFIX = "org.eclipse.ui.forms."; //$NON-NLS-1$
	/**
	 * Key for the form title foreground color.
	 */
	String TITLE = PREFIX + "TITLE"; //$NON-NLS-1$

	/**
	 * A prefix for the header color constants.
	 */
	String H_PREFIX = PREFIX + "H_"; //$NON-NLS-1$
	/*
	 * A prefix for the section title bar color constants.
	 */
	String TB_PREFIX = PREFIX + "TB_"; //$NON-NLS-1$	
	/**
	 * Key for the form header background gradient ending color.
	 */
	String H_GRADIENT_END = H_PREFIX + "GRADIENT_END"; //$NON-NLS-1$

	/**
	 * Key for the form header background gradient starting color.
	 * 
	 */
	String H_GRADIENT_START = H_PREFIX + "GRADIENT_START"; //$NON-NLS-1$
	/**
	 * Key for the form header bottom keyline 1 color.
	 * 
	 */
	String H_BOTTOM_KEYLINE1 = H_PREFIX + "BOTTOM_KEYLINE1"; //$NON-NLS-1$
	/**
	 * Key for the form header bottom keyline 2 color.
	 * 
	 */
	String H_BOTTOM_KEYLINE2 = H_PREFIX + "BOTTOM_KEYLINE2"; //$NON-NLS-1$
	/**
	 * Key for the form header light hover color.
	 * 
	 */
	String H_HOVER_LIGHT = H_PREFIX + "H_HOVER_LIGHT"; //$NON-NLS-1$
	/**
	 * Key for the form header full hover color.
	 * 
	 */
	String H_HOVER_FULL = H_PREFIX + "H_HOVER_FULL"; //$NON-NLS-1$

	/**
	 * Key for the tree/table border color.
	 */
	String BORDER = PREFIX + "BORDER"; //$NON-NLS-1$

	/**
	 * Key for the section separator color.
	 */
	String SEPARATOR = PREFIX + "SEPARATOR"; //$NON-NLS-1$

	/**
	 * Key for the section title bar background.
	 */
	String TB_BG = TB_PREFIX + "BG"; //$NON-NLS-1$

	/**
	 * Key for the section title bar foreground.
	 */
	String TB_FG = TB_PREFIX + "FG"; //$NON-NLS-1$

	/**
	 * Key for the section title bar gradient.
	 * @deprecated Since 3.3, this color is not used any more. The 
	 * tool bar gradient is created starting from {@link #TB_BG} to
	 * the section background color.
	 */
	String TB_GBG = TB_BG;

	/**
	 * Key for the section title bar border.
	 */
	String TB_BORDER = TB_PREFIX + "BORDER"; //$NON-NLS-1$

	/**
	 * Key for the section toggle color. Since 3.1, this color is used for all
	 * section styles.
	 */
	String TB_TOGGLE = TB_PREFIX + "TOGGLE"; //$NON-NLS-1$

	/**
	 * Key for the section toggle hover color.
	 * 
	 */
	String TB_TOGGLE_HOVER = TB_PREFIX + "TOGGLE_HOVER"; //$NON-NLS-1$		
}