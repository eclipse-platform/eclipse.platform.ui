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
package org.eclipse.ui.internal.commands;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * A proxy for a parameter value converter that has been defined in the regisry.
 * This delays the class loading until the converter is really asked to do
 * string/object conversions.
 * 
 * @since 3.2
 */
public final class ParameterValueConverterProxy extends
		AbstractParameterValueConverter {

	/**
	 * The configuration element providing the executable extension that will
	 * extend <code>AbstractParameterValueConverter</code>. This value will
	 * not be <code>null</code>.
	 */
	private final IConfigurationElement converterConfigurationElement;

	/**
	 * The real parameter value converter instance. This will be
	 * <code>null</code> until one of the conversion methods are used.
	 */
	private AbstractParameterValueConverter parameterValueConverter;

	/**
	 * Constructs a <code>ParameterValueConverterProxy</code> to represent the
	 * real converter until it is needed.
	 * 
	 * @param converterConfigurationElement
	 *            The configuration element from which the real converter can be
	 *            loaded.
	 */
	public ParameterValueConverterProxy(
			final IConfigurationElement converterConfigurationElement) {
		if (converterConfigurationElement == null) {
			throw new NullPointerException(
					"converterConfigurationElement must not be null"); //$NON-NLS-1$
		}

		this.converterConfigurationElement = converterConfigurationElement;
	}

	public final Object convertToObject(final String parameterValue)
			throws ParameterValueConversionException {
		return getConverter().convertToObject(parameterValue);
	}

	public final String convertToString(final Object parameterValue)
			throws ParameterValueConversionException {
		return getConverter().convertToString(parameterValue);
	}

	/**
	 * Returns the real parameter value converter for this proxy or throws an
	 * exception indicating the converter could not be obtained.
	 * 
	 * @return the real converter for this proxy; never <code>null</code>.
	 * @throws ParameterValueConversionException
	 *             if the converter could not be obtained
	 */
	private AbstractParameterValueConverter getConverter()
			throws ParameterValueConversionException {
		if (parameterValueConverter == null) {
			try {
				parameterValueConverter = (AbstractParameterValueConverter) converterConfigurationElement
						.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CONVERTER);
			} catch (final CoreException e) {
				throw new ParameterValueConversionException(
						"Problem creating parameter value converter", e); //$NON-NLS-1$
			} catch (final ClassCastException e) {
				throw new ParameterValueConversionException(
						"Parameter value converter was not a subclass of AbstractParameterValueConverter", e); //$NON-NLS-1$
			}
		}
		return parameterValueConverter;
	}
}
