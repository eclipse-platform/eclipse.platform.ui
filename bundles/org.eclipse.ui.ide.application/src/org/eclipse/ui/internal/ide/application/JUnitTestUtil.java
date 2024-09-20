/*******************************************************************************
 * Copyright (c) 2024 SAP SE.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthias Becker / Sebastian Ratz - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.application;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.osgi.service.environment.EnvironmentInfo;

public class JUnitTestUtil {
	private static Boolean cachedIsJunitTestRunning = null;

	public static boolean isJunitTestRunning() {
		if (cachedIsJunitTestRunning == null) {
			try {
				if (Platform.isRunning()) {
					AtomicBoolean result = new AtomicBoolean();
					cachedIsJunitTestRunning = ServiceCaller.callOnce(JUnitTestUtil.class, EnvironmentInfo.class, envInfo -> {
						String application = envInfo.getProperty("eclipse.application"); //$NON-NLS-1$
						result.set(application != null && Set.of( //
								// see org.eclipse.pde.internal.launching.IPDEConstants
								"org.eclipse.pde.junit.runtime.nonuithreadtestapplication", // //$NON-NLS-1$
								"org.eclipse.pde.junit.runtime.uitestapplication", // //$NON-NLS-1$
								"org.eclipse.pde.junit.runtime.coretestapplication", // //$NON-NLS-1$
								// bundle "org.eclipse.test" (Platform tests)
								"org.eclipse.test.uitestapplication", //$NON-NLS-1$
								"org.eclipse.test.coretestapplication", // //$NON-NLS-1$
								// see org.eclipse.tycho.surefire.AbstractTestMojo
								"org.eclipse.tycho.surefire.osgibooter.uitest", //$NON-NLS-1$
								"org.eclipse.tycho.surefire.osgibooter.headlesstest") // //$NON-NLS-1$
								.contains(application));
					});
					cachedIsJunitTestRunning = result.get();
				} else {
					cachedIsJunitTestRunning = true; // probably
				}
			} catch (Throwable t) {
				// log
				cachedIsJunitTestRunning = false;
			}
		}

		return cachedIsJunitTestRunning;
	}

}