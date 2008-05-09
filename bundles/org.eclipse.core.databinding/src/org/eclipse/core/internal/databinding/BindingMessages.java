/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl<tom.schindl@bestsolution.at> - bugfix for 217940
 *******************************************************************************/
package org.eclipse.core.internal.databinding;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ibm.icu.text.MessageFormat;

/**
 * @since 1.0
 *
 */
public class BindingMessages {

	/**
	 * The Binding resource bundle; eagerly initialized.
	 */
	private static final ResourceBundle bundle = ResourceBundle
			.getBundle("org.eclipse.core.internal.databinding.messages"); //$NON-NLS-1$

	/**
	 * Key to be used for an index out of range message.
	 */
	public static final String INDEX_OUT_OF_RANGE = "IndexOutOfRange"; //$NON-NLS-1$

	/**
	 * Key to be used for a "Multiple Problems." message.
	 */
	public static final String MULTIPLE_PROBLEMS = "MultipleProblems"; //$NON-NLS-1$

	/**
	 * Key to be used for a "ValueBinding_ErrorWhileSettingValue" message
	 */
	public static final String VALUEBINDING_ERROR_WHILE_SETTING_VALUE = "ValueBinding_ErrorWhileSettingValue"; //$NON-NLS-1$

	/**
	 * Key to be used for a "DateFormat_DateTime" message
	 */
	public static final String DATE_FORMAT_DATE_TIME = "DateFormat_DateTime"; //$NON-NLS-1$

	/**
	 * Key to be used for a "DateFormat_Time" message
	 */
	public static final String DATEFORMAT_TIME = "DateFormat_Time"; //$NON-NLS-1$

	/**
	 * Key to be used for a "ValueDelimiter" message
	 */
	public static final String VALUE_DELIMITER = "ValueDelimiter"; //$NON-NLS-1$

	/**
	 * Key to be used for a "TrueStringValues" message
	 */
	public static final String TRUE_STRING_VALUES = "TrueStringValues"; //$NON-NLS-1$

	/**
	 * Key to be used for a "FalseStringValues" message
	 */
	public static final String FALSE_STRING_VALUES = "FalseStringValues"; //$NON-NLS-1$

	/**
	 * Key to be used for a "Validate_NumberOutOfRangeError" message
	 */
	public static final String VALIDATE_NUMBER_OUT_OF_RANGE_ERROR = "Validate_NumberOutOfRangeError"; //$NON-NLS-1$

	/**
	 * Key to be used for a "Validate_NumberParseError" message
	 */
	public static final String VALIDATE_NUMBER_PARSE_ERROR = "Validate_NumberParseError"; //$NON-NLS-1$

	/**
	 * Key to be used for a "Validate_ConversionToPrimitive" message
	 */
	public static final String VALIDATE_CONVERSION_TO_PRIMITIVE = "Validate_ConversionToPrimitive"; //$NON-NLS-1$

	/**
	 * Key to be used for a "Validate_ConversionFromClassToPrimitive" message
	 */
	public static final String VALIDATE_CONVERSION_FROM_CLASS_TO_PRIMITIVE = "Validate_ConversionFromClassToPrimitive"; //$NON-NLS-1$

	/**
	 * Key to be used for a "Validate_NoChangeAllowedHelp" message
	 */
	public static final String VALIDATE_NO_CHANGE_ALLOWED_HELP = "Validate_NoChangeAllowedHelp"; //$NON-NLS-1$

	/**
	 * Key to be used for a "Validate_CharacterHelp" message
	 */
	public static final String VALIDATE_CHARACTER_HELP = "Validate_CharacterHelp"; //$NON-NLS-1$

	/**
	 * Key to be used for a "Examples" message
	 */
	public static final String EXAMPLES = "Examples"; //$NON-NLS-1$

	/**
	 * Key to be used for a "Validate_NumberParseErrorNoCharacter" message
	 */
	public static final String VALIDATE_NUMBER_PARSE_ERROR_NO_CHARACTER = "Validate_NumberParseErrorNoCharacter"; //$NON-NLS-1$

	/**
	 * Returns the resource object with the given key in the resource bundle for
	 * JFace Data Binding. If there isn't any value under the given key, the key
	 * is returned.
	 *
	 * @param key
	 *            the resource name
	 * @return the string
	 */
	public static String getString(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns a formatted string with the given key in the resource bundle for
	 * JFace Data Binding.
	 *
	 * @param key
	 * @param arguments
	 * @return formatted string, the key if the key is invalid
	 */
	public static String formatString(String key, Object[] arguments) {
		try {
			return MessageFormat.format(bundle.getString(key), arguments);
		} catch (MissingResourceException e) {
			return key;
		}
	}
}
