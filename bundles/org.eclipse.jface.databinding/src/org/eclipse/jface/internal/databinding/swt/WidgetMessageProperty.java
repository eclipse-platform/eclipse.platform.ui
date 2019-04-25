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
 *     Matthew Hall - initial API and implementation (bug 266563)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Widget;

/**
 * @param <S> type of the source object
 *
 * @since 3.3
 *
 */
public class WidgetMessageProperty<S extends Widget> extends WidgetDelegatingValueProperty<S, String> {
	private IValueProperty<S, String> text;
	private IValueProperty<S, String> toolTip;

	/**
	 *
	 */
	public WidgetMessageProperty() {
		super(String.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IValueProperty<S, String> doGetDelegate(S source) {
		if (source instanceof Text) {
			if (text == null)
				text = (IValueProperty<S, String>) new TextMessageProperty();
			return text;
		}
		if (source instanceof ToolTip) {
			if (toolTip == null)
				toolTip = (IValueProperty<S, String>) new ToolTipMessageProperty();
			return toolTip;
		}
		throw notSupported(source);
	}
}
