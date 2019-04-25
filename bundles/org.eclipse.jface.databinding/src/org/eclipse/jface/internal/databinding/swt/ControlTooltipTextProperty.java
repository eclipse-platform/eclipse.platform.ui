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
public class ControlTooltipTextProperty extends WidgetStringValueProperty<Control> {
	@Override
	String doGetStringValue(Control source) {
		return source.getToolTipText();
	}

	@Override
	void doSetStringValue(Control source, String value) {
		source.setToolTipText(value);
	}

	@Override
	public String toString() {
		return "Control.tooltipText <String>"; //$NON-NLS-1$
	}
}
