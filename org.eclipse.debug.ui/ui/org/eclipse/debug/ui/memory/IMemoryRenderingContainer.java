/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.memory;



/**
 * A memory rendering container is a container within a memory rendering site
 * for hosting a memory renderings.
 * <p>
 * Clients hosting memory renderings may implement this interface.
 * </p>  
 * @since 3.1
 */
public interface IMemoryRenderingContainer {
	/**
	 * Returns the rendering site hosting this container.
	 * 
	 * @return the rendering site hosting this container
	 */
	public IMemoryRenderingSite getMemoryRenderingSite();
	
	/**
	 * Returns the identifier of this container. Identifiers
	 * are unique within a container.
	 * 
	 * @return the identifier of this container
	 */
	public String getId();
	
	/**
	 * Adds the given rendering to this container. A rendering must be
	 * initialized before it is added to a container. This causes
	 * the rendering's control to be created.
	 * 
	 * @param rendering the rendering to add
	 */
	public void addMemoryRendering(IMemoryRendering rendering);
	
	/**
	 * Removes the given rendering from this container. This 
	 * causes the rendering to be disposed.
	 * 
	 * @param rendering the rendering to remove
	 */
	public void removeMemoryRendering(IMemoryRendering rendering);
	
	/**
	 * Returns all renderings currently hosted by this container.
	 *  
	 * @return all renderings currently hosted by this container
	 */
	public IMemoryRendering[] getRenderings();
	
	/**
	 * Returns the active rendering in this container, or <code>null</code>
	 * if none.
	 * 
	 * @return the active rendering in this container, or <code>null</code>
	 * if none
	 */
	public IMemoryRendering getActiveRendering();
	
	/**
	 * Returns the label for this container.
	 * 
	 * @return the label for this container
	 */
	public String getLabel();
}
