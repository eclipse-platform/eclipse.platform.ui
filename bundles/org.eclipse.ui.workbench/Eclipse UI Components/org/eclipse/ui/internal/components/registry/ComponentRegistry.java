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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.internal.components.framework.ClassIdentifier;
import org.eclipse.ui.internal.components.framework.ComponentFactory;

/**
 * @since 3.1
 */
public class ComponentRegistry {
    private Map scopes = new HashMap();
    
    public ComponentScope getScope(String scopeId) {
        return (ComponentScope) scopes.get(scopeId);
    }
    
    public void loadScope(String scopeId, ScopeDefinition def) {
        ComponentScope scope = createScope(scopeId);
        
        scope.load(def, this);
    }
    
    public void unloadScope(String scopeId) {
        ComponentScope scope = (ComponentScope)scopes.get(scopeId);
        
        if (scope != null) {
            scope.unload(this);
            if (scope.isRedundant()) {
                scopes.remove(scopeId);
            }
        }
    }
    
//    public void addModifier(String scopeId, IComponentType componentType, IComponentType protocol, IModifierFactory factory) {
//    	ComponentScope scope = createScope(scopeId);
//    	
//    	scope.putModifier(componentType, protocol, factory);
//    }
//    
//    public void removeModifier(String scopeId, IComponentType componentType, IComponentType protocol) {
//    	ComponentScope scope = getScope(scopeId);
//    	
//    	if (scope == null) {
//    		return;
//    	}
//    	
//    	scope.removeModifier(componentType, protocol);
//    	if (scope.isRedundant()) {
//    		scopes.remove(scopeId);
//    	}
//    }
    
    public void addType(String scopeId, ClassIdentifier type, ComponentFactory factory) {
        ComponentScope scope = createScope(scopeId);
        
        scope.put(type, factory);
    }
    
    public void removeType(String scopeId, ClassIdentifier type) {
        ComponentScope scope = (ComponentScope)scopes.get(scopeId);
        
        if (scope != null) {
            scope.remove(type);
            if (scope.isRedundant()) {
                scopes.remove(scopeId);
            }
        }        
    }
    
    public ComponentScope linkSubScope(String parentScope, ComponentScope child, int relationship) {
        ComponentScope scope = createScope(parentScope);
        
        scope.addChild(child, relationship);
        
        return scope;
    }
    
    public void unlinkSubScope(String parentScope, ComponentScope child) {
        ComponentScope scope = getScope(parentScope);
        if (scope != null) {
            scope.removeChild(child);
            if (scope.isRedundant()) {
                scopes.remove(parentScope);
            }
        }
    }
    
    private ComponentScope createScope(String scopeId) {
        ComponentScope result = getScope(scopeId);
        if (result == null) {
            result = new ComponentScope(scopeId);
            scopes.put(scopeId, result);
        }
        
        return result;
    }
    
}
