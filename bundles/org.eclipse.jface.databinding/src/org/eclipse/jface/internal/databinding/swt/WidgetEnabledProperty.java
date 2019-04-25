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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

/**
 * @param <S> type of the source object
 */
public class WidgetEnabledProperty<S extends Widget> extends WidgetDelegatingValueProperty<S, Boolean> {
	IValueProperty<Control, Boolean> control;
	IValueProperty<Menu, Boolean> menu;
	IValueProperty<MenuItem, Boolean> menuItem;
	IValueProperty<ScrollBar, Boolean> scrollBar;
	IValueProperty<ToolItem, Boolean> toolItem;

	/**
	 *
	 */
	public WidgetEnabledProperty() {
		super(Boolean.TYPE);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IValueProperty<S, Boolean> doGetDelegate(S source) {
		if (source instanceof Control) {
			if (control == null)
				control = new ControlEnabledProperty();
			return (IValueProperty<S, Boolean>) control;
		}
		if (source instanceof Menu) {
			if (menu == null)
				menu = new MenuEnabledProperty();
			return (IValueProperty<S, Boolean>) menu;
		}
		if (source instanceof MenuItem) {
			if (menuItem == null)
				menuItem = new MenuItemEnabledProperty();
			return (IValueProperty<S, Boolean>) menuItem;
		}
		if (source instanceof ScrollBar) {
			if (scrollBar == null)
				scrollBar = new ScrollBarEnabledProperty();
			return (IValueProperty<S, Boolean>) scrollBar;
		}
		if (source instanceof ToolItem) {
			if (toolItem == null)
				toolItem = new ToolItemEnabledProperty();
			return (IValueProperty<S, Boolean>) toolItem;
		}
		throw notSupported(source);
	}
}
