/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.parser;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.e4.ui.css.core.impl.dom.DocumentCSSImpl;
import org.eclipse.e4.ui.css.core.impl.dom.ViewCSSImpl;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.eclipse.e4.ui.tests.css.core.util.TestElement;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.ViewCSS;


public class ViewCSSTest extends TestCase {

	private Display display;
	private CSSSWTEngineImpl engine;

	protected void setUp() throws Exception {
		display = Display.getDefault();
		engine = new CSSSWTEngineImpl(display);
	}

	public void testGetComputedStyle() throws Exception {
		// Two rules with the same specificity, the second rule should take
		// precedence because of its position in the stylesheet
		String css = "Label { color: black; }"
			+ "Button { color: blue; font-weight: bold; }\n"
			+ "Button { color: green; }\n";
		ViewCSS viewCSS = createViewCss(css);

		TestElement shell = new TestElement("Shell", engine);
		CSSStyleDeclaration shellStyle = viewCSS.getComputedStyle(shell, null);
		assertNull( shellStyle );

		TestElement label = new TestElement("Label", engine);
		CSSStyleDeclaration labelStyle = viewCSS.getComputedStyle(label, null);
		assertNotNull( labelStyle );
		assertEquals( 1, labelStyle.getLength() );

		TestElement button = new TestElement("Button", engine);
		CSSStyleDeclaration buttonStyle = viewCSS.getComputedStyle(button, null);
		assertNotNull( buttonStyle );
		assertEquals( 2, buttonStyle.getLength() );
	}

	private static ViewCSS createViewCss(String css) throws IOException {
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		DocumentCSSImpl docCss = new DocumentCSSImpl();
		docCss.addStyleSheet(styleSheet);
		return new ViewCSSImpl(docCss);
	}
}
