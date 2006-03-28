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
package org.eclipse.jface.text;

/**
 * Interface for text related objects which may be used in the multi-threaded
 * context and thus must provide a way to prevent concurrent access and
 * manipulation.
 * <p>
 * In order to reduce the probability of dead locks clients should synchronize
 * their access to these objects by using the provided lock object rather than
 * the object itself.</p>
 * <p>
 * Managing objects can use the <code>setLockObject</code> method in order to
 * synchronize whole sets of objects.</p>
 *
 * @since 3.0
 */
public interface ISynchronizable {

    /**
	 * Sets the lock object for this object. If the lock object is not
	 * <code>null</code> subsequent calls to specified methods of this object
	 * are synchronized on this lock object. Which methods are synchronized is
	 * specified by the implementer.
	 * <p>
	 * <em>You should not override an existing lock object unless you own
	 * that lock object yourself. Use the existing lock object instead.</em>
	 * </p>
	 *
	 * @param lockObject the lock object. May be <code>null</code>.
	 */
    void setLockObject(Object lockObject);

    /**
	 * Returns the lock object or <code>null</code> if there is none. Clients
	 * should use the lock object in order to synchronize concurrent access to
	 * the implementer.
	 *
	 * @return the lock object or <code>null</code>
	 */
    Object getLockObject();
}
