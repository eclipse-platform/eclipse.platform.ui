/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.services.internal.context;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.e4.core.contexts.ContextChangeEvent;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.IRunAndTrack;

/**
 * Test for changing a context's parent.
 */
public class ReparentingTest extends TestCase {
	public static Test suite() {
		return new TestSuite(ReparentingTest.class);
	}

	public ReparentingTest() {
		super();
	}

	public ReparentingTest(String name) {
		super(name);
	}

	/**
	 * Tests handling of a context function defined in the parent when the parent is changed to no
	 * longer have the function.
	 */
	public void testContextFunctionInParentRemove() {
		IEclipseContext parent = EclipseContextFactory.create("parent");
		final IEclipseContext child = parent.createChild("child");
		parent.set("sum", new AddContextFunction());
		parent.set("x", new Integer(3));
		parent.set("y", new Integer(3));
		child.set("x", new Integer(1));
		child.set("y", new Integer(1));
		assertEquals(6, ((Integer) parent.get("sum")).intValue());
		assertEquals(2, ((Integer) child.get("sum")).intValue());
		child.set(IContextConstants.PARENT, EclipseContextFactory.create());
		assertEquals(6, ((Integer) parent.get("sum")).intValue());
		assertNull("Expected null but was: " + child.get("sum"), child.get("sum"));
	}

	/**
	 * Tests handling of a context function defined in the parent when the parent is changed to have
	 * the function
	 */
	public void testContextFunctionInParentAdd() {
		// setup
		IEclipseContext parent = EclipseContextFactory.create();
		final IEclipseContext child = parent.createChild();
		child.set("x", new Integer(1));
		child.set("y", new Integer(1));
		assertEquals(null, parent.get("sum"));
		assertEquals(null, child.get("sum"));

		// switch parent
		IEclipseContext newParent = EclipseContextFactory.create();
		child.set(IContextConstants.PARENT, newParent);
		newParent.set("sum", new AddContextFunction());
		assertEquals(0, ((Integer) newParent.get("sum")).intValue());
		assertEquals(2, ((Integer) child.get("sum")).intValue());

		// changed values in parent shouldn't affect child
		newParent.set("x", new Integer(3));
		newParent.set("y", new Integer(3));
		assertEquals(6, ((Integer) newParent.get("sum")).intValue());
		assertEquals(2, ((Integer) child.get("sum")).intValue());
	}

	public void testContextFunctionNullBecomeParent() {
		final IEclipseContext child = EclipseContextFactory.create();
		child.set("sum", new AddContextFunction());
		assertEquals(0, ((Integer) child.get("sum")).intValue());
		IEclipseContext parent = EclipseContextFactory.create();
		parent.set("x", new Integer(3));
		parent.set("y", new Integer(3));
		child.set(IContextConstants.PARENT, parent);
		assertEquals(6, ((Integer) child.get("sum")).intValue());

	}

	public void testContextFunctionParentBecomeNull() {
		IEclipseContext parent = EclipseContextFactory.create();
		final IEclipseContext child = parent.createChild();
		parent.set("x", new Integer(3));
		parent.set("y", new Integer(3));
		child.set("sum", new AddContextFunction());
		assertEquals(6, ((Integer) child.get("sum")).intValue());
		child.set(IContextConstants.PARENT, null);
		assertEquals(0, ((Integer) child.get("sum")).intValue());

	}

	public void testContextFunctionSwitchParent() {
		IEclipseContext parent = EclipseContextFactory.create();
		final IEclipseContext child = parent.createChild();
		parent.set("x", new Integer(3));
		parent.set("y", new Integer(3));
		child.set("sum", new AddContextFunction());
		assertEquals(6, ((Integer) child.get("sum")).intValue());
		IEclipseContext newParent = EclipseContextFactory.create();
		newParent.set("x", new Integer(1));
		newParent.set("y", new Integer(1));
		child.set(IContextConstants.PARENT, newParent);
		assertEquals(2, ((Integer) child.get("sum")).intValue());
	}

	/**
	 * Tests a child switching from a null parent to a non-null parent.
	 */
	public void testRunAndTrackNullBecomesParent() {
		final String[] value = new String[1];
		final IEclipseContext child = EclipseContextFactory.create();
		child.runAndTrack(new IRunAndTrack() {
			public boolean notify(ContextChangeEvent event) {
				value[0] = (String) child.get("x");
				return true;
			}
		}, null);
		assertEquals(null, value[0]);
		IEclipseContext parent = EclipseContextFactory.create();
		parent.set("x", "newParent");
		child.set(IContextConstants.PARENT, parent);
		assertEquals("newParent", value[0]);
	}

	/**
	 * Tests a child switching from a non-null parent to a null parent.
	 */
	public void testRunAndTrackParentBecomeNull() {
		final String[] value = new String[1];
		IEclipseContext parent = EclipseContextFactory.create();
		final IEclipseContext child = parent.createChild();
		parent.set("x", "oldParent");
		child.runAndTrack(new IRunAndTrack() {
			public boolean notify(ContextChangeEvent event) {
				value[0] = (String) child.get("x");
				return true;
			}
		}, null);
		assertEquals("oldParent", value[0]);
		child.set(IContextConstants.PARENT, null);
		assertNull(value[0]);
	}

	public void testRunAndTrackSwitchParent() {
		final String[] value = new String[1];
		IEclipseContext parent = EclipseContextFactory.create();
		final IEclipseContext child = parent.createChild();
		parent.set("x", "oldParent");
		child.runAndTrack(new IRunAndTrack() {
			public boolean notify(ContextChangeEvent event) {
				value[0] = (String) child.get("x");
				return true;
			}
		}, null);
		assertEquals("oldParent", value[0]);
		IEclipseContext newParent = EclipseContextFactory.create();
		newParent.set("x", "newParent");
		child.set(IContextConstants.PARENT, newParent);
		assertEquals("newParent", value[0]);
	}

	/**
	 * Tests an object consuming simple values from a parent context, and a parent change causes a
	 * change in simple values. TODO: Still fails
	 */
	public void testInjectSwitchParent() {

		IEclipseContext oldParent = EclipseContextFactory.create();
		oldParent.set("String", "oldField");
		oldParent.set(String.class.getName(), "old");
		oldParent.set(Float.class.getName(), new Float(12.3));
		IEclipseContext newParent = EclipseContextFactory.create();
		newParent.set("String", "newField");
		newParent.set(String.class.getName(), "new");
		newParent.set(Float.class.getName(), new Float(34.5));
		IEclipseContext child = oldParent.createChild();

		ObjectSuperClass object = new ObjectSuperClass();
		ContextInjectionFactory.inject(object, child);
		assertEquals(1, object.setStringCalled);
		assertEquals("old", object.getStringViaMethod());

		child.set(IContextConstants.PARENT, newParent);
		assertEquals("new", object.getStringViaMethod());
		assertEquals(2, object.setStringCalled);

	}

	/**
	 * Tests an object consuming services from a grandparent. A parent switch where the grandparent
	 * stays unchanged should ideally not cause changes for the injected object.
	 */
	public void testInjectSwitchParentSameGrandparent() {
		IEclipseContext grandpa = EclipseContextFactory.create();
		grandpa.set("String", "field");
		grandpa.set(String.class.getName(), "s");
		grandpa.set(Float.class.getName(), new Float(12.3));

		IEclipseContext oldParent = grandpa.createChild();
		IEclipseContext newParent = grandpa.createChild();
		IEclipseContext child = oldParent.createChild();

		ObjectSuperClass object = new ObjectSuperClass();
		ContextInjectionFactory.inject(object, child);
		assertEquals(1, object.setStringCalled);

		child.set(IContextConstants.PARENT, newParent);
		assertEquals(1, object.setStringCalled);
	}
}
