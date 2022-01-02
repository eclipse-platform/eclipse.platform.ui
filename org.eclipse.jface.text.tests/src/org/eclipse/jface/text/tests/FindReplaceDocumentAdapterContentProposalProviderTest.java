/*******************************************************************************
 * Copyright (c) 2022 Thomas Wolf <thomas.wolf@paranor.ch> and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import org.eclipse.jface.fieldassist.IContentProposal;

import org.eclipse.jface.text.FindReplaceDocumentAdapterContentProposalProvider;

public class FindReplaceDocumentAdapterContentProposalProviderTest {

	private FindReplaceDocumentAdapterContentProposalProvider provider= new FindReplaceDocumentAdapterContentProposalProvider(true);

	private void assertProposal(IContentProposal[] proposals, String prefix, String replacement) {
		IContentProposal match= null;
		for (IContentProposal p : proposals) {
			if (p.getLabel().startsWith(prefix)) {
				match= p;
				break;
			}
		}
		assertNotNull("No proposal for " + prefix + " found", match);
		assertEquals("Unexpected replacement", replacement, match.getContent());
	}

	@Test
	public void testEmptyTextProposal() {
		assertProposal(provider.getProposals("", 0), "\\r", "\\r");
	}

	@Test
	public void testNonEmptyProposal() {
		assertProposal(provider.getProposals("text", 3), "\\r", "\\r");
	}

	@Test
	public void testBackslashOddProposal() {
		assertProposal(provider.getProposals("te\\xt", 3), "\\r", "r");
		assertProposal(provider.getProposals("te\\\\\\xt", 5), "\\r", "r");
	}

	@Test
	public void testBackslashEvenProposal() {
		assertProposal(provider.getProposals("te\\\\xt", 4), "\\r", "\\r");
		assertProposal(provider.getProposals("te\\\\\\xt", 4), "\\r", "\\r");
		assertProposal(provider.getProposals("te\\\\\\\\xt", 6), "\\r", "\\r");
	}
}
