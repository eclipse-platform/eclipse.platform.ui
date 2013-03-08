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

package org.eclipse.e4.core.internal.tests.contexts.performance;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.tests.harness.PerformanceTestRunner;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.internal.tests.CoreTestsActivator;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.service.debug.DebugOptions;

/**
 *
 */
public class ContextPerformanceTest extends TestCase {

	IEclipseContext parentContext, context;

	public static Test suite() {
		return new TestSuite(ContextPerformanceTest.class);
		// TestSuite suite = new TestSuite();
		// suite.addTest(new ContextPerformanceTest("testSetValueRunAndTrack"));
		// return suite;
	}

	public ContextPerformanceTest(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		parentContext = EclipseContextFactory.getServiceContext(CoreTestsActivator.getDefault().getBundleContext());
		context = parentContext.createChild(getName());

		// add some values to the contexts
		for (int i = 0; i < 100; i++) {
			context.set("Value-" + i, new Integer(i));
		}
		// do some additional service lookups on non-existent keys
		for (int i = 0; i < 1000; i++) {
			context.get("NonExistentValue-" + i);
		}

		// lookup some OSGi services
		context.get(DebugOptions.class.getName());
		context.get(IAdapterManager.class.getName());
		context.get(IExtensionRegistry.class.getName());
		context.get(IPreferencesService.class.getName());
		context.get(Location.class.getName());
	}

	public void testLookup() {
		new PerformanceTestRunner() {
			protected void test() {
				context.get("something");
			}
		}.run(this, 10, 600000);
	}

	public void testLookupContextFunction() {
		context.set("somefunction", new ContextFunction() {
			public Object compute(IEclipseContext context, String contextKey) {
				return "result";
			}
		});
		new PerformanceTestRunner() {
			protected void test() {
				context.get("somefunction");
			}
		}.run(this, 10, 5000000);
	}

	public void testSetContextFunction() {
		context.set("somefunction", new ContextFunction() {
			public Object compute(IEclipseContext context, String contextKey) {
				return context.get("something");
			}
		});
		new PerformanceTestRunner() {
			int i = 0;

			protected void test() {
				context.set("something", "value-" + i++);
			}
		}.run(this, 10, 600000);
	}

	/**
	 * Tests setting a value in a context that a RAT is listening to. This test mimics what occurs
	 * when handlers change in e4. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=305038
	 */
	public void testSetValueRunAndTrack() {
		context.set("somefunction", new ContextFunction() {
			public Object compute(IEclipseContext context, String contextKey) {
				// make sure this function has a large number of dependencies
				for (int i = 0; i < 1000; i++) {
					context.get("NonExistentValue-" + i);
				}
				return context.get("something");
			}
		});
		context.runAndTrack(new RunAndTrack() {
			public boolean changed(IEclipseContext context) {
				context.get("somefunction");
				return true;
			}
		});
		new PerformanceTestRunner() {
			int i = 0;

			protected void test() {
				context.set("something", "value-" + i++);
			}
		}.run(this, 10, 400);
	}

}
