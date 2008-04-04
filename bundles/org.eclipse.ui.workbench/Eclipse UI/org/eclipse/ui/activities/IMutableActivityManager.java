/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
 * An instance of this interface allows clients to manage activities, as
 * defined by the extension point <code>org.eclipse.ui.activities</code>.
 * <p>
 * This interface extends <code>IActivityManager</code> by granting the ability 
 * to alter the set of currently enabled activities.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMutableActivityManager extends IActivityManager {

    /**
     * Sets the set of identifiers to enabled activities.
     * 
     * @param enabledActivityIds
     *            the set of identifiers to enabled activities. This set may be
     *            empty, but it must not be <code>null</code>. If this set
     *            is not empty, it must only contain instances of <code>String</code>.
     */
    void setEnabledActivityIds(Set enabledActivityIds);
}
