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

import java.util.Collection;

/**
 * Factory for services. Services are defined here to be "an object that can be
 * requested by key from an IServiceProvider". That is, a service is any object that
 * can be used to satisfy a component's dependencies. Any sort of java object 
 * may be used as a service key as long as the service factory and the code using it agree on the 
 * meaning of each key. The framework normally uses Class objects as keys, and always 
 * returns services that implement that Class. 
 * 
 * <p>
 * A service factory is different from a service provider: A service factory creates new
 * services whereas a service provider returns existing services.
 * </p>
 * 
 * <p>
 * A service factory will often contain many component factories. The service factory 
 * will typically use the key to select a specific component factory and delegate to 
 * the component factory to create the actual service. 
 * </p>
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public abstract class ServiceFactory {
	/**
	 * Returns a component handle for the given key or null if this context does not 
	 * recognize the given key. The handle will point to a component that is fully initialized
	 * and has all of its dependencies satisfied.
	 * 
	 * <p>
	 * By convention, if the key is a Class instance then the resulting component must be
	 * an instance of that class. 
	 * </p>
	 * 
     * @param key identifier for the service to create. This is typically a Class object for
     *        a class listed as a component interface in the org.eclipse.core.components.services 
     *        extension point
     * @param services available dependencies for the service
     * @return a newly created component handle or null if the key is not known to this factory
     * @throws ComponentException if unable to create the service
	 */
    public abstract ComponentHandle createHandle(Object key, IServiceProvider services) throws ComponentException;
    
    /**
     * Returns true iff the receiver knows how to create handles for the given service key. If
     * this method returns true, createHandle should not return null for the given key in normal
     * circumstances. 
     * 
     * @param componentKey key to check
     * @return true iff this factory knows about the given key
     */
    public abstract boolean hasService(Object componentKey);
    
    /**
     * Returns the set of missing dependencies for this factory. Missing dependencies are keys that may 
     * be requested by services in this factory but which this factory cannot construct directly. Any
     * service provider that is passed to createHandle should be able to satisfy all of the keys in
     * this set.
     *
     * @return a set of service keys that are required by this factory
     */
    public abstract Collection getMissingDependencies();
    
}
