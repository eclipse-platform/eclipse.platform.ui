/*******************************************************************************
 * Copyright (c) 2020 Jens Lidestrom and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.widgets.ToolTip;

/**
 * @since 1.10.0
 */
public class ToolTipVisibleProperty extends VisibleProperty<ToolTip> {
	@Override
	protected boolean doGetVisibleValue(ToolTip source) {
		return source.getVisible();
	}

	@Override
	protected void doSetBooleanValue(ToolTip source, boolean value) {
		source.setVisible(value);
	}

	@Override
	public String toString() {
		return "ToolTip.visible <boolean>"; //$NON-NLS-1$
	}
}
