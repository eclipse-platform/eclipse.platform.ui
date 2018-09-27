package org.eclipse.e4.ui.bindings.tests;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.osgi.framework.FrameworkUtil;

public class TestUtil {

	private static IEclipseContext appContext;

	public static IEclipseContext getGlobalContext() {
		if (appContext == null) {
			synchronized (TestUtil.class) {
				IEclipseContext serviceContext = EclipseContextFactory
						.getServiceContext(FrameworkUtil.getBundle(TestUtil.class).getBundleContext());
				appContext = serviceContext.createChild();
			}
		}

		return appContext;
	}

}
