/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *     Matthew Hall - bug 194734
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *******************************************************************************/

package org.eclipse.core.databinding.observable.value;

import java.util.Objects;

import org.eclipse.core.databinding.observable.IDiff;

/**
 * @param <T>
 *            the type of value being observed
 * @since 1.0
 */
public abstract class ValueDiff<T> implements IDiff {
	/**
	 * Creates a value diff.
	 */
	public ValueDiff() {
	}

	/**
	 * @return the old value
	 */
	public abstract T getOldValue();

	/**
	 * @return the new value
	 */
	public abstract T getNewValue();

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ValueDiff) {
			ValueDiff<?> val = (ValueDiff<?>) obj;

			return Objects.equals(val.getNewValue(), getNewValue()) && Objects.equals(val.getOldValue(), getOldValue());

		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hashCode(getNewValue());
		return prime * result + Objects.hashCode(getOldValue());
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer
			.append(getClass().getName())
			.append("{oldValue [") //$NON-NLS-1$
			.append(getOldValue() != null ? getOldValue().toString() : "null") //$NON-NLS-1$
			.append("], newValue [") //$NON-NLS-1$
			.append(getNewValue() != null ? getNewValue().toString() : "null") //$NON-NLS-1$
			.append("]}"); //$NON-NLS-1$
		return buffer.toString();
	}
}
