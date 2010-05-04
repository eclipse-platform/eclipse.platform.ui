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

import java.lang.reflect.InvocationTargetException;

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

// TBD add auto-conversion?
public class InjectionPreferencesTest extends TestCase {
	
	static final private String TEST_PREFS_KEY = "testPreferencesQualifier";  
	static final private String TEST_PREFS_NODE = "org.eclipse.e4.core.tests.ext";
	
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
	
	public void testPreferencesQualifier() throws BackingStoreException, InvocationTargetException, InstantiationException {
		setPreference(TEST_PREFS_KEY, "abc");
		setPreference(TEST_PREFS_KEY, TEST_PREFS_NODE, "123");
		IEclipseContext context = EclipseContextFactory.create();
		InjectTarget target = (InjectTarget) ContextInjectionFactory.make(InjectTarget.class, context);
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
	
	private void setPreference(String key, String value) throws BackingStoreException {
		String nodePath = CoreTestsActivator.getDefault().getBundleContext().getBundle().getSymbolicName();
		IEclipsePreferences node = new InstanceScope().getNode(nodePath);
		node.put(key, value);
		node.flush();
	}
	
	private void setPreference(String key, String nodePath, String value) throws BackingStoreException {
		IEclipsePreferences node = new InstanceScope().getNode(nodePath);
		node.put(key, value);
		node.flush();
	}
	
}
