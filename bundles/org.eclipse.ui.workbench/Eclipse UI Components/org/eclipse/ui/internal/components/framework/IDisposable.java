/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.components.framework;


/**
 * Interface for components that need to perform cleanup when they are destroyed.
 * This is an optional interface. Even if the component does not implement 
 * <code>IDisposable</code> it is still possible to clean up after the object by 
 * wrapping it in a custom subclass of AbstractComponentHandle that performs
 * any necessary cleanup.
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public interface IDisposable {
    /**
     * This method is called when the receiver is no longer needed. 
     * It should clean up any resources allocated by the receiver. 
     * Must only be called after everything that depends on the receiver
     * has been disposed.
     */
    public void dispose();
}
