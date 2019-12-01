/*******************************************************************************
 * Copyright (c) 2004, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import static java.util.Arrays.asList;

import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.wiring.FrameworkWiring;

public class TestInstallUtil {
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
		refreshPackages(asList(target));
		return target;
	}

	public static void uninstallBundle(Bundle target) throws BundleException {
		target.uninstall();
		refreshPackages(null);
	}

	public static void refreshPackages(List<Bundle> bundles) {
		FrameworkWiring wiring = context.getBundle(Constants.SYSTEM_BUNDLE_LOCATION).adapt(FrameworkWiring.class);

		final boolean[] flag = new boolean[] { false };
		FrameworkListener listener = event -> {
			if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED) {
				synchronized (flag) {
					flag[0] = true;
					flag.notifyAll();
				}
			}
		};

		wiring.refreshBundles(bundles, listener);

		synchronized (flag) {
			while (!flag[0]) {
				try {
					flag.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
