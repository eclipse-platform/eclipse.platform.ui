package org.eclipse.e4.tools.compat.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

@SuppressWarnings("deprecation")
public class Util {
	public static Bundle getBundle(String bundleName) {
		final Bundle bundle = FrameworkUtil.getBundle(Util.class);
		final BundleContext ctx = bundle.getBundleContext();
		final ServiceReference<PackageAdmin> ref = bundle.getBundleContext().getServiceReference(
			PackageAdmin.class);
		final PackageAdmin bundleAdmin = ctx.getService(ref);
		final Bundle[] bundles = bundleAdmin.getBundles(bundleName, null);
		if (bundles == null) {
			return null;
		}
		// Return the first bundle that is not installed or uninstalled
		for (int i = 0; i < bundles.length; i++) {
			if ((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
				return bundles[i];
			}
		}
		return null;
	}
}
