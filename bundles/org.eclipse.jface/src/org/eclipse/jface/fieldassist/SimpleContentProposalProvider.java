/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
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
 *     Amir Kouchekinia <amir@pyrus.us> - Bug 200762
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 525783
 *******************************************************************************/
package org.eclipse.jface.fieldassist;

import java.util.ArrayList;

/**
 * SimpleContentProposalProvider is a class designed to map a static list of
 * Strings to content proposals.
 *
 * @see IContentProposalProvider
 * @since 3.2
 */
public class SimpleContentProposalProvider implements IContentProposalProvider {

	/*
	 * The proposals provided.
	 */
	private String[] proposals;

	/*
	 * The proposals mapped to IContentProposal. Cached for speed in the case
	 * where filtering is not used.
	 */
	private IContentProposal[] contentProposals;

	/*
	 * Boolean that tracks whether filtering is used.
	 */
	private boolean filterProposals = false;

	/**
	 * Construct a SimpleContentProposalProvider whose content proposals are always
	 * the specified array of Objects.
	 *
	 * @param proposals
	 *            the Strings to be returned whenever proposals are requested.
	 */
	public SimpleContentProposalProvider(String... proposals) {
		super();
		this.proposals = proposals;
	}

	/**
	 * Return an array of Objects representing the valid content proposals for a
	 * field.
	 *
	 * @param contents
	 *            the current contents of the field (only consulted if filtering
	 *            is set to <code>true</code>)
	 * @param position
	 *            the current cursor position within the field (ignored)
	 * @return the array of Objects that represent valid proposals for the field
	 *         given its current content.
	 */
	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		if (filterProposals) {
			ArrayList<ContentProposal> list = new ArrayList<>();
			for (String proposal : proposals) {
				if (proposal.length() >= contents.length()
						&& proposal.substring(0, contents.length())
								.equalsIgnoreCase(contents)) {
					list.add(new ContentProposal(proposal));
				}
			}
			return list.toArray(new IContentProposal[list
					.size()]);
		}
		if (contentProposals == null) {
			contentProposals = new IContentProposal[proposals.length];
			for (int i = 0; i < proposals.length; i++) {
				contentProposals[i] = new ContentProposal(proposals[i]);
			}
		}
		return contentProposals;
	}

	/**
	 * Set the Strings to be used as content proposals.
	 *
	 * @param items
	 *            the Strings to be used as proposals.
	 */
	public void setProposals(String... items) {
		this.proposals = items;
		contentProposals = null;
	}

	/**
	 * Set the boolean that controls whether proposals are filtered according to
	 * the current field content.
	 *
	 * @param filterProposals
	 *            <code>true</code> if the proposals should be filtered to
	 *            show only those that match the current contents of the field,
	 *            and <code>false</code> if the proposals should remain the
	 *            same, ignoring the field content.
	 * @since 3.3
	 */
	public void setFiltering(boolean filterProposals) {
		this.filterProposals = filterProposals;
		// Clear any cached proposals.
		contentProposals = null;
	}
}
