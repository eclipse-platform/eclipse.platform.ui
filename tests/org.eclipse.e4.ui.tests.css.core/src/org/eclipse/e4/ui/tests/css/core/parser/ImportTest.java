/*******************************************************************************
 * Copyright (c) 2014 Stefan Winkler and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Winkler <stefan@winklerweb.net> - initial API and implementation
 *   Lars Vogel <Lars.Vogel@gmail.com> - Bug 430468
 *   Daniel Raap <raap@subshell.com> - Bug 511836
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.dom.CSSStyleRuleImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CSSStyleSheetImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CssRule;
import org.eclipse.e4.ui.css.core.impl.engine.CSSEngineImpl;
import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.eclipse.e4.ui.tests.css.core.util.TestElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.css.CSSStyleDeclaration;

public class ImportTest {

	private CSSEngine engine;

	@BeforeEach
	public void setUp() {
		engine = ParserTestUtil.createEngine();
	}

	/**
	 * Test case for Bug 430052 - [CSS] Imported rules cannot be overridden
	 */
	@Test
	void testOverrideImportedRule() throws Exception {
		String importedCss = """
			.ClassAlpha {
			     property: value;
			  }
			  .ClassBeta {
			     property: value1;
			  }""";

		File importedFile = createTempCssFile(importedCss);

		String importingCss = createImport(importedFile) //
				+ "  .ClassBeta {\n" //
				+ "     property: value2;\n" //
				+ "  }";

		// we need a file URL so that the import can be resolved
		String importedFolderPath = importedFile.getParent();
		String importingUrl = "file:///" + importedFolderPath + "/importing.css";

		parseStyleSheet(importingUrl, importingCss);

		TestElement buttonAlpha = new TestElement("Button", engine);
		buttonAlpha.setClass("ClassAlpha");

		TestElement buttonBeta = new TestElement("Button", engine);
		buttonBeta.setClass("ClassBeta");

		CSSStyleDeclaration styleAlpha = engine.computeStyle(buttonAlpha, null);
		CSSStyleDeclaration styleBeta = engine.computeStyle(buttonBeta, null);

		assertEquals("value", styleAlpha.getPropertyCSSValue("property")
				.getCssText());
		assertEquals("value2", styleBeta.getPropertyCSSValue("property")
				.getCssText());
	}

	/**
	 * Test for [CSS] nested imports duplicate rules
	 */
	@Test
	void testNestedImports() throws IOException {
		String deepNestedCss = "ChildChild { property: value; }\n";

		File importedFile = createTempCssFile(deepNestedCss);

		String childStyle = "Child { property: value; }\n";
		String childCss = createImport(importedFile) + childStyle;

		importedFile = createTempCssFile(childCss);

		String rootStyle = "Root { property: value; }\n";
		String rootCss = createImport(importedFile) + rootStyle;
		// we need a file URL so that the import can be resolved
		String importedFolderPath = importedFile.getParent();
		String importingUrl = "file:///" + importedFolderPath + "/root.css";

		CSSStyleSheetImpl result = parseStyleSheet(importingUrl, rootCss);

		// check the parsing result
		assertNotNull(result);
		List<CssRule> cssRules = result.getRules();
		assertEquals(3, cssRules.size());
		assertStyle(deepNestedCss, cssRules, 0);
		assertStyle(childStyle, cssRules, 1);
		assertStyle(rootStyle, cssRules, 2);
		// check that the engine's cascade holds exactly this sheet
		List<CSSStyleSheetImpl> documentStyleSheets = ((CSSEngineImpl) engine).getStyleSheets();
		assertEquals(1, documentStyleSheets.size());
		assertEquals(result, documentStyleSheets.get(0));
	}

	private void assertStyle(String expectedStyleText, List<CssRule> cssRules, int index) {
		assertTrue(cssRules.get(index) instanceof CSSStyleRuleImpl);
		assertEquals(expectedStyleText.trim(), ((CSSStyleRuleImpl) cssRules.get(index)).getCssText());
	}

	private File createTempCssFile(String cssString) throws IOException {
		File result = File.createTempFile("e4.ui.tests-", ".css");
		try (FileWriter fileWriter = new FileWriter(result)) {
			fileWriter.write(cssString);
			return result;
		}
	}

	private String createImport(File importedFile) {
		String cssUrl = importedFile.getName();
		return "@import url('" + cssUrl + "');\n";
	}

	private CSSStyleSheetImpl parseStyleSheet(String sourceUrl, String cssString) throws IOException {
		return engine.parseStyleSheet(
				new ByteArrayInputStream(cssString.getBytes(StandardCharsets.UTF_8)), sourceUrl);
	}
}
