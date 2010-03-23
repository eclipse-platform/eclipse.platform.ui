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

package org.eclipse.e4.core.tests.performance;

import junit.framework.TestCase;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.tests.harness.PerformanceTestRunner;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextFunction;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.tests.services.TestActivator;
import org.eclipse.e4.internal.core.services.osgi.OSGiContextStrategy;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.service.debug.DebugOptions;

/**
 *
 */
public class ContextPerformanceTest extends TestCase {

	IEclipseContext parentContext, context;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.parentContext = EclipseContextFactory.create(null, new OSGiContextStrategy(
				TestActivator.bundleContext));
		this.parentContext.set(IContextConstants.DEBUG_STRING, getName() + "-parent");
		this.context = EclipseContextFactory.create(parentContext, null);
		context.set(IContextConstants.DEBUG_STRING, getName());

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
		}.run(this, 10, 50000);
	}

	public void testLookupContextFunction() {
		context.set("somefunction", new ContextFunction() {
			public Object compute(IEclipseContext context, Object[] arguments) {
				return "result";
			}
		});
		new PerformanceTestRunner() {
			protected void test() {
				context.get("somefunction");
			}
		}.run(this, 10, 5000000);
	}

}
