/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.commands;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;

public class IntegerConverter extends AbstractParameterValueConverter {

	public Object convertToObject(String parameterValue)
			throws ParameterValueConversionException {
		try {
			int val = Integer.parseInt(parameterValue);
			return new Integer(val);
		} catch (NumberFormatException ex) {
			throw new ParameterValueConversionException(
					"Error parsing value: " + parameterValue, ex);
		}
	}
	
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
