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

import org.eclipse.core.resources.IFolder;
import org.eclipse.ui.internal.ide.dialogs.UIResourceFilterDescription;
import org.eclipse.ui.internal.ide.undo.ContainerDescription;

/**
 * A CreateGroupOperation represents an undoable operation for creating a
 * group in the workspace. If a link location is specified, the group is
 * considered to be linked to the specified location. If a link location is not
 * specified, the group will be created in the location specified by the
 * handle, and the entire containment path of the group will be created if it
 * does not exist. Clients may call the public API from a background thread.
 * <p>
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 * @since 3.6
 */
public class CreateGroupOperation extends AbstractCreateResourcesOperation {

	/**
	 * Create a CreateFolderOperation
	 * 
	 * @param groupHandle
	 *            the group to be created
	 * @param label
	 *            the label of the operation
	 */
	public CreateGroupOperation(IFolder groupHandle, String label) {
		this(null, null, label);
	}
	/**
	 * Create a CreateGroupOperation
	 * 
	 * @param groupHandle
	 *            the folder to be created
	 * @param filterList
	 *            the list of resource filters
	 * @param label
	 *            the label of the operation
	 * @since 3.6
	 */
	public CreateGroupOperation(IFolder groupHandle,
			UIResourceFilterDescription[] filterList, String label) {
		super(null, label);
		ContainerDescription containerDescription = ContainerDescription
		.fromGroupContainer(groupHandle);
		if (filterList != null)
			containerDescription.getFirstLeafFolder().setFilters(filterList);
		setResourceDescriptions(new ResourceDescription[] { containerDescription });
	}
}
