/*******************************************************************************
 * Copyright (c) 2016 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.jface.text.templates.TemplateVariableType;

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

		StringBuilder expected= new StringBuilder();
		expected.append("Selected word is !");
		assertBufferStringAndVariables(expected.toString(), buffer);
	}

	@Test
	public void testWithParameter() throws Exception {
		TemplateBuffer buffer= fTranslator.translate("No selection results in the ${w:word_selection('default')} text.");
		fType.resolve(buffer, fContext);

		StringBuilder expected= new StringBuilder();
		expected.append("No selection results in the default text.");
		assertBufferStringAndVariables(expected.toString(), buffer);
	}

	@Test
	public void testMulti() throws Exception {
		TemplateBuffer buffer = new TemplateTranslator() {

			@Override
			protected TemplateVariable createVariable(TemplateVariableType type, String name, int[] offsets) {
				if ("petType".equals(name)) {
					String[] pets = new String[] { "cat", "dog", "other" };
					TemplateVariable variable = new TemplateVariable(type.getName(), name, pets, offsets);
					variable.setUnambiguous(true);
					return variable;
				}
				return super.createVariable(type, name, offsets);
			}
		}.translate("My favorite pet is a ${petType:String}, I love my ${petType}.");
		assertEquals("My favorite pet is a cat, I love my cat.", buffer.getString());
		fType.resolve(buffer, fContext);

		StringBuilder expected = new StringBuilder();
		expected.append("My favorite pet is a cat, I love my cat.");
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
