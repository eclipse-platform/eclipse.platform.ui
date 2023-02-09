/*******************************************************************************
 * Copyright (c) 2014, 2023 Stefan Winkler and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Winkler - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandlerProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.dom.DocumentCSSImpl;
import org.eclipse.e4.ui.css.core.impl.engine.CSSEngineImpl;
import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.eclipse.e4.ui.tests.css.core.util.TestElement;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;

/**
 * Test the 'inherit' value independently of the SWT CSS engine. To do this, we
 * create a custom property handler and use CSSEngineImpl directly.
 *
 * @author Stefan Winkler
 */
public class InheritTest {

	@Test
	void testInheritFromParent() throws Exception {
		String css = "Canvas { property: myValue; }\n"
				+ "Button { property: inherit; }\n";

		CSSEngine engine = createEngine(css);
		final TestElement canvas = new TestElement("Canvas", engine);
		final TestElement button = new TestElement("Button", canvas, engine);

		engine.applyStyles(canvas, true);

		// after styles are applied, the CSS property "property" is reflected as
		// the attribute (see below in the PropertyToAttributeMapper class)
		assertEquals("myValue", button.getAttribute("property"));
	}

	@Test
	void testInheritAsDefault() throws Exception {
		String css = "* { property: inherit; }\n"
				+ "Canvas { property: myValue; }\n";

		CSSEngine engine = createEngine(css);
		final TestElement canvas = new TestElement("Canvas", engine);
		final TestElement button = new TestElement("Button", canvas, engine);

		engine.applyStyles(canvas, true);

		// after styles are applied, the CSS property "property" is reflected as
		// the attribute (see below in the PropertyToAttributeMapper class)
		assertEquals("myValue", button.getAttribute("property"));
	}

	@Test
	void testInheritExplicitProperty() throws Exception {
		String css = "Button { property: inherit; }\n";

		CSSEngine engine = createEngine(css);
		final TestElement canvas = new TestElement("Canvas", engine);
		canvas.setAttribute("property", "myValue");
		final TestElement button = new TestElement("Button", canvas, engine);

		engine.applyStyles(canvas, true);

		// after styles are applied, the CSS property "property" is reflected as
		// the attribute (see below in the PropertyToAttributeMapper class)
		assertEquals("myValue", button.getAttribute("property"));
	}

	private CSSEngine createEngine(String css) throws IOException {
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		DocumentCSSImpl docCss = new DocumentCSSImpl();
		docCss.addStyleSheet(styleSheet);

		return new CSSEngineImpl(docCss) {
			{
				registerCSSPropertyHandlerProvider(new TestHandlerProvider());
			}

			@Override
			public void reapply() {
			}

			@Override
			public Element getElement(Object element) {
				if (element instanceof TestElement e) {
					return e;
				}

				return super.getElement(element);
			}
		};
	}

	/**
	 * Very simple custom property handler, which maps the value of the CSS
	 * property "property" to the attribute of the {@link TestElement}.
	 *
	 * @author Stefan Winkler
	 */
	private static class PropertyToAttributeMapper implements
	ICSSPropertyHandler {

		@Override
		public boolean applyCSSProperty(Object element, String property,
				CSSValue value, String pseudo, CSSEngine engine)
						throws Exception {

			if ("property".equals(property)) {
				TestElement testElement = (TestElement) element;
				testElement.setAttribute("property", value.getCssText());
				return true;
			}
			return false;
		}

		@Override
		public String retrieveCSSProperty(Object element, String property,
				String pseudo, CSSEngine engine) throws Exception {
			if ("property".equals(property)) {
				TestElement testElement = (TestElement) element;
				return testElement.getAttribute("property");
			}
			return null;
		}
	}

	/**
	 * Provider implementation for the {@link PropertyToAttributeMapper}.
	 *
	 * @author Stefan Winkler
	 */
	private static class TestHandlerProvider implements
	ICSSPropertyHandlerProvider {

		private PropertyToAttributeMapper propertyToAttributeMapper = new PropertyToAttributeMapper();

		@Override
		public CSSStyleDeclaration getDefaultCSSStyleDeclaration(
				CSSEngine engine, Object element, CSSStyleDeclaration newStyle,
				String pseudoE) throws Exception {
			return null;
		}

		@Override
		public Collection<ICSSPropertyHandler> getCSSPropertyHandlers(
				Object element, String property) throws Exception {
			return Collections
					.singleton((ICSSPropertyHandler) propertyToAttributeMapper);
		}

		@Override
		public Collection<ICSSPropertyHandler> getCSSPropertyHandlers(
				String property) throws Exception {
			return Collections
					.singleton((ICSSPropertyHandler) propertyToAttributeMapper);
		}

		@Override
		public Collection<String> getCSSProperties(Object element) {
			return Collections.singleton("property");
		}
	}
}
