/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 263709)
 ******************************************************************************/

package org.eclipse.core.internal.databinding;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;

/**
 * @since 3.3
 * 
 */
public class BindingTargetProperty extends SimpleValueProperty {
	public Object getValueType() {
		return IObservable.class;
	}

	protected Object doGetValue(Object source) {
		return ((Binding) source).getTarget();
	}

	protected void doSetValue(Object source, Object value) {
		// no setter API
	}

	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		// no listener API
		return null;
	}

	protected void doAddListener(Object source, INativePropertyListener listener) {
	}

	protected void doRemoveListener(Object source,
			INativePropertyListener listener) {
	}

	public String toString() {
		return "Binding#target <IObservable>"; //$NON-NLS-1$
	}
}
