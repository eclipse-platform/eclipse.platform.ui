/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433608
 ******************************************************************************/
package org.eclipse.jface.tests.fieldassist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.junit.jupiter.api.Test;

public class FieldAssistAPITests extends AbstractFieldAssistTestCase {

	private final String description = "Description";
	private final String label = "LabelForName";
	private final String content = "Name";
	private IContentProposal proposal;

	@Test
	public void testSimpleContentProposal() {
		proposal = new ContentProposal(content);
		assertEquals(content, proposal.getContent(), "1.0");
		assertEquals(content, proposal.getLabel(), "1.1");
		assertNull(proposal.getDescription(), "1.2");
		assertEquals(content.length(), proposal.getCursorPosition(), "1.3");
	}

	@Test
	public void testContentProposalWithCursor() {
		proposal = new ContentProposal(content, label, description, 3);
		assertEquals(content, proposal.getContent(), "3.0");
		assertEquals(label, proposal.getLabel(), "3.1");
		assertEquals(description, proposal.getDescription(), "3.2");
		assertEquals(3, proposal.getCursorPosition(), "3.3");
	}

	@Test
	public void testContentProposalWithLabel() {
		proposal = new ContentProposal(content, label, description);
		assertEquals(content, proposal.getContent(), "3.0");
		assertEquals(label, proposal.getLabel(), "3.1");
		assertEquals(description, proposal.getDescription(), "3.2");
		assertEquals(content.length(), proposal.getCursorPosition(), "3.3");
	}

	@Test
	public void testContentProposalWithDescription() {
		proposal = new ContentProposal(content, description);
		assertEquals(content, proposal.getContent(), "2.0");
		assertEquals(content, proposal.getLabel(), "2.1");
		assertEquals(description, proposal.getDescription(), "2.2");
		assertEquals(content.length(), proposal.getCursorPosition(), "2.3");
	}

	@Test
	public void testInitializationWithInvalidCursor() {
		try {
			proposal = new ContentProposal(content, label, description, 100);
			fail("4.0");
		} catch (IllegalArgumentException e) {
			assertNull(proposal, "It is expected to be null");
		}
	}

	@Override
	protected AbstractFieldAssistWindow createFieldAssistWindow() {
		return new TextFieldAssistWindow();
	}
}
