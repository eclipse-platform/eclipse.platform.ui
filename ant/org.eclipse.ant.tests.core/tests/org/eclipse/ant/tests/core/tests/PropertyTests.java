/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.core.tests;

import org.eclipse.ant.core.Property;
import org.eclipse.ant.tests.core.AbstractAntTest;

/**
 * Tests the {@link Property} class
 * @since 3.8
 */
public class PropertyTests extends AbstractAntTest {

	/**
	 * Constructor
	 */
	public PropertyTests() {
		super("Ant property tests");
	}

	public void testPropertyEqual() throws Exception {
		Property p1 = new Property("one", "ONE");
		Property p2 = new Property("one", "ONE");
		assertTrue("The properties should be equal", p1.equals(p2));
	}
	
	public void testPropertyEqualNameOnly() throws Exception {
		Property p1 = new Property("two", "TWO");
		Property p2 = new Property("two", "FOUR");
		assertTrue("The properties should be equal", p1.equals(p2));
	}
	
	public void testPropertyNotEqual() throws Exception {
		Property p1 = new Property("three", "THREE");
		Property p2 = new Property("four", "FOUR");
		assertFalse("The properties should not be equal", p1.equals(p2));
	}
	
	public void testPropertyNotEqual2() throws Exception {
		Property p1 = new Property("five", "FIVE");
		Property p2 = new Property("six", "FIVE");
		assertFalse("The properties should not be equal", p1.equals(p2));
	}
	
	public void testPropertyNotEqualNull() throws Exception {
		Property p1 = new Property("seven", "SEVEN");
		assertFalse("The properties should not be equal", p1.equals(null));
	}
}
