/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 264286)
 *     Matthew Hall - bug 169876
 *******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;

/**
 * @since 3.3
 * 
 */
public final class WidgetSelectionProperty extends
		WidgetDelegatingValueProperty {
	private IValueProperty button;
	private IValueProperty cCombo;
	private IValueProperty combo;
	private IValueProperty dateTime;
	private IValueProperty list;
	private IValueProperty scale;
	private IValueProperty spinner;

	protected IValueProperty doGetDelegate(Object source) {
		if (source instanceof Button) {
			if (button == null)
				button = new ButtonSelectionProperty();
			return button;
		}
		if (source instanceof CCombo) {
			if (cCombo == null)
				cCombo = new CComboSelectionProperty();
			return cCombo;
		}
		if (source instanceof Combo) {
			if (combo == null)
				combo = new ComboSelectionProperty();
			return combo;
		}
		if (source instanceof DateTime) {
			if (dateTime == null)
				dateTime = new DateTimeSelectionProperty();
			return dateTime;
		}
		if (source instanceof List) {
			if (list == null)
				list = new ListSelectionProperty();
			return list;
		}
		if (source instanceof Scale) {
			if (scale == null)
				scale = new ScaleSelectionProperty();
			return scale;
		}
		if (source instanceof Spinner) {
			if (spinner == null)
				spinner = new SpinnerSelectionProperty();
			return spinner;
		}
		throw notSupported(source);
	}
}