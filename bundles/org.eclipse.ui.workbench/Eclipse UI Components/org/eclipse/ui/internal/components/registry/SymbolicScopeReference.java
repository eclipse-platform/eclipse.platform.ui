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
package org.eclipse.ui.internal.components.registry;

/**
 * @since 3.1
 */
public class SymbolicScopeReference {
    
    /**
     * @param scopeId
     * @param delegation
     */
    public SymbolicScopeReference(String scopeId, int relationship) {
        super();
        this.scopeId = scopeId;
        this.relationship = relationship;
    }
    
    public String scopeId;
    public int relationship;
}
