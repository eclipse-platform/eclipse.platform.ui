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

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.ui.internal.ide.undo.ProjectDescription;

/**
 * A CreateProjectOperation represents an undoable operation for creating a
 * project in the workspace. Clients may call the public API from a background
 * thread.
 * <p>
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 * @since 3.3
 */
public class CreateProjectOperation extends AbstractCreateResourcesOperation {

	/**
	 * Create a CreateProjectOperation
	 * 
	 * @param projectDescription
	 *            the project to be created
	 * @param label
	 *            the label of the operation
	 */
	public CreateProjectOperation(IProjectDescription projectDescription,
			String label) {
		super(new ProjectDescription[] { new ProjectDescription(
				projectDescription) }, label);
	}
}
