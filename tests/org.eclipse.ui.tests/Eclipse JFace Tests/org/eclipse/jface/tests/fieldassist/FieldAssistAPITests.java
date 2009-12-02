/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.tests.fieldassist;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;

public class FieldAssistAPITests extends AbstractFieldAssistTestCase {

	public void testSimpleContentProposal() {
		String content = "Name";
		String label = "LabelForName";
		String description = "Description";
		IContentProposal proposal = new ContentProposal(content);
		assertEquals("1.0", content, proposal.getContent());
		assertEquals("1.1", content, proposal.getLabel());
		assertNull("1.2", proposal.getDescription());
		assertEquals("1.3", content.length(), proposal.getCursorPosition());
		proposal = new ContentProposal(content, description);
		assertEquals("2.0", content, proposal.getContent());
		assertEquals("2.1", content, proposal.getLabel());
		assertEquals("2.2", description, proposal.getDescription());
		assertEquals("2.3", content.length(), proposal.getCursorPosition());
		proposal = new ContentProposal(content, label, description);
		assertEquals("3.0", content, proposal.getContent());
		assertEquals("3.1", label, proposal.getLabel());
		assertEquals("3.2", description, proposal.getDescription());
		assertEquals("3.3", content.length(), proposal.getCursorPosition());
		proposal = new ContentProposal(content, label, description, 3);
		assertEquals("3.0", content, proposal.getContent());
		assertEquals("3.1", label, proposal.getLabel());
		assertEquals("3.2", description, proposal.getDescription());
		assertEquals("3.3", 3, proposal.getCursorPosition());
		try {
			proposal = new ContentProposal(content, label, description, 100);
			fail("4.0");
		} catch (IllegalArgumentException e) {
			// expected
		}


	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.fieldassist.AbstractFieldAssistTestCase#createFieldAssistWindow()
	 */
	protected AbstractFieldAssistWindow createFieldAssistWindow() {
		return new TextFieldAssistWindow();
	}
}
