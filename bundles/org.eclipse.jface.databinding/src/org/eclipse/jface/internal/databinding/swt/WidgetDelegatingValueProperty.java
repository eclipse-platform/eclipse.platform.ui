/*******************************************************************************
 * Copyright (c) 2009, 2014 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 264286)
 *******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.property.value.DelegatingValueProperty;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Widget;

/**
 * @param <S>
 *            type of the source object
 * @param <T>
 *            type of the value of the property
 *
 * @since 3.3
 */
@SuppressWarnings("deprecation")
abstract class WidgetDelegatingValueProperty<S extends Widget, T> extends DelegatingValueProperty<S, T>
		implements IWidgetValueProperty<S, T> {

	RuntimeException notSupported(Object source) {
		return new IllegalArgumentException(
				"Widget [" + source.getClass().getName() + "] is not supported."); //$NON-NLS-1$//$NON-NLS-2$
	}

	public WidgetDelegatingValueProperty() {
	}

	public WidgetDelegatingValueProperty(Object valueType) {
		super(valueType);
	}

	@Override
	public ISWTObservableValue<T> observe(S widget) {
		return (ISWTObservableValue<T>) observe(DisplayRealm.getRealm(widget.getDisplay()), widget);
	}

	@Override
	public ISWTObservableValue<T> observeDelayed(int delay, S widget) {
		return SWTObservables.observeDelayedValue(delay, observe(widget));
	}
}
