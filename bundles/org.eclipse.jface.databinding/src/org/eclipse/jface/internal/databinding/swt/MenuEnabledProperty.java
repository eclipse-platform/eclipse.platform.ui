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
 *     Matthew Hall - initial API and implementation (bug 280157)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.widgets.Menu;

/**
 *
 */
public class MenuEnabledProperty extends WidgetBooleanValueProperty {
	@Override
	public boolean doGetBooleanValue(Object source) {
		return ((Menu) source).getEnabled();
	}

	@Override
	void doSetBooleanValue(Object source, boolean value) {
		((Menu) source).setEnabled(value);
	}

	@Override
	public String toString() {
		return "Menu.enabled <boolean>"; //$NON-NLS-1$
	}
}
