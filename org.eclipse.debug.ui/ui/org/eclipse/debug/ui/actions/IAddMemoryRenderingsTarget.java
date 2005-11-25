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

package org.eclipse.debug.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * An adpater for "add memory rendering" operation.  The platform provides
 * retargettable Add Memory Rendering Action.  Client implementing this adapter
 * is expected to add the necessary memory blocks and renderings when the action
 * is invoked.
 * 
 * Typically, to add a memory rendering, client needs to do the following:
 * (1)  Create a new memory block
 * (2)  Add the new memory block to the Memory Block Manager. (<code>IMemoryBlockManager</code>)
 * (3)  Create the new rendering from <code>IMemoryRenderingTypeDelegate</code>
 * (4)  Bring the required memory view to the top. (<code>IMemoryRenderingSite</code>)
 * (5)  Find the container from the memory view to host the new memory rendering. (<code>IMemoryRenderingContainer</code>) 
 * (6)  Initialize the new rendering with the appropriate memory block and container.
 * (7)  Add the new rendering to the container. 
 * 
 * TODO:  new api, needs review
 *
 *@since 3.2
 */
public interface IAddMemoryRenderingsTarget {
	/**
	 * Returns whether an add memory rendering operation can be performed from the specified
	 * part and the given selection.
	 * 
	 * @param part the part on which the action has been invoked
	 * @param selection the selection on which the action has been invoked
	 * @param retrieval IMemoryBlockRetrieval element for adding the memory block
	 * @throws CoreException if an error has occurred
	 */
	public boolean canAddMemoryRenderings(IWorkbenchPart part, ISelection selection, IMemoryBlockRetrieval retrieval) throws CoreException;
	
	/**
	 * Returns whether this target will support adding memory renderings from the specified
	 * part.
	 * @param part
	 * @return true if the target wants to support adding memory renderings from the given
	 * part, false otherwise.
	 */
	public boolean supportsAddMemoryRenderings(IWorkbenchPart part);
	
	/**
	 * Perform an add memory rendering operation. Based on the part, selection and retrieval, client
	 * must first create and add the required memory block to the Memory Block Manager.
	 * Once the memory block is added, client can create the specified renderings and add the renderings
	 * to the appropriate memory rendering containers.
	 * 
	 * @param part the part on which the action has been invoked
	 * @param selection the selection on which the action has been invoked
	 * @param retrieval <code>IMemoryBlockRetrieval</code> element to perform the "add memory block" action on
	 * @param renderingTypes renderings to add
	 * @throws CoreException if unable to perform the action 
	 * 
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval
	 * @see org.eclipse.debug.ui.memory.IMemoryRenderingManager
	 * @see org.eclipse.debug.core.IMemoryBlockManager
	 * @see org.eclipse.debug.ui.memory.IMemoryRenderingSite
	 * @see org.eclipse.debug.ui.memory.IMemoryRenderingContainer
	 */
	public void addMemoryRenderings(IWorkbenchPart part, ISelection selection, IMemoryBlockRetrieval retrieval, IMemoryRenderingType[] renderingTypes) throws CoreException;
	
	/**
	 * Return a list of rendering types that can be added based on the workbench part and its selection.
	 * @param part the part asking for the list of rendering types
	 * @param selection current selection from the part
	 * @param retrieval <code>IMemoryBlockRetrieval</code> element to perform the "add memory block" action 
	 * @return a list of rendering types applicable for the current selection, empty list if no applicable
	 * type can be found.
	 */
	public IMemoryRenderingType[] getMemoryRenderingTypes(IWorkbenchPart part, ISelection selection, IMemoryBlockRetrieval retrieval);
}
