/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor.spelling;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Default implementation of {@link SpellingProblem}.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p><p>
 * Not yet for public use. API under construction.
 * </p>
 * 
 * @see SpellingProblem
 * @since 3.1
 */
public class DefaultSpellingProblem extends SpellingProblem {

	/** Offset */
	private int fOffset;
	
	/** Length */
	private int fLength;
	
	/** Message */
	private String fMessage;
	
	/** Correction proposals */
	private ICompletionProposal[] fProposals;

	/**
	 * Initialize with the given parameters.
	 * 
	 * @param offset the offset
	 * @param length the length
	 * @param message the message
	 * @param proposals the correction proposals
	 */
	public DefaultSpellingProblem(int offset, int length, String message, ICompletionProposal[] proposals) {
		super();
		fOffset= offset;
		fLength= length;
		fMessage= message;
		fProposals= proposals;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.spelling.SpellingProblem#getOffset()
	 */
	public int getOffset() {
		return fOffset;
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.SpellingProblem#getLength()
	 */
	public int getLength() {
		return fLength;
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.SpellingProblem#getMessage()
	 */
	public String getMessage() {
		return fMessage;
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.SpellingProblem#getProposals()
	 */
	public ICompletionProposal[] getProposals() {
		return fProposals;
	}
}
