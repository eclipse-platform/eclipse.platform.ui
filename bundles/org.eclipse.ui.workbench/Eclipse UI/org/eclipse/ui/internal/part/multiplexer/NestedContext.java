/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.multiplexer;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.components.ComponentException;
import org.eclipse.core.components.ServiceFactory;
import org.eclipse.core.components.ComponentHandle;
import org.eclipse.core.components.IServiceProvider;
import org.eclipse.core.components.util.ServiceMap;
import org.eclipse.ui.internal.Messages;

/**
 * Contains a factory for services that can delegate to a shared implementation.
 * Many <code>NestedContext</code> instances can share the same <code>ISharedComponents</code>
 * instance, however only one of them will be active at a time. A <code>NestedContext</code>
 * remembers everything it has created. Calling activate 
 * 
 * When a <code>NestedContext</code>
 * is activated, it activate
 * 
 * @since 3.1
 */
public class NestedContext extends ServiceFactory {

	private List componentList = new ArrayList();
	private IServiceProvider sharedComponents;
    private ServiceFactory nestedFactories;
    
    private ISharedContext sharedContext = new ISharedContext() {
    /* (non-Javadoc)
     * @see org.eclipse.core.components.nesting.ISharedContext#getSharedComponents()
     */
    public IServiceProvider getSharedComponents() {
        return sharedComponents;
    }  
    };
    
    /**
     * Creates a new NestedContext 
     * 
     * @param sharedComponents
     * @param nestedFactories
     */
	public NestedContext(IServiceProvider sharedComponents, ServiceFactory nestedFactories) {
		this.sharedComponents = sharedComponents;
        this.nestedFactories = nestedFactories;
	}
	
	public ComponentHandle createHandle(Object componentKey, IServiceProvider container)
			throws ComponentException {
        
    	ComponentHandle handle = nestedFactories.createHandle(componentKey, new ServiceMap(container)
                .map(ISharedContext.class, sharedContext));
        
        if (handle == null) {
            return null;
        }
        
        Object component = handle.getInstance();
        
        if (!(component instanceof INestedComponent)) {
        	throw new ComponentException(MessageFormat.format(Messages.getString("NestedContext.0"), new String[] { //$NON-NLS-1$
        			INestedComponent.class.getName(), component.getClass().getName()}
        	), null);
        }
        
        INestedComponent nestedComponent = (INestedComponent)component;
                
        // Find the nested service
        componentList.add(component);
        
        return handle;     
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentContext#hasKey(java.lang.Object)
     */
    public boolean hasService(Object componentKey) {
        return nestedFactories.hasService(componentKey);
    }
    
	/**
	 * Activates all the components created by this context. The components
	 * will copy their current state to the shared container and start
	 * delegating to the shared implementation.
	 */
	public void activate() {
		for (Iterator iter = componentList.iterator(); iter.hasNext();) {
			INestedComponent next = (INestedComponent) iter.next();
			
			next.activate();
		}
	}
	
	/**
	 * Deactivates all the components created by this context. The components
	 * will stop delegating to the shared implementation.
	 */
	public void deactivate() {
		for (Iterator iter = componentList.iterator(); iter.hasNext();) {
			INestedComponent next = (INestedComponent) iter.next();
			
			next.deactivate();
		}		
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.core.components.IComponentContext#getMissingDependencies()
     */
    public Collection getMissingDependencies() {
        Collection result = nestedFactories.getMissingDependencies();
        result.remove(ISharedContext.class);
        return result;
    }
}
