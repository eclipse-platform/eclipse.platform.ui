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
 * An <code>ComponentFactory</code> is used to construct component handles. Most factories
 * will construct a new component instance each time they are asked for a handle, however
 * it is also possible for factories to return handles that point to a shared singleton or reference-counted
 * instance.
 * 
 * <p>
 * A "component" is defined here to be "an object created by a ComponentFactory". This
 * does not refer to a special kind of object, but rather an object created in a specific
 * way.</p>
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public abstract class ComponentFactory {
    /**
     * Returns a new component handle, given an <code>IServiceProvider</code>
     * that will contain all of the component's dependencies. If a required
     * dependency is missing, it will throw a ComponentException.
     * The caller is responsible for disposing the returned service by calling 
     * handle.dispose() when they are done with it.
     * 
     * @param availableServices the IContainer that holds all services available
     * to the newly constructed component (not null).
     * @return the newly created component (not null)
     * @throws ComponentException if unable to instatiate the component
     */
    public abstract ComponentHandle createHandle(IServiceProvider availableServices) throws ComponentException;

}
