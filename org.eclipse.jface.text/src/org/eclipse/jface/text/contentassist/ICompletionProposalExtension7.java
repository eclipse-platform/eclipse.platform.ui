/*******************************************************************************
 * Copyright (c) 2015, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.contentassist;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;

import org.eclipse.jface.text.IDocument;

/**
 * Extends the functionality of {@link org.eclipse.jface.text.contentassist.ICompletionProposal}
 * with the following function:
 * <ul>
 * <li>Emphasize ranges in the styled display string of the proposal that match the token at the
 * current caret offset.</li>
 * </ul>
 * <p>
 * <strong>Note:</strong> {@link ICompletionProposal#getDisplayString()} still needs to be correctly
 * implemented as this method might be ignored in case of uninstalled owner draw support.
 * </p>
 *
 * @since 3.11
 */
public interface ICompletionProposalExtension7 {
	/**
	 * Returns the styled display string for this proposal with emphasized ranges that match the
	 * token at the current caret offset. This can for example be used to emphasize prefix, camel
	 * case or substring matches in the display string. Clients can emphasize the matches using any
	 * font style. It is encouraged to use the bold font as the style.
	 *
	 * @param document the document where content assist is invoked
	 * @param offset the offset in the document at current caret location
	 * @param boldStylerProvider to provide a {@link Styler} that applies bold style
	 *
	 * @return the styled display string for this proposal with emphasized ranges matching the token
	 *         at the given offset
	 */
	StyledString getStyledDisplayString(IDocument document, int offset, BoldStylerProvider boldStylerProvider);
}
