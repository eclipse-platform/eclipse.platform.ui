/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
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
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.internal.tests.CoreTestsActivator;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.Bundle;
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
		
		String msg;

		public void draw() {
			if (palette == null)
				msg = "I'm out of ink!";
			else
				msg = "My ink is  " + palette.getColor();
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
		@Inject @Optional
		PrintService printer;

		public void print(String message) {
			if (printer != null)
				printer.print(message);
		}
	}

	private IEclipseContext context;
	private final List<ServiceRegistration<?>> registrations = new ArrayList<ServiceRegistration<?>>();

	protected void setUp() throws Exception {
		super.setUp();
		//don't use the global shared service context to avoid contamination across tests
		BundleContext bundleContext = CoreTestsActivator.getDefault().getBundleContext();
		context = EclipseContextFactory.getServiceContext(bundleContext);
		registrations.clear();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		// Consumers must not dispose OSGi context as it is reused
		//context.dispose();
		for (ServiceRegistration<?> reg : registrations) {
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
		ServiceRegistration<?> reg1 = null;
		ServiceRegistration<?> reg2 = null; 
		try {
			Printer userObject = new Printer();
	
			StringPrintService stringPrint1 = new StringPrintService();
			BundleContext bundleContext = CoreTestsActivator.getDefault().getBundleContext();
			reg1 = bundleContext.registerService(PrintService.SERVICE_NAME, stringPrint1, null);
	
			ContextInjectionFactory.inject(userObject, context);
			userObject.print("test");
			assertEquals("1.0", "test", stringPrint1.toString());
	
			// now remove the service
			reg1.unregister();
			reg1 = null;
			userObject.print("another test");
			// the string should be unchanged
			assertEquals("1.1", "test", stringPrint1.toString());
			assertNull("1.2", userObject.printer);
			
			// register a different service implementation
			StringPrintService stringPrint2 = new StringPrintService();
			reg2 = bundleContext.registerService(PrintService.SERVICE_NAME, stringPrint2, null);
			userObject.print("yet another test");
			// the second string should have the value
			assertEquals("2.0", "test", stringPrint1.toString());
			assertEquals("2.1", "yet another test", stringPrint2.toString());
			reg2.unregister();
			reg2 = null;
			assertNull("2.2", userObject.printer);
		} finally {
			if (reg1 != null) {
				reg1.unregister();
				reg1 = null;
			}
			if (reg2 != null) {
				reg2.unregister();
				reg2 = null;
			}
		}
	}

	public void testServiceAddition() {
		ServiceRegistration<?> reg1 = null;
		try {
			Printer userObject = new Printer();
			ContextInjectionFactory.inject(userObject, context);

			StringPrintService stringPrint1 = new StringPrintService();
			BundleContext bundleContext = CoreTestsActivator.getDefault().getBundleContext();
			reg1 = bundleContext.registerService(PrintService.SERVICE_NAME, stringPrint1, null);
	
			userObject.print("test");
			assertEquals("1.0", "test", stringPrint1.toString());
		} finally {
			if (reg1 != null) {
				reg1.unregister();
				reg1 = null;
			}
		}
	}

	protected void ensureUnregistered(ServiceRegistration<?> reg) {
		registrations.add(reg);
	}

	/**
	 * Tests that OSGi services are released when their context is disposed.
	 */
	public void testServiceRemovalOnContextDispose() {
		StringPrintService stringPrint1 = new StringPrintService();
		BundleContext bundleContext = CoreTestsActivator.getDefault()
				.getBundleContext();
		Bundle otherBundle = null;
		for (Bundle b : bundleContext.getBundles()) {
			if (b.getSymbolicName().equals("org.eclipse.core.tests.harness")) {
				otherBundle = b;
				break;
			}
		}
		assertNotNull(otherBundle);
		IEclipseContext otherServiceContext = EclipseContextFactory
				.getServiceContext(otherBundle.getBundleContext());
		ServiceRegistration<?> reg1 = bundleContext.registerService(
				PrintService.SERVICE_NAME, stringPrint1, null);
		try {
			ServiceReference<?> ref = reg1.getReference();

			PrintService service = (PrintService) otherServiceContext
					.get(PrintService.SERVICE_NAME);
			assertEquals("1.0", stringPrint1, service);
			assertEquals("1.1", 1, ref.getUsingBundles().length);
			service = null;
			otherServiceContext.dispose();
			assertNull("2.0", ref.getUsingBundles());
		} finally {
			reg1.unregister();
		}
	}

	public void testRecursiveServiceRemoval() {
		BundleContext bundleContext = CoreTestsActivator.getDefault().getBundleContext();
		ServiceRegistration<?> reg1 = bundleContext.registerService(PrintService.SERVICE_NAME, new StringPrintService(), null);
		final IEclipseContext child = context.createChild();
		final IEclipseContext child2 = context.createChild();
		child.get(PrintService.SERVICE_NAME);
		child2.get(PrintService.SERVICE_NAME);
		ensureUnregistered(reg1);
		final boolean[] done = new boolean[] {false};
		context.runAndTrack(new RunAndTrack() {
			public boolean changed(IEclipseContext context) {
				if (context.get(PrintService.SERVICE_NAME) == null) {
						child.dispose();
					done[0] = true;
				}
				return true;
			}
		});
		reg1.unregister();
	}

	public void testServiceExample() {
		BundleContext bundleContext = CoreTestsActivator.getDefault().getBundleContext();
		ServiceRegistration<?> reg = bundleContext.registerService(IPaletteService.class.getName(), new PaletteImpl(Color.BLUE), null);
		IEclipseContext context = EclipseContextFactory.getServiceContext(bundleContext);
		Crayon crayon = new Crayon();
		ContextInjectionFactory.inject(crayon, context);
		crayon.draw();
		reg.unregister();
		crayon.draw();
	}

}
