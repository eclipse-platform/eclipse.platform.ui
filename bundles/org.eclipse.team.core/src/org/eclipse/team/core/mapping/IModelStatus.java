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

import org.eclipse.core.runtime.IStatus;

/**
 * A status returned by a model from the resource operation validator.
 * The severity indicates the severity of the possible side effects
 * of the operation. Any severity other than <code>OK</code> should be
 * shown to the user. The message should be a human readable message that
 * will allow the user to make a decision as to whether to continue with the 
 * operation. The model provider id should indicate which model is flagging the
 * the possible side effects.
 * 
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
public interface IModelStatus extends IStatus {

	/**
	 * Return the id of the model provider from which this status 
	 * originated.
	 * @return the id of the model provider from which this status 
	 * originated
	 */
	String getModelProviderId();
}
