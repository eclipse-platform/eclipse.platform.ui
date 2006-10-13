/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.ide.undo;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.ide.undo.ProjectDescription;
import org.eclipse.ui.internal.ide.undo.UndoMessages;

/**
 * A CreateProjectOperation represents an undoable operation for creating a
 * project in the workspace. Clients may call the public API from a background
 * thread.
 * 
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * 
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

	/*
	 * (non-Javadoc)
	 * 
	 * Overridden to return a specific error message for the existence of a case
	 * variant of a project.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#getErrorMessage(org.eclipse.core.runtime.CoreException)
	 */
	protected String getErrorMessage(CoreException e) {
		if (e.getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
			if (resourceDescriptions != null
					&& resourceDescriptions.length == 1) {
				ProjectDescription project = (ProjectDescription) resourceDescriptions[0];
				return NLS
						.bind(
								UndoMessages.CreateProjectOperation_caseVariantExistsError,
								project.getName());
			}
		}
		return super.getErrorMessage(e);
	}
}
