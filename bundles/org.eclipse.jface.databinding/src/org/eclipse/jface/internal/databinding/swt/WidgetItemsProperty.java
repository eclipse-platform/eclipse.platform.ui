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

import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.List;

/**
 * @since 3.3
 * 
 */
public class WidgetItemsProperty extends WidgetDelegatingListProperty {
	private IListProperty cCombo = new CComboItemsProperty();
	private IListProperty combo = new ComboItemsProperty();
	private IListProperty list = new ListItemsProperty();

	/**
	 * 
	 */
	public WidgetItemsProperty() {
		super(String.class);
	}

	protected IListProperty doGetDelegate(Object source) {
		if (source instanceof CCombo)
			return cCombo;
		if (source instanceof Combo)
			return combo;
		if (source instanceof List)
			return list;
		throw notSupported(source);
	}
}