/*******************************************************************************
 *  Copyright (c) 2009, 2014 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *      Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.e4.ui.css.swt.dom.html.SWTHTMLElement;
import org.eclipse.swt.widgets.Widget;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class CSSSWTWidgetTest extends CSSSWTTestCase {

	private static final class WidgetElementWithSupplierReturningNull extends WidgetElement {
		private WidgetElementWithSupplierReturningNull(Widget widget, CSSEngine engine) {
			super(widget, engine);
		}

		@Override
		protected Supplier<String> internalGetAttribute(String attr) {
			return () -> null;
		}
	}

	private final class SWTHTMLElementWithAttributeTypeNull extends SWTHTMLElement {
		private SWTHTMLElementWithAttributeTypeNull(Widget widget, CSSEngine engine) {
			super(widget, engine);
			attributeType = null;
		}
	}

	private final class WidgetElementWithSwtStylesNull extends WidgetElement {

		private WidgetElementWithSwtStylesNull(Widget widget, CSSEngine engine) {
			super(widget, engine);
			swtStyles = null;
		}
	}

	@Disabled
	@Test
	void testEngineKey() {
		Widget widget = createTestLabel("Label { font: Arial 12px; font-weight: bold }");
		assertEquals(WidgetElement.getEngine(widget), engine);
	}

	@Test
	void testIDKey() {
		final String id = "some.test.id";
		Widget widget = createTestLabel("Label { font: Arial 12px; font-weight: bold }");
		WidgetElement.setID(widget, id);
		assertEquals(WidgetElement.getID(widget), id);
	}


	@Test
	void testCSSClassKey() {
		final String cssClass = "some.test.cssclassname";
		Widget widget = createTestLabel("Label { font: Arial 12px; font-weight: bold }");
		WidgetElement.setCSSClass(widget, cssClass);
		assertEquals(WidgetElement.getCSSClass(widget), cssClass);
	}

	@Test
	void testHasAttribute() {
		Widget widget = createTestLabel("Label { }");
		String propertySetToEmptyStringKey = "empty-property";
		widget.setData(propertySetToEmptyStringKey, "");
		assertTrue(engine.getElement(widget).hasAttribute(propertySetToEmptyStringKey));
		assertFalse(engine.getElement(widget).hasAttribute("foo-bar-attribute"));
		assertNotNull(widget);
	}

	@Test
	void testGetAttributeWithSwtStylesNull() {
		Widget widget = createTestLabel("Label { }");
		engine.setElementProvider((element, engine) -> new WidgetElementWithSwtStylesNull((Widget) element, engine));

		assertTrue(engine.getElement(widget).hasAttribute("style"));
		assertEquals("", engine.getElement(widget).getAttribute("style"));
	}

	@Test
	void testGetAttributeWithAttributeTypeNull() {
		Widget widget = createTestLabel("Label { }");
		engine.setElementProvider(
				(element, engine) -> new SWTHTMLElementWithAttributeTypeNull((Widget) element, engine));

		assertTrue(engine.getElement(widget).hasAttribute("type"));
		assertEquals("", engine.getElement(widget).getAttribute("type"));
	}

	@Test
	void testGetAttributeWithAttributeSupplierReturningNull() {
		Widget widget = createTestLabel("Label { }");
		engine.setElementProvider(
				(element, engine) -> new WidgetElementWithSupplierReturningNull((Widget) element, engine));

		// throws exception
		assertThrows(AssertionFailedException.class, () -> engine.getElement(widget).getAttribute("style"));

	}
}
