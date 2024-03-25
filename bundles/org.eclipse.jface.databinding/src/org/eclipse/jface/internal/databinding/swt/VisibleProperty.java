/*******************************************************************************
 * Copyright (c) 2020, Jens Lidestrom and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Lidestrom - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;

/**
 * This class is used to work around the fact that the SWT.Show event is sent
 * before the widget isVisible state changes. It uses {@link Widget#setData} to
 * store a the latest visibility value.
 *
 * @param <S> type of the source object
 */
public abstract class VisibleProperty<S extends Widget> extends WidgetBooleanValueProperty<S> {
	private static final int[] EVENT_TYPES = new int[] { SWT.Show, SWT.Hide };
	private static final String CACHED_VALUE_KEY = VisibleProperty.class.getName() + ".IS_VISIBLE"; //$NON-NLS-1$

	public VisibleProperty() {
		super(EVENT_TYPES);
	}

	protected abstract boolean doGetVisibleValue(S source);

	@Override
	final protected boolean doGetBooleanValue(S source) {
		Boolean cachedVisibleValue = (Boolean) source.getData(CACHED_VALUE_KEY);
		return cachedVisibleValue == null ? doGetVisibleValue(source) : cachedVisibleValue;
	}

	@Override
	public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, ValueDiff<? extends Boolean>> listener) {
		return new WidgetListener<S, ValueDiff<? extends Boolean>>(this, listener, EVENT_TYPES, null) {
			@Override
			public void handleEvent(Event event) {
				event.widget.setData(CACHED_VALUE_KEY, event.type == SWT.Show);
				super.handleEvent(event);
			}

			@Override
			protected void doRemoveFrom(S source) {
				source.setData(CACHED_VALUE_KEY, null);
				super.doRemoveFrom(source);
			}
		};
	}
}
