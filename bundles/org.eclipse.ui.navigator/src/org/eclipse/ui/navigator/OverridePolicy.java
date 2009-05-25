/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

/**
 * <p>
 * Enumeration of the OverridePolicy values supported by the Common Navigator.
 * </p>
 * 
 * @since 3.4
 */
public final class OverridePolicy {

	/**
	 * Indicates InvokeOnlyIfSuppressedExtAlsoVisibleAndActive OverridePolicy as
	 * an int.
	 */
	public static final int InvokeOnlyIfSuppressedExtAlsoVisibleAndActive_VALUE = -1;

	/**
	 * Indicates InvokeAlwaysRegardlessOfSuppressedExt OverridePolicy as an int.
	 */
	public static final int InvokeAlwaysRegardlessOfSuppressedExt_VALUE = 1;

	/**
	 * Indicates InvokeOnlyIfSuppressedExtAlsoVisibleAndActive OverridePolicy as
	 * a String.
	 */
	public static final String InvokeOnlyIfSuppressedExtAlsoVisibleAndActive_LITERAL = "InvokeOnlyIfSuppressedExtAlsoVisibleAndActive"; //$NON-NLS-1$

	/**
	 * Indicates InvokeAlwaysRegardlessOfSuppressedExt OverridePolicy as a
	 * String.
	 */
	public static final String InvokeAlwaysRegardlessOfSuppressedExt_LITERAL = "InvokeAlwaysRegardlessOfSuppressedExt"; //$NON-NLS-1$

	/**
	 * Indicates InvokeOnlyIfSuppressedExtAlsoVisibleAndActive OverridePolicy as
	 * a OverridePolicy enumeration.
	 */
	public static final OverridePolicy InvokeOnlyIfSuppressedExtAlsoVisibleAndActive = new OverridePolicy(
			InvokeOnlyIfSuppressedExtAlsoVisibleAndActive_VALUE,
			InvokeOnlyIfSuppressedExtAlsoVisibleAndActive_LITERAL);

	/**
	 * Indicates InvokeAlwaysRegardlessOfSuppressedExt OverridePolicy as a
	 * OverridePolicy enumeration.
	 */
	public static final OverridePolicy InvokeAlwaysRegardlessOfSuppressedExt = new OverridePolicy(
			InvokeAlwaysRegardlessOfSuppressedExt_VALUE,
			InvokeAlwaysRegardlessOfSuppressedExt_LITERAL);

	/**
	 * The ordered array of possible enumeration values.
	 */
	public static final OverridePolicy[] ENUM_ARRAY = new OverridePolicy[] {
			InvokeOnlyIfSuppressedExtAlsoVisibleAndActive,
			InvokeAlwaysRegardlessOfSuppressedExt };

	/**
	 * 
	 * Returns the correct instance of the OverridePolicy ENUM for aLiteral.
	 * 
	 * <p>
	 * This method will return InvokeAlwaysRegardlessOfSuppressedExt if the
	 * supplied value of aLiteral is invalid.
	 * </p>
	 * 
	 * @param aLiteral
	 *            One of the defined *_LITERAL constants of this class
	 * @return The corresponding OverridePolicy Enum or
	 *         InvokeAlwaysRegardlessOfSuppressedExt if aLiteral is invalid
	 */
	public static OverridePolicy get(String aLiteral) {
		for (int i = 0; i < ENUM_ARRAY.length; i++) {
			if (ENUM_ARRAY[i].getLiteral().equals(aLiteral)) {
				return ENUM_ARRAY[i];
			}
		}
		return InvokeAlwaysRegardlessOfSuppressedExt;
	}

	/**
	 * 
	 * Returns the correct instance of the OverridePolicy ENUM for aValue.
	 * 
	 * <p>
	 * This method will return InvokeAlwaysRegardlessOfSuppressedExt if the
	 * supplied value of aValue is invalid.
	 * </p>
	 * 
	 * @param aValue
	 *            One of the defined *_VALUE constants of this class
	 * @return The corresponding OverridePolicy Enum or
	 *         InvokeAlwaysRegardlessOfSuppressedExt if aValue is invalid
	 */
	public static OverridePolicy get(int aValue) {

		switch (aValue) {
		case InvokeOnlyIfSuppressedExtAlsoVisibleAndActive_VALUE:
			return InvokeOnlyIfSuppressedExtAlsoVisibleAndActive;
		case InvokeAlwaysRegardlessOfSuppressedExt_VALUE:
		default:
			return InvokeAlwaysRegardlessOfSuppressedExt;

		}
	}

	private final int value;

	private final String literal;

	protected OverridePolicy(int aValue, String aLiteral) {
		value = aValue;
		literal = aLiteral;
	}

	/**
	 * 
	 * @return The literal string for this specific OverridePolicy.
	 */
	public String getLiteral() {
		return literal;
	}

	/**
	 * 
	 * @return The integer value for this specific OverridePolicy.
	 */
	public int getValue() {
		return value;
	}
}
