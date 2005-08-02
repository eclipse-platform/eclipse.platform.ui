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
package org.eclipse.ui.internal.part.multiplexer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.ComponentHandle;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.components.framework.ServiceFactory;
import org.eclipse.ui.internal.components.util.ServiceMap;
import org.eclipse.ui.internal.part.Part;

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

	//private List componentList = new ArrayList();
	private IServiceProvider sharedComponents;
    private ServiceFactory nestedFactories;
    private Map componentMap = new HashMap();
    
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
        	throw new ComponentException(NLS.bind(WorkbenchMessages.NestedContext_0, new String[] {
        			INestedComponent.class.getName(), component.getClass().getName()}
        	), null);
        }
        
        componentMap.put(componentKey, component);
        
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
	public void activate(Part partBeingActivated) {
        Collection componentList = componentMap.values();
        
		for (Iterator iter = componentList.iterator(); iter.hasNext();) {
			INestedComponent next = (INestedComponent) iter.next();
			
			next.activate(partBeingActivated);
		}
	}
	
	/**
	 * Deactivates all the components created by this context. The components
	 * will stop delegating to the shared implementation.
     * 
     * @param newActive context that is about to be activated (or null if none)
	 */
	public void deactivate(NestedContext newActive) {
        Set entries = componentMap.entrySet();
        for (Iterator iter = entries.iterator(); iter.hasNext();) {
            Map.Entry next = (Map.Entry) iter.next();
            INestedComponent component = (INestedComponent)next.getValue();
            
            component.deactivate(newActive.componentMap.get(next.getKey()));
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
