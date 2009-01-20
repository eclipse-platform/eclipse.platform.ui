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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;

/**
 * @since 3.3
 * 
 */
public class ButtonSelectionProperty extends WidgetBooleanValueProperty {
	/**
	 * 
	 */
	public ButtonSelectionProperty() {
		super(SWT.Selection);
	}

	boolean doGetBooleanValue(Object source) {
		return ((Button) source).getSelection();
	}

	void doSetBooleanValue(Object source, boolean value) {
		((Button) source).setSelection(value);
	}

	public String toString() {
		return "Button.selection <Boolean>"; //$NON-NLS-1$
	}
}
