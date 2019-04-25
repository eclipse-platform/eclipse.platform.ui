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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;

/**
 * @param <S> type of the source object
 *
 * @since 3.3
 *
 */
public class ControlForegroundProperty<S extends Control> extends WidgetValueProperty<S, Color> {
	@Override
	public Object getValueType() {
		return Color.class;
	}

	@Override
	protected Color doGetValue(S source) {
		return source.getForeground();
	}

	@Override
	protected void doSetValue(S source, Color value) {
		source.setForeground(value);
	}

	@Override
	public String toString() {
		return "Control.foreground <Color>"; //$NON-NLS-1$
	}
}
