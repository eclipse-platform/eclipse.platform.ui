/*******************************************************************************
 * Copyright (c) 2011, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 306203)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.custom.CCombo;

/**
 * @since 3.3
 *
 */
public class CComboEditableProperty extends WidgetBooleanValueProperty {
	@Override
	boolean doGetBooleanValue(Object source) {
		return ((CCombo) source).getEditable();
	}

	@Override
	void doSetBooleanValue(Object source, boolean value) {
		((CCombo) source).setEditable(value);
	}

	@Override
	public String toString() {
		return "CCombo.editable <boolean>"; //$NON-NLS-1$
	}
}
