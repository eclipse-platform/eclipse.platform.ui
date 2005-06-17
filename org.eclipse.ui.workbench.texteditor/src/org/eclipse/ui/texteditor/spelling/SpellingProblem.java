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

package org.eclipse.ui.texteditor.spelling;

import org.eclipse.jface.text.contentassist.ICompletionProposal;


/**
 * A spelling problem as reported by the {@link SpellingService} service to the
 * {@link ISpellingProblemCollector}.
 * <p>
 * This class is intended to be subclassed by clients.
 * </p>
 *
 * @see SpellingService
 * @see ISpellingProblemCollector
 * @since 3.1
 */
public abstract class SpellingProblem {

	/**
	 * Returns the offset of the incorrectly spelled region.
	 *
	 * @return the offset of the incorrectly spelled region
	 */
	public abstract int getOffset();

	/**
	 * Returns the length of the incorrectly spelled region.
	 *
	 * @return the length of the incorrectly spelled region
	 */
	public abstract int getLength();

	/**
	 * Returns a localized, human-readable message string which describes the spelling problem.
	 *
	 * @return a localized, human-readable message string which describes the spelling problem
	 */
	public abstract String getMessage();

	/**
	 * Returns the proposals for the incorrectly spelled region.
	 *
	 * @return the proposals for the incorrectly spelled region
	 */
	public abstract ICompletionProposal[] getProposals();
}
