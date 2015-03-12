/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
