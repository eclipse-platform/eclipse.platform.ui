/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.mapping;

import org.eclipse.core.runtime.Status;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.core.mapping.WorkspaceOperationValidator;

/**
 * Implementation of the {@link IResourceOperationValidator} that returns an
 * <code>OK</code> status for each method.
 * <p>
 * This class may be subclasses by model providers that wish to take part
 * in resource operation validation. Clients that wish to validate
 * operations should use the validator return from 
 * {@link ResourceOperationValidator#getWorkbenchValidator()}
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see IResourceOperationValidator
 * 
 * @since 3.2
 * 
 */
public abstract class ResourceOperationValidator implements IResourceOperationValidator {

	private class ModelStatus extends Status implements IModelStatus {

		private final String modelProviderId;

		private ModelStatus(int severity, String modelProviderId, String message) {
			super(severity, TeamPlugin.ID, 0, message, null);
			this.modelProviderId = modelProviderId;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.model.IModelStatus#getModelProviderId()
		 */
		public String getModelProviderId() {
			return modelProviderId;
		}

	}

	private static IResourceOperationValidator instance;

	/**
	 * Return the workspace validator that can be used by clients to
	 * validate resource operations.
	 * @return the workspace validator 
	 */
	public static IResourceOperationValidator getWorkspaceValidator() {
		if (instance == null)
			instance = new WorkspaceOperationValidator();
		return instance;
	}

	/**
	 * Create an instance of {@link IModelStatus} that indicates the possible
	 * side effects of an operation.
	 * 
	 * @param severity the severity of the side effects
	 * @param modelProviderId the id of the model provider associated with the
	 *            side effect
	 * @param message a human readable message describing the possible side
	 *            effect
	 * @return a status encompassing the severity, model provider id and message
	 */
	protected IModelStatus createStatus(int severity, String modelProviderId,
			String message) {
		return new ModelStatus(severity, modelProviderId, message);
	}

}
