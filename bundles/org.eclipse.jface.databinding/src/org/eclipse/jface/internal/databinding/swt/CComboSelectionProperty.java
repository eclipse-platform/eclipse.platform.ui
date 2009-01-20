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
public class CComboSelectionProperty extends WidgetStringValueProperty {
	/**
	 * 
	 */
	public CComboSelectionProperty() {
		super(SWT.Modify);
	}

	String doGetStringValue(Object source) {
		return ((CCombo) source).getText();
	}

	void doSetStringValue(Object source, String value) {
		CCombo ccombo = (CCombo) source;
		String items[] = ccombo.getItems();
		int index = -1;
		if (value == null) {
			ccombo.select(-1);
		} else if (items != null) {
			for (int i = 0; i < items.length; i++) {
				if (value.equals(items[i])) {
					index = i;
					break;
				}
			}
			if (index == -1) {
				ccombo.setText(value);
			} else {
				ccombo.select(index); // -1 will not "unselect"
			}
		}
	}

	public String toString() {
		return "CCombo.selection <String>"; //$NON-NLS-1$
	}
}
