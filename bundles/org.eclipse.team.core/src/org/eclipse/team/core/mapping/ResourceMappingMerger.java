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

import org.eclipse.core.runtime.*;


/**
 * Abstract implementation of {@link IResourceMappingMerger}.
 * 
 * <p>
 * Clients may subclass this class.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see IResourceMappingMerger
 * 
 * @since 3.2
 */
public abstract class ResourceMappingMerger implements IResourceMappingMerger {

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceMappingMerger#validateMerge(org.eclipse.team.core.mapping.IMergeContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus validateMerge(IMergeContext mergeContext, IProgressMonitor monitor) {
		return Status.OK_STATUS;
	}
}
