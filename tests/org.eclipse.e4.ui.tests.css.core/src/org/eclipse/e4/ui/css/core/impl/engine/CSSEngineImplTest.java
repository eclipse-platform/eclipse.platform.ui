/*******************************************************************************
 * Copyright (c) 2015, 2023 Daniel Raap and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Daniel Raap <raap@subshell.com> - initial implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.engine;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Objects;

import org.eclipse.e4.ui.css.core.dom.properties.converters.AbstractCSSValueConverter;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverter;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverterConfig;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

public class CSSEngineImplTest {

	private CSSEngineImpl objectUnderTest;

	@BeforeEach
	public void setUp() {
		objectUnderTest = new CSSEngineImpl() {

			@Override
			public void reapply() {
				// mock does nothing
			}

		};
		objectUnderTest.setElementProvider((element, engine) -> {
			// throws NPE if parameter is null
			Objects.requireNonNull(element);
			if (element instanceof Element e) {
				return e;
			}
			return null;
		});
	}

	/**
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=506120">Bug
	 *      506120 - [CSS] NPE if CSS styling is disabled</a>
	 */
	@Test
	void testGetElement_null() {
		Element result = objectUnderTest.getElement(null);
		assertNull(result);
	}

	@Test
	void testUnregisterCSSValueConverter() {
		ICSSValueConverter converter = new TestConverter();
		objectUnderTest.registerCSSValueConverter(converter);
		assertSame(converter, objectUnderTest.getCSSValueConverter(TestConverter.class));

		objectUnderTest.unregisterCSSValueConverter(converter);
		assertNull(objectUnderTest.getCSSValueConverter(TestConverter.class));
	}

	@Test
	void testUnregisterReplacedConverterKeepsCurrentOne() {
		ICSSValueConverter replaced = new TestConverter();
		ICSSValueConverter current = new TestConverter();
		objectUnderTest.registerCSSValueConverter(replaced);
		objectUnderTest.registerCSSValueConverter(current);

		objectUnderTest.unregisterCSSValueConverter(replaced);
		assertSame(current, objectUnderTest.getCSSValueConverter(TestConverter.class));
	}

	private static class TestConverter extends AbstractCSSValueConverter {
		TestConverter() {
			super(TestConverter.class);
		}

		@Override
		public Object convert(CSSValue value, CSSEngine engine, Object context) {
			return null;
		}

		@Override
		public String convert(Object value, CSSEngine engine, Object context, ICSSValueConverterConfig config) {
			return null;
		}
	}

}
