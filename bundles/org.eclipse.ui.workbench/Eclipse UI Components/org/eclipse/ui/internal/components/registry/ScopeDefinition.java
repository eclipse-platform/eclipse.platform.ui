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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.internal.components.framework.ClassIdentifier;

/**
 * @since 3.1
 */
public class ScopeDefinition {
    List superTypes = new ArrayList();
    List dependencies = new ArrayList();
    String description = ""; //$NON-NLS-1$
    
    public void setDescription(String descr) {
        this.description = descr;
    }
    
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }
    
    public void addExtends(SymbolicScopeReference ref) {
        superTypes.add(ref);
    }
 
    public void addDependency(ClassIdentifier type) {
        dependencies.add(type);
    }
    
    public SymbolicScopeReference[] getExtends() {
        return (SymbolicScopeReference[]) superTypes.toArray(new SymbolicScopeReference[superTypes.size()]);
    }
    
    public ClassIdentifier[] getDependencies() {
        return (ClassIdentifier[]) dependencies.toArray(new ClassIdentifier[dependencies.size()]);
    }
    
}
