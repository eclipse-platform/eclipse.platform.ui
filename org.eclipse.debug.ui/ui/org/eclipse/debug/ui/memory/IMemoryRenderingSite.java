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
package org.eclipse.debug.ui.memory;

import org.eclipse.ui.IViewSite;

/**
 * A workbench site that hosts memory renderings and provides
 * synchronization services for memory renderings.
 * <p>
 * A rendering site has an optional synchronization provider at any one time. If a
 * rendering provides synchronization information it should set itself as the synchronization
 * prodiver for its memory rendering site when it is activated. 
 * </p>
 * <p>
 * Clients hosting memory rendering may implement this interface.
 * </p>
 * @since 3.1
 */
public interface IMemoryRenderingSite {

    /**
     * Returns the view site hosting memory renderings for this rendering site.
     * 
     * @return the view site hosting memory renderings for this rendering site
     * 
     * TODO: can we relax the restriction on a view being a container so we 
     * can host in other containers? what do we need from the view? It looks like
     * we can relax this to a workbench part site?
     */
    public IViewSite getViewSite();
        
    /**
     * Returns the syncrhonization serivce for this rendering site
     * or <code>null</code> if none.
     * 
     * @return the syncrhonization serivce for this rendering site or <code>null</code>
     */
    public IMemoryRenderingSynchronizationService getSynchronizationService();
    
    /**
     * Sets the rendering currently providing sychronization information for
     * this rendering site, or <code>null</code> if none.
     * 
     * @param rendering active rendering providing synchronization information or
     *  <code>null</code>
     *  
     *  TODO: should this be moved to the service?
     */
    public void setSynchronizationProvider(IMemoryRendering rendering);
    
    /**
     * Returns the rendering currengly providing synchronization information for
     * this rendering site, or <code>null</code if none.
     * @return rendering providing synchronization information or <code>null</null>
     * 
     * TODO: should this be moved to the service?
     */
    public IMemoryRendering getSynchronizationProvider();
    
    /**
     * Returns all the memory rendering containers within this rendering site.
     * 
     * @return all the memory rendering containers within this rendering site
     */
    public IMemoryRenderingContainer[] getMemoryRenderingContainers();
    
    /**
     * Returns the rendering container with the given id or <code>null</code>
     * if none.
     *
     * @param id identifier of the container being requested
     * @return the rendering container with the given id or <code>null</code>
     * if none
     */
    public IMemoryRenderingContainer getContainer(String id);
    
    
}
