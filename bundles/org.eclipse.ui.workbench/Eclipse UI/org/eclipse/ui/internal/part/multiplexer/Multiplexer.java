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
package org.eclipse.ui.internal.part.multiplexer;

import org.eclipse.core.components.Components;
import org.eclipse.core.components.ServiceFactory;
import org.eclipse.core.components.IServiceProvider;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.internal.part.DelegatingServiceFactory;
import org.eclipse.ui.internal.part.IWorkbenchScopeConstants;

/**
 * @since 3.1
 */
public class Multiplexer {
    private MultiplexerChild activePart;
    private IServiceProvider sharedComponents;
    private ServiceFactory context;
    private DelegatingServiceFactory delegatingContext;
    
    public Multiplexer(IServiceProvider sharedComponents) {
        this(sharedComponents, IWorkbenchScopeConstants.SITE_MULTIPLEXER_SCOPE, IWorkbenchScopeConstants.PART_DELEGATOR_SCOPE, 
                IWorkbenchScopeConstants.SITE_SCOPE);
    }
    
    public Multiplexer(IServiceProvider sharedComponents, String inputScope, String outputScope, String parentScope) {
        this.sharedComponents = sharedComponents;
        this.context = Components.getContext(inputScope);
        this.delegatingContext = new DelegatingServiceFactory(Components.getContext(outputScope));
        
        // Check to make sure the shared components provider is in the correct scope
        ServiceFactory parentContext = Components.getContext(parentScope);
        Assert.isTrue(sharedComponents.hasService(parentContext));
    }
    
    /**
     * Returns a nested context for a child of this multiplexer. Each child should have its
     * own NestedContext instance.
     *
     * @return a new NestedContext for use with this multiplexer
     */
    public NestedContext createNested() {
        return new NestedContext(sharedComponents, context);
    }
    
    public void setActive(MultiplexerChild newActive) {
        if (activePart != null) {
            activePart.getContext().deactivate();
        }
        
        this.activePart = newActive;

        if (activePart != null) {
            activePart.getContext().activate();
        }
        
        delegatingContext.setActive(activePart.getPart());
    }

    /**
     * Returns a context that delegates the implementation of every interface
     * to the active part. 
     *
     * @return
     */
    public ServiceFactory getDelegatingContext() {
        return delegatingContext;
    }
    
}
