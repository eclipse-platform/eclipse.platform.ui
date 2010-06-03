package org.eclipse.e4.tools.compat.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

public class Util {
	public static Bundle getBundle(String bundleName) {
		Bundle bundle = FrameworkUtil.getBundle(Util.class);
		BundleContext ctx = bundle.getBundleContext();
		ServiceReference ref = bundle.getBundleContext().getServiceReference(PackageAdmin.class.getName());
		PackageAdmin bundleAdmin = (PackageAdmin) ctx.getService(ref);
		Bundle[] bundles = bundleAdmin.getBundles(bundleName, null);
		if (bundles == null)
			return null;
		// Return the first bundle that is not installed or uninstalled
		for (int i = 0; i < bundles.length; i++) {
			if ((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
				return bundles[i];
			}
		}
		return null;
	}
}
