package org.eclipse.e4.tools.services;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

@SuppressWarnings("deprecation")
public class ToolsServicesActivator implements BundleActivator {

	static private ToolsServicesActivator defaultInstance;
	private BundleContext bundleContext;
	private ServiceTracker<PackageAdmin, PackageAdmin> pkgAdminTracker;
	private ServiceTracker<LogService, LogService> logTracker;
	public static final String PLUGIN_ID = "org.eclipse.e4.tools.services"; //$NON-NLS-1$

	public ToolsServicesActivator() {
		defaultInstance = this;
	}

	public static ToolsServicesActivator getDefault() {
		return defaultInstance;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		bundleContext = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (pkgAdminTracker != null) {
			pkgAdminTracker.close();
			pkgAdminTracker = null;
		}
		if (logTracker != null) {
			logTracker.close();
			logTracker = null;
		}
		bundleContext = null;
	}

	public PackageAdmin getPackageAdmin() {
		if (pkgAdminTracker == null) {
			if (bundleContext == null) {
				return null;
			}
			pkgAdminTracker = new ServiceTracker<PackageAdmin, PackageAdmin>(bundleContext,
				PackageAdmin.class, null);
			pkgAdminTracker.open();
		}
		return pkgAdminTracker.getService();
	}

	public LogService getLogService() {
		if (logTracker == null) {
			if (bundleContext == null) {
				return null;
			}
			logTracker = new ServiceTracker<LogService, LogService>(bundleContext,
				LogService.class, null);
			logTracker.open();
		}
		return logTracker.getService();
	}

}
