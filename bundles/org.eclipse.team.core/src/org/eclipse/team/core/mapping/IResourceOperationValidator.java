/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.mapping;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.IStatus;

/**
 * Interface for validating operations on resources.
 * If the validator indicates an error for a particular operations,
 * clients may still choose to perform the operation but should display
 * the returned messages to the user before doing so.
 * <p>
 * Each method will return a status or multi-status indicating the 
 * possible side effects of the operation on higher-level models.
 * The status may be an instance of IModelStatus in which case the
 * client can check the model provider id. If a client
 * is aware of all the models that returned status, it can safely 
 * perform the operation. However, if an unknown model returned a
 * non-OK status, the client should only proceed after prompting
 * the user.
 * <p>
 * This interface is not intended to be subclassed by clients.
 * Model providers that which to take part in resource operation validation
 * should subclass {@link ResourceOperationValidator} and adapt their
 * {@link ModelProvider} to their validator. Clients that wish to validate
 * operations should use the validator returned from 
 * {@link ResourceOperationValidator#getWorkspaceValidator()}
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see ResourceOperationValidator
 * @see IModelStatus
 * 
 * @since 3.2
 */
public interface IResourceOperationValidator {
	
	/**
	 * Validate the proposed changes contained in the given diff
	 * tree. 
	 * @param tree the diff tree containing the proposed changes
	 * @return a status indicating any potential side effects
	 * on the model that provided this validator.
	 */
	public IStatus validateChange(IResourceDiffTree tree);

}
