/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 263868)
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.value;

import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;

/**
 * @since 3.3
 * 
 */
public final class SelfValueProperty extends SimpleValueProperty {
	private final Object valueType;

	/**
	 * @param valueType
	 */
	public SelfValueProperty(Object valueType) {
		this.valueType = valueType;
	}

	public Object getValueType() {
		return valueType;
	}

	protected Object doGetValue(Object source) {
		return source;
	}

	protected void doSetValue(Object source, Object value) {
	}

	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return null;
	}

	protected void doAddListener(Object source, INativePropertyListener listener) {
	}

	protected void doRemoveListener(Object source,
			INativePropertyListener listener) {
	}
}