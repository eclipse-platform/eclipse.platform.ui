/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.content;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.content.ContentDescription;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.tests.runtime.RuntimeTest;

public class ContentDescriptionTest extends RuntimeTest {
	private static final QualifiedName ZOO_PROPERTY = new QualifiedName(PI_RUNTIME_TESTS, "zoo");
	private static final QualifiedName BAR_PROPERTY = new QualifiedName(PI_RUNTIME_TESTS, "bar");
	private static final QualifiedName FOO_PROPERTY = new QualifiedName(PI_RUNTIME_TESTS, "foo");
	private static final QualifiedName FRED_PROPERTY = new QualifiedName(PI_RUNTIME_TESTS, "fred");

	public ContentDescriptionTest(String name) {
		super(name);
	}

	public void testAllProperties() {
		ContentDescription description = new ContentDescription(IContentDescription.ALL);
		assertTrue("1.0", description.isRequested(FOO_PROPERTY));
		assertNull("1.1", description.getProperty(FOO_PROPERTY));
		description.setProperty(FOO_PROPERTY, "value1");
		assertEquals("1.2", "value1", description.getProperty(FOO_PROPERTY));
		description.setProperty(FOO_PROPERTY, "value1b");
		assertEquals("1.3", "value1b", description.getProperty(FOO_PROPERTY));
		assertTrue("2.0", description.isRequested(BAR_PROPERTY));
		description.setProperty(BAR_PROPERTY, "value2");
		assertEquals("2.1", "value2", description.getProperty(BAR_PROPERTY));
		description.setProperty(ZOO_PROPERTY, "value3");
		assertEquals("2.2", "value3", description.getProperty(ZOO_PROPERTY));		
		description.markImmutable();
		try {
			description.setProperty(FOO_PROPERTY, "value1c");
			fail("3.0 - should have failed");
		} catch (IllegalStateException e) {
			// success - the object was marked as immutable
		}
	}

	public void testOneProperty() {
		ContentDescription description = new ContentDescription(new QualifiedName[] {FOO_PROPERTY});
		assertTrue("1.0", description.isRequested(FOO_PROPERTY));
		assertNull("1.1", description.getProperty(FOO_PROPERTY));
		description.setProperty(FOO_PROPERTY, "value1");
		assertEquals("1.2", "value1", description.getProperty(FOO_PROPERTY));
		description.setProperty(FOO_PROPERTY, "value1b");
		assertEquals("1.3", "value1b", description.getProperty(FOO_PROPERTY));
		description.setProperty(BAR_PROPERTY, "value2");
		assertTrue("2.0", !description.isRequested(BAR_PROPERTY));
		description.setProperty(BAR_PROPERTY, "value2");
		assertNull("2.1", description.getProperty(BAR_PROPERTY));
		description.markImmutable();
		try {
			description.setProperty(FOO_PROPERTY, "value1c");
			fail("3.0 - should have failed");
		} catch (IllegalStateException e) {
			// success - the object was marked as immutable
		}
	}

	public void testZeroProperties() {
		ContentDescription description = new ContentDescription(new QualifiedName[0]);
		assertTrue("1.0", !description.isRequested(FOO_PROPERTY));
		assertNull("1.1", description.getProperty(FOO_PROPERTY));
		description.setProperty(FOO_PROPERTY, "value1");
		assertNull("1.2", description.getProperty(FOO_PROPERTY));
		description.markImmutable();
		try {
			description.setProperty(FOO_PROPERTY, "value1b");
			fail("2.0 - should have failed");
		} catch (IllegalStateException e) {
			// success - the object was marked as immutable
		}
	}

	public void testMultipleProperties() {
		ContentDescription description = new ContentDescription(new QualifiedName[] {FOO_PROPERTY, BAR_PROPERTY, ZOO_PROPERTY});
		assertTrue("1.0", description.isRequested(FOO_PROPERTY));
		assertNull("1.1", description.getProperty(FOO_PROPERTY));
		description.setProperty(FOO_PROPERTY, "value1");
		assertEquals("1.2", "value1", description.getProperty(FOO_PROPERTY));
		description.setProperty(FOO_PROPERTY, "value1b");
		assertEquals("1.3", "value1b", description.getProperty(FOO_PROPERTY));
		description.setProperty(BAR_PROPERTY, "value2");
		assertTrue("2.0", description.isRequested(BAR_PROPERTY));
		description.setProperty(BAR_PROPERTY, "value2");
		assertEquals("2.1", "value2", description.getProperty(BAR_PROPERTY));
		assertTrue("2.2", description.isRequested(ZOO_PROPERTY));
		description.setProperty(ZOO_PROPERTY, "value3");
		assertEquals("2.3", "value3", description.getProperty(ZOO_PROPERTY));
		assertTrue("3.0", !description.isRequested(FRED_PROPERTY));
		description.setProperty(FRED_PROPERTY, "value3");
		assertNull("3.1", description.getProperty(FRED_PROPERTY));
		description.markImmutable();
		try {
			description.setProperty(FOO_PROPERTY, "value1c");
			fail("4.0 - should have failed");
		} catch (IllegalStateException e) {
			// success - the object was marked as immutable
		}
	}

	public static Test suite() {
		return new TestSuite(ContentDescriptionTest.class);
	}
}