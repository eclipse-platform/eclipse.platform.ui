/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

package org.eclipse.jface.text.templates;


import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.IContextInformation;


/**
 * A position based completion proposal.
 *
 * @since 3.0
 */
final class PositionBasedCompletionProposal implements ICompletionProposal, ICompletionProposalExtension2 {

	/** The string to be displayed in the completion proposal popup */
	private final String fDisplayString;
	/** The replacement string */
	private final String fReplacementString;
	/** The replacement position. */
	private final Position fReplacementPosition;
	/** The cursor position after this proposal has been applied */
	private final int fCursorPosition;
	/** The image to be displayed in the completion proposal popup */
	private final Image fImage;
	/** The context information of this proposal */
	private final IContextInformation fContextInformation;
	/** The additional info of this proposal */
	private final String fAdditionalProposalInfo;

	/**
	 * Creates a new completion proposal based on the provided information.  The replacement string is
	 * considered being the display string too. All remaining fields are set to <code>null</code>.
	 *
	 * @param replacementString the actual string to be inserted into the document
	 * @param replacementPosition the position of the text to be replaced
	 * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
	 */
	public PositionBasedCompletionProposal(String replacementString, Position replacementPosition, int cursorPosition) {
		this(replacementString, replacementPosition, cursorPosition, null, null, null, null);
	}

	/**
	 * Creates a new completion proposal. All fields are initialized based on the provided information.
	 *
	 * @param replacementString the actual string to be inserted into the document
	 * @param replacementPosition the position of the text to be replaced
	 * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
	 * @param image the image to display for this proposal
	 * @param displayString the string to be displayed for the proposal
	 * @param contextInformation the context information associated with this proposal
	 * @param additionalProposalInfo the additional information associated with this proposal
	 */
	public PositionBasedCompletionProposal(String replacementString, Position replacementPosition, int cursorPosition, Image image, String displayString, IContextInformation contextInformation, String additionalProposalInfo) {
		Assert.isNotNull(replacementString);
		Assert.isTrue(replacementPosition != null);

		fReplacementString= replacementString;
		fReplacementPosition= replacementPosition;
		fCursorPosition= cursorPosition;
		fImage= image;
		fDisplayString= displayString;
		fContextInformation= contextInformation;
		fAdditionalProposalInfo= additionalProposalInfo;
	}

	@Override
	public void apply(IDocument document) {
		try {
			document.replace(fReplacementPosition.getOffset(), fReplacementPosition.getLength(), fReplacementString);
		} catch (BadLocationException x) {
			// ignore
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		return new Point(fReplacementPosition.getOffset() + fCursorPosition, 0);
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

	@Override
	public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
		apply(viewer.getDocument());
	}

	@Override
	public void selected(ITextViewer viewer, boolean smartToggle) {
	}

	@Override
	public void unselected(ITextViewer viewer) {
	}

	@Override
	public boolean validate(IDocument document, int offset, DocumentEvent event) {
		try {
			String content= document.get(fReplacementPosition.getOffset(), offset - fReplacementPosition.getOffset());
			if (fReplacementString.startsWith(content)) {
				return true;
			}
		} catch (BadLocationException e) {
			// ignore concurrently modified document
		}
		return false;
	}

}
