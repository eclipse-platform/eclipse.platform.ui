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

/**
 * Used to represent a relationship between two scopes.
 * 
 * Not intended to be implemented by clients.
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public interface IScopeReference {
    
    /**
     * Relationship constant indicating that the child scope extends the parent
     * scope. In this case, all of the services in the parent scope
     * are also visible in the child scope.
     */
    public static final int REL_EXTENDS = 0;
    
    /**
     * Relationship constant indicating that the child scope requires the parent
     * scope. In this case all of the services in the parent scope
     * are required by the child scope, but the child does not automatically
     * provide those services. 
     */
    public static final int REL_REQUIRES = 0;
    
    /**
     * Returns one of the REL_* constants, above. This indicates the nature
     * of the relationship between the two scopes.
     * 
     * @return one of the REL_* constants, above.
     */
    public int getRelationship();
    
    /**
     * Returns the scope being extended
     *
     * @return the target scope
     */
    public IComponentScope getTarget();
}
