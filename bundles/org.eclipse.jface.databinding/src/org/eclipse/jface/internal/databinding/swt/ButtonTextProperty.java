/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 213893)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.widgets.Button;

/**
 * @since 3.3
 * 
 */
public class ButtonTextProperty extends WidgetStringValueProperty {
	String doGetStringValue(Object source) {
		return ((Button) source).getText();
	}

	void doSetStringValue(Object source, String value) {
		((Button) source).setText(value == null ? "" : value); //$NON-NLS-1$
	}

	public String toString() {
		return "Button.text <String>"; //$NON-NLS-1$
	}
}
