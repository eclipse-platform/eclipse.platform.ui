/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.tests.contexts;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.osgi.framework.FrameworkUtil;

public class EclipseContextTest extends TestCase {

	private static class ComputedValueBar extends ContextFunction {
		public Object compute(IEclipseContext context, String contextKey) {
			return context.get("bar");
		}
	}

	private IEclipseContext context;
	private IEclipseContext parentContext;

	private int runCounter;

	protected void setUp() throws Exception {
		super.setUp();
		parentContext = EclipseContextFactory.create(getName() + "-parent");
		context = parentContext.createChild(getName());
		runCounter = 0;
	}

	public void testContainsKey() {
		assertFalse("1.0", context.containsKey("function"));
		assertFalse("1.1", context.containsKey("separator"));

		context.set("separator", ",");
		assertTrue("2.1", context.containsKey("separator"));

		// null value is still a value
		context.set("separator", null);
		assertTrue("3.0", context.containsKey("separator"));

		context.remove("separator");
		assertFalse("4.0", context.containsKey("separator"));
	}

	public void testGet() {
		assertNull(context.get("foo"));
		context.set("foo", "bar");
		assertEquals("bar", context.get("foo"));
		assertNull(parentContext.get("foo"));
		context.remove("foo");
		assertNull(context.get("foo"));
		parentContext.set("foo", "bar");
		assertEquals("bar", context.get("foo"));
		context.set("foo", new ComputedValueBar());
		assertNull(context.get("foo"));
		context.set("bar", "baz");
		assertEquals("baz", context.get("foo"));
	}

	public void testGetLocal() {
		assertNull(context.getLocal("foo"));
		context.set("foo", "bar");
		assertEquals("bar", context.getLocal("foo"));
		assertNull(parentContext.getLocal("foo"));
		context.remove("foo");
		assertNull(context.getLocal("foo"));
		parentContext.set("foo", "bar");
		assertNull(context.getLocal("foo"));
		context.set("foo", new ComputedValueBar());
		assertNull(context.getLocal("foo"));
		context.set("bar", "baz");
		assertEquals("baz", context.getLocal("foo"));
	}

	/**
	 * Tests that a context no longer looks up values from its parent when disposed.
	 */
	public void testDisposeRemovesParentReference() {
		assertNull(context.get("foo"));
		parentContext.set("foo", "bar");
		assertEquals("bar", context.get("foo"));
		context.dispose();
		assertNull(context.get("foo"));
		assertTrue(((EclipseContext)parentContext).getChildren().isEmpty());
	}

	/**
	 * Tests handling of a context function defined in the parent that uses values defined in the
	 * child
	 */
	public void testContextFunctionInParent() {
		IEclipseContext parent = EclipseContextFactory.create();
		final IEclipseContext child = parent.createChild();
		parent.set("sum", new AddContextFunction());
		parent.set("x", new Integer(3));
		parent.set("y", new Integer(3));
		child.set("x", new Integer(1));
		child.set("y", new Integer(1));
		assertEquals(6, ((Integer) parent.get("sum")).intValue());
		assertEquals(2, ((Integer) child.get("sum")).intValue());
		child.set("x", new Integer(5));
		assertEquals(6, ((Integer) parent.get("sum")).intValue());
		assertEquals(6, ((Integer) child.get("sum")).intValue());
		child.remove("x");
		assertEquals(6, ((Integer) parent.get("sum")).intValue());
		assertEquals(4, ((Integer) child.get("sum")).intValue());
		parent.set("x", new Integer(10));
		assertEquals(13, ((Integer) parent.get("sum")).intValue());
		assertEquals(11, ((Integer) child.get("sum")).intValue());
	}

	public void testRunAndTrack() {
		final Object[] value = new Object[1];
		context.runAndTrack(new RunAndTrack() {
			public boolean changed(IEclipseContext context) {
				runCounter++;
				value[0] = context.get("foo");
				return true;
			}
		});
		assertEquals(1, runCounter);
		assertEquals(null, value[0]);
		context.set("foo", "bar");
		assertEquals(2, runCounter);
		assertEquals("bar", value[0]);
		context.remove("foo");
		assertEquals(3, runCounter);
		assertEquals(null, value[0]);
		context.set("foo", new ContextFunction() {
			public Object compute(IEclipseContext context, String contextKey) {
				return context.get("bar");
			}
		});
		assertEquals(4, runCounter);
		assertEquals(null, value[0]);
		context.set("bar", "baz");
		assertEquals(5, runCounter);
		assertEquals("baz", value[0]);
		context.set("bar", "baf");
		assertEquals(6, runCounter);
		assertEquals("baf", value[0]);
		context.remove("bar");
		assertEquals(7, runCounter);
		assertEquals(null, value[0]);
		parentContext.set("bar", "bam");
		assertEquals(8, runCounter);
		assertEquals("bam", value[0]);
	}

	/**
	 * Tests registering a single run and track instance multiple times with the same context.
	 */
	public void testRegisterRunAndTrackTwice() {
		final Object[] value = new Object[1];
		RunAndTrack runnable = new RunAndTrack() {
			public boolean changed(IEclipseContext context) {
				runCounter++;
				value[0] = context.get("foo");
				return true;
			}
		};
		context.runAndTrack(runnable);
		assertEquals(1, runCounter);
		context.runAndTrack(runnable);
		assertEquals(2, runCounter);
		assertEquals(null, value[0]);
		context.set("foo", "bar");
		assertEquals(3, runCounter);
		assertEquals("bar", value[0]);
		context.remove("foo");
		assertEquals(4, runCounter);

	}

	public void testRunAndTrackMultipleValues() {
		IEclipseContext parent = EclipseContextFactory.create("ParentContext");
		final IEclipseContext child = parent.createChild("ChildContext");
		parent.set("parentValue", "x");
		child.set("childValue", "x");
		RunAndTrack runnable = new RunAndTrack() {
			public boolean changed(IEclipseContext context) {
				runCounter++;
				if (runCounter < 2) {
					child.get("childValue");
					return true;
				}
				if (runCounter < 3) {
					child.get("parentValue");
					return true;
				}
				return false;
			}
		};
		child.runAndTrack(runnable);
		assertEquals(1, runCounter);
		child.set("childValue", "z");
		assertEquals(2, runCounter);
		parent.set("parentValue", "z");
		assertEquals(3, runCounter);
	}

	public void testModify() {
		IEclipseContext grandParent = EclipseContextFactory.create();
		IEclipseContext parent = grandParent.createChild();
		IEclipseContext child = parent.createChild();

		child.set("a", "a1");
		parent.set("b", "b2");
		grandParent.set("c", "c3");

		child.declareModifiable("a");
		parent.declareModifiable("b");
		grandParent.declareModifiable("c");

		// test pre-conditions
		assertNull(grandParent.get("b"));
		assertEquals("b2", parent.get("b"));
		assertEquals("b2", child.get("b"));
		assertNull(child.getLocal("b"));

		// modify value on the middle node via its child
		child.modify("b", "abc");

		assertFalse(grandParent.containsKey("b"));
		assertEquals("abc", parent.get("b"));
		assertEquals("abc", child.get("b"));
		assertNull(child.getLocal("b"));

		// modifying non-exist values adds it to the context
		child.modify("d", "123");

		assertFalse(grandParent.containsKey("d"));
		assertFalse(parent.containsKey("d"));
		assertNull(parent.get("d"));
		assertEquals("123", child.get("d"));

		// edge conditions: modify value in the top node
		grandParent.modify("c", "cNew");
		assertTrue(grandParent.containsKey("c"));
		assertEquals("cNew", grandParent.get("c"));
		assertNull(parent.getLocal("c"));
		assertNull(child.getLocal("c"));
		assertTrue(child.containsKey("c"));

		// edge condition: modify value in the leaf node
		child.modify("a", "aNew");
		assertTrue(child.containsKey("a"));
		assertFalse(parent.containsKey("a"));
		assertFalse(grandParent.containsKey("a"));
		assertEquals("aNew", child.get("a"));
		assertNull(parent.get("a"));

		// test access rules
		child.set("aNo", "a1");
		parent.set("bNo", "b2");
		grandParent.set("cNo", "c3");

		boolean exception = false;
		try {
			child.modify("bNo", "new");
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);

		exception = false;
		try {
			grandParent.modify("cNo", "new");
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);

		exception = false;
		try {
			child.modify("aNo", "new");
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
	}

	public void testRemoveValueComputationOnDispose() {
		IEclipseContext parent = EclipseContextFactory.create("ParentContext");
		IEclipseContext child = parent.createChild("ChildContext");
		parent.set("x", new Integer(1));
		parent.set("y", new Integer(1));
		parent.set("sum", new AddContextFunction());

		child.get("sum");
		assertEquals(1, listenersCount(child));
		child.dispose();
		assertEquals(0, listenersCount(parent));
	}
	
	public void testNullInheritance() {
		IEclipseContext parent = EclipseContextFactory.create("ParentContext");
		IEclipseContext child = parent.createChild("ChildContext");
		parent.set("x", new Integer(1));
		child.set("x", null);
		assertNull(child.get("x"));
	}

	public void testGetCFNotAValue() {
		IEclipseContext context = EclipseContextFactory.create("ParentContext");
		context.set("x", new ContextFunction() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.e4.core.contexts.ContextFunction#compute(org.eclipse
			 * .e4.core.contexts.IEclipseContext, java.lang.String)
			 */
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				return IInjector.NOT_A_VALUE;
			}
		});

		// must call several times as the underlying ValueComputation wrapper is
		// created on the first time, but re-used for subsequent calls.
		assertNull(context.get("x"));
		assertNull(context.get("x"));
		assertNull(context.get("x"));
		context.dispose();
	}

	public void testGetCFNotAValueToParent() {
		IEclipseContext parent = EclipseContextFactory.create("ParentContext");
		IEclipseContext child = parent.createChild();
		parent.set("x", new Integer(1));
		child.set("x", new ContextFunction() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.e4.core.contexts.ContextFunction#compute(org.eclipse
			 * .e4.core.contexts.IEclipseContext, java.lang.String)
			 */
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				return IInjector.NOT_A_VALUE;
			}
		});

		assertEquals(1, child.get("x"));
		parent.dispose();
	}
	
	public void testContextFunctionOrdering() {
		IEclipseContext osgiContext = EclipseContextFactory.getServiceContext(FrameworkUtil.getBundle(getClass()).getBundleContext());
		assertEquals("High",osgiContext.get("test.contextfunction.ranking"));
	}

	private int listenersCount(IEclipseContext context) {
		return ((EclipseContext) context).getListeners().size();		
	}
}
