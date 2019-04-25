/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 195222, 263413
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.jface.databinding.swt.WidgetValueProperty;
import org.eclipse.swt.widgets.Widget;

/**
 * @param <S> type of the source object
 *
 * @since 3.3
 */
public abstract class WidgetIntValueProperty<S extends Widget> extends WidgetValueProperty<S, Integer> {
	WidgetIntValueProperty() {
		super();
	}

	WidgetIntValueProperty(int event) {
		super(event);
	}

	WidgetIntValueProperty(int[] events) {
		super(events);
	}

	@Override
	public Object getValueType() {
		return Integer.TYPE;
	}

	@Override
	protected Integer doGetValue(S source) {
		return doGetIntValue(source);
	}

	@Override
	protected void doSetValue(S source, Integer value) {
		doSetIntValue(source, value);
	}

	abstract int doGetIntValue(S source);

	abstract void doSetIntValue(S source, int intValue);
}
