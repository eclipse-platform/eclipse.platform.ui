/*******************************************************************************
 * Copyright (c) 2009, 2014 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   Stefan Winkler <stefan@winklerweb.net> - Bug 419482
 *   Lars Vogel <Lars.Vogel@gmail.com> - Bug 430468
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.eclipse.e4.ui.css.core.impl.dom.DocumentCSSImpl;
import org.eclipse.e4.ui.css.core.impl.dom.ViewCSSImpl;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.eclipse.e4.ui.tests.css.core.util.TestElement;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.ViewCSS;

public class ViewCSSTest {

	private Display display;
	private CSSSWTEngineImpl engine;

	@Before
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

	private static ViewCSS createViewCss(String css) throws IOException {
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		DocumentCSSImpl docCss = new DocumentCSSImpl();
		docCss.addStyleSheet(styleSheet);
		return new ViewCSSImpl(docCss);
	}
}
