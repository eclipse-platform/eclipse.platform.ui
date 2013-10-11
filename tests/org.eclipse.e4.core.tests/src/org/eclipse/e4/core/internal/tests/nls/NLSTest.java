package org.eclipse.e4.core.internal.tests.nls;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.tests.CoreTestsActivator;
import org.eclipse.e4.core.services.nls.Translation;

public class NLSTest extends TestCase {
	static class TestSimpleObject {
		@Inject
		@Translation
		SimpleMessages simpleMessages;
	}
	
	static class TestBundleObject {
		@Inject
		@Translation
		BundleMessages bundleMessages;
	}
	
	static class TestResourceBundleClassObject {
		@Inject
		@Translation
		ResourceBundleClassMessages bundleClassMessages;
	}
	
	private IEclipseContext context;
	
	private IEclipseContext getOrCreateContext() {
		if( context != null ) {
			return context;
		}
		context = EclipseContextFactory.getServiceContext(CoreTestsActivator.getDefault().getBundleContext());
		ContextInjectionFactory.setDefault(context);
		return context;
	}
	
	public void testSimple() {
		TestSimpleObject o = ContextInjectionFactory.make(TestSimpleObject.class, getOrCreateContext());
		assertNotNull(o.simpleMessages);
		assertNotNull(o.simpleMessages.message_1);
		assertEquals("SimpleMessages 1", o.simpleMessages.message_1);
	}
	
	public void testBundle() {
		TestBundleObject o = ContextInjectionFactory.make(TestBundleObject.class, getOrCreateContext());
		assertNotNull(o.bundleMessages);
		assertNotNull(o.bundleMessages.message_1);
		assertEquals("BundleMessages 1", o.bundleMessages.message_1);
	}
	
	public void testResourceBundle() {
		TestResourceBundleClassObject o = ContextInjectionFactory.make(TestResourceBundleClassObject.class, getOrCreateContext());
		assertNotNull(o.bundleClassMessages);
		assertNotNull(o.bundleClassMessages.message_1);
		assertEquals("ResourceBundleClass 1", o.bundleClassMessages.message_1);
	}
}