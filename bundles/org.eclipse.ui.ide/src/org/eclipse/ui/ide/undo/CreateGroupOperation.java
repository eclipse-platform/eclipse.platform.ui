/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import org.eclipse.ui.internal.ide.undo.ContainerDescription;

/**
 * A CreateGroupOperation represents an undoable operation for creating a
 * group in the workspace. If a link location is specified, the group is
 * considered to be linked to the specified location. If a link location is not
 * specified, the group will be created in the location specified by the
 * handle, and the entire containment path of the group will be created if it
 * does not exist. Clients may call the public API from a background thread.
 * 
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * 
 * @since 3.6
 * 
 */
public class CreateGroupOperation extends AbstractCreateResourcesOperation {

	/**
	 * Create a CreateFolderOperation
	 * 
	 * @param groupHandle
	 *            the group to be created
	 * @param linkLocation
	 *            the location of the group if it is to be linked
	 * @param label
	 *            the label of the operation
	 */
	public CreateGroupOperation(IFolder groupHandle, String label) {
		super(null, label);
		ContainerDescription containerDescription = ContainerDescription
				.fromGroupContainer(groupHandle);
		setResourceDescriptions(new ResourceDescription[] { containerDescription });
	}
}
