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


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.components.Assert;
import org.eclipse.ui.internal.components.ComponentMessages;
import org.eclipse.ui.internal.components.util.ServiceMap;

/**
 * Contains static helper methods for managing components. 
 * 
 * Not intended to be subclassed or instantiated.
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public final class Components {
    
    private Components() {
    }
    
    /**
     * Returns a service provider that provides no services
     */
    public static final IServiceProvider EMPTY_PROVIDER = new ServiceMap(); 

    /**
     * If it is possible to adapt the given object to the given type, this
     * returns the adapter. Performs the following checks:
     * 
     * <ol>
     * <li>Returns <code>sourceObject</code> if it is an instance of the adapter type.</li>
     * <li>If sourceObject implements IAdaptable, it is queried for adapters.</li> 
     * <li>If sourceObject implements IComponentProvider, it is queried for a component that
     * uses the adapter type as a key. Any exceptions are logged immediately.</li>
     * </ol>
     * 
     * Otherwise returns null. 
     *
     * @param sourceObject object to adapt
     * @param adapter type to adapt to
     * @return a representation of sourceObject that is assignable to the adapter type, or null
     * if no such representation exists
     */
	public static Object getAdapter(Object sourceObject, Class adapter) {
	    if (adapter.isInstance(sourceObject)) {
	        return sourceObject;
	    }
        
        if (sourceObject instanceof IAdaptable) {
	        IAdaptable adaptable = (IAdaptable)sourceObject;
	        
	        Object result = adaptable.getAdapter(adapter);
	        if (result != null) {
                // Sanity-check
                Assert.isTrue(adapter.isInstance(result));
	            return result;
	        }
	    } 
        
        if (sourceObject instanceof IServiceProvider) {
            try {
                return ((IServiceProvider)sourceObject).getService(adapter);
            } catch (ComponentException e) {
                WorkbenchPlugin.log(e);
            }
        }
	    
	    return null;
	}
    
    /**
     * Queries the given IAdaptable for a required adapter. Throws an exception if the adapter
     * does not exist or is of an unexpected type.
     *
     * @param toQuery adaptable to query for interfaces
     * @param interf interface to request
     * @return an object that implements the given interface. Does not return null.
     * @throws ComponentException if the given adapter does not exist.
     */
    public static Object queryInterface(IAdaptable toQuery, Class interf) throws ComponentException {
        Object result = toQuery.getAdapter(interf);
        
        if (result == null) {
            throw new ComponentException(NLS.bind(ComponentMessages.Components_missing_required, 
                   interf.toString()), null);
        }
        
        if (!interf.isInstance(result)) {
            throw new ComponentException(interf, 
                    NLS.bind(ComponentMessages.Components_wrong_type,interf.getName(), result.getClass().getName()),
                    null);
        }
        
        return result;
    }
    
    /**
     * Queries a service provider for the service identified by the given key.
     * Throws a <code>ComponentException</code> if no such component exists.
     *
     * <p>
     * By convention, the following keys have special meanings:
     * </p>
     * <ul>
     * <li>If the key is a Class instance, then the result must be an instance of that class</li>
     * <li>If the key is IServiceProvider.class, then this method returns the service provider itself</li>
     * <li>If the key is an instance of IComponentScope, then this method will throw an exception
     * unless the given service provider includes the given scope</li>
     * </ul>
     *
     * @param toQuery service provider to query 
     * @param key component being requested
     * @return the component for the given key (does not return null)
     * @throws ComponentException if no such component exists
     */
    public static Object queryInterface(IServiceProvider toQuery, Object key) throws ComponentException {
        Object result = toQuery.getService(key);
        
        if (key == IServiceProvider.class) {
            return toQuery;
        }
        
        if (result == null) {
        	throw new ComponentException(key, NLS.bind(ComponentMessages.Components_missing_required,
        			key.toString()), null);
        }
        
        if (key instanceof Class) {
        	Class c = (Class)key;
	        if (!c.isInstance(result)) {
	            throw new ComponentException(key, 
	                   NLS.bind(ComponentMessages.Components_wrong_type, 
	                            c.getName(),  result.getClass().getName()),
	                    null);
	        }
        }
        
        return result;
    }
    
    /**
     * Requests a set of services from the given service provider. Returns all of the requested 
     * interfaces or throws a ComponentException if any service is missing. This 
     * can be used to reduce tedious error-checking in situations where the application relies on 
     * one or more interfaces from a container. 
     * 
     * For example:
     * <code>
     * 		Container myContainer = new Container(Components.getContext("org.eclipse.core.components.plugin"));
     * 		try {	
     * 			Object[] interfaces = Components.queryInterfaces(myContainer, new Object[] {ISystemLog.class, IStatusFactory.class});
     * 			ISystemLog interface1 = (ISystemLog) interfaces[0];
     * 			IStatusFactory interface2 = (IStatusFactory) interfaces[1];
     * 
     * 			// Do something with interface1 and interface2
     * 			// ...
     * 
     * 		} catch (ComponentException e) {
     * 			// The container did not have implementations for the expected interfaces.
     *      } finally {
     *          // Clean up when done
     * 			myContainer.dispose();
     * 		}
     * </code>  
     * 
     * @param toQuery service provider that is expected to contain the given components
     * @param requiredKeys array of interface types. The order of the classes in this array
     *        corresponds to the order of instances in the return value. For example, if requiredInterfaces[1] == String.class
     *        then result[1] will be an instance of String.
     * @return an array of interfaces. All entries are non-null. The order and size of this array is determined by requiredInterfaces.
     *        If requiredInterfaces[n] contains type T, then the result will contain an instance of T at index n.
     * @throws ComponentException if any of the requested interfaces are missing from the container
     */
    public static Object[] queryInterfaces(IServiceProvider toQuery, Object[] requiredKeys) throws ComponentException {
        Object[] result = new Object[requiredKeys.length];
        
        for (int i = 0; i < result.length; i++) {
            result[i] = queryInterface(toQuery, requiredKeys[i]);
        }
        
        return result;
    }
	
}
