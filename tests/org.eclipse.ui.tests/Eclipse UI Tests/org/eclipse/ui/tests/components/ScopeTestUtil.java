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
package org.eclipse.ui.tests.components;

import junit.framework.TestSuite;

import org.eclipse.core.components.Components;
import org.eclipse.core.components.registry.ClassIdentifier;
import org.eclipse.core.components.registry.IComponentScope;
import org.eclipse.core.components.registry.IScopeReference;

/**
 * @since 3.1
 */
public class ScopeTestUtil {
    
    public static void addTestsFor(TestSuite suite, String context) {
        IComponentScope scope = Components.getScope(context);
        
        addTestsFor(suite, scope, context);
    }
    
    /**
     * Adds instantiation tests for all components in the given scope
     * 
     * @since 3.1 
     *
     * @param scope
     */
    public static void addTestsFor(TestSuite suite, IComponentScope scope, String context) {
        ClassIdentifier[] types = scope.getTypes();
        
        for (int i = 0; i < types.length; i++) {
            ClassIdentifier type = types[i];
            
            addTestFor(suite, type.getNamespace(), type.getTypeName(), context);
        }
        
        IScopeReference[] parents = scope.getParentScopes();
        
        for (int i = 0; i < parents.length; i++) {
            IComponentScope parent = parents[i].getTarget();
            
            addTestsFor(suite, parent, context);
        }
        
    }
    
    public static void addTestFor(TestSuite suite, String namespace, String type, String context) {
        suite.addTest(new ComponentTest(namespace, type, context));
    }
}
