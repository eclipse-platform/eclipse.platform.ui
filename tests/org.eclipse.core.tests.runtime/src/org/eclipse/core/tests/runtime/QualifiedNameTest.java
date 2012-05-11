/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.QualifiedName;

/**
 * Test cases for the QualifiedName class.
 */

public class QualifiedNameTest extends RuntimeTest {

	/**
	 * Need a zero argument constructor to satisfy the test harness.
	 * This constructor should not do any real work nor should it be
	 * called by user code.
	 */
	public QualifiedNameTest() {
		super(null);
	}

	/**
	 * Constructor for QualifiedNameTest
	 */
	public QualifiedNameTest(String name) {
		super(name);
	}

	public static Test suite() {

		TestSuite suite = new TestSuite(QualifiedNameTest.class.getName());
		suite.addTest(new QualifiedNameTest("testQualifiers"));
		suite.addTest(new QualifiedNameTest("testLocalNames"));
		suite.addTest(new QualifiedNameTest("testEqualsAndHashcode"));
		return suite;
	}

	public void testQualifiers() {

		try {
			new QualifiedName("foo", "bar");
		} catch (Exception e) {
			fail("1.0");
		}

		try {
			new QualifiedName(null, "bar");
		} catch (Exception e) {
			fail("1.1");
		}

		try {
			new QualifiedName(" ", "bar");
		} catch (Exception e) {
			fail("1.2");
		}

		try {
			new QualifiedName("", "bar");
		} catch (Exception e) {
			fail("1.3");
		}

	}

	public void testLocalNames() {

		try {
			new QualifiedName("foo", null);
			fail("2.0");
		} catch (Exception e) {
			// expected
		}

		try {
			new QualifiedName("foo", "");
			fail("2.1");
		} catch (Exception e) {
			// expected
		}

		try {
			new QualifiedName("foo", " ");
		} catch (Exception e) {
			fail("2.2");
		}

		try {
			new QualifiedName("foo", " port ");
		} catch (Exception e) {
			fail("2.3");
		}

	}

	public void testEqualsAndHashcode() {

		QualifiedName qN1 = new QualifiedName("org.eclipse.runtime", "myClass");
		QualifiedName qN2 = new QualifiedName("org.eclipse.runtime", "myClass");
		assertTrue("1.0", qN1.equals(qN2));
		assertTrue("1.1", qN2.equals(qN1));
		assertEquals("1.2", qN1.hashCode(), qN2.hashCode());

		assertTrue("2.0", !qN1.equals(new String("org.eclipse.runtime.myClass")));

		QualifiedName qN3 = new QualifiedName(null, "myClass");
		assertTrue("3.0", !qN1.equals(qN3));

		QualifiedName qN4 = new QualifiedName("org.eclipse.runtime", " myClass");
		assertTrue("3.1", !qN1.equals(qN4));

		QualifiedName qN5 = new QualifiedName("org.eclipse.runtime", "myClass ");
		assertTrue("3.2", !qN1.equals(qN5));

		QualifiedName qN6 = new QualifiedName(null, "myClass");
		QualifiedName qN7 = new QualifiedName(null, "myClass");
		assertTrue("4.0", qN6.equals(qN7));
		assertTrue("4.1", qN7.equals(qN6));
		assertEquals("4.2", qN7.hashCode(), qN6.hashCode());

		QualifiedName qN8 = new QualifiedName(" ", "myClass");
		assertTrue("5.0", !qN8.equals(qN7));
		QualifiedName qN9 = new QualifiedName("", "myClass");
		assertTrue("5.1", !qN8.equals(qN9));

	}

}
