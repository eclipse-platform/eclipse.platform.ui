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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.components.ComponentUtil;
import org.eclipse.ui.internal.components.framework.ClassIdentifier;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.ComponentFactory;
import org.eclipse.ui.internal.components.framework.ComponentHandle;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.components.framework.NonDisposingHandle;
import org.eclipse.ui.internal.components.framework.ServiceFactory;

/**
 * @since 3.1
 */
public class ComponentScope extends ServiceFactory implements IComponentScope {
    
    private String scopeId;
 
    private ComponentTypeMap types = new ComponentTypeMap();
    private ComponentTypeMap modifiers = new ComponentTypeMap();
    private IScopeReference[] parentScopes = new IScopeReference[0];
    private ClassIdentifier[] dependencies = new ClassIdentifier[0];
    private static final String impossibleToSatisfyDependency = "Dependency that is impossible to satisfy"; //$NON-NLS-1$
    
    private boolean loaded = false;

    private ArrayList children = new ArrayList();
    
    private ServiceFactory[] parentContexts = new ServiceFactory[0];
    
    private ArrayList scopeDependencies = new ArrayList();
    
	public ComponentScope(String scopeId) {
		this.scopeId = scopeId;
	}
    
	public IServiceProvider getContainer(ServiceFactory context) {
	    return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.component.IContainerContext#getFactory(java.lang.Object)
     */
    public ComponentHandle createHandle(Object key, IServiceProvider container) throws ComponentException {
        
        if (key == this) {
            return new NonDisposingHandle(this);
        }
        
        if (key instanceof Class) {
            ComponentFactory factory = (ComponentFactory)types.get((Class)key);
            
            if (factory != null) {
                return factory.createHandle(container);
            }
        }
        
        for (int i = 0; i < parentContexts.length; i++) {
            ServiceFactory context = parentContexts[i];
            
            ComponentHandle handle = context.createHandle(key, container);
            if (handle != null) {
                return handle;
            }
        }

        return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentContext#hasKey(java.lang.Object)
     */
    public boolean hasService(Object componentKey) {        
        return hasKey(componentKey, null);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IContainerContext#hasComponent(java.lang.Object, org.eclipse.core.components.IContainer)
     */
    private boolean hasKey(Object key, ServiceFactory parentScopeToSkip) {
        if (key == this) {
            return true;
        }
        
    	if (key instanceof Class) {
    		if (types.containsKey((Class)key)) {
    		    return true;      
            }
    	}
        
        for (int i = 0; i < parentContexts.length; i++) {
            ServiceFactory context = parentContexts[i];
            
            if (context == parentScopeToSkip) {
                continue;
            }
            
            if (context.hasService(key)) {
                return true;
            }
        }
        
    	return false;
    }
    
//    private IComponentFactory getComponentFactory(Object key, IContainerContext toSkip) {
//        if (key instanceof Class) {
//            IComponentFactory result = (IComponentFactory)types.get((Class)key);
//            
//            if (result != null) {
//                return result;
//            }
//        }
//        
////        for (int i = 0; i < parentContexts.length; i++) {
////            IContainerContext parent = parentContexts[i];
////            
////            if (parent == toSkip) {
////                continue;
////            }
////            
////            IComponentFactory parentFactory = parent.getComponentHandle(key);
////            
////            if (parentFactory != null) {
////                return parentFactory;
////            }
////        }
//        
//        return null;
//    }

    public ComponentFactory lookup(ClassIdentifier type) {
        return (ComponentFactory)types.get(type);
    }
    
    public void put(ClassIdentifier type, ComponentFactory factory) {
        types.put(type, factory);
    }
    
    public void remove(ClassIdentifier type) {
        types.remove(type);
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.core.component.registry.IComponentScope#getTypes()
	 */
	public ClassIdentifier[] getTypes() {
	    return types.getTypes();
	}

    /* (non-Javadoc)
     * @see org.eclipse.core.component.registry.IComponentScope#getScopeId()
     */
    public String getScopeId() {
        return scopeId;
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.core.component.registry.IComponentScope#getParentScopes()
     */
    public IScopeReference[] getParentScopes() {
        return parentScopes;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.component.registry.IComponentScope#getDependencies()
     */
    public ClassIdentifier[] getDependencies() {
        return dependencies;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.component.registry.IComponentScope#getContext()
     */
    public ServiceFactory getContext() {
        return this;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.component.IContainerContext#getMissingDependencies()
     */
    public Collection getMissingDependencies() {
        if (!loaded) {
            List result = new ArrayList();
            result.add (impossibleToSatisfyDependency);
            return result;
        }
        
        HashSet result = new HashSet();
        for (int i = 0; i < dependencies.length; i++) {
            ClassIdentifier type = dependencies[i];
            
            try {
                Class dep = ComponentUtil.loadClass(type);
                // Ensure that we haven't satisfied this dependency
                if (types.get(dep) == null) {
                    result.add(dep);
                }
            } catch (ComponentException e) {
                WorkbenchPlugin.log(e);
                result.add(impossibleToSatisfyDependency);
            }
        }
        
        // If we're referring to any scopes by delegation, then the scopes themselves
        // become dependencies
        for (int i = 0; i < parentScopes.length; i++) {
            IScopeReference ref = parentScopes[i];
            
            if (ref.getRelationship() == IScopeReference.REL_REQUIRES) {
                ServiceFactory context = ((ComponentScope)ref.getTarget()).getContext();
                
                if (!hasService(context)) {
                    result.add(context);
                }
            }
        }
        
        // Check for dependencies inherited from extended scopes 
        
        for (int i = 0; i < parentContexts.length; i++) {
            ServiceFactory next = parentContexts[i];

            // If we extend by inheritance, then we inherit all of the parent's dependencies
            Collection parentMissing = next.getMissingDependencies();
            
            for (Iterator iter = parentMissing.iterator(); iter.hasNext();) {
                Object object = (Object) iter.next();
            
                // Don't propogate the dependency if we've satisfied it
                if (!hasKey(object, next)) {
                    result.add(object);
                }
            }
        }

        return result;
    }
    
    public void load(ScopeDefinition def, ComponentRegistry reg) {
        if (loaded) {
            unload(reg);
        }
        
        dependencies = def.getDependencies();
        //description = def.getDescription();
        
        SymbolicScopeReference[] refs = def.getExtends();
        IScopeReference[] parents = new IScopeReference[refs.length];
        List parentContextList = new ArrayList();
        
        for (int i = 0; i < refs.length; i++) {
            SymbolicScopeReference reference = refs[i];
            
            IScopeReference ref = new ScopeReference(reference.relationship, 
                    reg.linkSubScope(reference.scopeId, this, reference.relationship));
            parents[i] = ref;
            
            if (reference.relationship == IScopeReference.REL_REQUIRES) {
                scopeDependencies.add(((ComponentScope) ref.getTarget()).getContext());
            } else {
                parentContextList.add(((ComponentScope) ref.getTarget()).getContext());
            }
        }
        
        parentContexts = (ServiceFactory[]) parentContextList.toArray(
                new ServiceFactory[parentContextList.size()]);
        parentScopes = parents;
        loaded = true;
    }
    
    public void unload(ComponentRegistry reg) {
        for (int i = 0; i < parentScopes.length; i++) {
            IScopeReference ref = parentScopes[i];
            
            reg.unlinkSubScope(ref.getTarget().getScopeId(), this);
        }
        
        scopeDependencies = new ArrayList();
        parentScopes = new IScopeReference[0];
        dependencies = new ClassIdentifier[0];
        parentContexts = new ServiceFactory[0];
        //description = UNRESOLVED_SCOPE; 
        loaded = false;
    }
    
    public boolean isRedundant() {
        return (!loaded) && getChildScopes().length == 0 && types.isEmpty() && modifiers.isEmpty();
    }

    /**
     * @since 3.1 
     *
     * @param child
     */
    public void addChild(ComponentScope child, int relationship) {
        children.add(new ScopeReference(relationship, child));
    }
    
    public void removeChild(ComponentScope child) {
        IScopeReference[] refs = (IScopeReference[]) children.toArray(new IScopeReference[children.size()]);
        for (int i = 0; i < refs.length; i++) {
            IScopeReference reference = refs[i];
            if (reference.getTarget() == child) {
                children.remove(reference);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "scope " + scopeId; //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.component.registry.IComponentScope#getChildScopes()
     */
    public IScopeReference[] getChildScopes() {
        return (IScopeReference[]) children.toArray(new IScopeReference[children.size()]);
    }



}
