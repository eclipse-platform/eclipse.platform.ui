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

package org.eclipse.e4.core.internal.tests.contexts.inject;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 * Tests updates of injected values and calls to runnables
 */
public class InjectionUpdateTest extends TestCase {

	private IEclipseContext c1; // common root
	private IEclipseContext c21; // dependents of root - path 1
	private IEclipseContext c22; // dependents of root - path 2

	static public class PropagationTest {

		public int called = 0;
		public String in; 

		@Inject
		public PropagationTest() {
			// placeholder
		}

		@Inject
		public void setCalculated(@Named("calculated") String string) {
			called++;
			in = string;
		}
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		c1 = EclipseContextFactory.create("c1");
		c1.set("id", "c1");

		c21 = c1.createChild("c21");
		c21.set("id", "c21");
		c1.set("c21", c21);

		c22 = c1.createChild("c22");
		c22.set("id", "c22");
		c1.set("c22", c22);
	}
	
	public void testPropagation() {
		c1.set("base", "abc");

		c21.set("derived1", new IContextFunction() {
			public Object compute(IEclipseContext context, Object[] arguments) {
				String baseString = (String) context.get("base");
				return baseString.charAt(0) + "_";
			}});

		c22.set("derived2", new IContextFunction() {
			public Object compute(IEclipseContext context, Object[] arguments) {
				String baseString = (String) context.get("base");
				return "_" + baseString.charAt(baseString.length() - 1);
			}});

		c1.set("calculated", new IContextFunction() {
			public Object compute(IEclipseContext context, Object[] arguments) {
				IEclipseContext context21 = (IEclipseContext) context.get("c21");
				String derived1 = (String) context21.get("derived1");
				
				IEclipseContext context22 = (IEclipseContext) context.get("c22");
				String derived2 = (String) context22.get("derived2");
				return derived1 + derived2;
			}});

		PropagationTest testObject = (PropagationTest) ContextInjectionFactory.make(PropagationTest.class, c1);
		assertNotNull(testObject);
		assertEquals(1, testObject.called);
		assertEquals("a__c", testObject.in);

		c1.set("base", "123"); // this should result in only one injection call
		assertEquals(2, testObject.called);
		assertEquals("1__3", testObject.in);

		c1.set("base", "xyz");
		assertEquals(3, testObject.called);
		assertEquals("x__z", testObject.in);
	}
}
