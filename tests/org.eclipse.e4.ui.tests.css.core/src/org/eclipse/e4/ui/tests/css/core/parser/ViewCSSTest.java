/*******************************************************************************
 * Copyright (c) 2009, 2018 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
t https://www.eclipse.org/legal/epl-2.0/
t
t SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   Stefan Winkler <stefan@winklerweb.net> - Bug 419482
 *   Lars Vogel <Lars.Vogel@gmail.com> - Bug 430468
 *   Karsten Thoms <karste.thoms@itemis.de> - Bug 532869
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.e4.ui.css.core.impl.dom.DocumentCSSImpl;
import org.eclipse.e4.ui.css.core.impl.dom.ViewCSSImpl;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.eclipse.e4.ui.tests.css.core.util.TestElement;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.ViewCSS;

public class ViewCSSTest {

	private Display display;
	private CSSSWTEngineImpl engine;

	@BeforeEach
	public void setUp() {
		display = Display.getDefault();
		engine = new CSSSWTEngineImpl(display);
	}

	@Test
	public void testGetComputedStyle() throws Exception {
		// Two rules with the same specificity, the second rule should take
		// precedence because of its position in the stylesheet
		String css = "Label { color: black; }" + "Button { color: blue; font-weight: bold; }\n"
				+ "Button { color: green; }\n";
		ViewCSS viewCSS = createViewCss(css);

		TestElement shell = new TestElement("Shell", engine);
		CSSStyleDeclaration shellStyle = viewCSS.getComputedStyle(shell, null);
		assertNull(shellStyle);

		TestElement label = new TestElement("Label", engine);
		CSSStyleDeclaration labelStyle = viewCSS.getComputedStyle(label, null);
		assertNotNull(labelStyle);
		assertEquals(1, labelStyle.getLength());

		TestElement button = new TestElement("Button", engine);
		CSSStyleDeclaration buttonStyle = viewCSS.getComputedStyle(button, null);
		assertNotNull(buttonStyle);
		assertEquals(2, buttonStyle.getLength());
	}

	@Test
	public void testBug419482_order1() throws Exception {
		String css = "Shell > * > * { color: red; }\n" + "Button { color: blue; }\n";
		ViewCSS viewCSS = createViewCss(css);

		final TestElement shell = new TestElement("Shell", engine);
		final TestElement composite = new TestElement("Composite", shell, engine);
		final TestElement button = new TestElement("Button", composite, engine);

		CSSStyleDeclaration buttonStyle = viewCSS.getComputedStyle(button, null);
		assertNotNull(buttonStyle);
		assertEquals(1, buttonStyle.getLength());
		assertEquals("color: blue;", buttonStyle.getCssText());
	}

	@Test
	public void testBug419482_order2() throws Exception {
		String css = "Button { color: blue; }\n" + "Shell > * > * { color: red; }\n";
		ViewCSS viewCSS = createViewCss(css);

		final TestElement shell = new TestElement("Shell", engine);
		final TestElement composite = new TestElement("Composite", shell, engine);
		final TestElement button = new TestElement("Button", composite, engine);

		CSSStyleDeclaration buttonStyle = viewCSS.getComputedStyle(button, null);
		assertNotNull(buttonStyle);
		assertEquals(1, buttonStyle.getLength());
		assertEquals("color: red;", buttonStyle.getCssText());
	}

	@Test
	public void testBug419482_higherSpecificity() throws Exception {
		String css = "Shell > * > Button { color: blue; }\n" + "Shell > * > * { color: red; }\n";
		ViewCSS viewCSS = createViewCss(css);

		final TestElement shell = new TestElement("Shell", engine);
		final TestElement composite = new TestElement("Composite", shell, engine);
		final TestElement button = new TestElement("Button", composite, engine);

		CSSStyleDeclaration buttonStyle = viewCSS.getComputedStyle(button, null);
		assertNotNull(buttonStyle);
		assertEquals(1, buttonStyle.getLength());
		assertEquals("color: blue;", buttonStyle.getCssText());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRuleCaching() throws Exception {
		String css = "Shell > * > * { color: red; }\n" + "Button { color: blue; }\n";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		DocumentCSSImpl docCss = new DocumentCSSImpl();
		docCss.addStyleSheet(styleSheet);
		ViewCSSImpl viewCSS = new ViewCSSImpl(docCss);

		Field currentCombinedRulesField = ViewCSSImpl.class.getDeclaredField("currentCombinedRules");
		currentCombinedRulesField.setAccessible(true);

		// after creation and before call of getComputedStyle() the fields are null
		assertNull(currentCombinedRulesField.get(viewCSS));

		// now call getComputedStyle() for a shell
		final TestElement shell = new TestElement("Shell", engine);
		final TestElement composite = new TestElement("Composite", shell, engine);
		final TestElement button = new TestElement("Button", composite, engine);
		CSSStyleDeclaration buttonStyle = viewCSS.getComputedStyle(button, null);
		assertNotNull(buttonStyle);

		// now the fields are filled
		assertNotNull(currentCombinedRulesField.get(viewCSS));

		// deeper inspection: check what private method getCombinedRules returns
		Method getCombinedRulesMethod = ViewCSSImpl.class.getDeclaredMethod("getCombinedRules");
		getCombinedRulesMethod.setAccessible(true);

		List<CSSRule> cssRules = (List<CSSRule>) getCombinedRulesMethod.invoke(viewCSS);
		// check caching: a 2nd call retrieves cached list
		assertSame(cssRules, getCombinedRulesMethod.invoke(viewCSS));

		// add a new stylesheet => flush cache
		css = "Shell > * > * { color: blue; }\n" + "Label { color: green; }\n";
		styleSheet = ParserTestUtil.parseCss(css);
		docCss.addStyleSheet(styleSheet);

		assertNull(currentCombinedRulesField.get(viewCSS));

		List<CSSRule> cssRules2 = (List<CSSRule>) getCombinedRulesMethod.invoke(viewCSS);
		assertNotSame(cssRules, cssRules2);
		// stylesheet added => more rules
		assertTrue(cssRules2.size() > cssRules.size());
	}

	private static ViewCSS createViewCss(String css) throws IOException {
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		DocumentCSSImpl docCss = new DocumentCSSImpl();
		docCss.addStyleSheet(styleSheet);
		return new ViewCSSImpl(docCss);
	}
}
