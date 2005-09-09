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
package org.eclipse.jface.text.contentassist;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Computes completions and context information displayed by a content assistant.
 * <p>
 * XXX this API is provisional and may change anytime during the course of 3.2
 * </p>
 * 
 * @since 3.2
 */
public interface ICompletionProposalComputer {

	/**
	 * Returns a list of completion proposals valid at the given invocation context.
	 * 
	 * @param context the context of the content assist invocation
	 * @param monitor a progress monitor to report progress. The monitor is private to this
	 *        invocation, i.e. there is no need for the receiver to spawn a sub monitor.
	 * @return an array of completion proposals (element type: {@link ICompletionProposal})
	 */
	List computeCompletionProposals(TextContentAssistInvocationContext context, IProgressMonitor monitor);

	/**
	 * Returns context information objects valid at the given invocation context.
	 * 
	 * @param context the context of the content assist invocation
	 * @param monitor a progress monitor to report progress. The monitor is private to this
	 *        invocation, i.e. there is no need for the receiver to spawn a sub monitor.
	 * @return an array of context information objects (element type: {@link IContextInformation})
	 */
	List computeContextInformation(TextContentAssistInvocationContext context, IProgressMonitor monitor);
}
