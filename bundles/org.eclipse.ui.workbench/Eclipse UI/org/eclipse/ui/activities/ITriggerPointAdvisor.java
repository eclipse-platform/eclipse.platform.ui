/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.activities;

import java.util.Set;

/**
 * The trigger point advisor is a mechanism provided by the workbench that is
 * consulted whenever code that is considered a trigger point is hit. It is the
 * role of the advisor to determine what, if any, activities should be enabled
 * as a consequence of this action. The advisor also has the option of vetoing
 * the operation.
 * 
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.1
 * @see org.eclipse.ui.activities.ITriggerPoint
 */
public interface ITriggerPointAdvisor {

    /**
     * Answer whether the activities bound to the identifier should be enabled
     * when triggered by the provided trigger point.
     * 
     * @param triggerPoint
     *            the trigger point to test
     * @param identifier
     *            the identifier to test against the trigger point
     * @return the set of activities to enable. If the set is not
     *         <code>null</code> the caller may proceed with their usage of
     *         the object represented by the identifier. If <code>null</code>
     *         the caller should abort the action.
     */
    Set allow(ITriggerPoint triggerPoint, IIdentifier identifier);
}
