/*******************************************************************************
 * Copyright (c) 2009, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 263709)
 ******************************************************************************/

package org.eclipse.core.internal.databinding;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;

/**
 * @since 3.3
 *
 */
public class BindingTargetProperty extends SimpleValueProperty<Binding, IObservable> {
	@Override
	public Object getValueType() {
		return IObservable.class;
	}

	@Override
	protected IObservable doGetValue(Binding source) {
		return source.getTarget();
	}

	@Override
	protected void doSetValue(Binding source, IObservable value) {
		// no setter API
	}

	@Override
	public INativePropertyListener<Binding> adaptListener(
			ISimplePropertyListener<Binding, ValueDiff<? extends IObservable>> listener) {
		// no listener API
		return null;
	}

	protected void doAddListener(Binding source,
			INativePropertyListener<Binding> listener) {
	}

	protected void doRemoveListener(Binding source,
			INativePropertyListener<Binding> listener) {
	}

	@Override
	public String toString() {
		return "Binding#target <IObservable>"; //$NON-NLS-1$
	}
}
