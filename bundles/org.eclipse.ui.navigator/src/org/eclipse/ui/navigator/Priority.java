/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
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
 * Enumeration of the Priority values supported by the Common Navigator.
 * </p>
 *  
 * @since 3.2
 */
public final class Priority {

	/**
	 * Indicates Highest priority as an int.
	 */
	public static final int HIGHEST_PRIORITY_VALUE = 0;

	/**
	 * Indicates Higher priority as an int.
	 */
	public static final int HIGHER_PRIORITY_VALUE = 1;

	/**
	 * Indicates High priority as an int.
	 */
	public static final int HIGH_PRIORITY_VALUE = 2;

	/**
	 * Indicates Normal priority as an int.
	 */
	public static final int NORMAL_PRIORITY_VALUE = 3;

	/**
	 * Indicates Low priority as an int.
	 */
	public static final int LOW_PRIORITY_VALUE = 4;

	/**
	 * Indicates Lower priority as an int.
	 */
	public static final int LOWER_PRIORITY_VALUE = 5;

	/**
	 * Indicates Lowest priority as an int.
	 */
	public static final int LOWEST_PRIORITY_VALUE = 6;

	/**
	 * Indicates Highest priority as a String.
	 */
	public static final String HIGHEST_PRIORITY_LITERAL = "highest"; //$NON-NLS-1$

	/**
	 * Indicates Higher priority as a String.
	 */
	public static final String HIGHER_PRIORITY_LITERAL = "higher"; //$NON-NLS-1$

	/**
	 * Indicates High priority as a String.
	 */
	public static final String HIGH_PRIORITY_LITERAL = "high"; //$NON-NLS-1$

	/**
	 * Indicates Normal priority as a String.
	 */
	public static final String NORMAL_PRIORITY_LITERAL = "normal"; //$NON-NLS-1$

	/**
	 * Indicates Low priority as a String.
	 */
	public static final String LOW_PRIORITY_LITERAL = "low"; //$NON-NLS-1$

	/**
	 * Indicates Lower priority as a String.
	 */
	public static final String LOWER_PRIORITY_LITERAL = "lower"; //$NON-NLS-1$

	/**
	 * Indicates Lowest priority as a String.
	 */
	public static final String LOWEST_PRIORITY_LITERAL = "lowest"; //$NON-NLS-1$

	/**
	 * Indicates Highest priority as a Priority enumeration.
	 */
	public static final Priority HIGHEST = new Priority(HIGHEST_PRIORITY_VALUE,
			HIGHEST_PRIORITY_LITERAL);

	/**
	 * Indicates Higher priority as a Priority enumeration.
	 */
	public static final Priority HIGHER = new Priority(HIGHER_PRIORITY_VALUE,
			HIGHER_PRIORITY_LITERAL);

	/**
	 * Indicates High priority as a Priority enumeration.
	 */
	public static final Priority HIGH = new Priority(HIGH_PRIORITY_VALUE,
			HIGH_PRIORITY_LITERAL);

	/**
	 * Indicates Normal priority as a Priority enumeration.
	 */
	public static final Priority NORMAL = new Priority(NORMAL_PRIORITY_VALUE,
			NORMAL_PRIORITY_LITERAL);

	/**
	 * Indicates Low priority as a Priority enumeration.
	 */
	public static final Priority LOW = new Priority(LOW_PRIORITY_VALUE,
			LOW_PRIORITY_LITERAL);

	/**
	 * Indicates Lower priority as a Priority enumeration.
	 */
	public static final Priority LOWER = new Priority(LOWER_PRIORITY_VALUE,
			LOWER_PRIORITY_LITERAL);

	/**
	 * Indicates Lowest priority as a Priority enumeration.
	 */
	public static final Priority LOWEST = new Priority(LOWEST_PRIORITY_VALUE,
			LOWEST_PRIORITY_LITERAL);

	/**
	 * The ordered array of possible enumeration values (0=> Highest,
	 * length-1=>Lowest)
	 */
	public static final Priority[] ENUM_ARRAY = new Priority[] { HIGHEST,
			HIGHER, HIGH, NORMAL, LOW, LOWER, LOWEST };

	/**
	 * 
	 * Returns the correct instance of the Priority ENUM for aLiteral.
	 * 
	 * <p>
	 * This method will return NORMAL if the supplied value of aLiteral is
	 * invalid.
	 * </p>
	 * 
	 * @param aLiteral
	 *            One of the defined *_LITERAL constants of this class
	 * @return The corresponding Priority Enum or NORMAL if aLiteral is invalid
	 */
	public static Priority get(String aLiteral) {
		for (int i = 0; i < ENUM_ARRAY.length; i++) {
			if (ENUM_ARRAY[i].getLiteral().equals(aLiteral)) {
				return ENUM_ARRAY[i];
			}
		}
		return NORMAL;
	}

	/**
	 * 
	 * Returns the correct instance of the Priority ENUM for aValue.
	 * 
	 * <p>
	 * This method will return NORMAL if the supplied value of aValue is
	 * invalid.
	 * </p>
	 * 
	 * @param aValue
	 *            One of the defined *_VALUE constants of this class
	 * @return The corresponding Priority Enum or NORMAL if aValue is invalid
	 */
	public static Priority get(int aValue) {

		switch (aValue) {
		case HIGHEST_PRIORITY_VALUE:
			return HIGHEST;
		case HIGHER_PRIORITY_VALUE:
			return HIGHER;
		case HIGH_PRIORITY_VALUE:
			return HIGH;
		case LOWER_PRIORITY_VALUE:
			return LOWER;
		case LOWEST_PRIORITY_VALUE:
			return LOWEST;
		case NORMAL_PRIORITY_VALUE:
		default:
			return NORMAL;
		}
	}

	private final int value;

	private final String literal;

	protected Priority(int aValue, String aLiteral) {
		value = aValue;
		literal = aLiteral;
	}

	/**
	 * 
	 * @return The literal string for this specific Priority.
	 */
	public String getLiteral() {
		return literal;
	}

	/**
	 * 0 is the lowest priority; 7 is the highest.
	 * 
	 * @return The integer value for this specific Priority. 
	 */
	public int getValue() {
		return value;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Priority ["+getLiteral()+"]";  //$NON-NLS-1$//$NON-NLS-2$
	}
}
