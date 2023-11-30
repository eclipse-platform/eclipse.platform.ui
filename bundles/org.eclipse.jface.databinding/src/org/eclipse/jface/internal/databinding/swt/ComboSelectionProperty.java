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
import org.eclipse.swt.widgets.Combo;

/**
 * @since 3.3
 */
public class ComboSelectionProperty extends WidgetStringValueProperty<Combo> {
	public ComboSelectionProperty() {
		super(SWT.Modify);
	}

	@Override
	protected String doGetStringValue(Combo source) {
		return source.getText();
	}

	@Override
	protected void doSetStringValue(Combo source, String value) {
		String items[] = source.getItems();
		int index = -1;
		if (items != null && value != null) {
			for (int i = 0; i < items.length; i++) {
				if (value.equals(items[i])) {
					index = i;
					break;
				}
			}
			if (index == -1) {
				source.setText(value);
			} else {
				source.select(index); // -1 will not "unselect"
			}
		}
	}

	@Override
	public String toString() {
		return "Combo.selection <String>"; //$NON-NLS-1$
	}
}
