/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

/**
 * Interface for objects which can be set into a synchronized mode when needed.
 * For that, the lock object must be set.
 * 
 * @since 3.0
 */
public interface ISynchronizable {

    /**
     * Sets the lock object for this object. If the lock object is not <code>null</code>
     * subsequent calls to methods of this object are synchronized on this lock
     * object.
     * 
     * @param lockObject the lock object. May be <code>null</code>.
     */
    void setLockObject(Object lockObject);

    /**
     * Returns the lock object or <code>null</code> if there is none.
     * 
     * @return the lock object or <code>null</code>
     */
    Object getLockObject();
}