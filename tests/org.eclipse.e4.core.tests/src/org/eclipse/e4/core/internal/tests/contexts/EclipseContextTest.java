/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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

import org.eclipse.e4.core.contexts.ContextChangeEvent;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.IRunAndTrack;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.core.internal.contexts.TestHelper;

public class EclipseContextTest extends TestCase {

	private static class ComputedValueBar extends ContextFunction {
		public Object compute(IEclipseContext context, Object[] arguments) {
			return context.get("bar");
		}
	}

	private static class ConcatFunction extends ContextFunction {
		public Object compute(IEclipseContext context, Object[] arguments) {
			String separator = (String) context.get("separator");
			StringBuffer result = new StringBuffer();
			for (int i = 0; i < arguments.length; i++) {
				if (i > 0) {
					result.append(separator);
				}
				result.append(arguments[i]);
			}
			return result.toString();
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

		context.set("function", new ConcatFunction());
		context.set("separator", ",");
		assertTrue("2.0", context.containsKey("function"));
		assertTrue("2.1", context.containsKey("separator"));

		// null value is still a value
		context.set("separator", null);
		assertTrue("3.0", context.containsKey("separator"));

		context.remove("separator");
		assertFalse("4.0", context.containsKey("separator"));
	}

	public void testFunctions() {
		context.set("function", new ConcatFunction());
		context.set("separator", ",");
		assertEquals("x", context.get("function", new String[] { "x" }));
		assertEquals("x,y", context.get("function", new String[] { "x", "y" }));
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
		((IDisposable) context).dispose();
		assertNull(context.get("foo"));
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

	/**
	 * Tests that listeners receive appropriate {@link ContextChangeEvent} instances that reflect
	 * the change in the context.
	 */
	public void testContextEvents() {
		final Object[] value = new Object[1];
		final int[] eventType = new int[] { 0 };
		IRunAndTrack runnable = new IRunAndTrack() {
			public boolean notify(ContextChangeEvent event) {
				runCounter++;
				eventType[0] = event.getEventType();
				value[0] = context.get("foo");
				return true;
			}
		};
		context.runAndTrack(runnable, null);
		assertEquals(1, runCounter);
		assertEquals(ContextChangeEvent.INITIAL, eventType[0]);
		context.set("foo", "bar");
		assertEquals(2, runCounter);
		assertEquals(ContextChangeEvent.ADDED, eventType[0]);
		assertEquals("bar", value[0]);
		context.remove("foo");
		assertEquals(ContextChangeEvent.REMOVED, eventType[0]);
		assertEquals(3, runCounter);
		((IDisposable) context).dispose();
		assertEquals(4, runCounter);
		assertEquals(ContextChangeEvent.DISPOSE, eventType[0]);

	}

	public void testRunAndTrack() {
		final Object[] value = new Object[1];
		context.runAndTrack(new IRunAndTrack() {
			public boolean notify(ContextChangeEvent event) {
				runCounter++;
				value[0] = context.get("foo");
				return true;
			}
		}, null);
		assertEquals(1, runCounter);
		assertEquals(null, value[0]);
		context.set("foo", "bar");
		assertEquals(2, runCounter);
		assertEquals("bar", value[0]);
		context.remove("foo");
		assertEquals(3, runCounter);
		assertEquals(null, value[0]);
		context.set("foo", new ContextFunction() {
			public Object compute(IEclipseContext context, Object[] arguments) {
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
		IRunAndTrack runnable = new IRunAndTrack() {
			public boolean notify(ContextChangeEvent event) {
				runCounter++;
				value[0] = context.get("foo");
				return true;
			}
		};
		context.runAndTrack(runnable, null);
		assertEquals(1, runCounter);
		context.runAndTrack(runnable, null);
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
		IRunAndTrack runnable = new IRunAndTrack() {
			public boolean notify(ContextChangeEvent event) {
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
		child.runAndTrack(runnable, null);
		assertEquals(1, runCounter);
		child.set("childValue", "z");
		assertEquals(2, runCounter);
		parent.set("parentValue", "z");
		assertEquals(3, runCounter);
		// at this point we should no longer be listening
		child.set("childValue", "y");
		assertEquals(3, runCounter);
		parent.set("parentValue", "y");
		assertEquals(3, runCounter);
		assertTrue(TestHelper.getListeners(child).isEmpty());
		assertTrue(TestHelper.getListeners(parent).isEmpty());
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
		assertEquals(1, TestHelper.getListeners(parent).size());
		child.dispose();
		assertEquals(0, TestHelper.getListeners(parent).size());
	}
}
