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

package org.eclipse.e4.core.internal.tests.contexts;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;

public class ActivationTest extends TestCase {

	static public class TestRAT extends ContextFunction {
		public Object compute(IEclipseContext context) {
			IEclipseContext activeContext = context.getActiveLeaf();
			// returns name of the context
			return activeContext.get("debugString"); 
		}
	}

	public void testContextActivation() {
		IEclipseContext rootContext = EclipseContextFactory.create("root");
		rootContext.set("testRAT", new TestRAT());

		IEclipseContext child1 = rootContext.createChild("child1");
		IEclipseContext child11 = child1.createChild("child11");
		IEclipseContext child12 = child1.createChild("child12");

		IEclipseContext child2 = rootContext.createChild("child2");
		IEclipseContext child21 = child2.createChild("child21");
		IEclipseContext child22 = child2.createChild("child22");

		assertEquals(rootContext, rootContext.getActiveLeaf());
		assertNull(rootContext.getActiveChild());
		assertEquals("root", rootContext.get("testRAT"));

		child12.activateBranch();
		assertEquals(child12, rootContext.getActiveLeaf());
		assertEquals(child1, rootContext.getActiveChild());
		assertEquals("child12", rootContext.get("testRAT"));

		assertEquals(child2, child2.getActiveLeaf());
		assertNull(child2.getActiveChild());
		assertEquals("child2", child2.get("testRAT"));

		child21.activateBranch();
		assertEquals(child21, rootContext.getActiveLeaf());
		assertEquals(child2, rootContext.getActiveChild());
		assertEquals("child21", rootContext.get("testRAT"));
		assertEquals(child12, child1.getActiveLeaf());
		assertEquals(child12, child1.getActiveChild());
		assertEquals("child12", child1.get("testRAT"));
		assertEquals(child21, child2.getActiveLeaf());
		assertEquals(child21, child2.getActiveChild());
		assertEquals("child21", child2.get("testRAT"));

		child21.deactivate();
		assertEquals(child2, rootContext.getActiveLeaf());
		assertEquals("child2", rootContext.get("testRAT"));
		assertEquals(child12, child1.getActiveLeaf());
		assertEquals("child12", child1.get("testRAT"));
		assertEquals(child2, child2.getActiveLeaf());
		assertNull(child2.getActiveChild());
		assertEquals("child2", child2.get("testRAT"));

		child22.activateBranch();
		assertEquals(child22, rootContext.getActiveLeaf());
		assertEquals("child22", rootContext.get("testRAT"));
		assertEquals(child12, child1.getActiveLeaf());
		assertEquals("child12", child1.get("testRAT"));
		assertEquals(child22, child2.getActiveLeaf());
		assertEquals("child22", child2.get("testRAT"));

		child11.activateBranch();
		assertEquals(child11, rootContext.getActiveLeaf());
		assertEquals("child11", rootContext.get("testRAT"));
		assertEquals(child11, child1.getActiveLeaf());
		assertEquals("child11", child1.get("testRAT"));
		assertEquals(child22, child2.getActiveLeaf());
		assertEquals("child22", child2.get("testRAT"));

		child11.deactivate();
		assertEquals(child1, rootContext.getActiveLeaf());
		assertEquals("child1", rootContext.get("testRAT"));
		assertEquals(child1, child1.getActiveLeaf());
		assertEquals("child1", child1.get("testRAT"));
		assertEquals(child22, child2.getActiveLeaf());
		assertEquals("child22", child2.get("testRAT"));

		child1.dispose();
		assertNull(rootContext.getActiveChild());
		child2.activateBranch();
		assertEquals(child22, rootContext.getActiveLeaf());
		assertEquals("child22", rootContext.get("testRAT"));
		assertEquals(child22, child2.getActiveLeaf());
		assertEquals("child22", child2.get("testRAT"));
	}
	
	public void testGetActive() {
		IEclipseContext root = EclipseContextFactory.create("root");
		
		IEclipseContext child1 = root.createChild("child1");
		IEclipseContext child11 = child1.createChild("child11");
		IEclipseContext child12 = child1.createChild("child12");
		
		IEclipseContext child2 = root.createChild("child2");
		IEclipseContext child21 = child2.createChild("child21");
		IEclipseContext child22 = child2.createChild("child22");
		
		child11.set("var", "1");
		child12.set("var", "2");
		child1.set("var", "3");
		child21.set("var", "4");
		child22.set("var", "5");
		child2.set("var", "6");
		root.set("var", "7");
		
		// nothing is active - we get value from the node
		assertEquals("3", child1.getActive("var"));
		
		child11.activateBranch();
		assertEquals("1", child1.getActive("var"));
		child12.activateBranch();
		assertEquals("2", child1.getActive("var"));
		child22.activateBranch();
		assertEquals("5", child2.getActive("var"));
	}
	
	public void testGetActiveRAT() {
		IEclipseContext root = EclipseContextFactory.create("root");
		
		IEclipseContext child1 = root.createChild("child1");
		IEclipseContext child11 = child1.createChild("child11");
		IEclipseContext child12 = child1.createChild("child12");
		
		IEclipseContext child2 = root.createChild("child2");
		IEclipseContext child21 = child2.createChild("child21");
		IEclipseContext child22 = child2.createChild("child22");
		
		child11.set("var", "1");
		child12.set("var", "2");
		child1.set("var", "3");
		child21.set("var", "4");
		child22.set("var", "5");
		child2.set("var", "6");
		root.set("var", "7");
		
		final String[] result = new String[1];
		
		child1.runAndTrack(new RunAndTrack() {
			public boolean changed(IEclipseContext context) {
				result[0] = (String) context.getActive("var");
				return true;
			}});
		
		// nothing is active - we get value from the node
		assertEquals("3", result[0]);
		
		child11.activateBranch();
		assertEquals("1", result[0]);
		child12.activateBranch();
		assertEquals("2", result[0]);
		child22.activateBranch();
		assertEquals("2", result[0]);
	}
	
	public void testGetActiveRATNumberOfCalls() {
		IEclipseContext root = EclipseContextFactory.create("root");
		
		IEclipseContext child1 = root.createChild("child1");
		IEclipseContext child11 = child1.createChild("child11");
		IEclipseContext child12 = child1.createChild("child12");
		
		IEclipseContext child2 = root.createChild("child2");
		IEclipseContext child21 = child2.createChild("child21");
		IEclipseContext child22 = child2.createChild("child22");
		
		child11.set("var", "1");
		child12.set("var", "1");
		child1.set("var", "3");
		child21.set("var", "4");
		child22.set("var", "4");
		child2.set("var", "6");
		root.set("var", "7");
		
		final String[] result = new String[1];
		final int[] called = new int[1];
		called[0] = 0;
		
		child1.runAndTrack(new RunAndTrack() {
			public boolean changed(IEclipseContext context) {
				result[0] = (String) context.getActive("var");
				called[0]++;
				return true;
			}});
		
		// nothing is active - we get value from the node
		assertEquals("3", result[0]);
		assertEquals(1, called[0]);
		
		child11.activateBranch();
		assertEquals("1", result[0]);
		assertEquals(2, called[0]);
		
		child12.activateBranch();
		assertEquals("1", result[0]);
		assertEquals(2, called[0]);
		
		child22.activateBranch();
		assertEquals("1", result[0]);
		assertEquals(2, called[0]);
		
		child21.activateBranch();
		assertEquals("1", result[0]);
		assertEquals(2, called[0]);
	}

	public static class ActiveInject {
		//@Inject @Named("var")
		public String value;
		
		@Inject
		public void setValue(@Named("var") String value) {
			this.value = value;
		}
	}
}
