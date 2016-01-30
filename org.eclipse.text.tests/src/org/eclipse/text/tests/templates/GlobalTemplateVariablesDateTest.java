/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 486889
 *******************************************************************************/
package org.eclipse.text.tests.templates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.ULocale;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.eclipse.jface.text.templates.TemplateVariable;

public class GlobalTemplateVariablesDateTest  {

	private TemplateTranslator fTranslator;

	private DocumentTemplateContext fContext;

	private TemplateContextType fType;

	@Before
	public void setUp()  {
		fTranslator= new TemplateTranslator();

		fType= new TemplateContextType();
		fType.addResolver(new GlobalTemplateVariables.Date());

		fContext= new DocumentTemplateContext(fType, new Document(), 0, 0);
	}
	
	@Test
	public void testWithoutParameter() throws Exception {
		TemplateBuffer buffer= fTranslator.translate("Today is ${date}!");
		fType.resolve(buffer, fContext);

		StringBuffer expected= new StringBuffer();
		expected.append("Today is ");
		expected.append(DateFormat.getDateInstance().format(new java.util.Date()));
		expected.append("!");
		assertBufferStringAndVariables(expected.toString(), buffer);
	}
	
	@Test
	public void testOneParameter() throws Exception {
		TemplateBuffer buffer= fTranslator.translate("This format ${d:date('dd MMM YYYY')} and not ${p:date('YYYY-MM-dd')}");
		fType.resolve(buffer, fContext);

		StringBuffer expected= new StringBuffer();
		expected.append("This format ");
		expected.append(new SimpleDateFormat("dd MMM YYYY").format(new java.util.Date()));
		expected.append(" and not ");
		expected.append(new SimpleDateFormat("YYYY-MM-dd").format(new java.util.Date()));
		assertBufferStringAndVariables(expected.toString(), buffer);
	}

	@Test
	public void testSimpleLocale() throws Exception {
		TemplateBuffer buffer= fTranslator.translate("From ${d:date('dd MMM YYYY', 'fr')} to ${d}");
		fType.resolve(buffer, fContext);

		StringBuffer expected= new StringBuffer();
		expected.append("From ");
		expected.append(new SimpleDateFormat("dd MMM YYYY", ULocale.FRENCH).format(new java.util.Date()));
		expected.append(" to ");
		expected.append(new SimpleDateFormat("dd MMM YYYY", ULocale.FRENCH).format(new java.util.Date()));
		assertBufferStringAndVariables(expected.toString(), buffer);
	}

	@Test
	public void testComplexLocale() throws Exception {
		TemplateBuffer buffer= fTranslator.translate("France ${d:date('EEEE dd MMMM YYYY', 'fr_FR')} and Germany ${p:date('EEEE dd. MMMM YYYY', 'de_DE')}");
		fType.resolve(buffer, fContext);

		StringBuffer expected= new StringBuffer();
		expected.append("France ");
		expected.append(new SimpleDateFormat("EEEE dd MMMM YYYY", ULocale.FRANCE).format(new java.util.Date()));
		expected.append(" and Germany ");
		expected.append(new SimpleDateFormat("EEEE dd. MMMM YYYY", ULocale.GERMANY).format(new java.util.Date()));
		assertBufferStringAndVariables(expected.toString(), buffer);
	}

	@Test
	public void testInvalidDateFormat() throws Exception {
		TemplateBuffer buffer= fTranslator.translate("Today is ${d:date('invalid')}!");
		fType.resolve(buffer, fContext);

		StringBuffer expected= new StringBuffer();
		expected.append("Today is ");
		expected.append(DateFormat.getDateInstance().format(new java.util.Date()));
		expected.append("!");
		assertBufferStringAndVariables(expected.toString(), buffer);
	}

	@Test
	public void testInvalidLocale() throws Exception {
		TemplateBuffer buffer= fTranslator.translate("Today is ${d:date('YYYY-MM-dd', 'this_invalid_locale')}!");
		fType.resolve(buffer, fContext);

		StringBuffer expected= new StringBuffer();
		expected.append("Today is ");
		expected.append(new SimpleDateFormat("YYYY-MM-dd", new ULocale("this_invalid_locale")).format(new java.util.Date()));
		expected.append("!");
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
