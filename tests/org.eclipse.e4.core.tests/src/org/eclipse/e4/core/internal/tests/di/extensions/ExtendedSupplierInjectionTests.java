package org.eclipse.e4.core.internal.tests.di.extensions;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.internal.tests.di.extensions.InjectionEventTest.EventAdminHelper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class ExtendedSupplierInjectionTests extends TestCase {
	static final String TOPIC = "org/eclipse/e4/core/tests/di/extensions/ExtendedSupplierInjectionTests";

	static class EventTestObject {
		static int count = 0;

		Object injectedObject;

		@Inject
		@Optional
		void dontExecute(@EventTopic(TOPIC) Object x) {
			count++;
			injectedObject = x;
		}
	}

	private EventAdminHelper helper;

	public void setUp() {
		InjectionEventTest.ensureEventAdminStarted();
		BundleContext bundleContext = FrameworkUtil.getBundle(getClass())
				.getBundleContext();
		IEclipseContext localContext = EclipseContextFactory
				.getServiceContext(bundleContext);
		helper = (EventAdminHelper) ContextInjectionFactory.make(
				EventAdminHelper.class, localContext);
	}

	/* Ensure extended suppliers are looked up first */
	public void testBug398728() {
		IEclipseContext context = EclipseContextFactory.create();
		context.set(Object.class, new Object());

		assertEquals(0, EventTestObject.count);

		EventTestObject target = ContextInjectionFactory.make(
				EventTestObject.class, context);
		// should be 0 since we haven't posted an event with this topic yet
		assertEquals(0, EventTestObject.count);

		helper.sendEvent(TOPIC, "event1data");

		assertEquals(1, EventTestObject.count);
		assertEquals("event1data", target.injectedObject);
	}

}
