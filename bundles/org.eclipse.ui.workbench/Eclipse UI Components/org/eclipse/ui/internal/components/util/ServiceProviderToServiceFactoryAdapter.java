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
package org.eclipse.ui.internal.components.util;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.ComponentHandle;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.components.framework.NonDisposingHandle;
import org.eclipse.ui.internal.components.framework.ServiceFactory;

/**
 * Adapts an <code>IServiceProvider</code> to an <code>AbstractServiceFactory</code>. This is 
 * essentially a factory that always returns existing instances from the <code>IServiceProvider</code>.
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public final class ServiceProviderToServiceFactoryAdapter extends ServiceFactory {

    private IServiceProvider target;
    
    /**
     * Creates a service factory that delegates to the given service provider.
     * 
     * @param target service provider to adapt
     */
    public ServiceProviderToServiceFactoryAdapter(IServiceProvider target) {
        this.target = target;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.component.IContainerContext#getComponentFactory(java.lang.Object)
     */
    public ComponentHandle createHandle(Object key, IServiceProvider provider) throws ComponentException {
    	Object component = target.getService(key);
    	
    	if (component != null) {
            return new NonDisposingHandle(component);
    	}

        return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentContext#hasKey(java.lang.Object)
     */
    public boolean hasService(Object componentKey) {
        return target.hasService(componentKey);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentContext#getMissingDependencies()
     */
    public Collection getMissingDependencies() {
        // Service providers don't have dependencies
        return Collections.EMPTY_SET;
    }
    
}
