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
import org.eclipse.swt.custom.CCombo;

/**
 * @since 3.3
 * 
 */
public class CComboSingleSelectionIndexProperty extends WidgetIntValueProperty {
	/**
	 * 
	 */
	public CComboSingleSelectionIndexProperty() {
		super(new int[] { SWT.Selection, SWT.DefaultSelection });
	}

	int doGetIntValue(Object source) {
		return ((CCombo) source).getSelectionIndex();
	}

	void doSetIntValue(Object source, int value) {
		((CCombo) source).select(value);
	}

	public String toString() {
		return "CCombo.selectionIndex <int>"; //$NON-NLS-1$
	}
}
