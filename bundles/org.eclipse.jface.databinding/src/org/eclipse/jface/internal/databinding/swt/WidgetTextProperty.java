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
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
	private IValueProperty button = new ButtonTextProperty();
	private IValueProperty cCombo = new CComboTextProperty();
	private IValueProperty cLabel = new CLabelTextProperty();
	private IValueProperty combo = new ComboTextProperty();
	private IValueProperty item = new ItemTextProperty();
	private IValueProperty label = new LabelTextProperty();
	private IValueProperty link = new LinkTextProperty();
	private IValueProperty shell = new ShellTextProperty();
	private IValueProperty styledText = new StyledTextTextProperty();
	private IValueProperty text = new TextTextProperty();

	/**
	 * 
	 */
	public WidgetTextProperty() {
		super(String.class);
	}

	protected IValueProperty doGetDelegate(Object source) {
		if (source instanceof Button)
			return button;
		if (source instanceof CCombo)
			return cCombo;
		if (source instanceof CLabel)
			return cLabel;
		if (source instanceof Combo)
			return combo;
		if (source instanceof Item)
			return item;
		if (source instanceof Label)
			return label;
		if (source instanceof Link)
			return link;
		if (source instanceof Shell)
			return shell;
		if (source instanceof StyledText)
			return styledText;
		if (source instanceof Text)
			return text;
		throw notSupported(source);
	}
}