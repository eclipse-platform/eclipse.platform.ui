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

import java.util.Map;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @since 3.3
 * 
 */
public class ItemData extends ServiceData {

	private final String commandId;

	private final Map parameters;

	private final ImageDescriptor icon;

	private final String label;

	private final String tooltip;

	/**
	 * @param id
	 *            The id for this item. May be <code>null</code>.
	 * @param commandId
	 *            The commandId. Must not be <code>null</code>.
	 * @param parameters
	 *            A map of strings to strings. Parameters names to values. This
	 *            may be <code>null</code>.
	 * @param icon
	 *            An icon for this item. May be <code>null</code>.
	 * @param label
	 *            A label for this item. May be <code>null</code>.
	 * @param tooltip
	 *            A tooltip for this item. May be <code>null</code>.
	 * @param visibleWhen
	 *            The visible when expression for this item. May be
	 *            <code>null</code>
	 */
	public ItemData(String id, String commandId, Map parameters,
			ImageDescriptor icon, String label, String tooltip,
			Expression visibleWhen) {
		super(id, visibleWhen);
		this.commandId = commandId;
		this.parameters = parameters;
		this.icon = icon;
		this.label = label;
		this.tooltip = tooltip;
	}

	/**
	 * @return Returns the commandId.
	 */
	public String getCommandId() {
		return commandId;
	}

	/**
	 * @return Returns the parameters.
	 */
	public Map getParameters() {
		return parameters;
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
