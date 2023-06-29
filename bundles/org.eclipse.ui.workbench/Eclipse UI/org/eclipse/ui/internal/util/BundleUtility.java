/*******************************************************************************
 * Copyright (c) 2003, 2014 IBM Corporation and others.
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
 *     Aurelien Pupier <aurelien.pupier@bonitasoft.com> - Bug 450701
 *******************************************************************************/
package org.eclipse.ui.internal.util;

import java.net.URL;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.osgi.framework.Bundle;

/**
 * A set of static methods that provide an nicer interface to common platform
 * operations related to bundle management.
 */
public class BundleUtility {
	public static boolean isActive(Bundle bundle) {
		if (bundle == null) {
			return false;
		}
		return bundle.getState() == Bundle.ACTIVE;
	}

	public static boolean isActivated(Bundle bundle) {
		if (bundle != null && (bundle.getState() & Bundle.STARTING) != 0)
			return WorkbenchPlugin.getDefault().isStarting(bundle);
		return bundle != null && (bundle.getState() & (Bundle.ACTIVE | Bundle.STOPPING)) != 0;
	}

	public static boolean isReady(Bundle bundle) {
		return bundle != null && isReady(bundle.getState());
	}

	public static boolean isReady(int bundleState) {
		return (bundleState & (Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING)) != 0;
	}

	public static boolean isActive(String bundleId) {
		return isActive(Platform.getBundle(bundleId));
	}

	public static boolean isActivated(String bundleId) {
		return isActivated(Platform.getBundle(bundleId));
	}

	public static boolean isReady(String bundleId) {
		return isReady(Platform.getBundle(bundleId));
	}

	public static URL find(Bundle bundle, String path) {
		if (bundle == null) {
			return null;
		}
		return FileLocator.find(bundle, IPath.fromOSString(path));
	}

	public static URL find(String bundleId, String path) {
		return find(Platform.getBundle(bundleId), path);
	}

	public static void log(String bundleId, Throwable exception) {
		Bundle bundle = Platform.getBundle(bundleId);
		if (bundle == null) {
			return;
		}
		IStatus status = new Status(IStatus.ERROR, bundleId, IStatus.ERROR,
				exception.getMessage() == null ? "" : exception.getMessage(), //$NON-NLS-1$
				exception);
		ILog.of(bundle).log(status);
	}
}
