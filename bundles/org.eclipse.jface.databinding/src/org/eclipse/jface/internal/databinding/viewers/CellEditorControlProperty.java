/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 234496)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.3
 * 
 */
public class CellEditorControlProperty extends SimpleValueProperty {
	@Override
	public Object getValueType() {
		return Control.class;
	}

	@Override
	protected Object doGetValue(Object source) {
		return ((CellEditor) source).getControl();
	}

	@Override
	protected void doSetValue(Object source, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return null;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
