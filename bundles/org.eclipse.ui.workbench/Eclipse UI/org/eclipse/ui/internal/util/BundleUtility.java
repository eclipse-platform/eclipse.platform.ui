/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
import org.osgi.framework.Bundle;

// TODO: needs a better name
public class BundleUtility {
	public static boolean isActive(Bundle bundle) {
		if (bundle == null)
			return false;

		return bundle.getState() == Bundle.ACTIVE;
	}

	public static boolean isActivated(Bundle bundle) {
		if (bundle == null)
			return false;

		switch (bundle.getState()) {
			case Bundle.STARTING :
			case Bundle.ACTIVE :
			case Bundle.STOPPING :
				return true;
			default :
				return false;
		}
	}

	// TODO: needs a better name
	public static boolean isReady(Bundle bundle) {
		if (bundle == null)
			return false;

		switch (bundle.getState()) {
			case Bundle.RESOLVED :
			case Bundle.STARTING :
			case Bundle.ACTIVE :
			case Bundle.STOPPING :
				return true;
			default :
				return false;
		}
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
	    if(bundle == null)
	        return null;
		return Platform.find(bundle, new Path(path));
	}

	public static URL find(String bundleId, String path) {
	    return find(Platform.getBundle(bundleId), path);
	}
 
 public static void log(String bundleId, Throwable exception) {
        Bundle bundle = Platform.getBundle(bundleId);
        if (bundle == null)
            return;

        IStatus status = new Status(IStatus.ERROR, bundleId, IStatus.ERROR,
        		exception.getMessage() == null ? "" : exception.getMessage(), //$NON-NLS-1$
        		exception);

        Platform.getLog(bundle).log(status);
    }
}
