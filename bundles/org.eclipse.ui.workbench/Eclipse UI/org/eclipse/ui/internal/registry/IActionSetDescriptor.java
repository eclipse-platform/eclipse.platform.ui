/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * A named set of actions which is defined as an extension to the workbench via
 * the standard workbench actions extension point
 * (<code>"org.eclipse.ui.workbenchActions"</code>).
 * <p>
 * [Issue: This interface is not exposed in API, but time may demonstrate that
 * it should be. For the short term leave it be. In the long term its use should
 * be re-evaluated. ]
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see ActionSetRegistry
 */
public interface IActionSetDescriptor {

	/**
	 * Creates a new action set from this descriptor.
	 * <p>
	 * [Issue: Consider throwing WorkbenchException rather than CoreException.]
	 * </p>
	 *
	 * @return the new action set
	 * @exception CoreException if the action set cannot be created
	 */
	IActionSet createActionSet() throws CoreException;

	/**
	 * Returns the description of this action set. This is the value of its
	 * <code>"description"</code> attribute.
	 *
	 * @return the description
	 */
	String getDescription();

	/**
	 * Returns the id of this action set. This is the value of its <code>"id"</code>
	 * attribute.
	 *
	 * @return the action set id
	 */
	String getId();

	/**
	 * Returns the label of this action set. This is the value of its
	 * <code>"label"</code> attribute.
	 *
	 * @return the label
	 */
	String getLabel();

	/**
	 * Returns whether this action set is initially visible.
	 *
	 * @return whether this action set is initially visible
	 */
	boolean isInitiallyVisible();

	/**
	 * Sets whether this action set is initially visible.
	 *
	 * @param visible whether the action set should be visible initially.
	 * @since 3.0
	 */
	void setInitiallyVisible(boolean visible);

	/**
	 * Returns the conconfigurationfig element.
	 *
	 * @return the configuration element
	 * @since 3.1
	 */
	IConfigurationElement getConfigurationElement();
}
