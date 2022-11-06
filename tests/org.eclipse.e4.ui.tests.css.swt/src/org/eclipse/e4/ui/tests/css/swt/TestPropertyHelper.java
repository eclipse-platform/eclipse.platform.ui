/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.e4.ui.css.swt.helpers.PropertyHelper;
import org.junit.jupiter.api.Test;

public class TestPropertyHelper {
	public static class Base {
		private String a = "A";
		public String getA() {
			return a;
		}
		public void setA(String a) {
			this.a = a;
		}

		public String getC() {
			return "C";
		}

		public boolean isD() {
			return true;
		}
	}

	public static class Impl extends Base {
		private String b = "B";
		private Base nested = new Base();
		{
			nested.a = "Nested";
		}

		public String getB() {
			return b;
		}

		public void setB(String b) {
			this.b = b;
		}

		public Base getNested() {
			return nested;
		}

		public void setNested(Base nested) {
			this.nested = nested;
		}
	}

	@Test
	void testReadWriteProperty() throws Exception {
		Impl bean = new Impl();
		assertEquals("A",PropertyHelper.getProperty(bean, "a"));
		assertEquals("B",PropertyHelper.getProperty(bean, "b"));
	}

	@Test
	void testReadOnlyProperty() throws Exception {
		Impl bean = new Impl();
		assertEquals("C",PropertyHelper.getProperty(bean, "c"));
		assertEquals(true,PropertyHelper.getProperty(bean, "d"));
	}

	@Test
	void testNestedProperty() throws Exception {
		Impl bean = new Impl();
		assertEquals("Nested",PropertyHelper.getProperty(bean, "nested.a"));
	}
}
