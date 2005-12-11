/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.fieldassist;

/**
 * SimpleContentProposalProvider is a class designed to map a static list of
 * Strings to content proposals.
 * <p>
 * This API is considered experimental. It is still evolving during 3.2 and is
 * subject to change. It is being released to obtain feedback from early
 * adopters.
 * 
 * @see IContentProposalProvider
 * @since 3.2
 * 
 */
public class SimpleContentProposalProvider implements IContentProposalProvider {

	/*
	 * The proposals provided.
	 */
	private String[] proposals;

	/*
	 * The proposals mapped to IContentProposal.
	 */
	private IContentProposal[] contentProposals;

	/**
	 * Construct a SimpleContentProposalProvider whose content proposals are
	 * always the specified array of Objects.
	 * 
	 * @param proposals
	 *            the array of Strings to be returned whenever proposals are
	 *            requested.
	 */
	public SimpleContentProposalProvider(String[] proposals) {
		super();
		this.proposals = proposals;
	}

	/**
	 * Return an array of Objects representing the valid content proposals for a
	 * field. Ignore the current contents of the field.
	 * 
	 * @param contents
	 *            the current contents of the field (ignored)
	 * @param position
	 *            the current cursor position within the field (ignored)
	 * @return the array of Objects that represent valid proposals for the field
	 *         given its current content.
	 */
public IContentProposal [] getProposals(String contents, int position) {
		if (contentProposals == null) {
			contentProposals = new IContentProposal[proposals.length];
			for (int i=0; i<proposals.length; i++) {
				final String proposal = proposals[i];
				contentProposals[i] = new IContentProposal() {
					public String getContent() {
						return proposal;
					}
					public String getDescription() {
						return null;
					}
					public String getLabel() {
						return null;
					}
					public int getCursorPosition() {
						return proposal.length();
					}
				};
			}
		}
		return contentProposals;
	}
	/**
	 * Set the Strings to be used as content proposals.
	 * 
	 * @param items
	 *            the array of Strings to be used as proposals.
	 */
	public void setProposals(String[] items) {
		this.proposals = items;
		contentProposals = null;
	}
}
