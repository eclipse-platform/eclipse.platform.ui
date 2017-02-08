package org.eclipse.e4.core.internal.tests.di.extensions;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Service;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
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

	private List<ServiceRegistration<?>> registrations = new ArrayList<>();

	@After
	public void cleanup() {
		this.registrations.forEach( r -> r.unregister());
	}

	@Test
	public void testInitialInject() {
		IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(FrameworkUtil.getBundle(getClass()).getBundleContext());
		TestBean bean = ContextInjectionFactory.make(TestBean.class, serviceContext);
		Assert.assertNotNull(bean.service);
		Assert.assertNotNull(bean.serviceList);
		Assert.assertSame(SampleServiceA.class,bean.service.getClass());
		Assert.assertEquals(1, bean.serviceInjectionCount);

		Assert.assertEquals(4, bean.serviceList.size());
		Assert.assertEquals(1, bean.serviceListInjectionCount);
		Assert.assertSame(SampleServiceA.class,bean.serviceList.get(0).getClass());
		Assert.assertSame(SampleServiceB.class,bean.serviceList.get(1).getClass());

	}

	@Test
	public void testStaticFilter() {
		IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(FrameworkUtil.getBundle(getClass()).getBundleContext());
		TestStaticFilterBean bean = ContextInjectionFactory.make(TestStaticFilterBean.class, serviceContext);

		Assert.assertNotNull(bean.service);
		Assert.assertNotNull(bean.serviceList);

		Assert.assertSame(FilterServiceA.class,bean.service.getClass());
		Assert.assertEquals(1, bean.serviceInjectionCount);

		Assert.assertEquals(2, bean.serviceList.size());
		Assert.assertEquals(1, bean.serviceListInjectionCount);
		Assert.assertSame(FilterServiceA.class,bean.serviceList.get(0).getClass());
		Assert.assertSame(FilterServiceB.class,bean.serviceList.get(1).getClass());
	}

	@Test
	public void testDynamicAdd() {
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(context);
		TestBean bean = ContextInjectionFactory.make(TestBean.class, serviceContext);

		Assert.assertEquals(1, bean.serviceInjectionCount);
		Assert.assertEquals(1, bean.serviceListInjectionCount);

		TestService t = new TestService() {
			// nothing todo
		};

		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put("service.ranking", 100); //$NON-NLS-1$
		this.registrations.add(context.registerService(TestService.class, t, properties));

		Assert.assertSame(t,bean.service);
		Assert.assertEquals(2, bean.serviceInjectionCount);

		Assert.assertEquals(2, bean.serviceListInjectionCount);
		Assert.assertEquals(5, bean.serviceList.size());
		Assert.assertSame(t,bean.serviceList.get(0));

		TestService t2 = new TestService() {
			// nothing todo
		};

		properties = new Hashtable<>();
		properties.put("service.ranking", Integer.valueOf(-1)); //$NON-NLS-1$
		this.registrations.add(context.registerService(TestService.class, t2, properties));

		Assert.assertSame(t,bean.service);
		Assert.assertEquals(3, bean.serviceInjectionCount);

		Assert.assertEquals(3, bean.serviceListInjectionCount);

		Assert.assertEquals(6, bean.serviceList.size());
		Assert.assertSame(t,bean.serviceList.get(0));
	}

	@Test
	public void testDynamicAddRemove() {
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(context);
		TestBean bean = ContextInjectionFactory.make(TestBean.class, serviceContext);

		Assert.assertEquals(1, bean.serviceInjectionCount);
		Assert.assertEquals(1, bean.serviceListInjectionCount);

		TestService t = new TestService() {
			// nothing todo
		};

		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put("service.ranking", 52); //$NON-NLS-1$
		this.registrations.add(context.registerService(TestService.class, t, properties));

		Assert.assertSame(t,bean.service);
		Assert.assertEquals(2, bean.serviceInjectionCount);

		Assert.assertEquals(2, bean.serviceListInjectionCount);
		Assert.assertEquals(5, bean.serviceList.size());
		Assert.assertSame(t,bean.serviceList.get(0));

		ServiceRegistration<?> registration = this.registrations.get(0);
		registration.unregister();
		this.registrations.remove(registration);

		Assert.assertEquals(3, bean.serviceInjectionCount);
		Assert.assertEquals(3, bean.serviceListInjectionCount);

		Assert.assertSame(SampleServiceA.class,bean.service.getClass());
		Assert.assertEquals(4, bean.serviceList.size());
		Assert.assertSame(SampleServiceA.class,bean.serviceList.get(0).getClass());
		Assert.assertSame(SampleServiceB.class,bean.serviceList.get(1).getClass());
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

		Assert.assertSame(SampleServiceA.class,bean.service.getClass());
	}
}
