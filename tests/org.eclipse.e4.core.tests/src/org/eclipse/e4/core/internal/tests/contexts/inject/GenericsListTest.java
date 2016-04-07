/*******************************************************************************
 * Copyright (c) 2015, 2016 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.tests.contexts.inject;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.junit.Test;

/**
 * Tests for the type erasure in lists
 */
public class GenericsListTest {

	static public class TestNamedObject {
		public List<String> field;

		@Inject
		public void setList(List<String> value) {
			field = value;
		}

		public String combineIt() {
			String result = "";
			for (String string : field) {
				result += string;
			}
			return result;
		}
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testRawListInjection() {

		List list = new ArrayList();

		// create context
		IEclipseContext context = EclipseContextFactory.create();
		context.set(List.class, list);

		TestNamedObject userObject = new TestNamedObject();
		ContextInjectionFactory.inject(userObject, context);

		// check field injection
		assertEquals(list, userObject.field);
	}

	@Test(expected = ClassCastException.class)
	public void testTypeErasure() {

		List<Integer> list = new ArrayList<>();
		list.add(1);

		// create context
		IEclipseContext context = EclipseContextFactory.create();
		context.set(List.class, list);

		TestNamedObject userObject = new TestNamedObject();
		ContextInjectionFactory.inject(userObject, context);
		// check field injection, should be successful because
		// of the type erasure
		assertEquals(list, userObject.field);

		userObject.combineIt();
	}


}
