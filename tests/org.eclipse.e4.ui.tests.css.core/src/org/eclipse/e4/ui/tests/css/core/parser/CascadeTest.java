/*******************************************************************************
 * Copyright (c) 2009, 2014 EclipseSource and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   Lars Vogel <Lars.Vogel@gmail.com> - Bug 430468
 *   Stefan Winkler <stefan@winklerweb.net> - Bug 458342
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.eclipse.e4.ui.css.core.impl.dom.DocumentCSSImpl;
import org.eclipse.e4.ui.css.core.impl.dom.ViewCSSImpl;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.eclipse.e4.ui.tests.css.core.util.TestElement;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.ViewCSS;

/**
 * Test to ensure the that CSS honors the CSS cascading rules, i.e.:
 *
 * <p>
 * !important after CSS properties.
 * </p>
 * <p>
 * Specificity of CSS rule selectors
 * </p>
 * <p>
 * Sequence of declaration.
 * </p>
 *
 **/

public class CascadeTest {

	private Display display;
	private CSSSWTEngineImpl engine;

	@BeforeEach
	public void setUp() throws Exception {
		display = Display.getDefault();
		engine = new CSSSWTEngineImpl(display);
	}

	@Test
	void testPosition() throws Exception {
		// Two rules with the same specificity, the second rule should take
		// precedence because of its position in the stylesheet
		String css = "Button { color: blue; font-weight: bold; }\n"
				+ "Button { color: black }\n";
		ViewCSS viewCSS = createViewCss(css);

		TestElement button = new TestElement("Button", engine);
		CSSStyleDeclaration style = viewCSS.getComputedStyle(button, null);
		assertEquals("black", style.getPropertyCSSValue("color").getCssText());
		assertEquals("bold", style.getPropertyCSSValue("font-weight").getCssText());
	}

	@Test
	void testSpecificity() throws Exception {
		// Two rules with different specificity, the first should take
		// precedence because of its higher specificity
		String css = "Label, Button.special { color: black; }\n"
				+ "Button { color: blue; font-weight: bold; }\n";
		ViewCSS viewCSS = createViewCss(css);

		TestElement button = new TestElement("Button", engine);
		CSSStyleDeclaration style = viewCSS.getComputedStyle(button, null);
		assertEquals("blue", style.getPropertyCSSValue("color").getCssText());

		button.setClass("special");
		style = viewCSS.getComputedStyle(button, null);
		assertEquals("black", style.getPropertyCSSValue("color").getCssText());
		assertEquals("bold", style.getPropertyCSSValue("font-weight")
				.getCssText());
	}

	@Test
	void ensureThatClassAndIdareConsideredIfOnTheSameLevel() throws Exception {
		// Rules for elements with children. The first should take
		// precedence because of its higher specificity
		String css = """
			CTabFolder > Composite > Toolbar { color: black; }
			CTabFolder > Composite > .special { color: blue; font-weight: bold; }
			CTabFolder > Composite > #special { color: red; font-weight: bold; }
			""";
		ViewCSS viewCSS = createViewCss(css);

		TestElement tabFolder = new TestElement("CTabFolder", engine);
		TestElement composite = new TestElement("Composite", tabFolder, engine);
		TestElement toolbar = new TestElement("Toolbar", composite, engine);

		CSSStyleDeclaration style = viewCSS.getComputedStyle(toolbar, null);
		assertEquals("black", style.getPropertyCSSValue("color").getCssText());

		toolbar.setClass("special");
		style = viewCSS.getComputedStyle(toolbar, null);
		assertEquals("blue", style.getPropertyCSSValue("color").getCssText());

		toolbar.setId("special");
		style = viewCSS.getComputedStyle(toolbar, null);
		assertEquals("red", style.getPropertyCSSValue("color").getCssText());

	}

	@Test
	void testSpecificities() throws Exception {
		// Different specificities
		String css = """
			* { color: black; }
			Button { color: blue; }
			Button[BORDER] { color: gray; }
			Button.special { color: green; }
			Button#myid { color: red; }
			""";
		ViewCSS viewCSS = createViewCss(css);

		TestElement label = new TestElement("Label", engine);
		CSSStyleDeclaration style = viewCSS.getComputedStyle(label, null);
		assertEquals("black", style.getPropertyCSSValue("color").getCssText());

		TestElement button = new TestElement("Button", engine);
		style = viewCSS.getComputedStyle(button, null);
		assertEquals("blue", style.getPropertyCSSValue("color").getCssText());

		button.setAttribute("BORDER", "true");
		style = viewCSS.getComputedStyle(button, null);
		assertEquals("gray", style.getPropertyCSSValue("color").getCssText());

		button.setClass("special");
		style = viewCSS.getComputedStyle(button, null);
		assertEquals("green", style.getPropertyCSSValue("color").getCssText());

		button.setId("myid");
		style = viewCSS.getComputedStyle(button, null);
		assertEquals("red", style.getPropertyCSSValue("color").getCssText());
	}

	private static ViewCSS createViewCss(String... css) throws IOException {
		DocumentCSSImpl docCss = new DocumentCSSImpl();
		for (String cssString : css) {
			CSSStyleSheet styleSheet = ParserTestUtil.parseCss(cssString);
			docCss.addStyleSheet(styleSheet);
		}
		return new ViewCSSImpl(docCss);
	}

	//	public void testImportantRule() throws Exception {
	//		//Several rules for the same class, if one rule has ! important
	//		//it takes precedence over all other, if more than one
	//		//last one gets precedence
	//
	//		String css = "Button{color:red ! important;}\n"
	//			+"Button{ color: blue ! important;}\n"
	//			+ "Button { color: black }\n";
	//		ViewCSS viewCSS = createViewCss(css);
	//
	//		TestElement button = new TestElement("Button", engine);
	//		CSSStyleDeclaration style = viewCSS.getComputedStyle(button, null);
	//		assertEquals("blue", style.getPropertyCSSValue("color").getCssText());
	//	}

	@Test
	void testBug261081() throws Exception {
		// Two rules with the same specificity, the second rule should take
		// precedence because of its position in the stylesheet
		String css = "Button, Label { color: blue; font-weight: bold; }\n"
				+ "Button { color: black }\n";
		ViewCSS viewCSS = createViewCss(css);

		TestElement button = new TestElement("Button", engine);
		CSSStyleDeclaration style = viewCSS.getComputedStyle(button, null);
		assertEquals("black", style.getPropertyCSSValue("color").getCssText());
		assertEquals("bold", style.getPropertyCSSValue("font-weight").getCssText());
	}

	@Test
	void testBug458342_combine() throws Exception {
		// the rules of two stylesheets should be combined
		String css1 = "Button { color: blue; }";
		String css2 = "Button { font-weight: bold; }";

		ViewCSS viewCSS = createViewCss(css1, css2);

		TestElement button = new TestElement("Button", engine);
		CSSStyleDeclaration style = viewCSS.getComputedStyle(button, null);
		assertEquals("blue", style.getPropertyCSSValue("color").getCssText());
		assertEquals("bold", style.getPropertyCSSValue("font-weight").getCssText());
	}

	@Test
	void testBug458342_override() throws Exception {
		// in case of two stylesheets, the second one should override properties
		// from the first one
		String css1 = "Button { color: blue; font-weight: bold; }";
		String css2 = "Button { color: black; }";

		ViewCSS viewCSS = createViewCss(css1, css2);

		TestElement button = new TestElement("Button", engine);
		CSSStyleDeclaration style = viewCSS.getComputedStyle(button, null);
		assertEquals("black", style.getPropertyCSSValue("color").getCssText());
		assertEquals("bold", style.getPropertyCSSValue("font-weight").getCssText());
	}
}
