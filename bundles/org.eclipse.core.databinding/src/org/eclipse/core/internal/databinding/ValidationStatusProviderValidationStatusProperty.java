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

import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.core.runtime.IStatus;

/**
 * @since 3.3
 *
 */
public final class ValidationStatusProviderValidationStatusProperty
		extends SimpleValueProperty<ValidationStatusProvider, IObservableValue<IStatus>> {
	@Override
	public Object getValueType() {
		return IObservableValue.class;
	}

	@Override
	protected IObservableValue<IStatus> doGetValue(ValidationStatusProvider source) {
		return source.getValidationStatus();
	}

	@Override
	protected void doSetValue(ValidationStatusProvider source, IObservableValue<IStatus> value) {
		// no setter API
	}

	@Override
	public INativePropertyListener<ValidationStatusProvider> adaptListener(
			ISimplePropertyListener<ValidationStatusProvider, ValueDiff<? extends IObservableValue<IStatus>>> listener) {
		// no listener API
		return null;
	}

	protected void doAddListener(ValidationStatusProvider source,
			INativePropertyListener<ValidationStatusProvider> listener) {
	}

	protected void doRemoveListener(ValidationStatusProvider source,
			INativePropertyListener<ValidationStatusProvider> listener) {
	}

	@Override
	public String toString() {
		return "ValidationStatusProvider#validationStatus <IObservableValue>"; //$NON-NLS-1$
	}
}