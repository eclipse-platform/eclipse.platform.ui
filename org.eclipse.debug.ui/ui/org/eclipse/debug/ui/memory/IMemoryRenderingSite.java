/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.memory;

import org.eclipse.ui.IViewSite;

/**
 * A workbench site that hosts memory renderings and provides synchronization services
 * for memory renderings.
 * <p>
 * A rendering site has an optional synchronization provider at any one time. If a
 * rendering provides synchronization information it should set itself as the synchronization
 * prodiver for its memory rendering site when it is activated. 
 * </p>
 * 
 * @since 3.1
 */
public interface IMemoryRenderingSite {

    /**
     * Returns the view site hosting memory renderings for this rendering site.
     * 
     * @return the view site hosting memory renderings for this rendering site
     */
    public IViewSite getViewSite();
        
    /**
     * Returns the syncrhonization serivce for this rendering site.
     * 
     * @return the syncrhonization serivce for this rendering site
     */
    public IMemoryRenderingSynchronizationService getSynchronizationService();
    
    /**
     * Sets the rendering currently providing sychronization information for
     * this rendering site, or <code>null</code> if none.
     * 
     * @param rendering active rendering providing synchronization information or
     *  <code>null</code>
     */
    public void setSynchronizationProvider(IMemoryRendering rendering);
}
