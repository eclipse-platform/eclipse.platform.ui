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
import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.internal.tests.CoreTestsActivator;
import org.osgi.service.prefs.BackingStoreException;

public class InjectionMixedSuppliersTest extends TestCase {
	
	static class InjectTarget {
		public String pref;
		public String other;
		
		@Inject
		public void setPrefs(@Named("testMixed") String otherString, @Preference("injectedPrefs") String string) {
			pref = string;
			other = otherString;
		}
	}
	
	public void testPreferencesQualifier() throws BackingStoreException, InvocationTargetException, InstantiationException {
		IEclipseContext context = EclipseContextFactory.create();
		setPreference("injectedPrefs", "abc");
		context.set("testMixed", "other");
		InjectTarget target = (InjectTarget) ContextInjectionFactory.make(InjectTarget.class, context);
		
		// test
		assertEquals("abc", target.pref);
		assertEquals("other", target.other);
		
		// change
		setPreference("injectedPrefs", "xyz");
		context.set("testMixed", "bingo");
		
		// re-test
		assertEquals("xyz", target.pref);
		assertEquals("bingo", target.other);
	}
	
	private void setPreference(String key, String value) throws BackingStoreException {
		String nodePath = CoreTestsActivator.getDefault().getBundleContext().getBundle().getSymbolicName();
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(nodePath);
		node.put(key, value);
		node.flush();
	}
	
}
