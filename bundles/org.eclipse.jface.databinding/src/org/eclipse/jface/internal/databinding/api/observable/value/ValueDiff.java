/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.api.observable.value;

import org.eclipse.jface.internal.databinding.api.observable.Diffs;

/**
 * @since 3.2
 *
 */
public class ValueDiff implements IValueDiff {
	
	private Object oldValue;
	private Object newValue;

	public ValueDiff(Object oldValue, Object newValue) {
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding2.value.IValueDiff#getOldValue()
	 */
	public Object getOldValue() {
		return oldValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding2.value.IValueDiff#getNewValue()
	 */
	public Object getNewValue() {
		return newValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ValueDiff) {
			ValueDiff val = (ValueDiff) obj;
			
			return Diffs.equals(val.newValue, newValue)
				&& Diffs.equals(val.oldValue, oldValue);
			
		}
		return false;
	}
	
}
