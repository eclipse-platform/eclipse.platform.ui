/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.model;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.tests.runtime.RuntimeTest;

/**
 * Test cases for the ConfigurationElementModel class.
 */
public class ConfigurationElementModelTest extends RuntimeTest {

	/**
	 * Need a zero argument constructor to satisfy the test harness.
	 * This constructor should not do any real work nor should it be
	 * called by user code.
	 */
	public ConfigurationElementModelTest() {
		super(null);
	}

	/**
	 * Constructor for ConfigurationElementModelTest
	 */
	public ConfigurationElementModelTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ConfigurationElementModelTest.class.getName());
		suite.addTest(new ConfigurationElementModelTest("testConstructor"));
		suite.addTest(new ConfigurationElementModelTest("testIsReadOnly"));
		suite.addTest(new ConfigurationElementModelTest("testMarkReadOnly"));
		return suite;
	}

	public void testConstructor() {

		ConfigurationElementModel model = new ConfigurationElementModel();

		assertNotNull("1.0", model);

		assertNull("2.0", model.getParent());
		assertNull("2.1", model.getParentExtension());
		assertNull("2.2", model.getProperties());
		assertNull("2.3", model.getSubElements());
		assertNull("2.4", model.getValue());
		assertNull("2.5", model.getName());

		//assumes that a new-created object is not read-only
		assertTrue("3.0", !model.isReadOnly());

	}

	public void testIsReadOnly() {

		assertTrue("1.0", !new ConfigurationElementModel().isReadOnly());

		ConfigurationElementModel model = new ConfigurationElementModel();
		model.markReadOnly();
		assertTrue("2.0", model.isReadOnly());
	}

	public void testMarkReadOnly() {

		ConfigurationElementModel model = new ConfigurationElementModel();
		model.markReadOnly();

		assertTrue("1.0", model.isReadOnly());

		Factory fact = new Factory(new MultiStatus("plugin-id", 10, "", new Throwable()));
		ConfigurationPropertyModel[] c = new ConfigurationPropertyModel[1];
		c[0] = fact.createConfigurationProperty();
		try {
			// try to set an attribute to a read-only object
			model.setProperties(c);
			fail("2.0");
		} catch (Exception e) {
		}

	}
	// End of class
}