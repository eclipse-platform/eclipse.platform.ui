/*******************************************************************************
 * Copyright (c) 2013, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430468
 *     Alain Le Guennec <Alain.LeGuennec@esterel-technologies.com> - Bug 458334
 *     Lars Sadau <lars@sadau-online.de> - Bug 487994
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.eclipse.e4.ui.css.core.impl.engine.CSSEngineImpl;
import org.eclipse.e4.ui.tests.css.core.util.TestElement;
import org.junit.Test;
import org.w3c.css.sac.Selector;
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
		engine.setElementProvider((element, engine1) -> new TestElement(element.getClass().getSimpleName(),
				engine1));
		assertFalse(engine.matches(list.item(0), new Object(), null));
		assertTrue(engine.matches(list.item(0), new Date(), null));
	}

	@Test
	public void testSelectorMatchOneOf() throws Exception {
		TestCSSEngine engine = engineWhichProducesElementsWithAttributeA();
		Selector selector = engine.parseSelectors("E[a~='B']").item(0);
		assertTrue(engine.matches(selector, "B AB", null));
		assertTrue(engine.matches(selector, "BC B", null));
		assertFalse(engine.matches(selector, "ABC", null));
		assertTrue(engine.matches(selector, "B", null));
	}

	@Test
	public void testSelectorAttributeIs() throws Exception {
		TestCSSEngine engine = engineWhichProducesElementsWithAttributeA();
		Selector selector = engine.parseSelectors("E[a='B']").item(0);
		assertFalse(engine.matches(selector, "ABC", null));
		assertTrue(engine.matches(selector, "B", null));
	}

	@Test
	public void testSelectorAttributeIs_EmptySting() throws Exception {
		TestCSSEngine engine = engineWhichProducesElementsWithAttributeA();
		Selector selector = engine.parseSelectors("E[a='']").item(0);
		assertFalse(engine.matches(selector, "ABC", null));
		assertTrue(engine.matches(selector, "", null));
	}

	@Test
	public void testSelectorAttributeIs_NotPresent() throws Exception {
		TestCSSEngine engine = engineWhichProducesElementsWithAttributeA();
		Selector selector = engine.parseSelectors("E[b='']").item(0);
		assertFalse(engine.matches(selector, "ABC", null));
		assertFalse(engine.matches(selector, "", null));
	}


	private TestCSSEngine engineWhichProducesElementsWithAttributeA() {
		TestCSSEngine engine = new TestCSSEngine();
		engine.setElementProvider((element, aEngine) -> {
			Element e = new TestElement("E", aEngine);
			e.setAttribute("a", element.toString());
			return e;
		});
		return engine;
	}

}
