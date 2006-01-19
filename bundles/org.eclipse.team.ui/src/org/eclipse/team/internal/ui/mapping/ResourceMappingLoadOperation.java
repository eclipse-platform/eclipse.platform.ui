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
package org.eclipse.team.internal.ui.mapping;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ui.operations.ResourceMappingOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * The steps of an load (replace with version or branch) operation are:
 * <ol>
 * <li>Obtain the selection to be operated on.
 * <li>Determine the projection of the selection onto resources
 * using resource mappings and traversals.
 * 		<ul>
 * 		<li>this will require traversal of remote only
 * 		</ul>
 * <li>Ensure that all affected mappings are known
 *      <ul>
 * 		<li>additional mappings may be included due to resource project
 *      (i.e. many-to-one case).
 *      <li>notify users of additional mappings that will be affected
 *      <li>this list must include locally changed model elements whose
 *      changes will be lost including any that were deleted.
 *      <li>this list could include changed remote elements that 
 *      will be received including additions
 * 		</ul>
 * <li>Perform the replace at the resource level
 * </ol>
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 32
 */
public class ResourceMappingLoadOperation extends ResourceMappingOperation {

	protected ResourceMappingLoadOperation(IWorkbenchPart part, ResourceMapping[] selectedMappings) {
		super(part, selectedMappings);
	}

	protected void execute(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		// TODO Auto-generated method stub

	}

}
