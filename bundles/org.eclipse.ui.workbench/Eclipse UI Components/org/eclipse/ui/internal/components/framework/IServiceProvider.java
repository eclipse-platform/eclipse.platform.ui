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
 * An <code>IServiceProvider</code> returns services given identifying keys. 
 * Most service providers will lazily each service the first time it is requested,
 * and will return the same instance thereafter. Component factories will typically use a 
 * service provider to locate the dependencies for a component when it is first
 * created, and the component itself will usually not need to use the service provider
 * once it is constructed.
 * 
 * <p>
 * Not intended to be implemented by clients.
 * </p>
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public interface IServiceProvider {
    /**
     * Returns a component for the given key. Returns null if the given key is not known.
     * Components returned by this method are managed by the component provider. The
     * caller does not need to dispose it when done.  
     *
     * @param key identifier for the service
     * @return a component that is assignable to the given type, such that 
     *         toQuery.isAssignable(container.getComponent(toQuery)).
     * @throws ComponentException if unable to create the component
     */
    Object getService(Object key) throws ComponentException;

    /**
     * Returns true iff this component provider recognises the given key. If this method
     * returns true, then getComponent(...) will not return null when given the same
     * key.
     * 
     * @param key identifier for the service
     * @return true iff this provider contains the given service
     */
    boolean hasService(Object key);
}
