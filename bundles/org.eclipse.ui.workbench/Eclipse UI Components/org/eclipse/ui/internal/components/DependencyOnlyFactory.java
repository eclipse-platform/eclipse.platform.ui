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
package org.eclipse.ui.internal.components;

import java.util.Collection;

import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.ComponentHandle;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.components.framework.ServiceFactory;

/**
 * Service factory that exposes the dependencies, but nothing else, from another
 * factory. 
 * 
 * @since 3.1
 */
public class DependencyOnlyFactory extends ServiceFactory {
    
    public ServiceFactory otherFactory;
    
    public DependencyOnlyFactory(ServiceFactory other) {
        this.otherFactory = other;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.AbstractServiceFactory#createHandle(java.lang.Object, org.eclipse.core.components.IServiceProvider)
     */
    public ComponentHandle createHandle(Object key,
            IServiceProvider services) throws ComponentException {
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.AbstractServiceFactory#getMissingDependencies()
     */
    public Collection getMissingDependencies() {
        return otherFactory.getMissingDependencies();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.AbstractServiceFactory#hasService(java.lang.Object)
     */
    public boolean hasService(Object componentKey) {
        return false;
    }

}
