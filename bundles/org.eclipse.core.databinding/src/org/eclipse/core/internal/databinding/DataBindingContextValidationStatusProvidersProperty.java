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

import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.property.list.ListProperty;

/**
 * @since 3.3
 *
 */
public final class DataBindingContextValidationStatusProvidersProperty
		extends ListProperty<DataBindingContext, ValidationStatusProvider> {
	@Override
	public Object getElementType() {
		return ValidationStatusProvider.class;
	}

	@Override
	protected List<ValidationStatusProvider> doGetList(DataBindingContext source) {
		return source.getValidationStatusProviders();
	}

	@Override
	protected void doSetList(DataBindingContext source, List<ValidationStatusProvider> list) {
		throw new UnsupportedOperationException(toString() + " is unmodifiable"); //$NON-NLS-1$
	}

	@Override
	protected void doUpdateList(DataBindingContext source, ListDiff<ValidationStatusProvider> diff) {
		throw new UnsupportedOperationException(toString() + " is unmodifiable"); //$NON-NLS-1$
	}

	@Override
	public IObservableList<ValidationStatusProvider> observe(Realm realm,
			DataBindingContext source) {
		return source.getValidationStatusProviders();
	}

	@Override
	public String toString() {
		return "Binding#validationStatusProviders[] <ValidationStatusProvider>"; //$NON-NLS-1$
	}
}