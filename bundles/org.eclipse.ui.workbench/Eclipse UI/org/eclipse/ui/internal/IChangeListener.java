/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

/**
 * Interface that can receive change notifiecations
 * from a Model object
 */
public interface IChangeListener {
    /**
     * Called with false when the listener is first
     * attached to the model, and called with true
     * every time the model's state changes.
     */
    void update(boolean changed);
}