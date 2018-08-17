/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.commands;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;

public class IntegerConverter extends AbstractParameterValueConverter {

	@Override
	public Object convertToObject(String parameterValue)
			throws ParameterValueConversionException {
		try {
			int val = Integer.parseInt(parameterValue);
			return Integer.valueOf(val);
		} catch (NumberFormatException ex) {
			throw new ParameterValueConversionException(
					"Error parsing value: " + parameterValue, ex);
		}
	}

	@Override
	public String convertToString(Object parameterValue)
			throws ParameterValueConversionException {
		if (!(parameterValue instanceof Integer)) {
			throw new ParameterValueConversionException("Invalid object type: "
					+ parameterValue);
		}
		Integer val = (Integer) parameterValue;
		return val.toString();
	}
}
