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
package org.eclipse.debug.internal.ui.views.memory;

/**
 * Represents a view pane in the Memory View.  The view pane allows
 * cliens to add and remove renderings.
 * @since 3.1
 */
public interface IRenderingViewPane extends IMemoryViewPane{
	
	/**
	 * Add the given rendering to the view pane.
	 * @param rendering to add
	 */
	public void addMemoryRendering(IMemoryRendering rendering);
	
	/**
	 * Remove the given rendering from the view pane.
	 * @param rendering rendering to remove
	 */
	public void removeMemoryRendering(IMemoryRendering rendering);

}
