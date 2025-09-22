/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
package org.eclipse.jface.fieldassist;

import org.eclipse.swt.widgets.Control;

/**
 * AutoCompleteField is a class which attempts to auto-complete a user's
 * keystrokes by activating a popup that filters a list of proposals according
 * to the content typed by the user.
 *
 * @see ContentProposalAdapter
 * @see SimpleContentProposalProvider
 *
 * @since 3.3
 */
public class AutoCompleteField {

	private final SimpleContentProposalProvider proposalProvider;
	private final ContentProposalAdapter adapter;

	/**
	 * Construct an AutoComplete field on the specified control, whose
	 * completions are characterized by the specified array of Strings.
	 *
	 * @param control
	 *            the control for which autocomplete is desired. May not be
	 *            <code>null</code>.
	 * @param controlContentAdapter
	 *            the <code>IControlContentAdapter</code> used to obtain and
	 *            update the control's contents. May not be <code>null</code>.
	 * @param proposals
	 *            the array of Strings representing valid content proposals for
	 *            the field.
	 */
	public AutoCompleteField(Control control, IControlContentAdapter controlContentAdapter, String... proposals) {
		proposalProvider = new SimpleContentProposalProvider(proposals);
		proposalProvider.setFiltering(true);
		adapter = new ContentProposalAdapter(control, controlContentAdapter, proposalProvider, null, null);
		adapter.setPropagateKeys(true);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
	}

	/**
	 * Set the Strings to be used as content proposals.
	 *
	 * @param proposals
	 *            the array of Strings to be used as proposals.
	 */
	public void setProposals(String... proposals) {
		proposalProvider.setProposals(proposals);
	}
}
