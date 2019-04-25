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
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;

/**
 * @param <S> type of the source object
 *
 * @since 3.3
 *
 */
public class WidgetTooltipTextProperty<S extends Widget> extends WidgetDelegatingValueProperty<S, String> {
	private IValueProperty<S, String> cTabItem;
	private IValueProperty<S, String> control;
	private IValueProperty<S, String> tabItem;
	private IValueProperty<S, String> tableColumn;
	private IValueProperty<S, String> toolItem;
	private IValueProperty<S, String> trayItem;
	private IValueProperty<S, String> treeColumn;

	/**
	 *
	 */
	public WidgetTooltipTextProperty() {
		super(String.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IValueProperty<S, String> doGetDelegate(S source) {
		if (source instanceof CTabItem) {
			if (cTabItem == null)
				cTabItem = (IValueProperty<S, String>) new CTabItemTooltipTextProperty();
			return cTabItem;
		}
		if (source instanceof Control) {
			if (control == null)
				control = (IValueProperty<S, String>) new ControlTooltipTextProperty();
			return control;
		}
		if (source instanceof TabItem) {
			if (tabItem == null)
				tabItem = (IValueProperty<S, String>) new TabItemTooltipTextProperty();
			return tabItem;
		}
		if (source instanceof TableColumn) {
			if (tableColumn == null)
				tableColumn = (IValueProperty<S, String>) new TableColumnTooltipTextProperty();
			return tableColumn;
		}
		if (source instanceof ToolItem) {
			if (toolItem == null)
				toolItem = (IValueProperty<S, String>) new ToolItemTooltipTextProperty();
			return toolItem;
		}
		if (source instanceof TrayItem) {
			if (trayItem == null)
				trayItem = (IValueProperty<S, String>) new TrayItemTooltipTextProperty();
			return trayItem;
		}
		if (source instanceof TreeColumn) {
			if (treeColumn == null)
				treeColumn = (IValueProperty<S, String>) new TreeColumnTooltipTextProperty();
			return treeColumn;
		}
		throw notSupported(source);
	}
}