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
package org.eclipse.ui.internal.components.registry;

import org.eclipse.ui.components.ClassIdentifier;


/**
 * Returns information about a scope registered with the
 * org.eclipse.core.components.services extension point.
 * 
 * Not intended to be implemented by clients.
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public interface IComponentScope {
    
    /**
     * Returns identifiers for the set of Class keys that can be obtained 
     * from this scope. Does not include classes inherited from parent
     * scopes. 
     *
     * @return the set of interface keys that can be obtained from this scope.
     */
	public ClassIdentifier[] getTypes();
    
    /**
     * Returns the ID for this scope.
     *
     * @return the ID for this scope
     */
	public String getScopeId();
    
    /**
     * Returns the set of known parent scopes. This includes both required
     * and extended scopes. If the scope is required, then components in this
     * scope depend on the services in the parent scope but this scope does
     * not include the parent scope. If this scope extends the parent scope,
     * then all of the services in the parent scope are also available through
     * this scope.
     *
     * @return the set of all scopes referenced by this scope
     */
    public IScopeReference[] getParentScopes();
    
    /**
     * Returns all scopes that reference this scope. This includes scopes
     * that extend this scope and scopes that depend on this scope. 
     *
     * @return the set of all scopes that reference this scope
     */
    public IScopeReference[] getChildScopes();
    
    /**
     * Returns the set of class dependencies for this scope. This does not
     * include dependencies inherited from other scopes or dependencies on
     * other scopes. 
     *
     * @return the set of class dependencies for this scope
     */
    public ClassIdentifier[] getDependencies();
}
