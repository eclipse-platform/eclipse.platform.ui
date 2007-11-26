/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.utils;

import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.utils.ObjectMap;
import org.eclipse.core.tests.resources.ResourceTest;

public class ObjectMapTest extends ResourceTest {
	private static final int MAXIMUM = 100;
	private Object[] values;

	/**
	 * Need a zero argument constructor to satisfy the test harness.
	 * This constructor should not do any real work nor should it be
	 * called by user code.
	 */
	public ObjectMapTest() {
		super();
	}

	/**
	 * Creates a new markers test.
	 */
	public ObjectMapTest(String name) {
		super(name);
	}

	/**
	 * Configures the markers test suite.
	 */
	public static Test suite() {
		return new TestSuite(ObjectMapTest.class);

		//TestSuite suite = new TestSuite();
		//suite.addTest(new ObjectMapTest("_testPR"));
		//return suite;
	}

	public void testPut() {

		// create the objects to insert into the map
		ObjectMap map = new ObjectMap();
		int max = 100;
		Object[] values = new Object[max];
		for (int i = 0; i < max; i++)
			values[i] = new Long(System.currentTimeMillis());

		// add each object to the map
		for (int i = 0; i < values.length; i++) {
			Object key = new Integer(i);
			map.put(key, values[i]);
			assertTrue("2.0." + i, map.containsKey(key));
			assertTrue("2.1." + i, map.containsValue(values[i]));
			assertEquals("2.2." + i, i + 1, map.size());
		}

		// make sure they are all still there
		assertEquals("3.0", max, map.size());
		for (int i = 0; i < values.length; i++) {
			Object key = new Integer(i);
			assertTrue("3.1." + i, map.containsKey(key));
			assertNotNull("3.2." + i, map.get(key));
		}
	}

	public void testPutEmptyMap() {
		ObjectMap map = new ObjectMap(new HashMap());
		map.put(new Object(), new Object());
	}

	public void testRemove() {

		// populate the map
		ObjectMap map = populateMap(MAXIMUM);

		// remove each element
		for (int i = MAXIMUM - 1; i >= 0; i--) {
			Object key = new Integer(i);
			map.remove(key);
			assertTrue("2.0." + i, !map.containsKey(key));
			assertEquals("2.1," + i, i, map.size());
			// check that the others still exist
			for (int j = 0; j < i; j++)
				assertTrue("2.2." + j, map.containsKey(new Integer(j)));
		}

		// all gone?
		assertEquals("3.0", 0, map.size());
	}

	public void testContains() {
		ObjectMap map = populateMap(MAXIMUM);

		for (int i = 0; i < MAXIMUM; i++) {
			assertTrue("2.0." + i, map.containsKey(new Integer(i)));
			assertTrue("2.1." + i, map.containsValue(values[i]));
		}

		assertFalse("3.0", map.containsKey(new Integer(MAXIMUM + 1)));
		assertFalse("3.1", map.containsKey(new Integer(-1)));
		assertFalse("3.2", map.containsValue(null));
		assertFalse("3.3", map.containsValue(getRandomString()));
	}

	public void testValues() {
		ObjectMap map = populateMap(MAXIMUM);

		Collection result = map.values();
		for (int i = 0; i < MAXIMUM; i++)
			assertTrue("2.0." + i, result.contains(values[i]));
	}

	public void testKeySet() {
		ObjectMap map = populateMap(MAXIMUM);
		Set keys = map.keySet();
		assertEquals("1.0", MAXIMUM, keys.size());
	}

	public void testEntrySet() {
		ObjectMap map = populateMap(MAXIMUM);
		Set entries = map.entrySet();
		for (int i = 0; i < MAXIMUM; i++)
			assertTrue("1.0." + i, contains(entries, values[i]));
	}

	/**
	 * The given set is a set of Map.Entry objects. 
	 */
	private boolean contains(Set set, Object value) {
		for (Iterator i = set.iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			if (entry.getValue().equals(value))
				return true;
		}
		return false;
	}

	private ObjectMap populateMap(int max) {
		// populate the map
		ObjectMap map = new ObjectMap();
		values = new Object[max];
		for (int i = 0; i < max; i++) {
			values[i] = new Long(System.currentTimeMillis());
			map.put(new Integer(i), values[i]);
		}
		assertEquals("#populateMap", max, map.size());
		return map;
	}

	/*
	 * Bug 62231 - empty ObjectMap.toHashMap() causes NullPointerException
	 */
	public void testBug_62231() {
		ObjectMap map = new ObjectMap();
		try {
			map.entrySet();
		} catch (NullPointerException e) {
			fail("1.0");
		}
		map.clear();
		try {
			map.entrySet();
		} catch (NullPointerException e) {
			fail("1.1");
		}
		
	}
}
