package org.eclipse.e4.core.internal.tests.di.extensions;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.di.suppliers.ExtendedObjectSupplier;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;
import org.eclipse.e4.core.internal.di.osgi.ProviderHelper;
import org.eclipse.e4.core.internal.tests.di.extensions.InjectionEventTest.EventAdminHelper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

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

	/** bug 428837: ensure suppliers are ranked by service.ranking */
	public void testSupplierOrdering() {
		BundleContext bc = FrameworkUtil.getBundle(getClass())
				.getBundleContext();
		ExtendedObjectSupplier supplier = new ExtendedObjectSupplier() {
			@Override
			public Object get(IObjectDescriptor descriptor,
					IRequestor requestor, boolean track, boolean group) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put(ExtendedObjectSupplier.SERVICE_CONTEXT_KEY,
				EventTopic.class.getName());
		properties.put(Constants.SERVICE_RANKING, 100);
		ServiceRegistration<?> sr = bc.registerService(
				ExtendedObjectSupplier.SERVICE_NAME, supplier, properties);
		try {
			assertEquals(supplier, ProviderHelper.findProvider(
					EventTopic.class.getName(), null));
		} finally {
			sr.unregister();
		}
	}

}
