/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
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
package org.eclipse.e4.ui.internal.workbench;

import java.util.Collections;
import java.util.Objects;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.IParameterValues;
import org.eclipse.core.commands.ITypedParameter;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.commands.ParameterValuesException;
import org.eclipse.core.commands.common.HandleObject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * <p>
 * A parameter for a command. A parameter identifies a type of information that the command might
 * accept. For example, a "Show View" command might accept the id of a view for display. This
 * parameter also identifies possible values, for display in the user interface.
 * </p>
 * <p>
 * Parameters are mutable, and can change as the command changes. Notifications will not be sent if
 * the parameter itself changes. Listeners can be attached to the command.
 * </p>
 *
 * @since 3.1
 */
public final class Parameter implements IParameter, ITypedParameter {
	/**
	 * The name of the configuration element attribute contain the values. This is used to retrieve
	 * the executable extension <code>IParameterValues</code>.
	 */
	private static final String ATTRIBUTE_VALUES = "values"; //$NON-NLS-1$

	/**
	 * The constant integer hash code value meaning the hash code has not yet been computed.
	 */
	private static final int HASH_CODE_NOT_COMPUTED = -1;

	/**
	 * A factor for computing the hash code for all schemes.
	 */
	private static final int HASH_FACTOR = 89;

	/**
	 * The seed for the hash code for all schemes.
	 */
	private static final int HASH_INITIAL = HandleObject.class.getName().hashCode();

	/**
	 * The hash code for this object. This value is computed lazily, and marked as invalid when one
	 * of the values on which it is based changes.
	 */
	private transient int hashCode = HASH_CODE_NOT_COMPUTED;

	/**
	 * The identifier for this object. This identifier should be unique across all objects of the
	 * same type and should never change. This value will never be <code>null</code>.
	 */
	protected final String id;

	/**
	 * The non-externalized name of this parameter. The name is used as the in a name-value
	 * parameter map. This value will never be <code>null</code>.
	 */
	private final String name;

	/**
	 * Whether the parameter is optional (as opposed to required).
	 */
	private final boolean optional;

	/**
	 * The type for this parameter. This value may be <code>null</code> if the parameter is not
	 * typed.
	 */
	private final ParameterType parameterType;

	/**
	 * The string representation of this object. This string is for debugging purposes only, and is
	 * not meant to be displayed to the user. This value is computed lazily, and is cleared if one
	 * of its dependent values changes.
	 */
	protected transient String string = null;

	/**
	 * The actual <code>IParameterValues</code> implementation. This is lazily loaded from the
	 * <code>valuesConfigurationElement</code>, to avoid unnecessary class-loading.
	 */
	private transient IParameterValues values = null;

	private final IConfigurationElement valuesConfigurationElement;

	/**
	 * Constructs a new instance of <code>Parameter</code> with all of its values pre-defined.
	 *
	 * @param id
	 *            The identifier for this parameter; must not be <code>null</code>.
	 * @param name
	 *            The name for this parameter; must not be <code>null</code>.
	 * @param values
	 *            The values for this parameter; must not be <code>null</code>.
	 * @param parameterType
	 *            the type for this parameter; may be <code>null</code> if the parmeter doesn't
	 *            declare type.
	 * @param optional
	 *            Whether this parameter is optional (as opposed to required).
	 */
	public Parameter(final String id, final String name, final IConfigurationElement values,
			final ParameterType parameterType, final boolean optional) {
		if (id == null) {
			throw new NullPointerException("Cannot create a parameter with a null id"); //$NON-NLS-1$
		}

		if (name == null) {
			throw new NullPointerException("The name of a parameter cannot be null."); //$NON-NLS-1$
		}

		this.valuesConfigurationElement = values;
		this.id = id;
		this.name = name;
		this.parameterType = parameterType;
		this.optional = optional;
	}

	/**
	 * Tests whether this object is equal to another object. A parameter is only equal to another
	 * parameter with the same properties.
	 *
	 * @param object
	 *            The object with which to compare; may be <code>null</code>.
	 * @return <code>true</code> if the objects are equal; <code>false</code> otherwise.
	 */
	@Override
	public final boolean equals(final Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof final Parameter parameter)) {
			return false;
		}

		if (!Objects.equals(id, parameter.id)) {
			return false;
		}
		if (!Objects.equals(name, parameter.name)) {
			return false;
		}
		if (!Objects.equals(values, parameter.values)) {
			return false;
		}

		return Objects.equals(optional, parameter.optional);
	}

	@Override
	public final String getId() {
		return id;
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final ParameterType getParameterType() {
		return parameterType;
	}

	@Override
	public final IParameterValues getValues() throws ParameterValuesException {
		if (values == null) {
			if (valuesConfigurationElement != null) {
				try {
					values = (IParameterValues) valuesConfigurationElement
							.createExecutableExtension(ATTRIBUTE_VALUES);
				} catch (final CoreException e) {
					throw new ParameterValuesException("Problem creating parameter values", e); //$NON-NLS-1$
				} catch (final ClassCastException e) {
					throw new ParameterValuesException(
							"Parameter values were not an instance of IParameterValues", e); //$NON-NLS-1$
				}
			} else {
				values = () -> Collections.EMPTY_MAP;
			}
		}

		return values;
	}

	@Override
	public final int hashCode() {
		if (hashCode == HASH_CODE_NOT_COMPUTED) {
			hashCode = HASH_INITIAL * HASH_FACTOR + Objects.hashCode(id);
			if (hashCode == HASH_CODE_NOT_COMPUTED) {
				hashCode++;
			}
		}
		return hashCode;
	}

	@Override
	public final boolean isOptional() {
		return optional;
	}

	@Override
	public final String toString() {
		if (string == null) {
			final StringBuilder buffer = new StringBuilder();

			buffer.append("Parameter("); //$NON-NLS-1$
			buffer.append(id);
			buffer.append(',');
			buffer.append(name);
			buffer.append(',');
			buffer.append(values);
			buffer.append(',');
			buffer.append(optional);
			buffer.append(')');

			string = buffer.toString();
		}

		return string;
	}
}
