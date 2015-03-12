/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

/**
 * @since 3.3
 *
 */
public class WidgetTextProperty extends WidgetDelegatingValueProperty {
	private IValueProperty button;
	private IValueProperty cCombo;
	private IValueProperty cLabel;
	private IValueProperty combo;
	private IValueProperty item;
	private IValueProperty label;
	private IValueProperty link;
	private IValueProperty shell;
	private IValueProperty styledText;
	private IValueProperty text;
	private IValueProperty group;

	/**
	 *
	 */
	public WidgetTextProperty() {
		super(String.class);
	}

	@Override
	protected IValueProperty doGetDelegate(Object source) {
		if (source instanceof Button) {
			if (button == null)
				button = new ButtonTextProperty();
			return button;
		}
		if (source instanceof CCombo) {
			if (cCombo == null)
				cCombo = new CComboTextProperty();
			return cCombo;
		}
		if (source instanceof CLabel) {
			if (cLabel == null)
				cLabel = new CLabelTextProperty();
			return cLabel;
		}
		if (source instanceof Combo) {
			if (combo == null)
				combo = new ComboTextProperty();
			return combo;
		}
		if (source instanceof Item) {
			if (item == null)
				item = new ItemTextProperty();
			return item;
		}
		if (source instanceof Label) {
			if (label == null)
				label = new LabelTextProperty();
			return label;
		}
		if (source instanceof Link) {
			if (link == null)
				link = new LinkTextProperty();
			return link;
		}
		if (source instanceof Shell) {
			if (shell == null)
				shell = new ShellTextProperty();
			return shell;
		}
		if (source instanceof StyledText) {
			if (styledText == null)
				styledText = new StyledTextTextProperty();
			return styledText;
		}
		if (source instanceof Text) {
			if (text == null)
				text = new TextTextProperty();
			return text;
		}
		if (source instanceof Group) {
			if (group == null)
				group = new GroupTextProperty();
			return group;
		}
		throw notSupported(source);
	}
}