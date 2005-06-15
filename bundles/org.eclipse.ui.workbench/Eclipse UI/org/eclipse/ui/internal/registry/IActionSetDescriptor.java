/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * A named set of actions which is defined as an extension to the workbench
 * via the standard workbench actions extension point
 * (<code>"org.eclipse.ui.workbenchActions"</code>). 
 * <p>
 * [Issue: This interface is not exposed in API, but time may
 * demonstrate that it should be.  For the short term leave it be.
 * In the long term its use should be re-evaluated. ]
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
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
    public IActionSet createActionSet() throws CoreException;

    /**
     * Returns the description of this action set.
     * This is the value of its <code>"description"</code> attribute.
     *
     * @return the description
     */
    public String getDescription();

    /**
     * Returns the id of this action set. 
     * This is the value of its <code>"id"</code> attribute.
     *
     * @return the action set id
     */
    public String getId();

    /**
     * Returns the label of this action set. 
     * This is the value of its <code>"label"</code> attribute.
     *
     * @return the label
     */
    public String getLabel();

    /**
     * Returns whether this action set is initially visible.
     * 
     * @return whether this action set is initially visible
     */
    public boolean isInitiallyVisible();

    /**
     * Sets whether this action set is initially visible.
     * 
     * @param visible whether the action set should be visible initially.
     * @since 3.0
     */
    public void setInitiallyVisible(boolean visible);
    
    /**
     * Returns the conconfigurationfig element.
     * 
     * @return the configuration element
     * @since 3.1
     */
    public IConfigurationElement getConfigurationElement();
}
