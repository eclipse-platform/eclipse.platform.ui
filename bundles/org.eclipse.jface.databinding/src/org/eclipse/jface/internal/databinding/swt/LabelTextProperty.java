/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.widgets.Label;

/**
 * @since 3.3
 * 
 */
public class LabelTextProperty extends WidgetStringValueProperty {
	String doGetStringValue(Object source) {
		return ((Label) source).getText();
	}

	void doSetStringValue(Object source, String value) {
		((Label) source).setText(value == null ? "" : value); //$NON-NLS-1$
	}

	public String toString() {
		return "Label.text <String>"; //$NON-NLS-1$
	}
}
