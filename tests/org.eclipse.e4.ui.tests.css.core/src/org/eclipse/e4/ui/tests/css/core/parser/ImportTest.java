/*******************************************************************************
 * Copyright (c) 2014 Stefan Winkler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
t https://www.eclipse.org/legal/epl-2.0/
t
t SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Winkler <stefan@winklerweb.net> - initial API and implementation
 *   Lars Vogel <Lars.Vogel@gmail.com> - Bug 430468
 *   Daniel Raap <raap@subshell.com> - Bug 511836
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.dom.DocumentCSSImpl;
import org.eclipse.e4.ui.css.core.impl.dom.ViewCSSImpl;
import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.eclipse.e4.ui.tests.css.core.util.TestElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.stylesheets.StyleSheet;
import org.w3c.dom.stylesheets.StyleSheetList;

public class ImportTest {

	private CSSEngine engine;

	@BeforeEach
	public void setUp() {
		engine = ParserTestUtil.createEngine();
	}

	/**
	 * Test case for Bug 430052 - [CSS] Imported rules cannot be overridden
	 *
	 * @throws Exception
	 */
	@Test
	public void testOverrideImportedRule() throws Exception {
		String importedCss = ".ClassAlpha {\n" //
				+ "     property: value;\n" //
				+ "  }\n" + "  .ClassBeta {\n" //
				+ "     property: value1;\n" //
				+ "  }";

		File importedFile = createTempCssFile(importedCss);

		String importingCss = createImport(importedFile) //
				+ "  .ClassBeta {\n" //
				+ "     property: value2;\n" //
				+ "  }";

		// we need a file URL so that the import can be resolved
		String importedFolderPath = importedFile.getParent();
		String importingUrl = "file:///" + importedFolderPath + "/importing.css";

		ViewCSS viewCSS = createViewCss(importingUrl, importingCss);

		TestElement buttonAlpha = new TestElement("Button", engine);
		buttonAlpha.setClass("ClassAlpha");

		TestElement buttonBeta = new TestElement("Button", engine);
		buttonBeta.setClass("ClassBeta");

		CSSStyleDeclaration styleAlpha = viewCSS.getComputedStyle(buttonAlpha,
				null);
		CSSStyleDeclaration styleBeta = viewCSS.getComputedStyle(buttonBeta,
				null);

		assertEquals("value", styleAlpha.getPropertyCSSValue("property")
				.getCssText());
		assertEquals("value2", styleBeta.getPropertyCSSValue("property")
				.getCssText());
	}

	/**
	 * Test for [CSS] nested imports duplicate rules
	 */
	@Test
	public void testNestedImports() throws IOException {
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

		CSSStyleSheet result = parseStyleSheet(importingUrl, rootCss);

		// check the parsing result
		assertNotNull(result);
		CSSRuleList cssRules = result.getCssRules();
		assertEquals(3, cssRules.getLength());
		assertStyle(deepNestedCss, cssRules, 0);
		assertStyle(childStyle, cssRules, 1);
		assertStyle(rootStyle, cssRules, 2);
		// check the full DocumentCSS of the engine
		StyleSheetList documentStyleSheets = engine.getDocumentCSS().getStyleSheets();
		assertEquals(1, documentStyleSheets.getLength());
		StyleSheet documentStyleSheet = documentStyleSheets.item(0);
		assertEquals(result, documentStyleSheet);
	}

	private void assertStyle(String expectedStyleText, CSSRuleList cssRules, int index) {
		assertEquals(CSSRule.STYLE_RULE, cssRules.item(index).getType());
		assertEquals(expectedStyleText.trim(), cssRules.item(index).getCssText());
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

	private CSSStyleSheet parseStyleSheet(String sourceUrl, String cssString) throws IOException {
		InputSource source = new InputSource();
		source.setURI(sourceUrl); // must not be null
		source.setCharacterStream(new StringReader(cssString));
		return (CSSStyleSheet) engine.parseStyleSheet(source);
	}

	private ViewCSS createViewCss(String sourceUrl, String cssString)
			throws IOException {
		StyleSheet styleSheet = parseStyleSheet(sourceUrl, cssString);

		DocumentCSSImpl docCss = new DocumentCSSImpl();
		docCss.addStyleSheet(styleSheet);
		return new ViewCSSImpl(docCss);
	}
}
