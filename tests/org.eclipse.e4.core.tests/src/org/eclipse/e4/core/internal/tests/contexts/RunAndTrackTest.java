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

package org.eclipse.e4.core.internal.tests.contexts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextChangeEvent;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.IRunAndTrack;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.core.internal.tests.CoreTestsActivator;

/**
 * Tests for {@link org.eclipse.e4.core.services.context.IRunAndTrack}.
 */
public class RunAndTrackTest extends TestCase {

	private class ActivePartLookupFunction extends ContextFunction {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.e4.core.services.context.spi.ContextFunction#compute(
		 * org.eclipse.e4.core.services.context.IEclipseContext, java.lang.Object[])
		 */
		public Object compute(IEclipseContext context, Object[] arguments) {
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
	 * Tests that {@link org.eclipse.e4.core.services.context.ContextChangeEvent#getOldValue()}
	 * returns the correct value for various change types.
	 */
	public void testContextEventOldValue() {
		final String NAME = "Name";
		class ContextListener implements IRunAndTrack {
			public Object newValue;
			public Object oldValue;

			public boolean notify(ContextChangeEvent event) {
				oldValue = event.getOldValue();
				IEclipseContext eventsContext = event.getContext();
				newValue = eventsContext.get(NAME);
				return true;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		context.set(NAME, "Value");
		ContextListener listener = new ContextListener();
		context.runAndTrack(listener, null);
		assertEquals("Value", listener.newValue);
		assertNull(listener.oldValue);

		// simple value change
		context.set(NAME, "NewValue");
		assertEquals("Value", listener.oldValue);
		assertEquals("NewValue", listener.newValue);

		// set value to null
		context.set(NAME, null);
		assertEquals("NewValue", listener.oldValue);
		assertEquals(null, listener.newValue);

		// set from null to non-null
		context.set(NAME, "Value");
		assertEquals(null, listener.oldValue);
		assertEquals("Value", listener.newValue);

		// remove
		context.remove(NAME);
		assertEquals("Value", listener.oldValue);
		assertEquals(null, listener.newValue);

	}

	/**
	 * Tests that {@link org.eclipse.e4.core.services.context.ContextChangeEvent#getOldValue()}
	 * returns the correct value for various change types.
	 */
	public void testContextEventType() {
		final String NAME = "Name";
		class ContextListener implements IRunAndTrack {
			int eventType;

			public boolean notify(ContextChangeEvent event) {
				// must get a value so we aren't removed
				eventType = event.getEventType();
				IEclipseContext eventsContext = event.getContext();
				eventsContext.get(NAME);
				return true;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		ContextListener listener = new ContextListener();
		context.runAndTrack(listener, null);
		assertEquals(ContextChangeEvent.INITIAL, listener.eventType);

		// add a new value
		context.set(NAME, "Value");
		assertEquals(ContextChangeEvent.ADDED, listener.eventType);

		// simple value change
		context.set(NAME, "NewValue");
		assertEquals(ContextChangeEvent.ADDED, listener.eventType);

		// set value to null
		context.set(NAME, null);
		assertEquals(ContextChangeEvent.ADDED, listener.eventType);

		// set from null to non-null
		context.set(NAME, "Value");
		assertEquals(ContextChangeEvent.ADDED, listener.eventType);

		// remove
		context.remove(NAME);
		assertEquals(ContextChangeEvent.REMOVED, listener.eventType);

		// dispose context
		((IDisposable) context).dispose();
		assertEquals(ContextChangeEvent.DISPOSE, listener.eventType);

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
		windows[0].runAndTrack(new IRunAndTrack() {
			public boolean notify(ContextChangeEvent event) {
				final Object part = windows[0].get(ACTIVE_PART);
				windows[0].set(ACTIVE_PART_ID, part);
				return true;
			}

			public String toString() {
				return ACTIVE_PART_ID;
			}
		}, null);

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
		windows[0].runAndTrack(new IRunAndTrack() {
			public boolean notify(ContextChangeEvent event) {
				final Object part = windows[0].get(ACTIVE_PART);
				windows[0].set(ACTIVE_PART_ID, part);
				return true;
			}

			public String toString() {
				return ACTIVE_PART_ID;
			}
		}, null);

		final IEclipseContext[] parts = createNextLevel(windows[0], "part", 2);
		assertEquals("part0", workbenchContext.get(ACTIVE_PART));
		assertEquals("part0", windows[0].get(ACTIVE_PART_ID));

		windows[0].set(ACTIVE_CHILD, parts[1]);
		assertEquals("part1", windows[0].get(ACTIVE_PART));
		assertEquals("part1", windows[0].get(ACTIVE_PART_ID));
	}
}
