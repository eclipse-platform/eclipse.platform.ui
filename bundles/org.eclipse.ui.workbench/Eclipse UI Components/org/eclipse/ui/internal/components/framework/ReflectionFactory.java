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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.ui.internal.components.ComponentUtil;
import org.eclipse.ui.internal.components.ComponentMessages;

/**
 * Factory that uses reflection to create instances by constructor injection. The factory
 * attempts to create the target class by invoking the greediest satisfiable constructor. That is,
 * the constructor with the greatest number of arguments where all the arguments can be found as
 * services.
 * 
 * <p>
 * This factory should only be used in situations where a component only has one constructor, 
 * or where it is completely obvious which constructor is the greediest.
 * If a component has many different constructors or needs to take arguments to its constructor
 * which can't simply be located by using the argument types as service keys, clients should implement
 * a custom factory rather than trying to reuse this class. 
 * </p>
 * 
 * <p>
 * This class is normally referred to in a plugin.xml file as part of some extension markup.
 * This will work for for any extension point that looks for objects of type AbstractComponentFactory.
 * (such as the org.eclipse.core.components.services extension point or the org.eclipse.ui.views extension
 * point).
 * </p>
 * 
 * <p>
 * For example, the following XML markup would create the org.eclipse.ui.examples.components.views.context.RedirectAllView
 * view by passing any dependencies it needs into its constructor. Notice that ReflectionFactory needs
 * to be given the name of the class to create by separating it with a colon.
 * </p>
 * <code>
 * <view
 *    class="org.eclipse.core.components.ReflectionFactory:org.eclipse.ui.examples.components.views.context.RedirectAllView"
 *    category="org.eclipse.ui.examples.components.context"
 *    name="RedirectAllView"
 *    id="org.eclipse.ui.examples.components.views.context.RedirectAllView"/>          
 *    
 * </code>
 *
 * <p>
 * This factory can also be created programmatically, in which case it needs to receive the class
 * to create (or its name) in the factory's constructor.
 * </p>
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
public class ReflectionFactory extends ComponentFactory implements IExecutableExtension {
    
    private ClassIdentifier classId = null;
    private Class targetClass = null;
    
    /**
     * Should only be called when this factory is created through an extension point.
     * If this constructor is used, the caller MUST call setInitializationData before
     * attempting to use the factory. Call one of the other constructors if instantiating
     * this object programmatically.
     */
    public ReflectionFactory() {
    }
    
    /**
     * Creates a factory that creates instances of the given class
     * 
     * @param toCreateByReflection class to instantiate
     */
    public ReflectionFactory(Class toCreateByReflection) {
        targetClass = toCreateByReflection;
    }
   
    /**
     * Creates a factory that creates instances of the given class
     * 
     * @param classId name of the class to instantiate
     */
    public ReflectionFactory(ClassIdentifier classId) {
        this.classId = classId;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) throws CoreException {

        classId = ComponentUtil.getClassFromInitializationData(config, data);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentFactory#getHandle(org.eclipse.core.components.IComponentProvider)
     */
    public ComponentHandle createHandle(IServiceProvider availableServices)
            throws ComponentException {
        
        Class targetClass = this.targetClass;
        
        // Find the greediest satisfiable constructor
        if (targetClass == null) {
            targetClass = ComponentUtil.loadClass(classId);
        }
        
        Constructor targetConstructor = null;
        
        Constructor[] constructors = targetClass.getConstructors();
        
        // Optimization: if there's only one constructor, use it.
        if (constructors.length == 1) {
            targetConstructor = constructors[0];
        } else {            
            ArrayList toSort = new ArrayList();
            
            for (int i = 0; i < constructors.length; i++) {
                Constructor constructor = constructors[i];
                
                // Filter out non-public constructors
                if ((constructor.getModifiers() & Modifier.PUBLIC) != 0) {
                    toSort.add(constructor);
                }
            }
            
            // Sort the constructors by descending number of constructor arguments
            Collections.sort(toSort, new Comparator() {
                /* (non-Javadoc)
                 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                 */
                public int compare(Object arg0, Object arg1) {
                    Constructor c1 = (Constructor)arg0;
                    Constructor c2 = (Constructor)arg1;
                    
                    int l1 = c1.getParameterTypes().length;
                    int l2 = c2.getParameterTypes().length;
                    
                    return l1 - l2;
                } 
            });
            
            // Find the first satisfiable constructor
            for (Iterator iter = toSort.iterator(); iter.hasNext() && targetConstructor == null;) {
                Constructor next = (Constructor) iter.next();
                
                boolean satisfiable = true;
                
                Class[] params = next.getParameterTypes();
                for (int i = 0; i < params.length && satisfiable; i++) {
                    Class clazz = params[i];
                    
                    if (!availableServices.hasService(clazz)) {
                        satisfiable = false;
                    }
                }
                
                if (satisfiable) {
                    targetConstructor = next;
                }
            }
        }
        
        if (targetConstructor == null) {
            throw new ComponentException(targetClass, ComponentMessages.ReflectionFactory_no_constructors, null);
        }

        Class[] paramKeys = targetConstructor.getParameterTypes();

        try {
            Object[] params = Components.queryInterfaces(availableServices, paramKeys);
            
            return new ComponentHandle(targetConstructor.newInstance(params));
        } catch (Exception e) {
            throw new ComponentException(targetClass, e);
        }
    }

}
