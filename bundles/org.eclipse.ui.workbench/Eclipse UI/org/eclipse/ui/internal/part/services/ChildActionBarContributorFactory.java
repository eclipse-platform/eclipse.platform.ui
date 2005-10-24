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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.IDisposable;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.part.Part;
import org.eclipse.ui.internal.part.components.services.IActionBarContributor;
import org.eclipse.ui.internal.part.components.services.IActionBarContributorFactory;
import org.eclipse.ui.internal.part.components.services.IPartDescriptor;
import org.eclipse.ui.internal.part.multiplexer.INestedComponent;
import org.eclipse.ui.internal.part.multiplexer.ISharedContext;

public class ChildActionBarContributorFactory implements INestedComponent, IActionBarContributorFactory, IDisposable {
    
    private IActionBarContributorFactory parent;
    private Map activeBars = new HashMap();
    private boolean active = false;
    private IActionBarContributor defaultContributor;
    
    private final class PartMap {
        PartMap(IActionBarContributor contributor, Part part) {
            this.contributor = contributor;
            this.part = part;
        }
        
        IActionBarContributor contributor;
        Part part;
    }
    
    public ChildActionBarContributorFactory(IPartDescriptor descr, ISharedContext shared) throws ComponentException {
        IServiceProvider sharedContainer = shared.getSharedComponents();
        this.parent = (IActionBarContributorFactory)sharedContainer.getService(IActionBarContributorFactory.class);
        this.defaultContributor = parent.getContributor(descr);
    }
    
    public void activate(Part activePart) {        
        for (Iterator iter = activeBars.values().iterator(); iter.hasNext();) {
            PartMap next = (PartMap) iter.next();
            
            parent.activateBars(next.contributor, next.part);
        }
        
        // Note: if the parent and child are of the same type, the parent gets precidence
        parent.activateBars(defaultContributor, activePart);
        
        active = true;
    }
    
    public void deactivate(Object newActive) {
        ChildActionBarContributorFactory factory = null;
        if (newActive instanceof ChildActionBarContributorFactory) {
            factory = (ChildActionBarContributorFactory)newActive;
        }
        
        // Deactivate all the bars that won't be re-activated when the new factory becomes active.
        for (Iterator iter = activeBars.values().iterator(); iter.hasNext();) {
            PartMap next = (PartMap) iter.next();
            
            String nextId = next.contributor.getDescriptor().getId();
            // No need to deactivate the bars if they'll be reused by the new contributor
            if (factory == null 
                    || (factory.activeBars.get(nextId) == null
                    && !factory.defaultContributor.getDescriptor().getId().equals(nextId))) {
                
                parent.deactivateBars(next.contributor);
            }
        }

        String nextId = defaultContributor.getDescriptor().getId();
        // No need to deactivate the bars if they'll be reused by the new contributor
        if (factory == null 
                || (factory.activeBars.get(nextId) == null
                && !factory.defaultContributor.getDescriptor().getId().equals(nextId))) {
            
            parent.deactivateBars(defaultContributor);
        }
        
        active = false;
    }
    
    public void activateBars(IActionBarContributor toActivate, Part actualPart) {
        String key = toActivate.getDescriptor().getId();
        PartMap existing = (PartMap) activeBars.get(key);
        
        if (existing == null) {
            existing = new PartMap(toActivate, actualPart);
            activeBars.put(key, existing);
        } else {
            existing.contributor = toActivate;
            existing.part = actualPart;
        }
        
        if (active) {
            parent.activateBars(toActivate, actualPart);
        }
    }
    
    public void deactivateBars(IActionBarContributor toDeactivate) {
        String key = toDeactivate.getDescriptor().getId();
        PartMap existing = (PartMap) activeBars.get(key);
        
        if (existing == null) {
            return;
        }
        
        activeBars.remove(key);
                
        if (active) {
            parent.deactivateBars(toDeactivate);
        }        
    }
    
    public IActionBarContributor getContributor(IPartDescriptor descriptor) {
        return parent.getContributor(descriptor);
    }

    public void dispose() {
        defaultContributor.dispose();
    }
}
