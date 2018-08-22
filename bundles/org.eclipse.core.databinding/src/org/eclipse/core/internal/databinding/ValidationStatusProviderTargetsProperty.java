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

import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.property.list.ListProperty;

/**
 * @since 3.3
 *
 */
public class ValidationStatusProviderTargetsProperty extends ListProperty<ValidationStatusProvider, IObservable> {
	@Override
	public Object getElementType() {
		return IObservable.class;
	}

	@Override
	protected List<IObservable> doGetList(ValidationStatusProvider source) {
		return source.getTargets();
	}

	@Override
	protected void doSetList(ValidationStatusProvider source, List<IObservable> list) {
		throw new UnsupportedOperationException(toString() + " is unmodifiable"); //$NON-NLS-1$
	}

	@Override
	protected void doUpdateList(ValidationStatusProvider source, ListDiff<IObservable> diff) {
		throw new UnsupportedOperationException(toString() + " is unmodifiable"); //$NON-NLS-1$
	}

	@Override
	public IObservableList<IObservable> observe(Realm realm, ValidationStatusProvider source) {
		return source.getTargets();
	}

	@Override
	public String toString() {
		return "ValidationStatusProvider#targets[] <IObservable>"; //$NON-NLS-1$
	}
}
