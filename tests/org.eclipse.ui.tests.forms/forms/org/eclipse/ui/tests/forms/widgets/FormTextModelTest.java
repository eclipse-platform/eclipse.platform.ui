/*******************************************************************************
 * Copyright (c) 2018 Ralf M Petter<ralf.petter@gmail.com> and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ralf M Petter<ralf.petter@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.forms.widgets;

import static org.junit.Assert.assertEquals;

import org.eclipse.ui.internal.forms.widgets.FormTextModel;
import org.junit.Test;

/**
 * Tests for FormTextModel
 */
public class FormTextModelTest {

	@Test
	public void testWhitespaceNormalized() {
		FormTextModel formTextModel = new FormTextModel();
		formTextModel.setWhitespaceNormalized(true);
		formTextModel.parseTaggedText("<form><p>   line with   \r\n   <b>  whitespace </b> Test </p></form>", false);
		assertEquals("FormTextModel does not remove whitespace correctly according to the rules",
				"line with whitespace Test" + System.lineSeparator(), formTextModel.getAccessibleText());
	}

	@Test
	public void testWhitespaceNotNormalized() {
		FormTextModel formTextModel = new FormTextModel();
		formTextModel.setWhitespaceNormalized(false);
		formTextModel.parseTaggedText("<form><p>   line with      <b>  whitespace </b> Test </p></form>", false);
		assertEquals("FormTextModel does not preserve whitespace correctly according to the rules",
				"   line with        whitespace  Test " + System.lineSeparator(), formTextModel.getAccessibleText());
	}

	@Test
	public void testTextWithAmpersand() {
		FormTextModel formTextModel = new FormTextModel();
		formTextModel.parseTaggedText("<form>Foo &Bar</form>", false);
		assertEquals("Foo &Bar" + System.lineSeparator(), formTextModel.getAccessibleText());
	}

	// Testing special cases for Bug 536693:
	// https://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references#Predefined_entities_in_XML
	private void goParse(String lin, String lout) {
		FormTextModel formTextModel = new FormTextModel();
		formTextModel.parseTaggedText("<form>" + lin + "</form>", false);
		assertEquals(lout + System.lineSeparator(), formTextModel.getAccessibleText());
	}

	@Test
	public void testAmpersandEscapes1() {
		String lin = "the &apos;quick&apos; & brown fox";
		String lout = "the 'quick' & brown fox";
		goParse(lin, lout);
	}

	@Test
	public void testAmpersandEscapes2() {
		String lin = "the &amp;quick&amp; & brown fox";
		String lout = "the &quick& & brown fox";
		goParse(lin, lout);
	}

	@Test
	public void testAmpersandEscapes3() {
		String lin = "the &lt;quick&gt; & brown fox";
		String lout = "the <quick> & brown fox";
		goParse(lin, lout);
	}

	@Test
	public void testAmpersandEscapes4() {
		String lin = "&&quot;&lt;&&apos;&&apos;&gt;&&amp;&";
		String lout = "&\"<&'&'>&&&";
		goParse(lin, lout);
	}

	@Test
	public void testAmpersandEscapes5() {
		String lin = "the &apos;quick&quot; & brown fox";
		String lout = "the 'quick\" & brown fox";
		goParse(lin, lout);
	}

}
