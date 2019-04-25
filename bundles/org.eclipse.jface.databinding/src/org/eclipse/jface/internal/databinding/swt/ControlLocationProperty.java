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
 *     Matthew Hall - initial API and implementation
 *     Tom Schindl - initial API and implementation
 *     Matthew Hall - bug 195222, 263413
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.jface.databinding.swt.WidgetValueProperty;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/**
 * @param <S> type of the source object
 *
 * @since 3.3
 *
 */
public class ControlLocationProperty<S extends Control> extends WidgetValueProperty<S, Point> {
	/**
	 *
	 */
	public ControlLocationProperty() {
		super(SWT.Move);
	}

	@Override
	public Object getValueType() {
		return Point.class;
	}

	@Override
	protected Point doGetValue(S source) {
		return source.getLocation();
	}

	@Override
	protected void doSetValue(S source, Point value) {
		source.setLocation(value);
	}

	@Override
	public String toString() {
		return "Control.location <Point>"; //$NON-NLS-1$
	}
}
