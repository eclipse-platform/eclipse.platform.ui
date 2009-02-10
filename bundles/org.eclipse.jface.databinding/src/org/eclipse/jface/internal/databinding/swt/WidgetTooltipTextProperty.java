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
	private IValueProperty cTabItem = new CTabItemTooltipTextProperty();
	private IValueProperty control = new ControlTooltipTextProperty();
	private IValueProperty tabItem = new TabItemTooltipTextProperty();
	private IValueProperty tableColumn = new TableColumnTooltipTextProperty();
	private IValueProperty toolItem = new ToolItemTooltipTextProperty();
	private IValueProperty trayItem = new TrayItemTooltipTextProperty();
	private IValueProperty treeColumn = new TreeColumnTooltipTextProperty();

	/**
	 * 
	 */
	public WidgetTooltipTextProperty() {
		super(String.class);
	}

	protected IValueProperty doGetDelegate(Object source) {
		if (source instanceof CTabItem)
			return cTabItem;
		if (source instanceof Control)
			return control;
		if (source instanceof TabItem)
			return tabItem;
		if (source instanceof TableColumn)
			return tableColumn;
		if (source instanceof ToolItem)
			return toolItem;
		if (source instanceof TrayItem)
			return trayItem;
		if (source instanceof TreeColumn)
			return treeColumn;
		throw notSupported(source);
	}
}