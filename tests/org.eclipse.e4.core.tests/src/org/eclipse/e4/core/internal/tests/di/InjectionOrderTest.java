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
package org.eclipse.e4.core.internal.tests.di;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.core.di.InjectionException;

public class InjectionOrderTest extends TestCase {
	
	public interface ITestObject {
		
	}

	static public class InjectUnsatisfied {

		@Inject
		ITestObject object;
		
		static public int count = 0;

		@PostConstruct
		public void postConstruct() {
			count++;
			object.toString();
		}

		@PreDestroy
		public void preDestroy() {
			count++;
			object.toString();
		}
	}

	/**
	 * Make sure special methods are not getting called in case injection failed
	 */
	public void testSpecialMethodOnFailure() {
		IEclipseContext appContext = EclipseContextFactory.create();
		boolean exception = false;
		try {
			ContextInjectionFactory.make(InjectUnsatisfied.class, appContext);
		} catch (InjectionException e) {
			exception = true;
		}
		assertTrue(exception);
		((IDisposable) appContext).dispose();
		assertEquals(0, InjectUnsatisfied.count);
	}
}
