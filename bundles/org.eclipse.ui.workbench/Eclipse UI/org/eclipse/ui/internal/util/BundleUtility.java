/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.util;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
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
    	if ((bundle.getState() & Bundle.STARTING) != 0)
    		return WorkbenchPlugin.getDefault().isStarting(bundle);
        return bundle != null && (bundle.getState() & (Bundle.ACTIVE | Bundle.STOPPING)) != 0;
    }

    // TODO: needs a better name
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
        return Platform.find(bundle, new Path(path));
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
        Platform.getLog(bundle).log(status);
    }
}
