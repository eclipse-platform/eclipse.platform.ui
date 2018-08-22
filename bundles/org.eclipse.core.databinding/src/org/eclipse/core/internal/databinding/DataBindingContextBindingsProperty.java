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

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.property.list.ListProperty;

/**
 * @since 3.3
 *
 */
public final class DataBindingContextBindingsProperty extends ListProperty<DataBindingContext, Binding> {
	@Override
	public Object getElementType() {
		return Binding.class;
	}

	@Override
	protected List<Binding> doGetList(DataBindingContext source) {
		return source.getBindings();
	}

	@Override
	protected void doSetList(DataBindingContext source, List<Binding> list) {
		throw new UnsupportedOperationException(toString() + " is unmodifiable"); //$NON-NLS-1$
	}

	@Override
	protected void doUpdateList(DataBindingContext source, ListDiff<Binding> diff) {
		throw new UnsupportedOperationException(toString() + " is unmodifiable"); //$NON-NLS-1$
	}

	@Override
	public IObservableList<Binding> observe(Realm realm, DataBindingContext source) {
		return source.getBindings();
	}

	@Override
	public String toString() {
		return "DataBindingContext#bindings[] <Binding>"; //$NON-NLS-1$
	}
}