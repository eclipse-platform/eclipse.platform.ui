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
package org.eclipse.jface.text.link;

import java.util.Arrays;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * LinkedPosition with added completion proposals.
 * <p>
 * Clients may instantiate or extend this class.
 * </p>
 * 
 * @since 3.0
 */
public class ProposalPosition extends LinkedPosition {

	/**
	 * The proposals
	 */
	private ICompletionProposal[] fProposals;

	/**
	 * Creates a new instance.
	 * 
	 * @param document the document
	 * @param offset the offset of the position
	 * @param length the length of the position
	 * @param sequence the iteration sequence rank
	 * @param proposals the proposals to be shown when entering this position
	 */
	public ProposalPosition(IDocument document, int offset, int length, int sequence, ICompletionProposal[] proposals) {
		super(document, offset, length, sequence);
		fProposals= proposals;
	}
	
	/**
	 * Creates a new instance, with no sequence number.
	 * 
	 * @param document the document
	 * @param offset the offset of the position
	 * @param length the length of the position
	 * @param proposals the proposals to be shown when entering this position
	 */
	public ProposalPosition(IDocument document, int offset, int length, ICompletionProposal[] proposals) {
		super(document, offset, length, LinkedPositionGroup.NO_STOP);
		fProposals= proposals;
	}
	
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof ProposalPosition) {
			if (super.equals(o)) {
				return Arrays.equals(fProposals, ((ProposalPosition)o).fProposals);
			}
		}
		return false;
	}

	/**
	 * Returns the proposals attached to this position.
	 * 
	 * @return an array of choices, including the initial one. Callers must not
	 *         modify it.
	 */
	public ICompletionProposal[] getChoices() {
		return fProposals;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.link.LinkedPosition#hashCode()
	 */
	public int hashCode() {
		return super.hashCode() | (fProposals == null ? 0 : fProposals.hashCode());
	}
}
