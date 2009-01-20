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
import org.eclipse.swt.widgets.List;

/**
 * @since 3.3
 * 
 */
public class ListSelectionProperty extends WidgetStringValueProperty {
	/**
	 * 
	 */
	public ListSelectionProperty() {
		super(SWT.Selection);
	}

	String doGetStringValue(Object source) {
		List list = (List) source;
		int index = list.getSelectionIndex();
		if (index >= 0)
			return list.getItem(index);
		return null;
	}

	void doSetStringValue(Object source, String value) {
		List list = (List) source;
		String items[] = list.getItems();
		int index = -1;
		if (items != null && value != null) {
			for (int i = 0; i < items.length; i++) {
				if (value.equals(items[i])) {
					index = i;
					break;
				}
			}
			list.select(index);
		}
	}

	public String toString() {
		return "List.selection <String>"; //$NON-NLS-1$
	}
}
