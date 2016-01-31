/*******************************************************************************
 * Copyright (c) 2016 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.tests.templates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.eclipse.jface.text.templates.TemplateVariable;

public class TemplateVariablesWordSelectionTest  {

	private TemplateTranslator fTranslator;

	private DocumentTemplateContext fContext;

	private TemplateContextType fType;

	@Before
	public void setUp()  {
		fTranslator= new TemplateTranslator();

		fType= new TemplateContextType();
		fType.addResolver(new GlobalTemplateVariables.WordSelection());

		fContext= new DocumentTemplateContext(fType, new Document(), 0, 0);
	}

	@Test
	public void testWithoutParameter() throws Exception {
		TemplateBuffer buffer= fTranslator.translate("Selected word is ${word_selection}!");
		fType.resolve(buffer, fContext);

		StringBuffer expected= new StringBuffer();
		expected.append("Selected word is !");
		assertBufferStringAndVariables(expected.toString(), buffer);
	}

	@Test
	public void testWithParameter() throws Exception {
		TemplateBuffer buffer= fTranslator.translate("No selection results in the ${w:word_selection('default')} text.");
		fType.resolve(buffer, fContext);

		StringBuffer expected= new StringBuffer();
		expected.append("No selection results in the default text.");
		assertBufferStringAndVariables(expected.toString(), buffer);
	}

	/**
	 * Ensures that {@link TemplateBuffer#getString()} equals the expected String and that all
	 * {@link TemplateBuffer#getVariables()} are resolved and unambiguous.
	 *
	 * @param expected expected result
	 * @param buffer the template buffer
	 */
	private static void assertBufferStringAndVariables(String expected, TemplateBuffer buffer) {
		assertEquals(expected, buffer.getString());
		for (TemplateVariable v : buffer.getVariables()) {
			assertTrue(v.isResolved());
			assertTrue(v.isUnambiguous());
		}
	}
}
