/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.services.internal.context;

import junit.framework.TestCase;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.annotations.In;
import org.eclipse.e4.core.services.annotations.Inject;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.tests.services.TestActivator;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * Tests for contexts used in OSGi services.
 */
public class ServiceContextTest extends TestCase {
	class Crayon {
		@In
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

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		context = EclipseContextFactory.createServiceContext(TestActivator.bundleContext);
	}

	@Override
	protected void tearDown() throws Exception {
		if (context instanceof IDisposable)
			((IDisposable) context).dispose();
		super.tearDown();
	}

	public void testDeclarativeService() {
		IEclipseContext context = EclipseContextFactory
				.createServiceContext(TestActivator.bundleContext);
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
		IEclipseContext child = EclipseContextFactory.create(context, null);
		child.set(IContextConstants.DEBUG_STRING, "child");
		DebugOptions service = (DebugOptions) child.get(DebugOptions.class.getName());
		assertNotNull(service);
	}

	public void testServiceInjection() {
		Printer userObject = new Printer();

		StringPrintService stringPrint1 = new StringPrintService();
		ServiceRegistration reg1 = TestActivator.bundleContext.registerService(
				PrintService.SERVICE_NAME, stringPrint1, null);

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
		ServiceRegistration reg2 = TestActivator.bundleContext.registerService(
				PrintService.SERVICE_NAME, stringPrint2, null);
		userObject.print("yet another test");
		// the second string should have the value
		assertEquals("2.0", "test", stringPrint1.toString());
		assertEquals("2.1", "yet another test", stringPrint2.toString());
		reg2.unregister();
		assertNull("2.2", userObject.printer);
	}

	/**
	 * Tests that OSGi services are released when their context is disposed.
	 */
	public void testServiceRemovalOnContextDispose() {
		StringPrintService stringPrint1 = new StringPrintService();
		ServiceRegistration reg1 = TestActivator.bundleContext.registerService(
				PrintService.SERVICE_NAME, stringPrint1, null);
		ServiceReference ref = reg1.getReference();

		PrintService service = (PrintService) context.get(PrintService.SERVICE_NAME);
		assertEquals("1.0", stringPrint1, service);
		assertEquals("1.1", 1, ref.getUsingBundles().length);
		service = null;
		((IDisposable) context).dispose();
		assertNull("2.0", ref.getUsingBundles());
		reg1.unregister();
	}

	public void testServiceExample() {
		ServiceRegistration reg = TestActivator.bundleContext.registerService(IPaletteService.class
				.getName(), new PaletteImpl(Color.BLUE), null);
		IEclipseContext context = EclipseContextFactory
				.createServiceContext(TestActivator.bundleContext);
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
		ServiceRegistration reg1 = TestActivator.bundleContext.registerService(
				PrintService.SERVICE_NAME, stringPrint1, null);
		ServiceReference ref = reg1.getReference();
		IEclipseContext child = EclipseContextFactory.create(context, null);
		child.set(IContextConstants.DEBUG_STRING, "child");

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
		// must call a method on context to give it a chance to clean up its references
		assertTrue("2.0", context.containsKey(DebugOptions.class.getName()));
		assertNull("2.1", ref.getUsingBundles());

		reg1.unregister();

	}
}
