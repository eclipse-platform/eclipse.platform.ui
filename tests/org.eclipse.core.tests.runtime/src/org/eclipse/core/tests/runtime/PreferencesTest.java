/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial test suite
 ******************************************************************************/
package org.eclipse.core.tests.runtime;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Preferences;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test suite for API class org.eclipse.core.runtime.Preferences
 */
public class PreferencesTest extends RuntimeTest {

	public PreferencesTest(String name) {
		super(name);
	}

	public static Test suite() {
		// all test methods are named "test..."
		TestSuite suite= new TestSuite(PreferencesTest.class);
		return suite;
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() {
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() {
	}

	public void testConstants() {
		// make sure that the preference store constants are defined properly
		assertTrue(Preferences.BOOLEAN_DEFAULT_DEFAULT == false);
		assertTrue(Preferences.INT_DEFAULT_DEFAULT == 0);
		assertTrue(Preferences.LONG_DEFAULT_DEFAULT == 0L);
		assertTrue(Preferences.FLOAT_DEFAULT_DEFAULT == 0.0f);
		assertTrue(Preferences.DOUBLE_DEFAULT_DEFAULT == 0.0);
		assertTrue(Preferences.STRING_DEFAULT_DEFAULT.equals(""));
	}

	public void testBasics() {
		
		Preferences ps = new Preferences();
		final String k1 = "key1";
		final String k2 = "key2";
		final String v1 = "1";
		final String v2 = "2";
		final String v3 = "3";

		// check that a random property in a newly created store
		// appearchs to have default-default values of whatever type asked for
		assertTrue("1.0", ps.isDefault(k1) == true);
		assertTrue("1.1", ps.getBoolean(k1) == Preferences.BOOLEAN_DEFAULT_DEFAULT);
		assertTrue("1.2", ps.getInt(k1) == Preferences.INT_DEFAULT_DEFAULT);
		assertTrue("1.3", ps.getLong(k1) == Preferences.LONG_DEFAULT_DEFAULT);
		assertTrue("1.4", ps.getFloat(k1) == Preferences.FLOAT_DEFAULT_DEFAULT);
		assertTrue("1.5", ps.getDouble(k1) == Preferences.DOUBLE_DEFAULT_DEFAULT);
		assertTrue("1.6", ps.getString(k1).equals(Preferences.STRING_DEFAULT_DEFAULT));

		assertTrue("1.7", ps.getDefaultBoolean(k1) == Preferences.BOOLEAN_DEFAULT_DEFAULT);
		assertTrue("1.8", ps.getDefaultInt(k1) == Preferences.INT_DEFAULT_DEFAULT);
		assertTrue("1.9", ps.getDefaultLong(k1) == Preferences.LONG_DEFAULT_DEFAULT);
		assertTrue("1.10", ps.getDefaultFloat(k1) == Preferences.FLOAT_DEFAULT_DEFAULT);
		assertTrue("1.11", ps.getDefaultDouble(k1) == Preferences.DOUBLE_DEFAULT_DEFAULT);
		assertTrue("1.12", ps.getDefaultString(k1).equals(Preferences.STRING_DEFAULT_DEFAULT));
		
		
		// test set/getString
		// give it a value
		ps.setValue(k1, v1);
		assertTrue("2.0", ps.isDefault(k1) == false);
		assertTrue("2.1", ps.getString(k1).equals(v1));
		assertTrue("2.2", ps.getDefaultString(k1).equals(Preferences.STRING_DEFAULT_DEFAULT));
		// change the value
		ps.setValue(k1, v2);
		assertTrue("2.3", ps.isDefault(k1) == false);
		assertTrue("2.4", ps.getString(k1).equals(v2));
		assertTrue("2.5", ps.getDefaultString(k1).equals(Preferences.STRING_DEFAULT_DEFAULT));
		// change to same value as default
		ps.setValue(k1, ps.getDefaultString(k1));
		assertTrue("2.6", ps.isDefault(k1) == true);
		assertTrue("2.7", ps.getString(k1).equals(ps.getDefaultString(k1)));
		assertTrue("2.8", ps.getDefaultString(k1).equals(Preferences.STRING_DEFAULT_DEFAULT));
		// reset to default
		ps.setValue(k1, v2);
		ps.setToDefault(k1);
		assertTrue("2.9", ps.isDefault(k1) == true);
		assertTrue("2.10", ps.getString(k1).equals(Preferences.STRING_DEFAULT_DEFAULT));
		assertTrue("2.11", ps.getDefaultString(k1).equals(Preferences.STRING_DEFAULT_DEFAULT));
		// change default
		ps.setDefault(k1, v1);
		assertTrue("2.12", ps.isDefault(k1) == true);
		assertTrue("2.13", ps.getString(k1).equals(v1));
		assertTrue("2.14", ps.getDefaultString(k1).equals(v1));
		// set the value
		ps.setValue(k1, v2);
		assertTrue("2.15", ps.isDefault(k1) == false);
		assertTrue("2.16", ps.getString(k1).equals(v2));
		assertTrue("2.17", ps.getDefaultString(k1).equals(v1));
		// change to same value as default
		ps.setValue(k1, ps.getDefaultString(k1));
		assertTrue("2.18", ps.isDefault(k1) == true);
		assertTrue("2.19", ps.getString(k1).equals(ps.getDefaultString(k1)));
		assertTrue("2.20", ps.getDefaultString(k1).equals(v1));
		// reset to default
		ps.setValue(k1, v2);
		ps.setToDefault(k1);
		assertTrue("2.21", ps.isDefault(k1) == true);
		assertTrue("2.22", ps.getString(k1).equals(v1));
		assertTrue("2.23", ps.getDefaultString(k1).equals(v1));
		// change default
		ps.setDefault(k1, v3);
		assertTrue("2.24", ps.isDefault(k1) == true);
		assertTrue("2.25", ps.getString(k1).equals(v3));
		assertTrue("2.26", ps.getDefaultString(k1).equals(v3));
								
	}
	
	public void testBoolean() {
		
		Preferences ps = new Preferences();
		final String k1 = "key1";

		assertEquals("1.0", false, Preferences.BOOLEAN_DEFAULT_DEFAULT);
		assertEquals("1.1", Preferences.BOOLEAN_DEFAULT_DEFAULT, ps.getBoolean(k1));

		ps.setValue(k1, true);
		assertEquals("1.2", true, ps.getBoolean(k1));
		ps.setValue(k1, false);
		assertEquals("1.3", false, ps.getBoolean(k1));
		
		ps.setDefault(k1, true);
		assertEquals("1.4", true, ps.getDefaultBoolean(k1));
		ps.setDefault(k1, false);
		assertEquals("1.5", false, ps.getDefaultBoolean(k1));
		
	}

	public void testInteger() {

		Preferences ps = new Preferences();
		final String k1 = "key1";
		final int[] values = { 0, 1002, -201788, Integer.MAX_VALUE, Integer.MIN_VALUE};

		assertEquals("1.0", 0, Preferences.INT_DEFAULT_DEFAULT);
		assertEquals("1.1", Preferences.INT_DEFAULT_DEFAULT, ps.getInt(k1));

		for (int i = 0; i < values.length; i++) {
			int v1 = values[i];
			int v2 = v1 + 1;
			ps.setValue(k1, v1);
			assertEquals("1.2", v1, ps.getInt(k1));
			ps.setDefault(k1, v2);
			assertEquals("1.3", v2, ps.getDefaultInt(k1));
		}
	}
	
	public void testLong() {

		Preferences ps = new Preferences();
		final String k1 = "key1";
		final long[] values = { 0L, 1002L, -201788L, Long.MAX_VALUE, Long.MIN_VALUE};

		assertEquals("1.0", 0L, Preferences.LONG_DEFAULT_DEFAULT);
		assertEquals("1.1", Preferences.LONG_DEFAULT_DEFAULT, ps.getLong(k1));

		for (int i = 0; i < values.length; i++) {
			long v1 = values[i];
			long v2 = v1 + 1;
			ps.setValue(k1, v1);
			assertEquals("1.2", v1, ps.getLong(k1));
			ps.setDefault(k1, v2);
			assertEquals("1.3", v2, ps.getDefaultLong(k1));
		}
	}
	
	public void testFloat() {

		Preferences ps = new Preferences();
		final String k1 = "key1";
		final float[] values = { 0.0f, 1002.5f, -201788.55f, Float.MAX_VALUE, Float.MIN_VALUE};
		final float tol = 1.0e-20f;
		
		assertEquals("1.0", 0.0f, Preferences.FLOAT_DEFAULT_DEFAULT, tol);
		assertEquals("1.1", Preferences.FLOAT_DEFAULT_DEFAULT, ps.getFloat(k1), tol);

		for (int i = 0; i < values.length; i++) {
			float v1 = values[i];
			float v2 = v1 + 1.0f;
			ps.setValue(k1, v1);
			assertEquals("1.2", v1, ps.getFloat(k1), tol);
			ps.setDefault(k1, v2);
			assertEquals("1.3", v2, ps.getDefaultFloat(k1), tol);
		}
		
		try {
			ps.setValue(k1, Float.NaN);
			assertTrue("1.4", false);
		} catch (IllegalArgumentException e) {
			// NaNs should be rejected
		}
			
	}
	
	public void testDouble() {

		Preferences ps = new Preferences();
		final String k1 = "key1";
		final double[] values = { 0.0, 1002.5, -201788.55, Double.MAX_VALUE, Double.MIN_VALUE};
		final double tol = 1.0e-20;

		assertEquals("1.0", 0.0, Preferences.DOUBLE_DEFAULT_DEFAULT, tol);
		assertEquals("1.1", Preferences.DOUBLE_DEFAULT_DEFAULT, ps.getDouble(k1), tol);

		for (int i = 0; i < values.length; i++) {
			double v1 = values[i];
			double v2 = v1 + 1.0;
			ps.setValue(k1, v1);
			assertEquals("1.2", v1, ps.getDouble(k1), tol);
			ps.setDefault(k1, v2);
			assertEquals("1.3", v2, ps.getDefaultDouble(k1), tol);
		}

		try {
			ps.setValue(k1, Float.NaN);
			assertTrue("1.4", false);
		} catch (IllegalArgumentException e) {
			// NaNs should be rejected
		}
			
	}
	
	public void testString() {

		Preferences ps = new Preferences();
		final String k1 = "key1";
		final String[] values = {"", "hello", " x ", "\n"};

		assertEquals("1.0", "", Preferences.STRING_DEFAULT_DEFAULT);
		assertEquals("1.1", ps.getString(k1), Preferences.STRING_DEFAULT_DEFAULT);

		for (int i = 0; i < values.length; i++) {
			String v1 = values[i];
			String v2 = v1 + "x";
			ps.setValue(k1, v1);
			assertEquals("1.2", v1, ps.getString(k1));
			ps.setDefault(k1, v2);
			assertEquals("1.3", v2, ps.getDefaultString(k1));
		}
	}
	
	public void testPropertyNames() {

		Preferences ps = new Preferences();
		
		// there are no properties initially
		assertEquals("1.0", 0, ps.propertyNames().length);
		
		String[] keys = {"a", "b", "c", "d"};
		
		// setting defaults does not add name to set
		for (int i = 0; i < keys.length; i++) {
			ps.setDefault(keys[i], "default");
		}
		assertEquals("1.1", 0, ps.propertyNames().length);

		// setting real values does add name to set
		for (int i = 0; i < keys.length; i++) {
			ps.setValue(keys[i], "actual");
		}
		assertEquals("1.2", keys.length, ps.propertyNames().length);
		
		Set s1 = new HashSet(Arrays.asList(keys));
		Set s2 = new HashSet(Arrays.asList(ps.propertyNames()));
		assertEquals("1.3", s1, s2);
		
		// setting to default does remove name from set
		for (int i = 0; i < keys.length; i++) {
			ps.setToDefault(keys[i]);
			Set s = new HashSet(Arrays.asList(ps.propertyNames()));
			assertTrue("1.4", !s.contains(keys[i]));
		}
		assertEquals("1.5", 0, ps.propertyNames().length);
	}
	
	public void testContains() {

		Preferences ps = new Preferences();
		String k1 = "missing";
		String k2 = "a";
		String k3 = "b";
		String k4 = "c";
		
		// there are no properties initially
		assertEquals("1.0", false, ps.contains("a"));
		
		// setting defaults adds name
		ps.setDefault("a", "default");
		assertEquals("1.1", true, ps.contains("a"));

		// setting value adds name
		assertEquals("1.2", false, ps.contains("b"));
		ps.setValue("b", "any");
		assertEquals("1.3", true, ps.contains("b"));
		
		// setting value does not remove entry already there
		ps.setValue("a", "any");
		assertEquals("1.4", true, ps.contains("a"));
		assertEquals("1.5", true, ps.contains("b"));
		
		// setting to default removes name from set unless there is a default too
		ps.setToDefault("b");
		assertEquals("1.6", false, ps.contains("b"));
		ps.setToDefault("a");
		assertEquals("1.7", true, ps.contains("a"));
	}
	
	public void testDefaultPropertyNames() {

		Preferences ps = new Preferences();
		
		// there are no default properties initially
		assertEquals("1.0", 0, ps.defaultPropertyNames().length);
		
		String[] keys = {"a", "b", "c", "d"};
		
		// setting actual values does not add name to set
		for (int i = 0; i < keys.length; i++) {
			ps.setValue(keys[i], "actual");
		}
		assertEquals("1.1", 0, ps.defaultPropertyNames().length);

		// setting defaults does add name to set
		for (int i = 0; i < keys.length; i++) {
			ps.setDefault(keys[i], "default");
		}
		assertEquals("1.2", keys.length, ps.defaultPropertyNames().length);
		
		Set s1 = new HashSet(Arrays.asList(keys));
		Set s2 = new HashSet(Arrays.asList(ps.defaultPropertyNames()));
		assertEquals("1.3", s1, s2);
		
		// setting to default does not remove name from set
		for (int i = 0; i < keys.length; i++) {
			ps.setToDefault(keys[i]);
			Set s = new HashSet(Arrays.asList(ps.defaultPropertyNames()));
			assertTrue("1.4", s.contains(keys[i]));
		}
		assertEquals("1.5", keys.length, ps.defaultPropertyNames().length);
		
		// setting to default-default does not remove name from set either
		for (int i = 0; i < keys.length; i++) {
			ps.setDefault(keys[i], Preferences.STRING_DEFAULT_DEFAULT);
			Set s = new HashSet(Arrays.asList(ps.defaultPropertyNames()));
			assertTrue("1.6.1", s.contains(keys[i]));
			
			ps.setDefault(keys[i], Preferences.BOOLEAN_DEFAULT_DEFAULT);
			s = new HashSet(Arrays.asList(ps.defaultPropertyNames()));
			assertTrue("1.6.2", s.contains(keys[i]));

			ps.setDefault(keys[i], Preferences.INT_DEFAULT_DEFAULT);
			s = new HashSet(Arrays.asList(ps.defaultPropertyNames()));
			assertTrue("1.6.3", s.contains(keys[i]));

			ps.setDefault(keys[i], Preferences.LONG_DEFAULT_DEFAULT);
			s = new HashSet(Arrays.asList(ps.defaultPropertyNames()));
			assertTrue("1.6.4", s.contains(keys[i]));

			ps.setDefault(keys[i], Preferences.FLOAT_DEFAULT_DEFAULT);
			s = new HashSet(Arrays.asList(ps.defaultPropertyNames()));
			assertTrue("1.6.5", s.contains(keys[i]));

			ps.setDefault(keys[i], Preferences.DOUBLE_DEFAULT_DEFAULT);
			s = new HashSet(Arrays.asList(ps.defaultPropertyNames()));
			assertTrue("1.6.6", s.contains(keys[i]));
		}
		assertEquals("1.7", keys.length, ps.defaultPropertyNames().length);
	}
	
	public void testListeners() {

		final Preferences ps = new Preferences();
		
		class Tracer implements Preferences.IPropertyChangeListener {
			public StringBuffer log = new StringBuffer();

			public void propertyChange(Preferences.PropertyChangeEvent event) {
				log.append("[");
				log.append(event.getProperty());
				log.append(":");
				log.append(event.getOldValue() == null ? "null" : event.getOldValue());
				log.append("->");
				log.append(event.getNewValue() == null ? "null" : event.getNewValue());
				log.append("]");
			}
		}
		
		final Tracer tracer1 = new Tracer();
		final Tracer tracer2 = new Tracer();
		
		// register one listener
		ps.addPropertyChangeListener(tracer1);
		assertEquals("1.0", "", tracer1.log.toString());
		
		// make sure it is notified
		ps.setValue("a", "1");
		assertEquals("1.1", "[a:->1]", tracer1.log.toString());

		ps.setValue("a", "2");
		assertEquals("1.2", "[a:->1][a:1->2]", tracer1.log.toString());

		ps.setValue("a", ps.getDefaultString("a"));
		assertEquals("1.2.1", "[a:->1][a:1->2][a:2->]", tracer1.log.toString());

		ps.setValue("a", "3");
		assertEquals("1.2.2", "[a:->1][a:1->2][a:2->][a:->3]", tracer1.log.toString());

		ps.setToDefault("a");
		assertEquals("1.2.3", "[a:->1][a:1->2][a:2->][a:->3][a:3->null]", tracer1.log.toString());

		// change to same value - no one notified
		ps.setValue("a", "2");
		tracer1.log.setLength(0);
		assertEquals("1.3", "", tracer1.log.toString());

		// register second listener
		ps.addPropertyChangeListener(tracer2);

		// make sure both are notified
		ps.setValue("a", "3");
		assertEquals("1.4", "[a:2->3]", tracer1.log.toString());
		assertEquals("1.5", "[a:2->3]", tracer2.log.toString());
		
		// deregister is honored
		ps.removePropertyChangeListener(tracer2);
		tracer1.log.setLength(0);
		tracer2.log.setLength(0);
		ps.setValue("a", "1");
		assertEquals("1.6", "[a:3->1]", tracer1.log.toString());
		assertEquals("1.7", "", tracer2.log.toString());
		
		// duplicate deregister is ignored
		ps.removePropertyChangeListener(tracer2);
		tracer1.log.setLength(0);
		tracer2.log.setLength(0);
		ps.setValue("a", "2");
		assertEquals("1.8", "[a:1->2]", tracer1.log.toString());
		assertEquals("1.9", "", tracer2.log.toString());
		
		// duplicate register is ignored
		ps.addPropertyChangeListener(tracer1);
		tracer1.log.setLength(0);
		ps.setValue("a", "1");
		assertEquals("1.10", "[a:2->1]", tracer1.log.toString());
		
		// last deregister is honored
		ps.removePropertyChangeListener(tracer1);
		tracer1.log.setLength(0);
		ps.setValue("a", "4");
		assertEquals("1.11", "", tracer1.log.toString());
		
		
		// adds 2 and removes 1 during during callback!
		class Trouble implements Preferences.IPropertyChangeListener {
			public void propertyChange(Preferences.PropertyChangeEvent event) {
				ps.removePropertyChangeListener(tracer1);
				ps.addPropertyChangeListener(tracer2);
			}
		}
		
		ps.setValue("a", "0");
		ps.addPropertyChangeListener(tracer1);
		ps.addPropertyChangeListener(new Trouble());
		tracer1.log.setLength(0);
		tracer2.log.setLength(0);
		ps.setValue("a", "1");
		ps.setValue("a", "2");
		assertEquals("1.12", "[a:0->1]", tracer1.log.toString());
		assertEquals("1.13", "[a:1->2]", tracer2.log.toString());
		
	}
	
	public void testLoadStore() {

		final Preferences ps = new Preferences();
		
		ps.setValue("b1", true);
		ps.setValue("i1", 1);
		ps.setValue("l1", 2L);
		ps.setValue("f1", 1.0f);
		ps.setValue("d1", 1.0);
		ps.setValue("s1", "x");
		String[] keys = {"b1", "i1", "l1", "f1", "d1", "s1", };
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ps.store(out, "test header");
		} catch (IOException e) {
			assertTrue("0.1", false);
		}

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
				
		final Preferences ps2 = new Preferences();
		try {
			ps2.load(in);
		} catch (IOException e) {
			assertTrue("0.2", false);
		}
		
		assertEquals("1.1", true, ps2.getBoolean("b1"));
		assertEquals("1.2", 1, ps2.getInt("i1"));
		assertEquals("1.3", 2L, ps2.getLong("l1"));
		assertEquals("1.4", 1.0f, ps2.getFloat("f1"), 1e-20f);
		assertEquals("1.5", 1.0, ps2.getDouble("d1"), 1e-20);
		assertEquals("1.6", "x", ps2.getString("s1"));

		Set s1 = new HashSet(Arrays.asList(keys));
		Set s2 = new HashSet(Arrays.asList(ps2.propertyNames()));
		assertEquals("1.7", s1, s2);

		// load discards current values
		in = new ByteArrayInputStream(out.toByteArray());
		final Preferences ps3 = new Preferences();
		ps3.setValue("s1", "y");
		try {
			ps3.load(in);
			assertEquals("1.8", "x", ps3.getString("s1"));
			Set k1 = new HashSet(Arrays.asList(keys));
			Set k2 = new HashSet(Arrays.asList(ps3.propertyNames()));
			assertEquals("1.9", k1, k2);
		} catch (IOException e) {
			assertTrue("1.10", false);
		}
		
	}
	
	public void testNeedsSaving() {

		Preferences ps = new Preferences();
		
		// setValue dirties
		ps = new Preferences();
		assertEquals("1.0", false, ps.needsSaving());
		ps.setValue("b1", true);
		assertEquals("1.1", true, ps.needsSaving());

		ps = new Preferences();
		assertEquals("2.0", false, ps.needsSaving());
		ps.setValue("i1", 1);
		assertEquals("2.1", true, ps.needsSaving());

		ps = new Preferences();
		assertEquals("3.0", false, ps.needsSaving());
		ps.setValue("l1", 2L);
		assertEquals("3.1", true, ps.needsSaving());

		ps = new Preferences();
		assertEquals("4.0", false, ps.needsSaving());
		ps.setValue("f1", 1.0f);
		assertEquals("4.1", true, ps.needsSaving());

		ps = new Preferences();
		assertEquals("5.0", false, ps.needsSaving());
		ps.setValue("d1", 1.0);
		assertEquals("5.1", true, ps.needsSaving());

		ps = new Preferences();
		assertEquals("6.0", false, ps.needsSaving());
		ps.setValue("s1", "x");
		assertEquals("6.1", true, ps.needsSaving());

		// setToDefault does not dirty if value not set
		ps = new Preferences();
		assertEquals("7.0", false, ps.needsSaving());
		ps.setToDefault("any");
		assertEquals("7.1", false, ps.needsSaving());

		// setToDefault dirties if value was set
		try {
			ps = new Preferences();
			assertEquals("7.2", false, ps.needsSaving());
			ps.setValue("any", "x");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ps.store(out, "test header");
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			ps.load(in);
			assertEquals("7.3", false, ps.needsSaving());
			ps.setToDefault("any");
			assertEquals("7.4", true, ps.needsSaving());
		} catch (IOException e) {
			assertTrue("7.5", false);
		}

		// setDefault, getT, getDefaultT do not dirty
		ps = new Preferences();
		assertEquals("8.0", false, ps.needsSaving());
		ps.setDefault("b1", true);
		ps.getBoolean("b1");
		ps.getDefaultBoolean("b1");
		ps.setDefault("i1", 1);
		ps.getInt("i1");
		ps.getDefaultInt("i1");
		ps.setDefault("l1", 1L);
		ps.getLong("l1");
		ps.getDefaultLong("l1");
		ps.setDefault("f1", 1.0f);
		ps.getFloat("f1");
		ps.getDefaultFloat("f1");
		ps.setDefault("d1", 1.0);
		ps.getDouble("d1");
		ps.getDefaultDouble("d1");
		ps.setDefault("s1", "x");
		ps.getString("s1");
		ps.getDefaultString("s1");
		assertEquals("8.1", false, ps.needsSaving());

		try {
			ps = new Preferences();
			assertEquals("9.1", false, ps.needsSaving());
			ps.setValue("b1", true);
			assertEquals("9.2", true, ps.needsSaving());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			// store makes not dirty
			ps.store(out, "test header");
			assertEquals("9.3", false, ps.needsSaving());
			
			// load comes in not dirty
			ps.setValue("b1", false);
			assertEquals("9.4", true, ps.needsSaving());
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			ps.load(in);
			assertEquals("9.5", false, ps.needsSaving());
		} catch (IOException e) {
			assertTrue("9.0", false);
		}
	}
	
/* Comment this test out until we are able to use session tests
 * with it. - ddw
 * 	public void testPluginPrefs() {
		IPluginRegistry registry = InternalPlatform.getPluginRegistry();
		IPluginDescriptor resPlugin = registry.getPluginDescriptor("org.eclipse.core.resources");
		Preferences perfs = null;
		try {
			perfs = resPlugin.getPlugin().getPluginPreferences();
		} catch (CoreException ce) {
			fail("0.1 core exception from getPlugin");
		}
		boolean oneBoolean = perfs.getBoolean("OneBoolean");
		double oneDouble = perfs.getDouble("OneDouble");
		float oneFloat = perfs.getFloat("OneFloat");
		int oneInt = perfs.getInt("OneInt");
		long oneLong = perfs.getLong("OneLong");
		String oneString = perfs.getString("OneString");
		assertTrue("1.0 boolean", oneBoolean);
		assertTrue("2.0 double", oneDouble == 4);
		assertTrue("3.0 float", oneFloat ==	4.3f);
		assertTrue("4.0 int", oneInt == 5);
		assertTrue("5.0 long", oneLong == 6);
		assertTrue("6.0 string", oneString.equals("A string from the plugin root directory"));
		int a = 4;
		int b = 3;
	}
*/
}