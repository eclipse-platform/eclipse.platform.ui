/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Serge Beauchamp (Freescale Semiconductor) - filter support
 *******************************************************************************/

package org.eclipse.ui.ide.undo;

import java.net.URI;

import org.eclipse.core.resources.IResourceFilter;
import org.eclipse.core.resources.IFolder;
import org.eclipse.ui.internal.ide.undo.ContainerDescription;

/**
 * A CreateFolderOperation represents an undoable operation for creating a
 * folder in the workspace. If a link location is specified, the folder is
 * considered to be linked to the specified location. If a link location is not
 * specified, the folder will be created in the location specified by the
 * handle, and the entire containment path of the folder will be created if it
 * does not exist. The folder should not already exist, and the existence of the
 * containment path should not be changed between the time this operation is
 * created and the time it is executed.
 * 
 * Clients may call the public API from a background thread.
 * 
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * 
 * @since 3.3
 * 
 */
public class CreateFolderOperation extends AbstractCreateResourcesOperation {

	public CreateFolderOperation(IFolder folderHandle, URI linkLocation,
			String label) {
		this(folderHandle, linkLocation, null, label);
	}

	/**
	 * Create a CreateFolderOperation
	 * 
	 * @param folderHandle
	 *            the folder to be created
	 * @param linkLocation
	 *            the location of the folder if it is to be linked
	 * @param label
	 *            the label of the operation
	 * @param filterList
	 *            The filters to apply to the created folder
	 * @since 3.6
	 */
	public CreateFolderOperation(IFolder folderHandle, URI linkLocation,
			IResourceFilter[] filterList, String label) {
		super(null, label);
		ContainerDescription containerDescription = ContainerDescription
				.fromContainer(folderHandle);
		if (linkLocation != null) {
			containerDescription.getFirstLeafFolder().setLocation(linkLocation);
		}
		if (filterList != null)
			containerDescription.getFirstLeafFolder().setFilters(filterList);
		setResourceDescriptions(new ResourceDescription[] { containerDescription });
	}
}
