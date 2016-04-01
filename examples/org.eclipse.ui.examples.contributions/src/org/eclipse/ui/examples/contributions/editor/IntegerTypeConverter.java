/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.examples.contributions.editor;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;

/**
 * Convert between Integer and String for a command parameter type.
 * 
 * @since 3.4
 */
public class IntegerTypeConverter extends AbstractParameterValueConverter {

	@Override
	public Object convertToObject(String parameterValue)
			throws ParameterValueConversionException {
		try {
			return Integer.decode(parameterValue);
		} catch (NumberFormatException e) {
			throw new ParameterValueConversionException("Failed to decode", e); //$NON-NLS-1$
		}
	}

	@Override
	public String convertToString(Object parameterValue)
			throws ParameterValueConversionException {
		if (!(parameterValue instanceof Integer)) {
			throw new ParameterValueConversionException("Failed to convert"); //$NON-NLS-1$
		}
		return parameterValue.toString();
	}

}
