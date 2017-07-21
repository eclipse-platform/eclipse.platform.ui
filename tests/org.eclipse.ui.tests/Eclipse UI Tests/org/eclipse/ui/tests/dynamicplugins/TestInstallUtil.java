/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

import junit.framework.TestCase;

public class TestInstallUtil extends TestCase {
    static BundleContext context;

    public static void setContext(BundleContext newContext) {
        context = newContext;
    }

    public static Bundle installBundle(String pluginLocation)
            throws BundleException, IllegalStateException {
        Bundle target = context.installBundle(pluginLocation);
        int state = target.getState();
        if (state != Bundle.INSTALLED) {
			throw new IllegalStateException("Bundle " + target
                    + " is in a wrong state: " + state);
		}
        refreshPackages(new Bundle[] { target });
        return target;
    }

    public static void uninstallBundle(Bundle target) throws BundleException {
        target.uninstall();
        refreshPackages(null);
    }

    public static void refreshPackages(Bundle[] bundles) {
		ServiceReference<PackageAdmin> packageAdminRef = context
				.getServiceReference(PackageAdmin.class);
        PackageAdmin packageAdmin = null;
        if (packageAdminRef != null) {
            packageAdmin = context.getService(packageAdminRef);
            if (packageAdmin == null) {
				return;
			}
        }

        final boolean[] flag = new boolean[] { false };
		FrameworkListener listener = event -> {
			if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED) {
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
                }
            }
        }
        context.removeFrameworkListener(listener);
        context.ungetService(packageAdminRef);
    }
}
