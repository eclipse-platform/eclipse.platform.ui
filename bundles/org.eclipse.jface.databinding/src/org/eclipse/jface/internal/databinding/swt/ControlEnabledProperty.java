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
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.widgets.Control;

/**
 * @since 3.3
 *
 */
public class ControlEnabledProperty extends WidgetBooleanValueProperty<Control> {
	@Override
	public boolean doGetBooleanValue(Control source) {
		return source.getEnabled();
	}

	@Override
	void doSetBooleanValue(Control source, boolean value) {
		source.setEnabled(value);
	}

	@Override
	public String toString() {
		return "Control.enabled <boolean>"; //$NON-NLS-1$
	}
}
