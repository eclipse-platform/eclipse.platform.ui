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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.internal.components.Assert;
import org.eclipse.ui.internal.components.DependencyOnlyFactory;
import org.eclipse.ui.internal.components.util.InstanceToComponentFactoryAdapter;
import org.eclipse.ui.internal.components.util.InstanceToServiceFactoryAdapter;
import org.eclipse.ui.internal.components.util.ServiceProviderToServiceFactoryAdapter;

/**
 * Default implementation of <code>ServiceFactory</code> that is useful for expressing
 * hardcoded dependency rules. A <code>FactoryMap</code> contains a map of 
 * component factories and an ordered list of other service factories to delegate to. It
 * resolves dependencies like this:
 * 
 * <ol>
 * <li>Check if the type is being explicitly excluded.</li>
 * <li>Check if it there is a specific mapping for the dependency.</li>
 * <li>Run through the list of other service factories to delegate to. The first factory
 *     that can handle the dependency will be used. When delegating, all possible types
 *     are delegated to the target object.</li>
 * <li>Return null</li>
 * </ol>
 * 
 * <p>Note that this is only a convenience class. Any methods that take a service factory as input
 * argument should generally take an <code>AbstractServiceFactory</code> rather than depending
 * on this concrete class.</p>
 * 
 * <p>Class keys must match exactly. When a service is looked up by class, the factory will
 * <em>not</em> look for mappings to other compatible types if an exact match is not found.</p>
 * 
 * <p>As a convenience, all of the setters on this object return <i>this</i>,
 * allowing them to be chained Smalltalk-style. For example: </p>
 * 
 * <code>
 *    // Chained initialization
 *    Container myContainer = new Container(new FactoryMap()
 *      .mapInstance(Dog.class, myDog)
 *      .mapInstance(Cat.class, myCat));
 *      
 *    // Equivalent non-chained initialization
 *    FactoryMap context = new FactoryMap();
 *    context.mapInstance(Dog.class, myDog);
 *    context.mapInstance(Cat.class, myCat);
 *    Container myContainer = new Container(context);
 * </code>
 *
 * <p>Not intended to be subclassed by clients.</p>
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public final class FactoryMap extends ServiceFactory {
    
    /**
     * Mixed-type Map of Object keys onto IComponentInfo (null if empty)
     */
    private Map adapters = null;
    
    private List exclusions = null;
    
    /**
     * List of IContainerContext, or null if empty. 
     */
    private List parentContexts = null;
     
    /**
     * Create an empty ContainerContext that does not recognize any types. 
     */
    public FactoryMap() {
    }
    
    /**
     * Blocks the given type from being returned by this context. Subsequent calls to 
     * getComponentInfo will return null when asked for this interface, even if there is
     * an instance or parent context that can provide the component. This is not the same 
     * as unmapping the type, which simply undoes the effects of a previous mapping and
     * may still fall through to an object registered through add*. This overrides any
     * previous mapping for this interface, and does not affect its subtypes or supertypes. 
     *
     * @param interface_ interface to exclude
     * @return this
     */
    public FactoryMap mapExclusion(Object interface_) {
        Assert.isNotNull(interface_);
        if (exclusions == null) {
            exclusions = new ArrayList();
        }
        exclusions.add(interface_);
        internalAddMapping(interface_, null);
        return this;        
    }
    
    /**
     * Maps the given interface to the given factory. The factory must be able
     * to create objects that are assignable to the interface. This will override the
     * effects of any previous mapping for this interface, but does not affect its
     * subtypes or supertypes.
     *
     * @param interface_ interface to implement
     * @param adapter a factory that can create objects of the given type
     * @return this
     */
    public FactoryMap map(Object interface_, ComponentFactory adapter) {
        Assert.isNotNull(adapter);
        Assert.isNotNull(interface_);
        
        internalAddMapping(interface_, adapter);
        return this;
    }
    
    /**
     * Maps the given interface to the given context. Whenever this context is asked
     * for a dependency of the given type, it will delegate to the given context.
     * This will override the effects of any previous mapping for this interface, 
     * but does not affect its subtypes or supertypes.
     *
     * @param interfaceType interface to map
     * @param context context to delegate to
     * @return this
     */
    public FactoryMap map(Object interfaceType, ServiceFactory context) {
        Assert.isNotNull(interfaceType);
        Assert.isNotNull(context);
        
        internalAddMapping(interfaceType, context);
        
        // Inherit all of the dependencies from the given factory.
        add(new DependencyOnlyFactory(context));
        return this;
    }
        
    /**
     * Maps the given interface type to the given object instance. Whenever the context
     * is asked for a dependency of the given type, it will return the given object.
     * This will override the effects of any previous mapping for this interface 
     * but does not affect its subtypes or supertypes.
     *
     * @param interfaceType interface type to map
     * @param component component instance that either implements the interface or supplies
     * an adapter to the interface
     * @return this
     */
    public FactoryMap mapInstance(Object interfaceType, Object component) {
        Assert.isNotNull(interfaceType);
        Assert.isNotNull(component);
        
        if (interfaceType instanceof Class) {
            Class c = (Class)interfaceType;
            
            Assert.isTrue(c.isInstance(component));
        }
        
        return map(interfaceType, new InstanceToComponentFactoryAdapter(component));
    }
    
    public FactoryMap addInstance(Object instance) {
        return add(new InstanceToServiceFactoryAdapter(instance));
    }
    
    /**
     * Causes the receiver to delegate to the given context whenever it can't find a dependency.
     * 
     * <p>Dependencies registered through add* are processed after specific mappings registered
     * through map*. If two add* calls can satisfy the same dependency, the one that was added
     * first takes precidence.</p> 
     *
     * @param context newly added context
     * @return this
     */
    public FactoryMap add(ServiceFactory context) {
    	Assert.isNotNull(context);
        internalAddInstance(context);
        
        return this;
    }
    
    public FactoryMap add(IServiceProvider toAdd) {
    	Assert.isNotNull(toAdd);
        internalAddInstance(new ServiceProviderToServiceFactoryAdapter(toAdd));
        
        return this;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.core.component.IComponentArguments#getComponentInstance(java.lang.Class)
	 */
	ComponentHandle getInstance(Object type, IServiceProvider availableDependencies) throws ComponentException {
		if (parentContexts == null) {
			return null;
		}
		
		for (Iterator iter = parentContexts.iterator(); iter.hasNext();) {
            ServiceFactory context = (ServiceFactory) iter.next();
            
            ComponentHandle result = context.createHandle(type, availableDependencies);
            if (result != null) {
                return result;
            }
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.component.IComponentContext#get(java.lang.Class)
	 */
	public ComponentHandle createHandle(Object key, IServiceProvider availableDependencies) throws ComponentException {
        if (adapters != null) {            
            if (adapters.containsKey(key)) {
                Object target = adapters.get(key);
                if (target == null) {
                	return null;
                }
                
                if (target instanceof ComponentFactory) {
                    ComponentFactory factory = (ComponentFactory)target;                    
                    return factory.createHandle(availableDependencies);
                } else if (target instanceof ServiceFactory) {
                    ServiceFactory targetContext = (ServiceFactory)target;
                    
                    ComponentHandle handle = targetContext.createHandle(key, availableDependencies);
                    if (handle != null) {
                        return handle;
                    }
                }
            }
        }
        
        // Look for inherited implementations
        return getInstance(key, availableDependencies);
	}

    private void internalAddInstance(ServiceFactory component) {
        if (parentContexts == null) {
            parentContexts = new ArrayList();
        }
        
        if (!parentContexts.contains(component)) {
            parentContexts.add(component);
        }
    }

    private void internalAddMapping(Object interface_, Object toMap) {
        if (adapters == null) {
            adapters = new HashMap();
        }
        
        adapters.put(interface_, toMap);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentContext#hasKey(java.lang.Object)
     */
    private boolean hasKey(Object componentKey, ServiceFactory toSkip) {
        if (adapters != null) {
            if (adapters.containsKey(componentKey)) {
                return (adapters.get(componentKey) != null);
            }
        }

        if (parentContexts != null) {
            for (Iterator iter = parentContexts.iterator(); iter.hasNext();) {
                ServiceFactory context = (ServiceFactory) iter.next();
                
                if (context == toSkip) {
                    continue;
                }
                
                if (context.hasService(componentKey)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentContext#getMissingDependencies()
     */
    public Collection getMissingDependencies() {
        
        HashSet result = new HashSet();
        
        if (parentContexts != null) {
            for (Iterator iter = parentContexts.iterator(); iter.hasNext();) {
                ServiceFactory next = (ServiceFactory) iter.next();
                
                Collection inheritedDeps = next.getMissingDependencies();
                
                for (Iterator iterator = inheritedDeps.iterator(); iterator
                        .hasNext();) {
                    Object dep = iterator.next();
                    
                    if (!hasKey(dep, next)) {
                        result.add(dep);
                    }
                }
            }
        }
        
        if (exclusions != null) {
            result.addAll(exclusions);
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentContext#hasKey(java.lang.Object)
     */
    public boolean hasService(Object componentKey) {
        return hasKey(componentKey, null);
    }
}
