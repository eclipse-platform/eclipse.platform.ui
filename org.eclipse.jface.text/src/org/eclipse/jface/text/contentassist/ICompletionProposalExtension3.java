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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;


/**
 * Extends {@link org.eclipse.jface.text.contentassist.ICompletionProposal}
 * with the following functions:
 * <ul>
 *	<li>provision of a custom information control creator</li>
 *	<li>provide a custom completion text and offset for prefix completion</li>
 * </ul>
 *
 * @since 3.0
 */
public interface ICompletionProposalExtension3 {
	/**
	 * Returns the information control creator of this completion proposal.
	 *
	 * @return the information control creator, or <code>null</code> if no custom control creator is available
	 */
	IInformationControlCreator getInformationControlCreator();

	/**
	 * Returns the string that would be inserted at the position returned from
	 * {@link #getPrefixCompletionStart(IDocument, int)} if this proposal was
	 * applied. If the replacement string cannot be determined,
	 * <code>null</code> may be returned.
	 * <p>
	 * If this interface is not implemented,
	 * {@link ICompletionProposal#getDisplayString()} will be used instead.
	 * </p>
	 *
	 * @param document the document that the receiver applies to
	 * @param completionOffset the offset into <code>document</code> where the
	 *        completion takes place
	 * @return the replacement string or <code>null</code> if it cannot be
	 *         determined
	 */
	CharSequence getPrefixCompletionText(IDocument document, int completionOffset);

	/**
	 * Returns the document offset at which the receiver would insert its
	 * proposal.
	 * <p>
	 * If this interface is not implemented, <code>completionOffset</code> will
	 * be used instead.
	 * </p>
	 *
	 * @param document the document that the receiver applies to
	 * @param completionOffset the offset into <code>document</code> where the
	 *        completion takes place
	 * @return the offset at which the proposal would insert its proposal
	 */
	int getPrefixCompletionStart(IDocument document, int completionOffset);

}
