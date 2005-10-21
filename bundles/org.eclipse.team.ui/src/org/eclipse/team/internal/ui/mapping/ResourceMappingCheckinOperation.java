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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ui.mapping.IResourceMappingOperationInput;
import org.eclipse.team.ui.mapping.ResourceMappingOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * The check-in operation needs to delegate the check-in to the
 * repository tooling somehow. The things we can do up front is to 
 * adjust the input using the participants. TO do this, we need the
 * resource mapping context of the check-in.
 * <p>
 * Another thing we could potentially do is display a model diff
 * tree that shows the outgoing changes. This would need to be
 * integrated with the repository tooling check-in UI artifacts
 * (e.g. commit comment).
 * 
 * The steps of a check-in operation are:
 * <ol>
 * <li>Obtain the selection to be operated on.
 * <li>Determine the projection of the selection onto resources
 * using resource mappings and traversals.
 * 		<ul>
 * 		<li>this will require traversals from ancestor
 *      <li>may require traversals from remote for conflict notification
 * 		</ul>
 * <li>Ensure that all affected mappings are known
 *      <ul>
 * 		<li>additional mappings may be included due to resource project
 *      (i.e. many-to-one case).
 *      <li>notify users of additional mappings that will be affected.
 *      <li>this list must include locally modified model elements including
 *      deletions.
 *      <li>the list could also indicate remote changes that will cause the commit
 *      to fail
 * 		</ul>
 * <li>Prompt the user for additional information required by check-in 
 * (e.g. comment)
 *      <ul>
 * 		<li>This may show model elements being included in the commit
 *      <li>Could also show sync state and support merging
 *      <li>Could if support exclusion of model elements? This would be complicated.
 * 		</ul>
 * <li>Perform the check-in on the resources
 * </ol>
 * <p>
 * Special case involving sub-file elements. 
 * <ul>
 * <li>Level 1: prompt to indicate that the check-in will include
 * additional elements (Need a way to detect this is the case).
 * <li>Level 2: prompt to display the additional elements. A participant
 * can provide the elements but they need to be able to include deletions.
 * Display could be a flat list that makes use of Generic Navigator type API.
 * <li>Level 3: Display diff tree and highlight the additional elements.
 * Need to be able to identify and highlight the additional elements.
 * </ul>
 * One way to handle this would be to delegate the prompt to the model provider.
 * That is, collect the resource mappings involved and ask the model provider
 * to indicate to the user what additional resource mappings will be operated
 * on and return an adjusted list. 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public abstract class ResourceMappingCheckinOperation extends ResourceMappingOperation {

	protected ResourceMappingCheckinOperation(IWorkbenchPart part, IResourceMappingOperationInput input) {
		super(part, input);
	}

	protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		// TODO Auto-generated method stub

	}

}
