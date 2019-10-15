/*******************************************************************************
 * Copyright (c) 2019 itemis AG.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Karsten Thoms <karsten.thoms@itemis.de> - Initial implementation and API
 ******************************************************************************/
package org.eclipse.e4.emf.xpath.test;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.e4.emf.internal.xpath.helper.EDynamicPropertyHandler;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.Menu;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestFactory;
import org.junit.Test;

public class EDynamicPropertyHandlerTest {

	@Test
	public void test_getPropertyNames() {
		EDynamicPropertyHandler propertyHandler = new EDynamicPropertyHandler();
		Menu menu = XpathtestFactory.eINSTANCE.createMenu();
		String propertyNames = stream(propertyHandler.getPropertyNames(menu)).sorted().collect(joining(","));
		assertEquals("children,id,label", propertyNames);
	}

	@Test
	public void test_getProperty() {
		EDynamicPropertyHandler propertyHandler = new EDynamicPropertyHandler();
		Menu menu = XpathtestFactory.eINSTANCE.createMenu();
		menu.setLabel("foo");
		assertEquals("foo", propertyHandler.getProperty(menu, "label"));
	}

	@Test
	public void test_getProperty_When_UnknownProperty_Expect_NullResult() {
		EDynamicPropertyHandler propertyHandler = new EDynamicPropertyHandler();
		Menu menu = XpathtestFactory.eINSTANCE.createMenu();
		assertNull(propertyHandler.getProperty(menu, "invalid"));
	}

	@Test
	public void test_setProperty() {
		EDynamicPropertyHandler propertyHandler = new EDynamicPropertyHandler();
		Menu menu = XpathtestFactory.eINSTANCE.createMenu();
		propertyHandler.setProperty(menu, "label", "foo");
		assertEquals("foo", menu.getLabel());
	}
}
