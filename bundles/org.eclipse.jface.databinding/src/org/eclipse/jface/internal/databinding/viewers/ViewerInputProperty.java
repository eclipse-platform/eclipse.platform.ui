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

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.jface.databinding.viewers.ViewerValueProperty;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.3
 *
 */
public class ViewerInputProperty extends ViewerValueProperty {
	@Override
	public Object getValueType() {
		return null;
	}

	@Override
	protected Object doGetValue(Object source) {
		return ((Viewer) source).getInput();
	}

	@Override
	protected void doSetValue(Object source, Object value) {
		((Viewer) source).setInput(value);
	}

	@Override
	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return null;
	}

	protected void doAddListener(Object source, INativePropertyListener listener) {
	}

	protected void doRemoveListener(Object source,
			INativePropertyListener listener) {
	}

	@Override
	public String toString() {
		return "Viewer.input"; //$NON-NLS-1$
	}
}
