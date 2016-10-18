/*******************************************************************************
 * Copyright (c) 2015, 2016 Daniel Raap and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Raap <raap@subshell.com> - initial implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.engine;

import static org.junit.Assert.assertNull;

import java.util.Objects;

import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.dom.parsers.CSSParser;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class AbstractCSSEngineTest {
	private AbstractCSSEngine objectUnderTest;

	@Before
	public void setUp() {
		objectUnderTest = new AbstractCSSEngine() {

			@Override
			public void reapply() {
				// mock does nothing
			}

			@Override
			public CSSParser makeCSSParser() {
				return null;
			}

		};
		objectUnderTest.setElementProvider(new IElementProvider() {

			@Override
			public Element getElement(Object element, CSSEngine engine) {
				// throws NPE if parameter is null
				Objects.requireNonNull(element);
				if (element instanceof Element) {
					return (Element) element;
				}
				return null;
			}
		});
	}

	/**
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=506120">Bug
	 *      506120 - [CSS] NPE if CSS styling is disabled</a>
	 */
	@Test
	public void testGetElement_null() {
		Element result = objectUnderTest.getElement(null);
		assertNull(result);
	}

}
