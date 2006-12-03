/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @since 3.3
 * 
 */
public class MenuData extends ServiceData {

	private List items = new ArrayList();

	private final ImageDescriptor icon;

	private final String label;

	private final String tooltip;

	/**
	 * @param id
	 *            The id of the menu. Must not be <code>null</code>.
	 * @param icon
	 *            an icon for this menu. May be <code>null</code>.
	 * @param label
	 *            The label for this menu. Must not be <code>null</code>.
	 * @param tooltip
	 *            The tooltip for this menu. Usually <code>null</code>.
	 * @param visibleWhen
	 *            The visible when expression for this menu. May be
	 *            <code>null</code>.
	 */
	public MenuData(String id, ImageDescriptor icon, String label,
			String tooltip, Expression visibleWhen) {
		super(id, visibleWhen);
		this.icon = icon;
		this.label = label;
		this.tooltip = tooltip;
	}

	/**
	 * Add items to this menu
	 * 
	 * @param item
	 *            another MenuData or ItemData
	 */
	public void add(ServiceData item) {
		items.add(item);
	}

	/**
	 * @return the list of items contained by this menu.
	 */
	public List getItems() {
		return items;
	}

	/**
	 * @return Returns the icon.
	 */
	public ImageDescriptor getIcon() {
		return icon;
	}

	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return Returns the tooltip.
	 */
	public String getTooltip() {
		return tooltip;
	}
}
