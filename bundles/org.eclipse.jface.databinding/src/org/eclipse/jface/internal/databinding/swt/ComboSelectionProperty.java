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
import org.eclipse.swt.widgets.Combo;

/**
 * @since 3.3
 * 
 */
public class ComboSelectionProperty extends WidgetStringValueProperty {
	/**
	 * 
	 */
	public ComboSelectionProperty() {
		super(SWT.Modify);
	}

	String doGetStringValue(Object source) {
		return ((Combo) source).getText();
	}

	void doSetStringValue(Object source, String value) {
		Combo combo = (Combo) source;
		String items[] = combo.getItems();
		int index = -1;
		if (items != null && value != null) {
			for (int i = 0; i < items.length; i++) {
				if (value.equals(items[i])) {
					index = i;
					break;
				}
			}
			if (index == -1) {
				combo.setText(value);
			} else {
				combo.select(index); // -1 will not "unselect"
			}
		}
	}

	public String toString() {
		return "Combo.selection <String>"; //$NON-NLS-1$
	}
}
