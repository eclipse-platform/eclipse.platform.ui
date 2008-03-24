/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Carter - bug 170668
 *     Brad Reynolds - bug 170848
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.swt;

/**
 * Constants used to describe properties of SWT controls.
 * 
 * @since 1.0
 *
 */
public interface SWTProperties {

	/**
	 * Applies to Control
	 */
	public static final String ENABLED = "enabled"; //$NON-NLS-1$
	/**
	 * Applies to Control
	 */
	public static final String VISIBLE = "visible"; //$NON-NLS-1$
	/**
	 * Applies to Control
	 */
	public static final String TOOLTIP_TEXT = "tooltip"; //$NON-NLS-1$	
	/**
	 * Applies to
	 */
	public static final String ITEMS = "items"; //$NON-NLS-1$
	/**
	 * Applies to Spinner
	 */
	public static final String MAX = "max"; //$NON-NLS-1$
	/**
	 * Applies to Spinner
	 */
	public static final String MIN = "min"; //$NON-NLS-1$
	/**
	 * Applies to Spinner, Button
	 */
	public static final String SELECTION = "selection"; //$NON-NLS-1$
	/**
	 * Applies to Spinner, Button
	 */
	public static final String SELECTION_INDEX = "index"; //$NON-NLS-1$
	/**
	 * Applies to Text, Label, Combo
	 */
	public static final String TEXT = "text"; //$NON-NLS-1$
	
	/**
	 * Applies to Label, CLabel.
	 */
	public static final String IMAGE = "image"; //$NON-NLS-1$
	/**
	 * Applies to Control
	 */
	public static final String FOREGROUND = "foreground"; //$NON-NLS-1$
	/**
	 * Applies to Control
	 */
	public static final String BACKGROUND = "background"; //$NON-NLS-1$
	/**
	 * Applies to Control
	 */
	public static final String FONT = "font"; //$NON-NLS-1$

}
