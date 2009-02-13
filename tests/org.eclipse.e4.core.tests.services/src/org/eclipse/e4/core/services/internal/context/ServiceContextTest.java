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
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.osgi.IServiceAliasRegistry;
import org.eclipse.e4.core.tests.services.TestActivator;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * Tests for contexts used in OSGi services.
 */
public class ServiceContextTest extends TestCase {
	static class Printer {
		PrintService diPrinter;

		public void print(String message) {
			if (diPrinter != null)
				diPrinter.print(message);
			else
				System.out.println(message);
		}
	}

	private IEclipseContext context;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		context = EclipseContextFactory.createServiceContext(TestActivator.bundleContext);
		((IServiceAliasRegistry) context.get(IServiceAliasRegistry.SERVICE_NAME)).registerAlias("Printer", PrintService.SERVICE_NAME);
	}
	
	@Override
	protected void tearDown() throws Exception {
		((IServiceAliasRegistry) context.get(IServiceAliasRegistry.SERVICE_NAME)).unregisterAlias("Printer");
		if (context instanceof IDisposable) 
			((IDisposable)context).dispose();
		super.tearDown();
	}

	/**
	 * Tests accessing OSGi services through a child context that is not aware of them.
	 */
	public void testServiceContextAsParent() {
		IEclipseContext child = EclipseContextFactory.create("child", context, null);
		IServiceAliasRegistry service = (IServiceAliasRegistry) child.get(IServiceAliasRegistry.SERVICE_NAME);
		assertNotNull(service);
	}
	
	public void testServiceInjection() {
		Printer userObject = new Printer();

		StringPrintService stringPrint1 = new StringPrintService();
		ServiceRegistration reg1 = TestActivator.bundleContext.registerService(PrintService.SERVICE_NAME, stringPrint1, null);

		ContextInjectionFactory.inject(userObject, context);
		userObject.print("test");
		assertEquals("1.0", "test", stringPrint1.toString());

		//now remove the service
		reg1.unregister();
		userObject.print("another test");
		//the string should be unchanged
		assertEquals("1.1", "test", stringPrint1.toString());
		assertNull("1.2", userObject.diPrinter);

		//register a different service implementation
		StringPrintService stringPrint2 = new StringPrintService();
		ServiceRegistration reg2 = TestActivator.bundleContext.registerService(PrintService.SERVICE_NAME, stringPrint2, null);
		userObject.print("yet another test");
		//the second string should have the value
		assertEquals("2.0", "test", stringPrint1.toString());
		assertEquals("2.1", "yet another test", stringPrint2.toString());
		reg2.unregister();
		assertNull("2.2", userObject.diPrinter);
	}
	
	/**
	 * Tests that OSGi services are released when their context is disposed.
	 */
	public void testServiceRemovalOnContextDispose() {
		StringPrintService stringPrint1 = new StringPrintService();
		ServiceRegistration reg1 = TestActivator.bundleContext.registerService(PrintService.SERVICE_NAME, stringPrint1, null);
		ServiceReference ref = reg1.getReference();
		
		PrintService service = (PrintService) context.get(PrintService.SERVICE_NAME);
		assertEquals("1.0", stringPrint1, service);
		assertEquals("1.1", 1, ref.getUsingBundles().length);
		service = null;
		((IDisposable) context).dispose();
		assertNull("2.0", ref.getUsingBundles());
		reg1.unregister();
	}

	/**
	 * Tests that OSGi services are released when the context that requested
	 * the service is removed.
	 */
	public void testServiceRemovalOnChildContextDispose() {
		StringPrintService stringPrint1 = new StringPrintService();
		ServiceRegistration reg1 = TestActivator.bundleContext.registerService(PrintService.SERVICE_NAME, stringPrint1, null);
		ServiceReference ref = reg1.getReference();
		IEclipseContext child = EclipseContextFactory.create("child", context, null);
		
		PrintService service = (PrintService) child.get(PrintService.SERVICE_NAME);
		assertEquals("1.0", stringPrint1, service);
		assertEquals("1.1", 1, ref.getUsingBundles().length);
		assertTrue("1.2", context.containsKey(PrintService.SERVICE_NAME));
		//when the child that used the service is gc'ed the parent should no longer contain the service
		service = null;
		child = null;
		System.gc();
		System.runFinalization();
		System.gc();
		//must call a method on context to give it a chance to clean up its references
		assertTrue("2.0", context.containsKey(IServiceAliasRegistry.SERVICE_NAME));
		assertNull("2.1", ref.getUsingBundles());
		
		reg1.unregister();
		
	}
}
