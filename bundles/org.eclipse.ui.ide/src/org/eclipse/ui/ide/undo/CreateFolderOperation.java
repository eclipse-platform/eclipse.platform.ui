/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.ide.undo;

import java.net.URI;

import org.eclipse.core.resources.IFolder;
import org.eclipse.ui.ide.dialogs.UIResourceFilterDescription;
import org.eclipse.ui.internal.ide.undo.ContainerDescription;

/**
 * A CreateFolderOperation represents an undoable operation for creating a
 * folder in the workspace. If a link location is specified, the folder is
 * considered to be linked to the specified location. If a link location is not
 * specified, the folder will be created in the location specified by the
 * handle, and the entire containment path of the folder will be created if it
 * does not exist.  The folder should not already exist, and the existence 
 * of the containment path should not be changed between the time this operation
 * is created and the time it is executed.
 * <p>
 * Clients may call the public API from a background thread.
 * </p>
 * <p>
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 * @since 3.3
 */
public class CreateFolderOperation extends AbstractCreateResourcesOperation {

	/**
	 * Create a CreateFolderOperation
	 * 
	 * @param folderHandle
	 *            the folder to be created
	 * @param linkLocation
	 *            the location of the folder if it is to be linked
	 * @param label
	 *            the label of the operation
	 */

	public CreateFolderOperation(IFolder folderHandle, URI linkLocation,
			String label) {
		this(folderHandle, linkLocation, false, null, label);
	}

	/**
	 * Create a CreateFolderOperation
	 * 
	 * @param folderHandle
	 *            the folder to be created
	 * @param linkLocation
	 *            the location of the folder if it is to be linked
	 * @param virtual
	 *            Create a virtual folder
	 * @param filterList
	 *            The filters to apply to the created folder
	 * @param label
	 *            the label of the operation
	 * @since 3.6
	 */
	public CreateFolderOperation(IFolder folderHandle, URI linkLocation,
			boolean virtual,
			UIResourceFilterDescription[] filterList, String label) {
		super(null, label);
		ContainerDescription containerDescription = virtual? ContainerDescription
				.fromVirtualFolderContainer(folderHandle):
				ContainerDescription.fromContainer(folderHandle);
		if (linkLocation != null) {
			containerDescription.getFirstLeafFolder().setLocation(linkLocation);
		}
		if (filterList != null)
			containerDescription.getFirstLeafFolder().setFilters(filterList);
		setResourceDescriptions(new ResourceDescription[] { containerDescription });
	}
}
