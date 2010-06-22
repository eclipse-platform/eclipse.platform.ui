/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.tests.contexts.inject;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;

/**
 * Tests for the context injection functionality using 2 contexts 
 */
public class InjectStaticContextTest extends TestCase {
	static class TestClass {
		public IEclipseContext injectedContext;
		public String aString;
		public String bString;
		public String cString;

		public String aConstructorString;
		public String bConstructorString;
		
		public int postConstructCalled = 0;
		public int preDestroyCalled = 0;

		@Inject
		public void contextSet(IEclipseContext context) {
			injectedContext = context;
		}
		
		@Inject
		public void setA(@Named("a") String aString) {
			this.aString = aString;
		}
		
		@Inject
		public void setB(@Named("b") String bString) {
			this.bString = bString;
		}
		
		@Inject
		public void setC(@Named("c") String cString) {
			this.cString = cString;
		}

		@Inject
		public void InjectedMethod(@Named("aConstructor") String aString, @Named("bConstructor") String bString) {
			aConstructorString = aString;
			bConstructorString = bString;
		}
		
		@PostConstruct 
		public void init() {
			postConstructCalled++;
		}
		
		@PreDestroy
		public void dispose() {
			preDestroyCalled++;
		}
	}
	
	static class TestInvokeClass {
		public String aString;
		public String bString;
		
		@Execute
		public String testMethod(@Named("a") String aString, @Named("b") String bString) {
			this.aString = aString;
			this.bString = bString;
			return aString + bString;
		}
	}
	
	public void testStaticMake() {
		IEclipseContext parentContext = EclipseContextFactory.create();
		parentContext.set("a", "abc");
		parentContext.set("aConstructor", "abcConstructor");
		parentContext.set("b", "bbc");
		
		IEclipseContext localContext = EclipseContextFactory.create();
		localContext.set("b", "123"); // local values override
		localContext.set("bConstructor", "123Constructor");
		localContext.set("c", "xyz");
		
		TestClass testObject = ContextInjectionFactory.make(TestClass.class, parentContext, localContext);

		assertEquals(localContext, testObject.injectedContext);
		assertEquals("abcConstructor", testObject.aConstructorString);
		assertEquals("123Constructor", testObject.bConstructorString);
		assertEquals("abc", testObject.aString);
		assertEquals("123", testObject.bString);
		assertEquals("xyz", testObject.cString);
		assertEquals(1, testObject.postConstructCalled);
		assertEquals(0, testObject.preDestroyCalled);
		
		// modify local context -> should have no effect
		localContext.set("b", "_123_");
		localContext.set("bConstructor", "_123Constructor_");
		localContext.set("c", "_xyz_");
		
		assertEquals("abcConstructor", testObject.aConstructorString);
		assertEquals("123Constructor", testObject.bConstructorString);
		assertEquals("abc", testObject.aString);
		assertEquals("123", testObject.bString);
		assertEquals("xyz", testObject.cString);
		assertEquals(1, testObject.postConstructCalled);
		assertEquals(0, testObject.preDestroyCalled);
		
		// dispose local context -> should have no effect
		localContext.dispose();
		
		assertEquals("abcConstructor", testObject.aConstructorString);
		assertEquals("123Constructor", testObject.bConstructorString);
		assertEquals("abc", testObject.aString);
		assertEquals("123", testObject.bString);
		assertEquals("xyz", testObject.cString);
		assertEquals(1, testObject.postConstructCalled);
		assertEquals(0, testObject.preDestroyCalled);
		
		// modify parent context -> should propagate
		parentContext.set("a", "_abc_");
		parentContext.set("b", "_bbc_");
		
		assertEquals("_abc_", testObject.aString);
		assertEquals("123", testObject.bString);
		
		// uninject from the parent context
		ContextInjectionFactory.uninject(testObject, parentContext);
		
		assertNull(testObject.injectedContext);
		assertNull(testObject.aString);
		
		assertEquals(1, testObject.postConstructCalled);
		assertEquals(1, testObject.preDestroyCalled);
		
		// further changes should have no effect
		parentContext.set("a", "+abc+");
		assertNull(testObject.aString);
		
		parentContext.dispose();
		assertEquals(1, testObject.postConstructCalled);
		assertEquals(1, testObject.preDestroyCalled);
	}
	
	public void testStaticInvoke() {
		IEclipseContext parentContext = EclipseContextFactory.create();
		parentContext.set("a", "abc");
		
		IEclipseContext localContext = EclipseContextFactory.create();
		localContext.set("b", "123");
		
		TestInvokeClass testObject = new TestInvokeClass();
		assertNull(testObject.aString);
		assertNull(testObject.bString);
		
		Object result = ContextInjectionFactory.invoke(testObject, Execute.class, parentContext, localContext, null);
		
		assertEquals("abc123", result);

		assertEquals("abc", testObject.aString);
		assertEquals("123", testObject.bString);
	}
}
