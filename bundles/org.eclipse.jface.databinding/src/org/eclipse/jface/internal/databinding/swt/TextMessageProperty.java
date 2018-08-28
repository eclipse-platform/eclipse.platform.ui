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

import org.eclipse.swt.widgets.Text;

/**
 * @since 3.3
 *
 */
public class TextMessageProperty extends WidgetStringValueProperty {
	@Override
	String doGetStringValue(Object source) {
		return ((Text) source).getMessage();
	}

	@Override
	void doSetStringValue(Object source, String value) {
		((Text) source).setMessage(value == null ? "" : value); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		return "Text.message<String>"; //$NON-NLS-1$
	}
}
