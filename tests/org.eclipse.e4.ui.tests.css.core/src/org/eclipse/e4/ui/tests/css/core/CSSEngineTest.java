/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430468
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.engine.CSSEngineImpl;
import org.eclipse.e4.ui.tests.css.core.util.TestElement;
import org.junit.Test;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.Element;

public class CSSEngineTest {

	private static class TestCSSEngine extends CSSEngineImpl {
		@Override
		public void reapply() {
		}
	}

	@Test
	public void testSelectorMatch() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		SelectorList list = engine.parseSelectors("Date");
		engine.setElementProvider(new IElementProvider() {
			@Override
			public Element getElement(Object element, CSSEngine engine) {
				return new TestElement(element.getClass().getSimpleName(),
						engine);
			}
		});
		assertFalse(engine.matches(list.item(0), new Object(), null));
		assertTrue(engine.matches(list.item(0), new Date(), null));
	}

}
