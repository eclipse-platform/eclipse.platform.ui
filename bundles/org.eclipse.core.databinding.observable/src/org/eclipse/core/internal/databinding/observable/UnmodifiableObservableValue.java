/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 219909)
 *     Matthew Hall - bugs 237884, 237718
 *     Ovidio Mallo - bug 237163
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import org.eclipse.core.databinding.observable.value.DecoratingObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;

/**
 * An unmodifiable wrapper class for IObservableValue instances.
 *
 * @param <T>
 *            the type of the value being observed
 *
 * @since 1.1
 */
public class UnmodifiableObservableValue<T> extends DecoratingObservableValue<T> {
	/**
	 * Constructs an UnmodifiableObservableValue which wraps the given
	 * observable value
	 *
	 * @param wrappedValue
	 *            the observable value to wrap in an unmodifiable instance.
	 */
	public UnmodifiableObservableValue(IObservableValue<T> wrappedValue) {
		super(wrappedValue, false);
	}

	@Override
	public void setValue(T value) {
		throw new UnsupportedOperationException();
	}
}
