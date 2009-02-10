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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;

/**
 * @since 3.3
 * 
 */
public final class WidgetSelectionProperty extends
		WidgetDelegatingValueProperty {
	private IValueProperty button = new ButtonSelectionProperty();
	private IValueProperty cCombo = new CComboSelectionProperty();
	private IValueProperty combo = new ComboSelectionProperty();
	private IValueProperty list = new ListSelectionProperty();
	private IValueProperty scale = new ScaleSelectionProperty();
	private IValueProperty spinner = new SpinnerSelectionProperty();

	protected IValueProperty doGetDelegate(Object source) {
		if (source instanceof Button)
			return button;
		if (source instanceof CCombo)
			return cCombo;
		if (source instanceof Combo)
			return combo;
		if (source instanceof List)
			return list;
		if (source instanceof Scale)
			return scale;
		if (source instanceof Spinner)
			return spinner;
		throw notSupported(source);
	}
}