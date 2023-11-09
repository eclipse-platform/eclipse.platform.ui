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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.junit.Test;

public class FieldAssistAPITests extends AbstractFieldAssistTestCase {

	private final String description = "Description";
	private final String label = "LabelForName";
	private final String content = "Name";
	private IContentProposal proposal;

	@Test
	public void testSimpleContentProposal() {
		proposal = new ContentProposal(content);
		assertEquals("1.0", content, proposal.getContent());
		assertEquals("1.1", content, proposal.getLabel());
		assertNull("1.2", proposal.getDescription());
		assertEquals("1.3", content.length(), proposal.getCursorPosition());
	}

	@Test
	public void testContentProposalWithCursor() {
		proposal = new ContentProposal(content, label, description, 3);
		assertEquals("3.0", content, proposal.getContent());
		assertEquals("3.1", label, proposal.getLabel());
		assertEquals("3.2", description, proposal.getDescription());
		assertEquals("3.3", 3, proposal.getCursorPosition());
	}

	@Test
	public void testContentProposalWithLabel() {
		proposal = new ContentProposal(content, label, description);
		assertEquals("3.0", content, proposal.getContent());
		assertEquals("3.1", label, proposal.getLabel());
		assertEquals("3.2", description, proposal.getDescription());
		assertEquals("3.3", content.length(), proposal.getCursorPosition());
	}

	@Test
	public void testContentProposalWithDescription() {
		proposal = new ContentProposal(content, description);
		assertEquals("2.0", content, proposal.getContent());
		assertEquals("2.1", content, proposal.getLabel());
		assertEquals("2.2", description, proposal.getDescription());
		assertEquals("2.3", content.length(), proposal.getCursorPosition());
	}

	public void testInitializationWithInvalidCursor() {
		try {
			proposal = new ContentProposal(content, label, description, 100);
			fail("4.0");
		} catch (IllegalArgumentException e) {
			assertNull("It is expected to be null", proposal);
		}
	}

	@Override
	protected AbstractFieldAssistWindow createFieldAssistWindow() {
		return new TextFieldAssistWindow();
	}
}
