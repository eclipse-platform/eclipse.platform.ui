/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
		fProposals= copy(proposals);
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
		fProposals= copy(proposals);
	}

	/*
	 * @since 3.1
	 */
	private ICompletionProposal[] copy(ICompletionProposal[] proposals) {
		if (proposals != null) {
			ICompletionProposal[] copy= new ICompletionProposal[proposals.length];
			System.arraycopy(proposals, 0, copy, 0, proposals.length);
			return copy;
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ProposalPosition) {
			if (super.equals(o)) {
				return Arrays.equals(fProposals, ((ProposalPosition)o).fProposals);
			}
		}
		return false;
	}

	/**
	 * Returns the proposals attached to this position. The returned array is owned by
	 * this <code>ProposalPosition</code> and may not be modified by clients.
	 *
	 * @return an array of choices, including the initial one. Callers must not
	 *         modify it.
	 */
	public ICompletionProposal[] getChoices() {
		return fProposals;
	}

	@Override
	public int hashCode() {
		return super.hashCode() | (fProposals == null ? 0 : fProposals.hashCode());
	}
}
