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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;

/**
 * @param <S> type of the source object
 *
 * @since 3.3
 *
 */
public class WidgetMinimumProperty<S extends Control> extends WidgetDelegatingValueProperty<S, Integer> {
	private IValueProperty<Scale, Integer> scale;
	private IValueProperty<Slider, Integer> slider;
	private IValueProperty<Spinner, Integer> spinner;

	/**
	 *
	 */
	public WidgetMinimumProperty() {
		super(Integer.TYPE);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IValueProperty<S, Integer> doGetDelegate(S source) {
		if (source instanceof Scale) {
			if (scale == null)
				scale = new ScaleMinimumProperty();
			return (IValueProperty<S, Integer>) scale;
		}
		if (source instanceof Slider) {
			if (slider == null) {
				slider = new SliderMinimumProperty();
			}
			return (IValueProperty<S, Integer>) slider;
		}
		if (source instanceof Spinner) {
			if (spinner == null)
				spinner = new SpinnerMinimumProperty();
			return (IValueProperty<S, Integer>) spinner;
		}
		throw notSupported(source);
	}
}