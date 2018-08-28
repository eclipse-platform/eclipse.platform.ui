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

import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.List;

/**
 * @since 3.3
 *
 */
public class WidgetItemsProperty extends WidgetDelegatingListProperty {
	private IListProperty cCombo;
	private IListProperty combo;
	private IListProperty list;

	/**
	 *
	 */
	public WidgetItemsProperty() {
		super(String.class);
	}

	@Override
	protected IListProperty doGetDelegate(Object source) {
		if (source instanceof CCombo) {
			if (cCombo == null)
				cCombo = new CComboItemsProperty();
			return cCombo;
		}
		if (source instanceof Combo) {
			if (combo == null)
				combo = new ComboItemsProperty();
			return combo;
		}
		if (source instanceof List) {
			if (list == null)
				list = new ListItemsProperty();
			return list;
		}
		throw notSupported(source);
	}
}