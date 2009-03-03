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
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * @since 3.3
 * 
 */
public class WidgetTooltipTextProperty extends WidgetDelegatingValueProperty {
	private IValueProperty cTabItem;
	private IValueProperty control;
	private IValueProperty tabItem;
	private IValueProperty tableColumn;
	private IValueProperty toolItem;
	private IValueProperty trayItem;
	private IValueProperty treeColumn;

	/**
	 * 
	 */
	public WidgetTooltipTextProperty() {
		super(String.class);
	}

	protected IValueProperty doGetDelegate(Object source) {
		if (source instanceof CTabItem) {
			if (cTabItem == null)
				cTabItem = new CTabItemTooltipTextProperty();
			return cTabItem;
		}
		if (source instanceof Control) {
			if (control == null)
				control = new ControlTooltipTextProperty();
			return control;
		}
		if (source instanceof TabItem) {
			if (tabItem == null)
				tabItem = new TabItemTooltipTextProperty();
			return tabItem;
		}
		if (source instanceof TableColumn) {
			if (tableColumn == null)
				tableColumn = new TableColumnTooltipTextProperty();
			return tableColumn;
		}
		if (source instanceof ToolItem) {
			if (toolItem == null)
				toolItem = new ToolItemTooltipTextProperty();
			return toolItem;
		}
		if (source instanceof TrayItem) {
			if (trayItem == null)
				trayItem = new TrayItemTooltipTextProperty();
			return trayItem;
		}
		if (source instanceof TreeColumn) {
			if (treeColumn == null)
				treeColumn = new TreeColumnTooltipTextProperty();
			return treeColumn;
		}
		throw notSupported(source);
	}
}