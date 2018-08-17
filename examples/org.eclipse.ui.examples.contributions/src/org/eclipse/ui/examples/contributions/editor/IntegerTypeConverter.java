/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
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
