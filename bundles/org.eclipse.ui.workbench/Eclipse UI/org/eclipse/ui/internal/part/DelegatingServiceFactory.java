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
package org.eclipse.ui.internal.part;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.ComponentHandle;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.components.framework.ServiceFactory;
import org.eclipse.ui.internal.components.util.ServiceMap;
import org.eclipse.ui.internal.part.multiplexer.IDelegatingComponent;
import org.eclipse.ui.internal.part.multiplexer.IDelegatingContext;

public class DelegatingServiceFactory extends ServiceFactory implements IDelegatingContext {

	private IServiceProvider active;
    private ServiceFactory delegatingComponentFactory;
		
    private List delegatingComponents = new ArrayList();
    
    public DelegatingServiceFactory(ServiceFactory delegatingComponentFactory) {
        this.delegatingComponentFactory = delegatingComponentFactory;
    }
    
	public ComponentHandle createHandle(Object componentKey, IServiceProvider container)
			throws ComponentException {
		
        ComponentHandle handle = delegatingComponentFactory.createHandle(componentKey, 
                new ServiceMap(container).map(IDelegatingContext.class, this));
        
        if (handle == null) {
            return null;
        }
        
        Object component = handle.getInstance();
        
        if (component instanceof IDelegatingComponent) {
            delegatingComponents.add(component);
        }
        
        return handle;     
	}

    /* (non-Javadoc)
     * @see org.eclipse.core.components.IContainerContext#hasComponent(java.lang.Object)
     */
    public boolean hasService(Object key) {
        return delegatingComponentFactory.hasService(key);
    }

    public IServiceProvider getActive() {
    	return active;
    }
    
    public void setActive(IServiceProvider active) {
    	this.active = active;
        
        for (Iterator iter = delegatingComponents.iterator(); iter.hasNext();) {
            IDelegatingComponent next = (IDelegatingComponent) iter.next();
            
            next.setActive(active);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentContext#getMissingDependencies()
     */
    public Collection getMissingDependencies() {
        Collection result = delegatingComponentFactory.getMissingDependencies();
        
        result.remove(IDelegatingContext.class);
        return result;
    }
}
