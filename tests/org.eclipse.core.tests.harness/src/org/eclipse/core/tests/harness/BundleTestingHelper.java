/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.harness;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.*;
import org.osgi.service.packageadmin.PackageAdmin;

public class BundleTestingHelper {
	public static Bundle installBundle(BundleContext context, String location) throws BundleException, MalformedURLException, IOException {
		URL entry = context.getBundle().getEntry(location);
		Bundle installed = context.installBundle(Platform.asLocalURL(entry).toExternalForm());
		return installed;
	}

	/**
	 * Do PackageAdmin.refreshPackages() in a synchronous way.  After installing
	 * all the requested bundles we need to do a refresh and want to ensure that 
	 * everything is done before returning.
	 * @param bundles
	 */
	//copied from EclipseStarter
	public static void refreshPackages(BundleContext context, Bundle[] bundles) {
		if (bundles.length == 0)
			return;
		ServiceReference packageAdminRef = context.getServiceReference(PackageAdmin.class.getName());
		PackageAdmin packageAdmin = null;
		if (packageAdminRef != null) {
			packageAdmin = (PackageAdmin) context.getService(packageAdminRef);
			if (packageAdmin == null)
				return;
		}
		// TODO this is such a hack it is silly.  There are still cases for race conditions etc
		// but this should allow for some progress...
		// (patch from John A.)
		final boolean[] flag = new boolean[] {false};
		FrameworkListener listener = new FrameworkListener() {
			public void frameworkEvent(FrameworkEvent event) {
				if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED)
					synchronized (flag) {
						flag[0] = true;
						flag.notifyAll();
					}
			}
		};
		context.addFrameworkListener(listener);
		packageAdmin.refreshPackages(bundles);
		synchronized (flag) {
			while (!flag[0]) {
				try {
					flag.wait();
				} catch (InterruptedException e) {
					// who cares....
				}
			}
		}
		context.removeFrameworkListener(listener);
		context.ungetService(packageAdminRef);
	}

	public static Bundle[] getBundles(BundleContext context, String symbolicName, String version) {
		ServiceReference packageAdminReference = context.getServiceReference(PackageAdmin.class.getName());
		if (packageAdminReference == null)
			throw new IllegalStateException("No package admin service found");
		PackageAdmin packageAdmin = (PackageAdmin) context.getService(packageAdminReference);
		Bundle[] result = packageAdmin.getBundles(symbolicName, version);
		context.ungetService(packageAdminReference);
		return result;
	}

}