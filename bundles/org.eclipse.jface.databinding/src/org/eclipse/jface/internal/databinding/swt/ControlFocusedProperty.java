/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation
 *     Tom Schindl - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.3
 * 
 */
public class ControlFocusedProperty extends WidgetBooleanValueProperty {
	/**
	 * 
	 */
	public ControlFocusedProperty() {
		super(new int[] { SWT.FocusIn, SWT.FocusOut });
	}

	public boolean doGetBooleanValue(Object source) {
		return ((Control) source).isFocusControl();
	}

	public void doSetBooleanValue(Object source, boolean value) {
		if (value)
			((Control) source).setFocus();
	}

	public String toString() {
		return "Control.focus <boolean>"; //$NON-NLS-1$
	}
}
