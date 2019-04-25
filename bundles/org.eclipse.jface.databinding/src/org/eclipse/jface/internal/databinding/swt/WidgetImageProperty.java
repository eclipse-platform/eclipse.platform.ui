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
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

/**
 * @param <S> type of the source object
 *
 * @since 3.3
 *
 */
public class WidgetImageProperty<S extends Widget> extends WidgetDelegatingValueProperty<S, Image> {
	private IValueProperty<S, Image> button;
	private IValueProperty<S, Image> cLabel;
	private IValueProperty<S, Image> item;
	private IValueProperty<S, Image> label;

	/**
	 *
	 */
	public WidgetImageProperty() {
		super(Image.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IValueProperty<S, Image> doGetDelegate(S source) {
		if (source instanceof Button) {
			if (button == null)
				button = (IValueProperty<S, Image>) new ButtonImageProperty();
			return button;
		}
		if (source instanceof CLabel) {
			if (cLabel == null)
				cLabel = (IValueProperty<S, Image>) new CLabelImageProperty();
			return cLabel;
		}
		if (source instanceof Item) {
			if (item == null)
				item = (IValueProperty<S, Image>) new ItemImageProperty();
			return item;
		}
		if (source instanceof Label) {
			if (label == null)
				label = (IValueProperty<S, Image>) new LabelImageProperty();
			return label;
		}
		throw notSupported(source);
	}
}