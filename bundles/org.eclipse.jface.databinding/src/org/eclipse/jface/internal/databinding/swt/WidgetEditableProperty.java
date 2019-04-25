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
 *     Matthew Hall - bug 306203
 *******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * @param <S> type of the source object
 *
 * @since 3.3
 *
 */
public class WidgetEditableProperty<S extends Control> extends WidgetDelegatingValueProperty<S, Boolean> {
	IValueProperty<Text, Boolean> text;
	IValueProperty<CCombo, Boolean> ccombo;
	IValueProperty<StyledText, Boolean> styledText;

	/**
	 *
	 */
	public WidgetEditableProperty() {
		super(Boolean.TYPE);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IValueProperty<S, Boolean> doGetDelegate(S source) {
		if (source instanceof Text) {
			if (text == null)
				text = new TextEditableProperty();
			return (IValueProperty<S, Boolean>) text;
		}
		if (source instanceof CCombo) {
			if (ccombo == null) {
				ccombo = new CComboEditableProperty();
			}
			return (IValueProperty<S, Boolean>) ccombo;
		}
		if (source instanceof StyledText) {
			if (styledText == null) {
				styledText = new StyledTextEditableProperty();
			}
			return (IValueProperty<S, Boolean>) styledText;
		}
		throw notSupported(source);
	}
}