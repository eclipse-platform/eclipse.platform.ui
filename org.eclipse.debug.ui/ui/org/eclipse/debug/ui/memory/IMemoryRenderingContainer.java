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



/**
 * A memory rendering container is the actual container within
 * a memory rendering site for hosting a memory rendering.  
 * @since 3.1
 */
public interface IMemoryRenderingContainer {
	/**
	 * @return the rendering site hosting this container
	 */
	public IMemoryRenderingSite getMemoryRenderingSite();
	
	/**
	 * @return the id of this container
	 */
	public String getId();
	
	/**
	 * Add the given rendering to the container
	 * When this method is called, the rendering should have been
	 * initialized.
	 * @param rendering to add
	 */
	public void addMemoryRendering(IMemoryRendering rendering);
	
	/**
	 * Remove the given rendering from container
	 * @param rendering rendering to remove
	 */
	public void removeMemoryRendering(IMemoryRendering rendering);
	
	/**
	 * @return all renderings currently hosted by this container
	 */
	public IMemoryRendering[] getRenderings();
	
	/**
	 * @return the active rendering from this container.
	 */
	public IMemoryRendering getActiveRendering();
	
	/**
	 * @return the label for this memory rendering container
	 */
	public String getLabel();
}
