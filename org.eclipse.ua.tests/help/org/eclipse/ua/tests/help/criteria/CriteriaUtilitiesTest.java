/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.criteria;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.help.internal.base.util.CriteriaUtilities;

public class CriteriaUtilitiesTest extends TestCase {

	public void testNullValues() {
		List values = CriteriaUtilities.getCriteriaValues(null);
		assertEquals(0, values.size());
	}

	public void testSingleValue() {
		List values = CriteriaUtilities.getCriteriaValues("1.0");
		assertEquals(1, values.size());
		assertEquals("1.0", values.get(0));
	}

	public void testSingleValueWithWhitespace() {
		List values = CriteriaUtilities.getCriteriaValues(" 1.0 ");
		assertEquals(1, values.size());
		assertEquals("1.0", values.get(0));
	}
	
	public void testMultipleValues() {
		List values = CriteriaUtilities.getCriteriaValues(" 1.0, 2.0 ");
		assertEquals(2, values.size());
		assertEquals("1.0", values.get(0));
		assertEquals("2.0", values.get(1));
	}
	
	public void testUppercaseValue() {
		List values = CriteriaUtilities.getCriteriaValues("LINUX");
		assertEquals(1, values.size());
		assertNotSame("linux", values.get(0));
	}
	
}
