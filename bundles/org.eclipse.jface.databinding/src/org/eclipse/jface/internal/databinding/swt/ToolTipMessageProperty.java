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
 *     Matthew Hall - initial API and implementation (bug 266563)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.widgets.ToolTip;

/**
 * @since 3.3
 *
 */
public class ToolTipMessageProperty extends WidgetStringValueProperty<ToolTip> {
	@Override
	String doGetStringValue(ToolTip source) {
		return source.getMessage();
	}

	@Override
	void doSetStringValue(ToolTip source, String value) {
		source.setMessage(value == null ? "" : value); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		return "ToolTip.message<String>"; //$NON-NLS-1$
	}
}
