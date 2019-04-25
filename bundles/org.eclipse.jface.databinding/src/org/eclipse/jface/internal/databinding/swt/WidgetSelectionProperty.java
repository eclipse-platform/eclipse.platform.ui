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
 *     Matthew Hall - bug 169876
 *******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Widget;

/**
 * @param <S> type of the source object
 * @param <T> type of the value of the property
 *
 * @since 3.3
 *
 */
public final class WidgetSelectionProperty<S extends Widget, T> extends WidgetDelegatingValueProperty<S, T> {
	private IValueProperty<S, T> button;
	private IValueProperty<S, T> cCombo;
	private IValueProperty<S, T> combo;
	private IValueProperty<S, T> dateTime;
	private IValueProperty<S, T> list;
	private IValueProperty<S, T> menuItem;
	private IValueProperty<S, T> scale;
	private IValueProperty<S, T> slider;
	private IValueProperty<S, T> spinner;

	@SuppressWarnings("unchecked")
	@Override
	protected IValueProperty<S, T> doGetDelegate(S source) {
		if (source instanceof Button) {
			if (button == null)
				button = (IValueProperty<S, T>) new ButtonSelectionProperty();
			return button;
		}
		if (source instanceof CCombo) {
			if (cCombo == null)
				cCombo = (IValueProperty<S, T>) new CComboSelectionProperty();
			return cCombo;
		}
		if (source instanceof Combo) {
			if (combo == null)
				combo = (IValueProperty<S, T>) new ComboSelectionProperty();
			return combo;
		}
		if (source instanceof DateTime) {
			if (dateTime == null)
				dateTime = (IValueProperty<S, T>) new DateTimeSelectionProperty();
			return dateTime;
		}
		if (source instanceof List) {
			if (list == null)
				list = (IValueProperty<S, T>) new ListSelectionProperty();
			return list;
		}
		if (source instanceof MenuItem) {
			if (menuItem == null)
				menuItem = (IValueProperty<S, T>) new MenuItemSelectionProperty();
			return menuItem;
		}
		if (source instanceof Scale) {
			if (scale == null)
				scale = (IValueProperty<S, T>) new ScaleSelectionProperty();
			return scale;
		}
		if (source instanceof Slider) {
			if (slider == null)
				slider = (IValueProperty<S, T>) new SliderSelectionProperty();
			return slider;
		}
		if (source instanceof Spinner) {
			if (spinner == null)
				spinner = (IValueProperty<S, T>) new SpinnerSelectionProperty();
			return spinner;
		}
		throw notSupported(source);
	}
}