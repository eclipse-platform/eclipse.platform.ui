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

package org.eclipse.e4.core.internal.tests.contexts.inject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.core.internal.tests.CoreTestsActivator;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * Tests for contexts used in OSGi services.
 */
public class ServiceContextTest extends TestCase {
	class Crayon {
		@Inject
		IPaletteService palette;

		public void draw() {
			if (palette == null)
				System.out.println("I'm out of ink!");
			else
				System.out.println("My ink is  " + palette.getColor());
		}
	}

	static enum Color {
		RED, BLUE, YELLOW, GREEN, ORANGE, PURPLE;
	}

	interface IPaletteService {
		public Color getColor();
	}

	class PaletteImpl implements IPaletteService {
		private final Color color;

		PaletteImpl(Color color) {
			this.color = color;
		}

		public Color getColor() {
			return color;
		}
	}

	static class Printer {
		@Inject
		PrintService printer;

		public void print(String message) {
			if (printer != null)
				printer.print(message);
			else
				System.out.println(message);
		}
	}

	private IEclipseContext context;
	private final List<ServiceRegistration> registrations = new ArrayList<ServiceRegistration>();

	protected void setUp() throws Exception {
		super.setUp();
		//don't use the global shared service context to avoid contamination across tests
		BundleContext bundleContext = CoreTestsActivator.getDefault().getBundleContext();
		context = EclipseContextFactory.getServiceContext(bundleContext);
		registrations.clear();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		((IDisposable) context).dispose();
		for (ServiceRegistration reg : registrations) {
			try {
				reg.unregister();
			} catch (IllegalStateException e) {
				//ignore
			}
		}
		registrations.clear();
		context = null;
	}

	public void testDeclarativeService() {
		assertTrue(context.containsKey("sum"));
		assertEquals(0, context.get("sum"));
		context.set("x", 1);
		context.set("y", 2);
		int result = (Integer) context.get("sum");
		assertEquals("1.0", 3, result);
		context.set("x", 5);
		result = (Integer) context.get("sum");
		assertEquals("1.0", 7, result);
	}

	/**
	 * Tests accessing OSGi services through a child context that is not aware of them.
	 */
	public void testServiceContextAsParent() {
		IEclipseContext child = context.createChild( "child");
		DebugOptions service = (DebugOptions) child.get(DebugOptions.class.getName());
		assertNotNull(service);
	}

	public void testServiceInjection() {
		Printer userObject = new Printer();

		StringPrintService stringPrint1 = new StringPrintService();
		BundleContext bundleContext = CoreTestsActivator.getDefault().getBundleContext();
		ServiceRegistration reg1 = bundleContext.registerService(PrintService.SERVICE_NAME, stringPrint1, null);

		ContextInjectionFactory.inject(userObject, context);
		userObject.print("test");
		assertEquals("1.0", "test", stringPrint1.toString());

		// now remove the service
		reg1.unregister();
		userObject.print("another test");
		// the string should be unchanged
		assertEquals("1.1", "test", stringPrint1.toString());
		assertNull("1.2", userObject.printer);

		// register a different service implementation
		StringPrintService stringPrint2 = new StringPrintService();
		ServiceRegistration reg2 = bundleContext.registerService(PrintService.SERVICE_NAME, stringPrint2, null);
		userObject.print("yet another test");
		// the second string should have the value
		assertEquals("2.0", "test", stringPrint1.toString());
		assertEquals("2.1", "yet another test", stringPrint2.toString());
		reg2.unregister();
		assertNull("2.2", userObject.printer);
	}

	protected void ensureUnregistered(ServiceRegistration reg) {
		registrations.add(reg);
	}

	/**
	 * Tests that OSGi services are released when their context is disposed.
	 */
	public void testServiceRemovalOnContextDispose() {
		StringPrintService stringPrint1 = new StringPrintService();
		BundleContext bundleContext = CoreTestsActivator.getDefault().getBundleContext();
		ServiceRegistration reg1 = bundleContext.registerService(PrintService.SERVICE_NAME, stringPrint1, null);
		try {
			ServiceReference ref = reg1.getReference();

			PrintService service = (PrintService) context.get(PrintService.SERVICE_NAME);
			assertEquals("1.0", stringPrint1, service);
			assertEquals("1.1", 1, ref.getUsingBundles().length);
			service = null;
			((IDisposable) context).dispose();
			assertNull("2.0", ref.getUsingBundles());
		} finally {
			reg1.unregister();
		}
	}

	public void testRecursiveServiceRemoval() {
		BundleContext bundleContext = CoreTestsActivator.getDefault().getBundleContext();
		ServiceRegistration reg1 = bundleContext.registerService(PrintService.SERVICE_NAME, new StringPrintService(), null);
		final IEclipseContext child = context.createChild();
		final IEclipseContext child2 = context.createChild();
		child.get(PrintService.SERVICE_NAME);
		child2.get(PrintService.SERVICE_NAME);
		ensureUnregistered(reg1);
		final boolean[] done = new boolean[] {false};
		context.runAndTrack(new RunAndTrack() {
			public boolean changed(IEclipseContext context) {
				if (context.get(PrintService.SERVICE_NAME) == null) {
						((IDisposable) child).dispose();
					done[0] = true;
				}
				return true;
			}
		});
		reg1.unregister();
	}

	public void testServiceExample() {
		BundleContext bundleContext = CoreTestsActivator.getDefault().getBundleContext();
		ServiceRegistration reg = bundleContext.registerService(IPaletteService.class.getName(), new PaletteImpl(Color.BLUE), null);
		IEclipseContext context = EclipseContextFactory.getServiceContext(bundleContext);
		Crayon crayon = new Crayon();
		ContextInjectionFactory.inject(crayon, context);
		crayon.draw();
		reg.unregister();
		crayon.draw();
	}

	/**
	 * Tests that OSGi services are released when the context that requested the service is removed.
	 */
	public void testServiceRemovalOnChildContextDispose() {
		StringPrintService stringPrint1 = new StringPrintService();
		BundleContext bundleContext = CoreTestsActivator.getDefault().getBundleContext();
		ServiceRegistration reg1 = bundleContext.registerService(PrintService.SERVICE_NAME, stringPrint1, null);
		ServiceReference ref = reg1.getReference();
		assertNull("0.1", ref.getUsingBundles());
		IEclipseContext child = context.createChild("child");

		PrintService service = (PrintService) child.get(PrintService.SERVICE_NAME);
		assertEquals("1.0", stringPrint1, service);
		assertEquals("1.1", 1, ref.getUsingBundles().length);
		assertTrue("1.2", context.containsKey(PrintService.SERVICE_NAME));
		// when the child that used the service is gc'ed the parent should no longer contain the
		// service
		service = null;
		child = null;
		System.gc();
		System.runFinalization();
		System.gc();
		//create and dispose another child that uses the service
		child = context.createChild("child-2");
		service = (PrintService) child.get(PrintService.SERVICE_NAME);
		service = null;
		((IDisposable) child).dispose();
		child = null;

		//now there should be no service references, even though child1 was never disposed
		assertNull("2.2", ref.getUsingBundles());

		reg1.unregister();

	}
}
