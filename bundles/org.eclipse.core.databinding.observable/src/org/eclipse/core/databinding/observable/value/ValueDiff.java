/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 194734
 *******************************************************************************/

package org.eclipse.core.databinding.observable.value;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IDiff;

/**
 * @since 1.0
 * 
 */
public abstract class ValueDiff implements IDiff {
	/**
	 * Creates a value diff.
	 */
	public ValueDiff() {
	}

	/**
	 * @return the old value
	 */
	public abstract Object getOldValue();

	/**
	 * @return the new value
	 */
	public abstract Object getNewValue();

	public boolean equals(Object obj) {
		if (obj instanceof ValueDiff) {
			ValueDiff val = (ValueDiff) obj;

			return Diffs.equals(val.getNewValue(), getNewValue())
					&& Diffs.equals(val.getOldValue(), getOldValue());

		}
		return false;
	}
		
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		Object nv = getNewValue();
		Object ov = getOldValue();
		result = prime * result + ((nv == null) ? 0 : nv.hashCode());
		result = prime * result + ((ov == null) ? 0 : ov.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
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
