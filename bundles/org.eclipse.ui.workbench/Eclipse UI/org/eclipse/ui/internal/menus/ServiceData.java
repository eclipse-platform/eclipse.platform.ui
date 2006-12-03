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
public class ServiceData {
	private final String id;

	private final String commandId;

	private final Map parameters;

	private final ImageDescriptor icon;

	private final String label;

	private final String tooltip;

	private final Expression visibleWhenExpression;

	/**
	 * @param id
	 * @param commandId
	 * @param parameters
	 * @param icon
	 * @param label
	 * @param tooltip
	 * @param visibleWhen 
	 */
	public ServiceData(String id, String commandId, Map parameters,
			ImageDescriptor icon, String label, String tooltip,
			Expression visibleWhen) {
		this.id = id;
		this.commandId = commandId;
		this.parameters = parameters;
		this.icon = icon;
		this.label = label;
		this.tooltip = tooltip;
		this.visibleWhenExpression = visibleWhen;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
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

	/**
	 * @return Returns the visible.
	 */
	public Expression getVisibleWhen() {
		return visibleWhenExpression;
	}
}
