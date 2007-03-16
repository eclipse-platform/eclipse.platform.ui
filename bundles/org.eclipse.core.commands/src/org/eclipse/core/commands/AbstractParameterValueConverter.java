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
package org.eclipse.core.commands;

/**
 * <p>
 * Supports conversion between objects and strings for command parameter values.
 * Extenders must produce strings that identify objects (of a specific command
 * parameter type) as well as consume the strings to locate and return the
 * objects they identify.
 * </p>
 * <p>
 * This class offers multiple handlers of a command a consistent way of
 * converting string parameter values into the objects that the handlers would
 * prefer to deal with. This class also gives clients a way to serialize
 * object parameters as strings so that entire parameterized commands can be
 * serialized, stored and later deserialized and executed.
 * </p>
 * <p>
 * This class will typically be extended so the subclass can be referenced from
 * the <code>converter</code> attribute of the
 * <code>commandParameterType</code> elemement of the
 * <code>org.eclipse.ui.commands</code> extension-point. Objects implementing
 * this interface may also be passed directly to
 * {@link ParameterType#define(String, AbstractParameterValueConverter)} by
 * clients.
 * </p>
 * 
 * @see ParameterType#define(String, AbstractParameterValueConverter)
 * @see ParameterizedCommand#serialize()
 * @since 3.2
 */
public abstract class AbstractParameterValueConverter {

	/**
	 * Converts a string encoded command parameter value into the parameter
	 * value object.
	 * 
	 * @param parameterValue
	 *            a command parameter value string describing an object; may be
	 *            <code>null</code>
	 * @return the object described by the command parameter value string; may
	 *         be <code>null</code>
	 * @throws ParameterValueConversionException
	 *             if an object cannot be produced from the
	 *             <code>parameterValue</code> string
	 */
	public abstract Object convertToObject(final String parameterValue)
			throws ParameterValueConversionException;

	/**
	 * Converts a command parameter value object into a string that encodes a
	 * reference to the object or serialization of the object.
	 * 
	 * @param parameterValue
	 *            an object to convert into an identifying string; may be
	 *            <code>null</code>
	 * @return a string describing the provided object; may be <code>null</code>
	 * @throws ParameterValueConversionException
	 *             if a string reference or serialization cannot be provided for
	 *             the <code>parameterValue</code>
	 */
	public abstract String convertToString(final Object parameterValue)
			throws ParameterValueConversionException;

}
