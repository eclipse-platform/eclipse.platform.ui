/*******************************************************************************
 * Copyright (c) 2009, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.swt.widgets.Widget;

/**
 * @param <S> type of the source object
 *
 * @since 3.3
 *
 */
public final class WidgetSingleSelectionIndexProperty<S extends Widget>
		extends WidgetDelegatingValueProperty<S, Integer> {
	private IValueProperty<S, Integer> cCombo;
	private IValueProperty<S, Integer> combo;
	private IValueProperty<S, Integer> list;
	private IValueProperty<S, Integer> table;

	/**
	 *
	 */
	public WidgetSingleSelectionIndexProperty() {
		super(Integer.TYPE);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IValueProperty<S, Integer> doGetDelegate(S source) {
		if (source instanceof CCombo) {
			if (cCombo == null)
				cCombo = (IValueProperty<S, Integer>) new CComboSingleSelectionIndexProperty();
			return cCombo;
		}
		if (source instanceof Combo) {
			if (combo == null)
				combo = (IValueProperty<S, Integer>) new ComboSingleSelectionIndexProperty();
			return combo;
		}
		if (source instanceof List) {
			if (list == null)
				list = (IValueProperty<S, Integer>) new ListSingleSelectionIndexProperty();
			return list;
		}
		if (source instanceof Table) {
			if (table == null)
				table = (IValueProperty<S, Integer>) new TableSingleSelectionIndexProperty();
			return table;
		}
		throw notSupported(source);
	}
}