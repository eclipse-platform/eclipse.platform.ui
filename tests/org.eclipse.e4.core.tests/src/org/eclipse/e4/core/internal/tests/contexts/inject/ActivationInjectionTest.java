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

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;

public class ActivationInjectionTest extends TestCase {

	static public class TestRAT extends ContextFunction {
		public Object compute(IEclipseContext context, String contextKey) {
			IEclipseContext activeContext = context.getActiveLeaf();
			// returns name of the context
			return "_" + activeContext.get("debugString") + "_";
		}
	}
	
	static public class TestObject {
		
		public String name;
		
		@Inject
		public TestObject() {
			//
		}
		
		@Inject 
		public void setActiveContextName(@Named("testRAT") String name) {
			this.name = name;
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

		TestObject testObject = ContextInjectionFactory.make(TestObject.class, rootContext);

		child12.activateBranch();
		assertEquals(child12, rootContext.getActiveLeaf());
		assertEquals("_child12_", testObject.name);

		child21.activateBranch();
		assertEquals(child21, rootContext.getActiveLeaf());
		assertEquals("_child21_", testObject.name);

		child21.deactivate();
		assertEquals(child2, rootContext.getActiveLeaf());
		assertEquals("_child2_", testObject.name);

		child22.activateBranch();
		assertEquals(child22, rootContext.getActiveLeaf());
		assertEquals("_child22_", testObject.name);

		child11.activateBranch();
		assertEquals(child11, rootContext.getActiveLeaf());
		assertEquals("_child11_", testObject.name);

		child11.deactivate();
		assertEquals(child1, rootContext.getActiveLeaf());
		assertEquals("_child1_", testObject.name);

		child1.dispose();
		child2.activateBranch();
		assertEquals(child22, rootContext.getActiveLeaf());
		assertEquals("_child22_", testObject.name);
	}
	
	static public class TestObjectInject {
		public String name;
		@Inject
		public TestObjectInject() {
			//
		}
		@Inject 
		public void setActiveContextName(@Active @Named("debugString") String name) {
			this.name = "_" + name + "_";
		}
	}
	
	public void testActivationInjection() {
		IEclipseContext rootContext = EclipseContextFactory.create("root");

		IEclipseContext child1 = rootContext.createChild("child1");
		IEclipseContext child11 = child1.createChild("child11");
		IEclipseContext child12 = child1.createChild("child12");

		IEclipseContext child2 = rootContext.createChild("child2");
		IEclipseContext child21 = child2.createChild("child21");
		IEclipseContext child22 = child2.createChild("child22");

		TestObjectInject testObject = ContextInjectionFactory.make(TestObjectInject.class, rootContext);

		child12.activateBranch();
		assertEquals(child12, rootContext.getActiveLeaf());
		assertEquals("_child12_", testObject.name);

		child21.activateBranch();
		assertEquals(child21, rootContext.getActiveLeaf());
		assertEquals("_child21_", testObject.name);

		child21.deactivate();
		assertEquals(child2, rootContext.getActiveLeaf());
		assertEquals("_child2_", testObject.name);

		child22.activateBranch();
		assertEquals(child22, rootContext.getActiveLeaf());
		assertEquals("_child22_", testObject.name);

		child11.activateBranch();
		assertEquals(child11, rootContext.getActiveLeaf());
		assertEquals("_child11_", testObject.name);

		child11.deactivate();
		assertEquals(child1, rootContext.getActiveLeaf());
		assertEquals("_child1_", testObject.name);

		child1.dispose();
		child2.activateBranch();
		assertEquals(child22, rootContext.getActiveLeaf());
		assertEquals("_child22_", testObject.name);
	}
	
	static public class TestInject {
		@Optional @Inject @Active 
		public Integer number;
	}
	
	public void testInjection() {
		IEclipseContext rootContext = EclipseContextFactory.create("root");

		IEclipseContext child1 = rootContext.createChild("child1");
		child1.set(Integer.class, new Integer(1));
		IEclipseContext child11 = child1.createChild("child11");
		child11.set(Integer.class, new Integer(2));
		IEclipseContext child12 = child1.createChild("child12");
		child12.set(Integer.class, new Integer(3));

		IEclipseContext child2 = rootContext.createChild("child2");
		child2.set(Integer.class, new Integer(4));
		IEclipseContext child21 = child2.createChild("child21");
		child21.set(Integer.class, new Integer(5));
		IEclipseContext child22 = child2.createChild("child22");
		child22.set(Integer.class, new Integer(6));

		TestInject testObjectRoot = ContextInjectionFactory.make(TestInject.class, rootContext);
		TestInject testObjectChild1 = ContextInjectionFactory.make(TestInject.class, child1);
		TestInject testObjectChild2 = ContextInjectionFactory.make(TestInject.class, child2);

		child12.activateBranch();
		assertEquals(new Integer(3), testObjectRoot.number);
		assertEquals(new Integer(3), testObjectChild1.number);
		assertEquals(new Integer(4), testObjectChild2.number);

		child21.activateBranch();
		assertEquals(new Integer(5), testObjectRoot.number);
		assertEquals(new Integer(3), testObjectChild1.number);
		assertEquals(new Integer(5), testObjectChild2.number);

		child21.deactivate();
		assertEquals(new Integer(4), testObjectRoot.number);
		assertEquals(new Integer(3), testObjectChild1.number);
		assertEquals(new Integer(4), testObjectChild2.number);

		child22.activateBranch();
		assertEquals(new Integer(6), testObjectRoot.number);
		assertEquals(new Integer(3), testObjectChild1.number);
		assertEquals(new Integer(6), testObjectChild2.number);

		child11.activateBranch();
		assertEquals(new Integer(2), testObjectRoot.number);
		assertEquals(new Integer(2), testObjectChild1.number);
		assertEquals(new Integer(6), testObjectChild2.number);

		child11.deactivate();
		assertEquals(new Integer(1), testObjectRoot.number);
		assertEquals(new Integer(1), testObjectChild1.number);
		assertEquals(new Integer(6), testObjectChild2.number);

		child1.dispose();
		child2.activateBranch();
		assertEquals(new Integer(6), testObjectRoot.number);
		assertEquals(new Integer(6), testObjectChild2.number);
	}

}
