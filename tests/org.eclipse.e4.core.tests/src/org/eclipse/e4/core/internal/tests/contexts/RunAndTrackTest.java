/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.internal.tests.contexts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.internal.tests.CoreTestsActivator;

/**
 * Tests for {@link org.eclipse.e4.core.RunAndTrack.context.IRunAndTrack}.
 */
public class RunAndTrackTest extends TestCase {
	
	private static final class TestRAT extends RunAndTrack {

		
		private String varName;
		private Object varValue;
		private int calls = 0;
		
		public TestRAT(String varName) {
			this.varName = varName;
		}
		
		@Override
		public boolean changed(IEclipseContext context) {
			++calls;
			varValue = context.get(varName);
			return true;
		}
		
		public int getCalls() {
			return calls;
		}

		public Object getVarValue() {
			return varValue;
		}
		
		public void resetCalls() {
			calls = 0;
		}

	}

	private class ActivePartLookupFunction extends ContextFunction {

		public Object compute(IEclipseContext context, String contextKey) {
			IEclipseContext childContext = (IEclipseContext) context.getLocal(ACTIVE_CHILD);
			if (childContext != null) {
				return childContext.get(ACTIVE_PART);
			}
			return context.get(INTERNAL_LOCAL_PART);
		}

	}

	static final String ACTIVE_CHILD = "activeChild";

	static final String ACTIVE_PART = "activePart";

	static final String ACTIVE_PART_ID = "activePartId";

	static final String INTERNAL_LOCAL_PART = "localPart";

	private List<IEclipseContext> createdContexts = new ArrayList<IEclipseContext>();

	private IEclipseContext createContext(IEclipseContext parentContext, String level) {
		IEclipseContext childContext = parentContext.createChild(level);
		createdContexts.add(childContext);
		return childContext;
	}

	private IEclipseContext getGlobalContext() {
		IEclipseContext serviceContext = EclipseContextFactory
				.getServiceContext(CoreTestsActivator.getDefault().getBundleContext());
		// global initialization and setup, usually done by workbench
		IEclipseContext appContext = createContext(serviceContext, "globalContext");

		appContext.set("globalContext", appContext);

		return appContext;
	}

	private IEclipseContext[] createNextLevel(IEclipseContext parent, String prefix, int num) {
		assertTrue(num > 0);
		IEclipseContext[] contexts = new IEclipseContext[num];
		for (int i = 0; i < num; i++) {
			contexts[i] = createContext(parent, prefix + i);
			contexts[i].set(INTERNAL_LOCAL_PART, prefix + i);
		}
		parent.set(ACTIVE_CHILD, contexts[0]);
		return contexts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		for (Iterator<IEclipseContext> i = createdContexts.iterator(); i.hasNext();) {
			IEclipseContext context = i.next();
			context.dispose();
		}
		createdContexts.clear();
		super.tearDown();
	}

	public void testActiveChain() throws Exception {
		final IEclipseContext workbenchContext = getGlobalContext();
		workbenchContext.set("activePart", new ActivePartLookupFunction());
		final IEclipseContext[] windows = createNextLevel(workbenchContext, "window", 1);
		createNextLevel(windows[0], "part", 2);
		assertEquals("part0", workbenchContext.get(ACTIVE_PART));
	}

	public void testActiveChange() throws Exception {
		final IEclipseContext workbenchContext = getGlobalContext();
		workbenchContext.set("activePart", new ActivePartLookupFunction());
		final IEclipseContext[] windows = createNextLevel(workbenchContext, "window", 1);
		final IEclipseContext[] parts = createNextLevel(windows[0], "part", 2);
		assertEquals("part0", workbenchContext.get(ACTIVE_PART));

		windows[0].set(ACTIVE_CHILD, parts[1]);
		assertEquals("part1", workbenchContext.get(ACTIVE_PART));
	}

	/**
	 * There was a failing scenario in the legacy workbench support. This captures the hierarchy and
	 * function (without any workbench level references). It should be updated when we figure out
	 * the failing scenario :-)
	 * 
	 * @throws Exception
	 */
	public void testRunAndTrackComplex() throws Exception {
		final IEclipseContext workbenchContext = getGlobalContext();
		workbenchContext.set("activePart", new ActivePartLookupFunction());
		final IEclipseContext[] windows = createNextLevel(workbenchContext, "window", 1);
		windows[0].runAndTrack(new RunAndTrack() {
			public boolean changed(IEclipseContext context) {
				final Object part = windows[0].get(ACTIVE_PART);
				windows[0].set(ACTIVE_PART_ID, part);
				return true;
			}

			public String toString() {
				return ACTIVE_PART_ID;
			}
		});

		final IEclipseContext[] mainSashes = createNextLevel(windows[0], "mainSash", 2);
		createNextLevel(mainSashes[1], "editorArea", 1);
		final IEclipseContext[] viewSashes = createNextLevel(mainSashes[0], "viewSashes", 2);

		// create package explorer stack
		final IEclipseContext[] packageStack = createNextLevel(viewSashes[0], "packageStack", 1);
		final IEclipseContext[] packageViews = createNextLevel(packageStack[0], "packageViews", 3);
		assertNotNull(packageViews);
		assertEquals("packageViews0", windows[0].get(ACTIVE_PART));
		assertEquals("packageViews0", windows[0].get(ACTIVE_PART_ID));

		// create problems stack
		final IEclipseContext[] problemsStack = createNextLevel(viewSashes[1], "problemsStack", 1);
		final IEclipseContext[] problemsViews = createNextLevel(problemsStack[0], "problemViews", 5);
		assertNotNull(problemsViews);
		assertEquals("packageViews0", windows[0].get(ACTIVE_PART));
		assertEquals("packageViews0", windows[0].get(ACTIVE_PART_ID));

		assertEquals("problemViews0", problemsStack[0].get(ACTIVE_PART));
		// this won't change since it is a "runAndTrack" at the window context
		// level
		assertEquals("packageViews0", problemsStack[0].get(ACTIVE_PART_ID));

		// set the "problems view" active, propagating the information up
		// the active chain.
		problemsStack[0].set(ACTIVE_CHILD, problemsViews[0]);
		viewSashes[1].set(ACTIVE_CHILD, problemsStack[0]);
		mainSashes[0].set(ACTIVE_CHILD, viewSashes[1]);
		windows[0].set(ACTIVE_CHILD, mainSashes[0]);
		workbenchContext.set(ACTIVE_CHILD, windows[0]);

		assertEquals("problemViews0", windows[0].get(ACTIVE_PART));
		assertEquals("problemViews0", windows[0].get(ACTIVE_PART_ID));

		assertEquals("packageViews0", packageStack[0].get(ACTIVE_PART));
		assertEquals("problemViews0", packageStack[0].get(ACTIVE_PART_ID));
	}

	public void testRunAndTrackSimple() throws Exception {
		final IEclipseContext workbenchContext = getGlobalContext();
		workbenchContext.set("activePart", new ActivePartLookupFunction());
		final IEclipseContext[] windows = createNextLevel(workbenchContext, "window", 1);
		windows[0].runAndTrack(new RunAndTrack() {
			public boolean changed(IEclipseContext context) {
				final Object part = windows[0].get(ACTIVE_PART);
				windows[0].set(ACTIVE_PART_ID, part);
				return true;
			}

			public String toString() {
				return ACTIVE_PART_ID;
			}
		});

		final IEclipseContext[] parts = createNextLevel(windows[0], "part", 2);
		assertEquals("part0", workbenchContext.get(ACTIVE_PART));
		assertEquals("part0", windows[0].get(ACTIVE_PART_ID));

		windows[0].set(ACTIVE_CHILD, parts[1]);
		assertEquals("part1", windows[0].get(ACTIVE_PART));
		assertEquals("part1", windows[0].get(ACTIVE_PART_ID));
	}
	
	/**
	 * Test how a RAT responds to a change hidden from it; changed value is == to child value
	 */
	public void testSetHiddenValueToChildObject() {
		final String newRootValue = "child";
		
		doHiddenValueChangeTest(newRootValue);
	}

	/**
	 * Test how a RAT responds to a change hidden from it; changed value is != to child value
	 */
	public void testSetHiddenValueToDifferentObject() {
		final String newRootValue = "other";
		
		doHiddenValueChangeTest(newRootValue);
	}

	/**
	 * Test how a RAT responds to a change hidden from it; changed value is != to child value (but is .equals())
	 */
	public void testSetHiddenValueToObjectEqualToChild() {
		// avoid compiler's pushing all my strings into a single string pool
		final String newRootValue = new String("child"); 
		
		doHiddenValueChangeTest(newRootValue);
	}

	/**
	 * Test how a RAT responds to a change hidden from it; changed value is == to root value
	 */
	public void testSetHiddenValueToRootObject() {
		final String newRootValue = "root"; 
		
		doHiddenValueChangeTest(newRootValue);
	}

	/**
	 * Test how a RAT responds to a change hidden from it; changed value is != to root value (but is .equals())
	 */
	public void testSetHiddenValueToEqualRootObject() {
		// avoid compiler's pushing all my strings into a single string pool
		final String newRootValue = new String("root"); 
		
		doHiddenValueChangeTest(newRootValue);
	}

	/**
	 * Test how a RAT responds to a change hidden from it; changed value is == to root value
	 */
	public void testSetHiddenValueToNull() {
		final String newRootValue = null; 
		
		doHiddenValueChangeTest(newRootValue);
	}

	/**
	 * Perform a hidden value test that verifies that the test RAT does not run, and
	 * that has last seen the initial value in the child context (namely "child").
	 * @param newRootValue the new value for the variable 'v' in the root context.
	 * @see #doHiddenValueChangeTest(ITestAction, Object, int)
	 */
	void doHiddenValueChangeTest(final String newRootValue) {
		doHiddenValueChangeTest(new ITestAction() {
			
			public void execute(IEclipseContext root, String var) {
				root.set(var, newRootValue);
				
			}
		}, "child", 0);
	}

	/**
	 * Interface defining function
	 *
	 */
	private interface ITestAction {

		void execute(IEclipseContext root, String var);
		
	}
	/**
	 * Create a two level hierarchy of contexts, each defining a variable 'v' with values 'root' and 'child', respectively.
	 * Create and install a RAT on the child context that is dependent on 'v'.
	 * Run <code>testAction</code>.
	 * Tests whether the RAT ran the expected number of times,
	 * and tests last value of 'v' that the RAT saw.
	 * @param testAction the context action to perform as part of the test
	 * @param expectedValue the expected last value of variable 'v' that the RAT saw.
	 * @param expectedRATCalls the expected number of times the RAT was run in response to testAction
	 */
	void doHiddenValueChangeTest(ITestAction testAction, Object expectedValue, int expectedRATCalls) {
		final IEclipseContext root = getGlobalContext();
		final IEclipseContext child = root.createChild("child");

		root.set("v", "root");
		child.set("v", "child");
		final TestRAT testRAT = new TestRAT("v");
		
		// install the RAT
		child.runAndTrack(testRAT);
		assertEquals("child", testRAT.getVarValue());
		assertEquals(1, testRAT.getCalls());
		
		testRAT.resetCalls();
		// set the new root value
		testAction.execute(root, "v");
		assertEquals(expectedValue, testRAT.getVarValue());
		assertEquals(expectedRATCalls, testRAT.getCalls());
	}

	/**
	 * Test that a variable change in a context hidden from a RAT in
	 * a child context does not re-run the RAT.
	 */
	public void testRemoveHiddenVariable() {
		doHiddenValueChangeTest(new ITestAction() {
			
			public void execute(IEclipseContext root, String var) {
				root.remove(var);;
				
			}
		}, "child", 0);
	}
	
	/**
	 * Test that setting a context variable to it's existing
	 * value does not re-run dependent RATs
	 */
	public void testSetContextVarToSameObject() {
		doSingleContextChangeTest(new ITestAction() {
			public void execute(IEclipseContext root, String var) {
				root.set(var, "root");
			}
		}, "root", 0);
	}
	
	/**
	 * Test that setting a context variable to a value that {@link Object#equals(Object) equals}
	 * the current value, but is same object DOES re-run dependent RATs.
	 */
	public void testSetContextVarToEqualObject() {
		doSingleContextChangeTest(new ITestAction() {
			public void execute(IEclipseContext root, String var) {
				root.set(var, new String("root"));
			}
		}, "root", 1);
	}
	
	/**
	 * Test that setting a context variable to a different object, not equal to the
	 * current value re-runs dependent RATs.
	 */
	public void testSetContextVarToOtherObject() {
		doSingleContextChangeTest(new ITestAction() {
			public void execute(IEclipseContext root, String var) {
				root.set(var, "other");
			}
		}, "other", 1);
	}
	
	/**
	 * Test that removing a context variable re-runs dependent RATs.
	 */
	public void testRemoveContextVar() {
		doSingleContextChangeTest(new ITestAction() {
			public void execute(IEclipseContext root, String var) {
				root.remove(var);
			}
		}, null, 1);
		
	}
	
	/**
	 * Creates a context, sets a variable 'v' to "root", creates a RAT dependent on 'v' in the context,
	 * then executes <code>testAction</code> and tests whether the RAT ran the expected number of times,
	 * and tests last value of 'v' that the RAT saw.
	 * @param testAction the context action to perform as part of the test
	 * @param expectedValue the expected last value of variable 'v' that the RAT saw.
	 * @param expectedRATCalls the expected number of times the RAT was run in response to testAction
	 */
	private void doSingleContextChangeTest(ITestAction testAction, Object expectedValue, int expectedRATCalls) {
		final IEclipseContext root = getGlobalContext();
		
		root.set("v", "root");
		
		final TestRAT testRAT = new TestRAT("v");
		// install the RAT
		root.runAndTrack(testRAT);
		assertEquals("root", testRAT.getVarValue());
		assertEquals(1, testRAT.getCalls());

		testRAT.resetCalls();
		testAction.execute(root, "v");
		assertEquals(expectedRATCalls, testRAT.getCalls());
		assertEquals(expectedValue, testRAT.getVarValue());
		
	}
}
