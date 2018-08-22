/*
 * Copyright (C) 2005, 2014 db4objects Inc.  http://www.db4o.com
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 *     Tom Schindl<tom.schindl@bestsolution.at> - bugfix for 217940
 */
package org.eclipse.core.internal.databinding.conversion;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.internal.databinding.BindingMessages;

/**
 * StringToBooleanPrimitiveConverter.
 */
public class StringToBooleanPrimitiveConverter implements IConverter<String, Boolean> {
	private static final String[] trueValues;

	private static final String[] falseValues;

	static {
		String delimiter = BindingMessages.getString(BindingMessages.VALUE_DELIMITER);
		String values = BindingMessages.getString(BindingMessages.TRUE_STRING_VALUES);
		trueValues = valuesToSortedArray(delimiter, values);

		values = BindingMessages.getString(BindingMessages.FALSE_STRING_VALUES);
		falseValues = valuesToSortedArray(delimiter, values);
	}

	/**
	 * Returns a sorted array with all values converted to upper case.
	 *
	 * @param delimiter
	 * @param values
	 * @return sorted array of values
	 */
	private static String[] valuesToSortedArray(String delimiter, String values) {
		List<String> list = new LinkedList<>();
		StringTokenizer tokenizer = new StringTokenizer(values, delimiter);
		while (tokenizer.hasMoreTokens()) {
			list.add(tokenizer.nextToken().toUpperCase());
		}

		String[] array = list.toArray(new String[list.size()]);
		Arrays.sort(array);

		return array;
	}

	@Override
	public Boolean convert(String s) {
		s = s.toUpperCase();

		if (Arrays.binarySearch(trueValues, s) > -1) {
			return Boolean.TRUE;
		}

		if (Arrays.binarySearch(falseValues, s) > -1) {
			return Boolean.FALSE;
		}

		throw new IllegalArgumentException(s + " is not a legal boolean value"); //$NON-NLS-1$
	}

	@Override
	public Object getFromType() {
		return String.class;
	}

	@Override
	public Object getToType() {
		return Boolean.TYPE;
	}

}
