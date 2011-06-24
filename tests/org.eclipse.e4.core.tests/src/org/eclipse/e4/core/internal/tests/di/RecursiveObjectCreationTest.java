/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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
import javax.inject.Inject;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;

public class RecursiveObjectCreationTest extends TestCase {
	
	static public class CheckSelfInject {
		
		public CheckSelfInject other;
		
		@Inject
		public CheckSelfInject(CheckSelfInject other) {
			this.other = other;
		}
	}

	/**
	 * Checks a simple case of constructor needing the same class
	 */
	public void testSelfInject() {
		IEclipseContext context = EclipseContextFactory.create();
		boolean exceptionReceived = false;
		try {
			CheckSelfInject testInstance = ContextInjectionFactory.make(CheckSelfInject.class, context);
			assertNotNull(testInstance); // unreachable
		} catch (InjectionException e) {
			exceptionReceived = true; 
		}
		assertTrue(exceptionReceived);
	}
	
	///////////////////////////////////////////////////////////////////////

	static public class TestOuterClass {
		
		public class TestInnerClassInject {
			@Inject
			public TestInnerClassInject() {
				// placeholder
			}
		}
		
		public TestInnerClassInject innerInject;
		
		@Inject
		public TestOuterClass() {
			// placeholder
		}
		
		@PostConstruct
		public void init(IEclipseContext context) {
			innerInject = ContextInjectionFactory.make(TestInnerClassInject.class, context);
		}
	}

	/**
	 * Checks inner class using outer class which is still being created
	 */
	public void testNested() {
		IEclipseContext context = EclipseContextFactory.create();
		boolean exceptionReceived = false;
		try {
			TestOuterClass outer = ContextInjectionFactory.make(TestOuterClass.class, context);
			assertNotNull(outer); // unreachable
		} catch (InjectionException e) {
			exceptionReceived = true; 
		}
		assertTrue(exceptionReceived);
	}
}
