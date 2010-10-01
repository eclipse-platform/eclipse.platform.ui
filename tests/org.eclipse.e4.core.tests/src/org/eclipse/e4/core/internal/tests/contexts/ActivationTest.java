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

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

public class ActivationTest extends TestCase {

	static public class TestRAT extends ContextFunction {
		public Object compute(IEclipseContext context) {
			IEclipseContext activeContext = context.getActive();
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

		assertEquals(rootContext, rootContext.getActive());
		assertEquals("root", rootContext.get("testRAT"));

		child12.activate();
		assertEquals(child12, rootContext.getActive());
		assertEquals("child12", rootContext.get("testRAT"));

		assertEquals(child2, child2.getActive());
		assertEquals("child2", child2.get("testRAT"));

		child21.activate();
		assertEquals(child21, rootContext.getActive());
		assertEquals("child21", rootContext.get("testRAT"));
		assertEquals(child12, child1.getActive());
		assertEquals("child12", child1.get("testRAT"));
		assertEquals(child21, child2.getActive());
		assertEquals("child21", child2.get("testRAT"));

		child21.deactivate();
		assertEquals(child2, rootContext.getActive());
		assertEquals("child2", rootContext.get("testRAT"));
		assertEquals(child12, child1.getActive());
		assertEquals("child12", child1.get("testRAT"));
		assertEquals(child2, child2.getActive());
		assertEquals("child2", child2.get("testRAT"));

		child22.activate();
		assertEquals(child22, rootContext.getActive());
		assertEquals("child22", rootContext.get("testRAT"));
		assertEquals(child12, child1.getActive());
		assertEquals("child12", child1.get("testRAT"));
		assertEquals(child22, child2.getActive());
		assertEquals("child22", child2.get("testRAT"));

		child11.activate();
		assertEquals(child11, rootContext.getActive());
		assertEquals("child11", rootContext.get("testRAT"));
		assertEquals(child11, child1.getActive());
		assertEquals("child11", child1.get("testRAT"));
		assertEquals(child22, child2.getActive());
		assertEquals("child22", child2.get("testRAT"));

		child11.deactivate();
		assertEquals(child12, rootContext.getActive());
		assertEquals("child12", rootContext.get("testRAT"));
		assertEquals(child12, child1.getActive());
		assertEquals("child12", child1.get("testRAT"));
		assertEquals(child22, child2.getActive());
		assertEquals("child22", child2.get("testRAT"));

		child1.dispose();
		assertEquals(child22, rootContext.getActive());
		assertEquals("child22", rootContext.get("testRAT"));
		assertEquals(child22, child2.getActive());
		assertEquals("child22", child2.get("testRAT"));
	}
}
