/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.handlers;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;

/**
 * A command parameter value converter to convert between Integers and their
 * String representations for use in the open dialog commands.
 * 
 * @since 3.2
 */
public class DialogIntegerValueConverter extends
		AbstractParameterValueConverter {

	public Object convertToObject(String parameterValue)
			throws ParameterValueConversionException {

		try {
			int i = Integer.parseInt(parameterValue);
			return new Integer(i);
		} catch (NumberFormatException ex) {
			throw new ParameterValueConversionException(
					"error converting to integer: " + parameterValue); //$NON-NLS-1$
		}
	}

	public String convertToString(Object parameterValue)
			throws ParameterValueConversionException {

		if (!(parameterValue instanceof Integer)) {
			throw new ParameterValueConversionException(
					"value for conversion must be an Integer"); //$NON-NLS-1$
		}

		Integer i = (Integer) parameterValue;
		return Integer.toString(i.intValue());
	}

}
