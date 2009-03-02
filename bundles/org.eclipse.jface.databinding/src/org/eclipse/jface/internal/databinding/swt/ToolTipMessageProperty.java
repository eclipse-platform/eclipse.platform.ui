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

import org.eclipse.swt.widgets.ToolTip;

/**
 * @since 3.3
 * 
 */
public class ToolTipMessageProperty extends WidgetStringValueProperty {
	String doGetStringValue(Object source) {
		return ((ToolTip) source).getMessage();
	}

	void doSetStringValue(Object source, String value) {
		((ToolTip) source).setMessage(value == null ? "" : value); //$NON-NLS-1$
	}

	public String toString() {
		return "ToolTip.message<String>"; //$NON-NLS-1$
	}
}
