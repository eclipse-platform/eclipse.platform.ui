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
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.components.Assert;
import org.eclipse.ui.internal.components.ComponentMessages;

/**
 * A dependency injection container for one or more components. Containers are an implementation of
 * <code>IServiceProvider</code> which first look for an existing instance of a service before attempting
 * to create one using a service factory. 
 *
 * The owner of a container must dispose it when it is no longer needed.
 * 
 * <p>
 * Example usage:
 * </p>
 * <code>
 * class Cat {
 *      String catName;
 *      
 *      Cat(String name) {
 *          catName = name;
 *      };
 * }
 *  
 * class Dog {
 *      Cat theCat;
 * 
 *      Dog(Cat toChase) {
 *          theCat = toChase;
 *      }
 *      
 *      public chaseCat() {
 *          System.out.println("Chasing " + theCat.getName());
 *      }
 * }
 * 
 * ...
 * 
 * public static void main(String[] args) {
 *      // Create a context that knows how to create Dogs. Any time it needs a Cat,
 *      // it will refer to poor Fluffy.
 *      FactoryMap context = new FactoryMap()
 *          .add(Dog.class, new ReflectionFactory(Dog.class))
 *          .addInstance(Cat.class, new Cat("Fluffy"));
 *      
 *      // Create a container for a particular Dog 
 *      Container container = new Container(context);
 *      
 *      try {
 *          // Create a Dog
 *          Dog myDog = (Dog)container.getService(Dog.class);
 *      
 *          // Chase Fluffy around
 *          myDog.chaseCat();
 *          
 *      } catch (ComponentException e) {
 *          // We weren't able to create the Dog
 *          System.out.println(e.toString());
 *      } finally {
 *          // Clean up the container when no longer needed
 *          container.dispose();
 *      }
 * }
 * </code>
 * 
 * <p>
 * Not intended to be subclassed by clients.
 * </p>
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public final class Container implements IDisposable, IServiceProvider {
    
    private static final class ComponentInfo {    
        IDisposable disposable;
        Object component;
        Object key;
        
        /**
         * Creates a ComponentInfo given an interface type and an optional existing instance and factory.
         * 
         * @param key existing component instance. May be null if no instance is available. Must be 
         *                 assignable to interfaceType unless null.
         * @param component factory for the given type. May be null if no factory is available. Must create
         *                instances that are assignable to interfaceType unless null. 
         * @param disposable component type
         */
        public ComponentInfo(Object key, Object component, IDisposable disposable) {
            this.key = key;
            this.component = component;
            this.disposable = disposable;
        }
        
        public Object getKey() {
            return key;
        }
        
        public Object getInstance() {
            return component;
        }
        
        public void dispose() {
            if (disposable != null) {
                disposable.dispose();
            }
        }
        
    }    
    
    // Array of ComponentInfo
    private ArrayList services = new ArrayList();
    private LinkedList inProgress = null;
    
    private ServiceFactory defaultContext;
        
    /**
     * Creates a new Container that will create services using the given factory.
     * 
     * @param context factory that will supply the instances to this container
     */
    public Container(ServiceFactory context) {
        Assert.isNotNull(context);
        Collection deps = context.getMissingDependencies();
        if (deps.size() != 0) {
            Assert.isTrue(false, ComponentMessages.Container_missing_dependency + deps.toArray()[0].toString()); 
        }
        
        this.defaultContext = context;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.component.IComponent#dispose()
     */
    public void dispose() {
        if (isDisposed()) {
            return;
        }
        deallocateComponents(0);
                
        services = null;
    }

    /**
     * Deallocates all components beyond the given index
     * 
     * @since 3.1 
     *
     * @param index 
     */
    private void deallocateComponents(int index) {
        ComponentInfo[] servicesArray = (ComponentInfo[]) services.toArray(new ComponentInfo[services.size()]);
        for (int i = servicesArray.length - 1; i >= index; i--) {
            ComponentInfo info = servicesArray[i];
            
            try {
                info.dispose();
            } catch (Exception e) {
                WorkbenchPlugin.log(e);
            }
        }
        
        services = new ArrayList();
        for (int i = 0; i < index; i++) {
            ComponentInfo info = servicesArray[i];
            
            services.add(info);
        }
    }
    
    private final Object getComponent(Object key, ServiceFactory context, IServiceProvider dependencies) throws ComponentException {        
        
        Object existingInstance = getExistingComponent(key, context);
        
        if (existingInstance != null) {
            return existingInstance;
        }
        
        // See if our context knows about this type

        // If we're in the process of creating another component in this container,
        // check for cycles
        if (inProgress != null) {
            for (Iterator iter = inProgress.iterator(); iter.hasNext();) {
                //ComponentKey next = (ComponentKey) iter.next();
                Object next = iter.next();
                
                if (next.equals(key)) {
                    throw new ComponentException(key, NLS.bind(
                            ComponentMessages.Container_cycle_detected, 
                          key.toString()), null);
                }
            }
        } else {
            inProgress = new LinkedList();
        }
        
        inProgress.add(key);
        
        int start = services.size();
        
        boolean success = false;
        
        try {                
            ComponentHandle handle = context.createHandle(key, dependencies);
            if (handle != null) {                
                if (handle.requiresDisposal()) {
                    services.add(new ComponentInfo(key, handle.getInstance(), handle.getDisposable()));
                }
                Object result = handle.getInstance();
                success = true;
                return result;
            }
        } finally {
            if (!success) {
                // If something went wrong, deallocate everything that was allocated for
                // the faulty component.
                deallocateComponents(start);
            }
            inProgress.removeLast();
            if (inProgress.isEmpty()) {
                inProgress = null;
            }
        }
        
        return null;
    }
        
    private final Object getExistingComponent(Object key, ServiceFactory context) {
        // Look for an existing component of this type
        for (Iterator iter = services.iterator(); iter.hasNext();) {
            ComponentInfo info = (ComponentInfo) iter.next();
                            
            if (info.getKey().equals(key)) {
                return info.getInstance();
            }
        }
        
        return null;
    }
    
    /**
     * Returns true iff this container has been disposed. 
     *
     * @return true iff this container has been disposed.
     */
    public boolean isDisposed() {
        return services == null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.components.IServiceProvider#getService(java.lang.Object)
     */
    public Object getService(Object key) throws ComponentException {
        try {
            return getComponent(key, defaultContext, this);
        } catch (ComponentException e) {
            throw new ComponentException(key, e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IServiceProvider#hasKey(java.lang.Object)
     */
    public boolean hasService(Object key) {
        return defaultContext.hasService(key);
    }
    
}
