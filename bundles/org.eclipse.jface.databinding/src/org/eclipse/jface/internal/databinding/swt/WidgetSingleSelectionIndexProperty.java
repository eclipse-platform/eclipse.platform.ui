/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 264286)
 *******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;

/**
 * @since 3.3
 * 
 */
public final class WidgetSingleSelectionIndexProperty extends
		WidgetDelegatingValueProperty {
	private IValueProperty cCombo;
	private IValueProperty combo;
	private IValueProperty list;
	private IValueProperty table;

	/**
	 * 
	 */
	public WidgetSingleSelectionIndexProperty() {
		super(Integer.TYPE);
	}

	protected IValueProperty doGetDelegate(Object source) {
		if (source instanceof CCombo) {
			if (cCombo == null)
				cCombo = new CComboSingleSelectionIndexProperty();
			return cCombo;
		}
		if (source instanceof Combo) {
			if (combo == null)
				combo = new ComboSingleSelectionIndexProperty();
			return combo;
		}
		if (source instanceof List) {
			if (list == null)
				list = new ListSingleSelectionIndexProperty();
			return list;
		}
		if (source instanceof Table) {
			if (table == null)
				table = new TableSingleSelectionIndexProperty();
			return table;
		}
		throw notSupported(source);
	}
}