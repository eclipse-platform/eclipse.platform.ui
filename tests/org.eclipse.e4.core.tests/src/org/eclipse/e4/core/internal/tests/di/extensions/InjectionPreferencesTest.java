/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.internal.tests.di.extensions;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.internal.tests.CoreTestsActivator;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Note: we do not support byte arrays at this time.
 */
public class InjectionPreferencesTest extends TestCase {
	
	static final private String TEST_PREFS_KEY = "testPreferencesQualifier";  
	static final private String TEST_PREFS_NODE = "org.eclipse.e4.core.tests.ext";
	
	static final private String KEY_INT = "testPreferencesInt";
	static final private String KEY_BOOL = "testPreferencesBoolean";
	static final private String KEY_DOUBLE = "testPreferencesDouble";
	static final private String KEY_FLOAT = "testPreferencesFloat";
	static final private String KEY_LONG = "testPreferencesLong";
//	static final private String KEY_BYTE_ARRAY = "testPreferencesByteArray";
	
	static class InjectTarget {
		public int counter = 0;
		public int counterNode = 0;
		public int counterOptional = 0;
		
		public String pref;
		public String prefNode;
		public String prefOptional1;
		public String prefOptional2;
		
		@Inject
		public void setPrefs(@Preference(TEST_PREFS_KEY) String string) {
			counter++;
			pref = string;
		}
		
		@Inject
		public void setPrefsNode(@Preference(value=TEST_PREFS_KEY, nodePath=TEST_PREFS_NODE) String string) {
			counterNode++;
			prefNode = string;
		}
		
		@Inject
		public void setOptionalPrefs(@Optional @Preference("something") String string1, @Preference(TEST_PREFS_KEY) String string2) {
			counterOptional++;
			prefOptional1 = string1;
			prefOptional2 = string2;
		}
	}
	
	static class InjectTargetPrimitive {
		@Inject @Preference(KEY_INT)
		public int intField;

		@Inject @Preference(KEY_BOOL)
		public boolean booleanField;

		@Inject @Preference(KEY_DOUBLE)
		public double doubleField;

		@Inject @Preference(KEY_FLOAT)
		public float floatField;

		@Inject @Preference(KEY_LONG)
		public long longField;

//		@Inject @Preference(KEY_BYTE_ARRAY)
//		public byte[] byteArrayField;
		
		public int intArg;
		public boolean booleanArg;
		
		@Inject
		public void set(@Preference(KEY_INT) int intArg, @Preference(KEY_BOOL) boolean booleanArg) {
			this.intArg = intArg;
			this.booleanArg = booleanArg;
		}
	}

	static class InjectTargetConversion {
		@Inject @Preference(KEY_INT)
		public Integer intField;

		@Inject @Preference(KEY_BOOL)
		public Boolean booleanField;

		@Inject @Preference(KEY_DOUBLE)
		public Double doubleField;

		@Inject @Preference(KEY_FLOAT)
		public Float floatField;

		@Inject @Preference(KEY_LONG)
		public Long longField;

		public IEclipsePreferences preferences;

		public Integer intArg;
		public Boolean booleanArg;
		
		@Inject
		public void set(@Preference(KEY_INT) Integer intArg, @Preference(KEY_BOOL) Boolean booleanArg) {
			this.intArg = intArg;
			this.booleanArg = booleanArg;
		}
		
		@Inject
		public void set2(@Preference IEclipsePreferences prefNode) {
			preferences = prefNode;
			prefNode.put("testOutValue", "abc");
		}
	}
	
	public void testPreferencesQualifier() throws BackingStoreException {
		setPreference(TEST_PREFS_KEY, "abc");
		setPreference(TEST_PREFS_KEY, TEST_PREFS_NODE, "123");
		IEclipseContext context = EclipseContextFactory.create();
		InjectTarget target = ContextInjectionFactory.make(InjectTarget.class, context);
		// default node
		assertEquals(1, target.counter);
		assertEquals("abc", target.pref);
		// specific node
		assertEquals(1, target.counterNode);
		assertEquals("123", target.prefNode);
		// optional preference
		assertEquals(1, target.counterOptional);
		assertNull(target.prefOptional1);
		assertEquals("abc", target.prefOptional2);
		
		// change
		setPreference(TEST_PREFS_KEY, "xyz");
		setPreference(TEST_PREFS_KEY, TEST_PREFS_NODE, "456");
		
		// default node
		assertEquals(2, target.counter);
		assertEquals("xyz", target.pref);
		// specific node
		assertEquals(2, target.counterNode);
		assertEquals("456", target.prefNode);
		// optional preference
		assertEquals(2, target.counterOptional);
		assertNull(target.prefOptional1);
		assertEquals("xyz", target.prefOptional2);
	}
	
	public void testBaseTypeConversion() throws BackingStoreException {
		// setup preferences
		String nodePath = CoreTestsActivator.getDefault().getBundleContext().getBundle().getSymbolicName();
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(nodePath);
		node.putInt(KEY_INT, 12);
		node.putBoolean(KEY_BOOL, true);
		node.putDouble(KEY_DOUBLE, 12.35345345345d);
		node.putFloat(KEY_FLOAT, 5.13f);
		node.putLong(KEY_LONG, 131232343453453L);
//		node.putByteArray(KEY_BYTE_ARRAY, new byte[] { 12, 34, 45, 67});
		node.flush();
		
		IEclipseContext context = EclipseContextFactory.create();
		InjectTargetPrimitive target = ContextInjectionFactory.make(InjectTargetPrimitive.class, context);
		
		assertEquals(12, target.intField);
		assertEquals(true, target.booleanField);
		assertEquals(12.35345345345d, target.doubleField);
		assertEquals(5.13f, target.floatField);
		assertEquals(131232343453453L, target.longField);
//		assertNotNull(target.byteArrayField);
//		assertEquals(4, target.byteArrayField.length);
//		assertEquals(12, target.byteArrayField[0]);
//		assertEquals(34, target.byteArrayField[1]);
//		assertEquals(45, target.byteArrayField[2]);
//		assertEquals(67, target.byteArrayField[3]);
		
		assertEquals(12, target.intArg);
		assertEquals(true, target.booleanArg);
		
		// change
		node.putInt(KEY_INT, 777);
		node.putBoolean(KEY_BOOL, false);
		
		assertEquals(777, target.intField);
		assertEquals(false, target.booleanField);
		
		assertEquals(777, target.intArg);
		assertEquals(false, target.booleanArg);
	}
	
	public void testAutoConversion() throws BackingStoreException {
		// setup preferences
		String nodePath = CoreTestsActivator.getDefault().getBundleContext().getBundle().getSymbolicName();
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(nodePath);
		node.putInt(KEY_INT, 12);
		node.putBoolean(KEY_BOOL, true);
		node.putDouble(KEY_DOUBLE, 12.35345345345d);
		node.putFloat(KEY_FLOAT, 5.13f);
		node.putLong(KEY_LONG, 131232343453453L);
		node.flush();
		
		IEclipseContext context = EclipseContextFactory.create();
		InjectTargetConversion target = ContextInjectionFactory.make(InjectTargetConversion.class, context);
		
		assertEquals(new Integer(12), target.intField);
		assertEquals(new Boolean(true), target.booleanField);
		assertEquals(new Double(12.35345345345d), target.doubleField);
		assertEquals(new Float(5.13f), target.floatField);
		assertEquals(new Long(131232343453453L), target.longField);
		
		assertEquals(new Integer(12), target.intArg);
		assertEquals(new Boolean(true), target.booleanArg);
		
		// change
		node.putInt(KEY_INT, 777);
		node.putBoolean(KEY_BOOL, false);
		
		assertEquals(new Integer(777), target.intField);
		assertEquals(new Boolean(false), target.booleanField);
		
		assertEquals(new Integer(777), target.intArg);
		assertEquals(new Boolean(false), target.booleanArg);
		
		assertNotNull(target.preferences);
		assertEquals("abc", node.get("testOutValue", null)); 
	}

	private void setPreference(String key, String value) throws BackingStoreException {
		String nodePath = CoreTestsActivator.getDefault().getBundleContext().getBundle().getSymbolicName();
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(nodePath);
		node.put(key, value);
		node.flush();
	}
	
	private void setPreference(String key, String nodePath, String value) throws BackingStoreException {
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(nodePath);
		node.put(key, value);
		node.flush();
	}
	
}
