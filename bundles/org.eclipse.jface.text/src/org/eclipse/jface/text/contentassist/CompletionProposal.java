/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.contentassist;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;


/**
 * The standard implementation of the <code>ICompletionProposal</code> interface.
 */
public final class CompletionProposal implements ICompletionProposal {

	/** The string to be displayed in the completion proposal popup. */
	private final String fDisplayString;
	/** The replacement string. */
	private final String fReplacementString;
	/** The replacement offset. */
	private final int fReplacementOffset;
	/** The replacement length. */
	private final int fReplacementLength;
	/** The cursor position after this proposal has been applied. */
	private final int fCursorPosition;
	/** The image to be displayed in the completion proposal popup. */
	private final Image fImage;
	/** The context information of this proposal. */
	private final IContextInformation fContextInformation;
	/** The additional info of this proposal. */
	private final String fAdditionalProposalInfo;

	/**
	 * Creates a new completion proposal based on the provided information. The replacement string is
	 * considered being the display string too. All remaining fields are set to <code>null</code>.
	 *
	 * @param replacementString the actual string to be inserted into the document
	 * @param replacementOffset the offset of the text to be replaced
	 * @param replacementLength the length of the text to be replaced
	 * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
	 */
	public CompletionProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition) {
		this(replacementString, replacementOffset, replacementLength, cursorPosition, null, null, null, null);
	}

	/**
	 * Creates a new completion proposal. All fields are initialized based on the provided information.
	 *
	 * @param replacementString the actual string to be inserted into the document
	 * @param replacementOffset the offset of the text to be replaced
	 * @param replacementLength the length of the text to be replaced
	 * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
	 * @param image the image to display for this proposal
	 * @param displayString the string to be displayed for the proposal
	 * @param contextInformation the context information associated with this proposal
	 * @param additionalProposalInfo the additional information associated with this proposal
	 */
	public CompletionProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition, Image image, String displayString, IContextInformation contextInformation, String additionalProposalInfo) {
		Assert.isNotNull(replacementString);
		Assert.isTrue(replacementOffset >= 0);
		Assert.isTrue(replacementLength >= 0);
		Assert.isTrue(cursorPosition >= 0);

		fReplacementString= replacementString;
		fReplacementOffset= replacementOffset;
		fReplacementLength= replacementLength;
		fCursorPosition= cursorPosition;
		fImage= image;
		fDisplayString= displayString;
		fContextInformation= contextInformation;
		fAdditionalProposalInfo= additionalProposalInfo;
	}

	@Override
	public void apply(IDocument document) {
		try {
			document.replace(fReplacementOffset, fReplacementLength, fReplacementString);
		} catch (BadLocationException x) {
			// ignore
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		return new Point(fReplacementOffset + fCursorPosition, 0);
	}

	@Override
	public IContextInformation getContextInformation() {
		return fContextInformation;
	}

	@Override
	public Image getImage() {
		return fImage;
	}

	@Override
	public String getDisplayString() {
		if (fDisplayString != null) {
			return fDisplayString;
		}
		return fReplacementString;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return fAdditionalProposalInfo;
	}
}
