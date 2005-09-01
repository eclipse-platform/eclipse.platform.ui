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
package org.eclipse.ui.internal.part.services;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.internal.components.Assert;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.part.Part;
import org.eclipse.ui.internal.part.multiplexer.INestedComponent;
import org.eclipse.ui.internal.part.multiplexer.ISharedContext;

public class ChildKeyBindingService implements IKeyBindingService, INestedComponent {

    private String[] scopes = new String[0];
    private boolean active = false;
    private IKeyBindingService parent;
    private ArrayList actions = new ArrayList();
    
    /**
     * Component constructor. Do not invoke directly.
     */
    public ChildKeyBindingService(ISharedContext shared) throws ComponentException {
        Assert.isNotNull(shared);
        IServiceProvider sharedContainer = shared.getSharedComponents();
        
        this.parent = (IKeyBindingService)sharedContainer.getService(IKeyBindingService.class);
    }
    
    public String[] getScopes() {
        return scopes;
    }

    public void registerAction(IAction action) {
        actions.add(action);
        
        if (active) {
            parent.registerAction(action);
        }
    }

    public void setScopes(String[] scopes) {
        this.scopes = scopes;
        
        if (active) {
            parent.setScopes(scopes);
        }
    }

    public void unregisterAction(IAction action) {
        actions.remove(action);
        
        if (active) {
            parent.unregisterAction(action);
        }
    }

    public void activate(Part partBeingActivated) {
        for (Iterator iter = actions.iterator(); iter.hasNext();) {
            IAction next = (IAction) iter.next();
            
            parent.registerAction(next);
        }
        parent.setScopes(scopes);
        active = true;
    }
    
    public void deactivate(Object newActive) {
        for (Iterator iter = actions.iterator(); iter.hasNext();) {
            IAction next = (IAction) iter.next();
            
            parent.unregisterAction(next);
        }
        active = false;
    }
}
