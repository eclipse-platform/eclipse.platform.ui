/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474274
 ******************************************************************************/
package org.eclipse.e4.core.internal.tests.di.extensions;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;

/**
 * This test should be execute without an OSGI runtime running to verfiy BR
 * 513883
 * 
 * @author jonas
 *
 */
public class InjectionWithoutOSGITest {

	static class InjectTarget {
		public String other;

		@Inject
		public void setSth(@Named("testMixed") String otherString) {
			other = otherString;
		}
	}

	@Test
	public void testPreferencesQualifier() throws BackingStoreException, InvocationTargetException, InstantiationException {
		IEclipseContext context = EclipseContextFactory.create();
		context.set("testMixed", "other");
		InjectTarget target = ContextInjectionFactory.make(InjectTarget.class, context);

		// test
		assertEquals("other", target.other);

		// change
		context.set("testMixed", "bingo");

		// re-test
		// assertEquals("xyz", target.pref);
		assertEquals("bingo", target.other);
	}


}
