package org.eclipse.e4.core.internal.tests.di.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Service;
import org.junit.After;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class ServiceSupplierTestCase {
	public static class TestBean {
		TestService service;
		List<TestService> serviceList;
		int serviceInjectionCount;
		int serviceListInjectionCount;

		@Inject
		public void setService(@Service TestService service) {
			this.service = service;
			this.serviceInjectionCount++;
		}

		@Inject
		public void setServiceList(@Service List<TestService> serviceList) {
			this.serviceList = serviceList;
			this.serviceListInjectionCount++;
		}
	}

	public static class TestStaticFilterBean {
		TestService service;
		List<TestService> serviceList;
		int serviceInjectionCount;
		int serviceListInjectionCount;

		@Inject
		public void setService(@Service(filterExpression="(filtervalue=Test)") TestService service) {
			this.service = service;
			this.serviceInjectionCount++;
		}

		@Inject
		public void setServiceList(@Service(filterExpression="(filtervalue=Test)") List<TestService> serviceList) {
			this.serviceList = serviceList;
			this.serviceListInjectionCount++;
		}
	}

	public static class TestDisabledBean {
		@Inject
		@Optional
		@Service(filterExpression = "(component=disabled)")
		TestService disabledService;

		@Inject
		@Service(filterExpression = "(component=disabled)")
		List<TestService> services;
	}

	private List<ServiceRegistration<?>> registrations = new ArrayList<>();

	@After
	public void cleanup() {
		this.registrations.forEach( r -> r.unregister());
	}

	@Test
	public void testInitialInject() {
		IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(FrameworkUtil.getBundle(getClass()).getBundleContext());
		TestBean bean = ContextInjectionFactory.make(TestBean.class, serviceContext);
		assertNotNull(bean.service);
		assertNotNull(bean.serviceList);
		assertSame(SampleServiceA.class, bean.service.getClass());
		assertEquals(1, bean.serviceInjectionCount);

		assertEquals(4, bean.serviceList.size());
		assertEquals(1, bean.serviceListInjectionCount);
		assertSame(SampleServiceA.class, bean.serviceList.get(0).getClass());
		assertSame(SampleServiceB.class, bean.serviceList.get(1).getClass());

	}

	@Test
	public void testStaticFilter() {
		IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(FrameworkUtil.getBundle(getClass()).getBundleContext());
		TestStaticFilterBean bean = ContextInjectionFactory.make(TestStaticFilterBean.class, serviceContext);

		assertNotNull(bean.service);
		assertNotNull(bean.serviceList);

		assertSame(FilterServiceA.class, bean.service.getClass());
		assertEquals(1, bean.serviceInjectionCount);

		assertEquals(2, bean.serviceList.size());
		assertEquals(1, bean.serviceListInjectionCount);
		assertSame(FilterServiceA.class, bean.serviceList.get(0).getClass());
		assertSame(FilterServiceB.class, bean.serviceList.get(1).getClass());
	}

	@Test
	public void testDynamicAdd() {
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(context);
		TestBean bean = ContextInjectionFactory.make(TestBean.class, serviceContext);

		assertEquals(1, bean.serviceInjectionCount);
		assertEquals(1, bean.serviceListInjectionCount);

		TestService t = new TestService() {
			// nothing todo
		};

		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put("service.ranking", 100); //$NON-NLS-1$
		this.registrations.add(context.registerService(TestService.class, t, properties));

		assertSame(t, bean.service);
		assertEquals(2, bean.serviceInjectionCount);

		assertEquals(2, bean.serviceListInjectionCount);
		assertEquals(5, bean.serviceList.size());
		assertSame(t, bean.serviceList.get(0));

		TestService t2 = new TestService() {
			// nothing todo
		};

		properties = new Hashtable<>();
		properties.put("service.ranking", Integer.valueOf(-1)); //$NON-NLS-1$
		this.registrations.add(context.registerService(TestService.class, t2, properties));

		assertSame(t, bean.service);
		assertEquals(3, bean.serviceInjectionCount);

		assertEquals(3, bean.serviceListInjectionCount);

		assertEquals(6, bean.serviceList.size());
		assertSame(t, bean.serviceList.get(0));
	}

	@Test
	public void testDynamicAddRemove() {
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(context);
		TestBean bean = ContextInjectionFactory.make(TestBean.class, serviceContext);

		assertEquals(1, bean.serviceInjectionCount);
		assertEquals(1, bean.serviceListInjectionCount);

		TestService t = new TestService() {
			// nothing todo
		};

		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put("service.ranking", 52); //$NON-NLS-1$
		this.registrations.add(context.registerService(TestService.class, t, properties));

		assertSame(t, bean.service);
		assertEquals(2, bean.serviceInjectionCount);

		assertEquals(2, bean.serviceListInjectionCount);
		assertEquals(5, bean.serviceList.size());
		assertSame(t, bean.serviceList.get(0));

		ServiceRegistration<?> registration = this.registrations.get(0);
		registration.unregister();
		this.registrations.remove(registration);

		assertEquals(3, bean.serviceInjectionCount);
		assertEquals(3, bean.serviceListInjectionCount);

		assertSame(SampleServiceA.class, bean.service.getClass());
		assertEquals(4, bean.serviceList.size());
		assertSame(SampleServiceA.class, bean.serviceList.get(0).getClass());
		assertSame(SampleServiceB.class, bean.serviceList.get(1).getClass());
	}

	@Test
	public void testCleanup() {
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		IEclipseContext iec = EclipseContextFactory.getServiceContext(context).createChild();
		TestBean bean = ContextInjectionFactory.make(TestBean.class, iec);
		iec.dispose();

		TestService t = new TestService() {
			// nothing todo
		};

		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put("service.ranking", 2); //$NON-NLS-1$
		this.registrations.add(context.registerService(TestService.class, t, properties));

		assertSame(SampleServiceA.class, bean.service.getClass());
	}

	@Test
	public void testOptionalReferences() throws InterruptedException {
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(context);
		TestDisabledBean bean = ContextInjectionFactory.make(TestDisabledBean.class, serviceContext);

		assertNull(bean.disabledService);
		assertEquals(0, bean.services.size());

		ServiceReference<ComponentEnabler> ref = context.getServiceReference(ComponentEnabler.class);
		ComponentEnabler enabler = context.getService(ref);
		try {
			enabler.enableDisabledServiceA();
			// give the service registry and the injection some time
			Thread.sleep(100);
			assertNotNull(bean.disabledService);
			assertEquals(1, bean.services.size());
			assertSame(DisabledServiceA.class, bean.disabledService.getClass());

			enabler.enableDisabledServiceB();
			// give the service registry and the injection some time
			Thread.sleep(100);
			assertNotNull(bean.disabledService);
			assertEquals(2, bean.services.size());
			assertSame(DisabledServiceB.class, bean.disabledService.getClass());

			enabler.disableDisabledServiceB();
			// give the service registry and the injection some time
			Thread.sleep(100);
			assertNotNull(bean.disabledService);
			assertEquals(1, bean.services.size());
			assertSame(DisabledServiceA.class, bean.disabledService.getClass());

			enabler.disableDisabledServiceA();
			// give the service registry and the injection some time
			Thread.sleep(100);
			assertNull(bean.disabledService);
			assertEquals(0, bean.services.size());
		} finally {
			enabler.disableDisabledServiceA();
			enabler.disableDisabledServiceB();
			// give the service registry and the injection some time to ensure
			// clear state after this test
			Thread.sleep(100);
		}
	}
}
