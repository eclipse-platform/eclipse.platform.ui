/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.commands;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.internal.commands.util.Util;

/**
 * <p>
 * A parameter with a specific value. This is usually a part of a
 * <code>ParameterizedCommand</code>, which is used to refer to a command
 * with a collection of parameterizations.
 * </p>
 * 
 * @since 3.1
 */
public final class Parameterization {

	/**
	 * The constant integer hash code value meaning the hash code has not yet
	 * been computed.
	 */
	private static final int HASH_CODE_NOT_COMPUTED = -1;

	/**
	 * A factor for computing the hash code for all parameterized commands.
	 */
	private static final int HASH_FACTOR = 89;

	/**
	 * The seed for the hash code for all parameterized commands.
	 */
	private static final int HASH_INITIAL = Parameterization.class.getName()
			.hashCode();

	/**
	 * The hash code for this object. This value is computed lazily, and marked
	 * as invalid when one of the values on which it is based changes.
	 */
	private transient int hashCode = HASH_CODE_NOT_COMPUTED;

	/**
	 * The parameter that is being parameterized. This value is never
	 * <code>null</code>.
	 */
	private final IParameter parameter;

	/**
	 * The value that defines the parameterization. This value may be
	 * <code>null</code>.
	 */
	private final String value;

	/**
	 * Constructs a new instance of <code>Parameterization</code>.
	 * 
	 * @param parameter
	 *            The parameter that is being parameterized; must not be
	 *            <code>null</code>.
	 * @param value
	 *            The value for the parameter; may be <code>null</code>.
	 */
	public Parameterization(final IParameter parameter, final String value) {
		if (parameter == null) {
			throw new NullPointerException(
					"You cannot parameterize a null parameter"); //$NON-NLS-1$
		}

		this.parameter = parameter;
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public final boolean equals(final Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof Parameterization)) {
			return false;
		}

		final Parameterization parameterization = (Parameterization) object;
		if (!(Util.equals(this.parameter.getId(), parameterization.parameter
				.getId()))) {
			return false;
		}

		return Util.equals(this.value, parameterization.value);
	}

	/**
	 * Returns the parameter that is being parameterized.
	 * 
	 * @return The parameter; never <code>null</code>.
	 */
	public final IParameter getParameter() {
		return parameter;
	}

	/**
	 * Returns the value for the parameter in this parameterization.
	 * 
	 * @return The value; may be <code>null</code>.
	 */
	public final String getValue() {
		return value;
	}

	/**
	 * Returns the human-readable name for the current value, if any. If the
	 * name cannot be found, then it simply returns the value. It also ensures
	 * that any <code>null</code> values are converted into an empty string.
	 * 
	 * @return The human-readable name of the value; never <code>null</code>.
	 * @throws ParameterValuesException
	 *             If the parameter needed to be initialized, but couldn't be.
	 */
	public final String getValueName() throws ParameterValuesException {
		final Map parameterValues = parameter.getValues().getParameterValues();
		final Iterator parameterValueItr = parameterValues.entrySet()
				.iterator();
		String returnValue = null;
		while (parameterValueItr.hasNext()) {
			final Map.Entry entry = (Map.Entry) parameterValueItr.next();
			final String currentValue = (String) entry.getValue();
			if (Util.equals(value, currentValue)) {
				returnValue = (String) entry.getKey();
				break;
			}
		}

		if (returnValue == null) {
			return Util.ZERO_LENGTH_STRING;
		}

		return returnValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public final int hashCode() {
		if (hashCode == HASH_CODE_NOT_COMPUTED) {
			hashCode = HASH_INITIAL * HASH_FACTOR + Util.hashCode(parameter);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(value);
			if (hashCode == HASH_CODE_NOT_COMPUTED) {
				hashCode++;
			}
		}
		return hashCode;

	}
}
