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
package org.eclipse.ui.internal.components;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.components.ComponentException;
import org.eclipse.ui.components.ComponentFactory;
import org.eclipse.ui.components.ComponentHandle;
import org.eclipse.ui.components.IServiceProvider;

/**
 * Creates components from a named attribute of a configuration element.
 * 
 * @since 3.1
 */
public class ExecutableExtensionFactory extends ComponentFactory {

	private String attributeId;
	private IConfigurationElement configElement; 

	private ComponentFactory cachedAdapter = null;
	
    /**
     * Creates a component factory based on an attribute from a configuration element. The attribute
     * must point to a class that either implements IComponent or ComponentFactory.
     * 
     * @param configElement configuration element
     * @param attributeId attribute from the given configuration element that contains the name of
     * a java class that implements either IComponent or ComponentFactory
     * @param singleton if true, this factory will return the same instance every time. If false, this 
     * factory will create a new instance every time it is asked for a handle
     */
	public ExecutableExtensionFactory(IConfigurationElement configElement, String attributeId) {
		this.configElement = configElement;
		this.attributeId = attributeId;
	}
    
	/* (non-Javadoc)
     * @see org.eclipse.core.component.IComponentAdapter#createInstance(org.eclipse.core.component.IContainer)
     */
    public ComponentHandle createHandle(IServiceProvider availableServices) throws ComponentException {        
        // If this class is created by a factory and we've already cached an instance of the
        // factory, use the cached factory.
        if (cachedAdapter != null) {
            try {
                ComponentHandle result = cachedAdapter.createHandle(availableServices);
                return result;
            } catch (ComponentException e) {
                throw new ComponentException(cachedAdapter.getClass(), e);
            }
        }
        
        // If we're loading a singleton object, create a new container for it.
        // Otherwise, create a filtered view of the existing container that
        // will only resolve services visible in this scope.
        IServiceProvider actualContainer = availableServices;

        try {
            cachedAdapter = (ComponentFactory)configElement.createExecutableExtension("class"); //$NON-NLS-1$
        } catch (CoreException e) {
            throw new ComponentException(configElement, e);
        }
        
        return cachedAdapter.createHandle(actualContainer);        
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return configElement.hashCode() + attributeId.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ExecutableExtensionFactory)) {
			return false;
		}
		ExecutableExtensionFactory t = (ExecutableExtensionFactory)obj;
		
		return configElement == t.configElement 
			&& attributeId.equals(t.attributeId);
	}

    public void dispose() {
        cachedAdapter = null;
    }
        
}

