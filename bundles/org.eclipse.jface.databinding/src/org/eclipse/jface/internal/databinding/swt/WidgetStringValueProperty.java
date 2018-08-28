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
 *     Matthew Hall - bugs 195222, 256543, 263413, 262287
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.jface.databinding.swt.WidgetValueProperty;

/**
 * @since 3.3
 *
 */
public abstract class WidgetStringValueProperty extends WidgetValueProperty {
	WidgetStringValueProperty() {
		super();
	}

	WidgetStringValueProperty(int event) {
		super(event);
	}

	WidgetStringValueProperty(int[] events) {
		super(events);
	}

	WidgetStringValueProperty(int[] events, int[] staleEvents) {
		super(events, staleEvents);
	}

	@Override
	public Object getValueType() {
		return String.class;
	}

	@Override
	protected Object doGetValue(Object source) {
		return doGetStringValue(source);
	}

	@Override
	protected void doSetValue(Object source, Object value) {
		doSetStringValue(source, (String) value);
	}

	abstract String doGetStringValue(Object source);

	abstract void doSetStringValue(Object source, String value);
}
