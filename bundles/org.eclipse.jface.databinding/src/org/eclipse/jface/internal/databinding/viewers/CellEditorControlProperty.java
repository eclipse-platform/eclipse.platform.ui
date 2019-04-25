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
 *     Matthew Hall - initial API and implementation (bug 234496)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.3
 *
 */
public class CellEditorControlProperty extends SimpleValueProperty<CellEditor, Control> {
	@Override
	public Object getValueType() {
		return Control.class;
	}

	@Override
	protected Control doGetValue(CellEditor source) {
		return source.getControl();
	}

	@Override
	protected void doSetValue(CellEditor source, Control value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public INativePropertyListener<CellEditor> adaptListener(
			ISimplePropertyListener<CellEditor, ValueDiff<? extends Control>> listener) {
		return null;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
