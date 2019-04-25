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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;

/**
 * @since 3.3
 *
 */
public class ButtonSelectionProperty extends WidgetBooleanValueProperty<Button> {
	/**
	 *
	 */
	public ButtonSelectionProperty() {
		super(SWT.Selection);
	}

	@Override
	boolean doGetBooleanValue(Button source) {
		return source.getSelection();
	}

	@Override
	void doSetBooleanValue(Button source, boolean value) {
		source.setSelection(value);
	}

	@Override
	public String toString() {
		return "Button.selection <Boolean>"; //$NON-NLS-1$
	}
}
