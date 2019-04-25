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
 *     Eugen Neufeld - bug 461560
 *******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * @param <S> type of the source object
 *
 * @since 3.3
 *
 */
public class WidgetTextProperty<S extends Widget> extends WidgetDelegatingValueProperty<S, String> {
	private IValueProperty<S, String> button;
	private IValueProperty<S, String> cCombo;
	private IValueProperty<S, String> cLabel;
	private IValueProperty<S, String> combo;
	private IValueProperty<S, String> item;
	private IValueProperty<S, String> label;
	private IValueProperty<S, String> link;
	private IValueProperty<S, String> shell;
	private IValueProperty<S, String> styledText;
	private IValueProperty<S, String> text;
	private IValueProperty<S, String> group;

	/**
	 *
	 */
	public WidgetTextProperty() {
		super(String.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IValueProperty<S, String> doGetDelegate(S source) {
		if (source instanceof Button) {
			if (button == null)
				button = (IValueProperty<S, String>) new ButtonTextProperty();
			return button;
		}
		if (source instanceof CCombo) {
			if (cCombo == null)
				cCombo = (IValueProperty<S, String>) new CComboTextProperty();
			return cCombo;
		}
		if (source instanceof CLabel) {
			if (cLabel == null)
				cLabel = (IValueProperty<S, String>) new CLabelTextProperty();
			return cLabel;
		}
		if (source instanceof Combo) {
			if (combo == null)
				combo = (IValueProperty<S, String>) new ComboTextProperty();
			return combo;
		}
		if (source instanceof Item) {
			if (item == null)
				item = (IValueProperty<S, String>) new ItemTextProperty();
			return item;
		}
		if (source instanceof Label) {
			if (label == null)
				label = (IValueProperty<S, String>) new LabelTextProperty();
			return label;
		}
		if (source instanceof Link) {
			if (link == null)
				link = (IValueProperty<S, String>) new LinkTextProperty();
			return link;
		}
		if (source instanceof Shell) {
			if (shell == null)
				shell = (IValueProperty<S, String>) new ShellTextProperty();
			return shell;
		}
		if (source instanceof StyledText) {
			if (styledText == null)
				styledText = (IValueProperty<S, String>) new StyledTextTextProperty();
			return styledText;
		}
		if (source instanceof Text) {
			if (text == null)
				text = (IValueProperty<S, String>) new TextTextProperty();
			return text;
		}
		if (source instanceof Group) {
			if (group == null)
				group = (IValueProperty<S, String>) new GroupTextProperty();
			return group;
		}
		throw notSupported(source);
	}
}