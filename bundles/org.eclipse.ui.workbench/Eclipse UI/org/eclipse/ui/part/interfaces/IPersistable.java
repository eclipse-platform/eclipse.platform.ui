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
package org.eclipse.ui.part.interfaces;

import org.eclipse.ui.IMemento;

/**
 * Objects implementing this interface are capable of saving their
 * state in an IMemento. 
 * <p>
 * Note that parts that implement this interface will typically take 
 * an ISavedState in their constructor which supplies their initial state.
 * </p>
 * 
 * @since 3.1
 */
public interface IPersistable {
    /**
     * Saves the state of the object in the given memento.
     *
     * @param memento the storage area for object's state
     */
    public void saveState(IMemento memento);
}
