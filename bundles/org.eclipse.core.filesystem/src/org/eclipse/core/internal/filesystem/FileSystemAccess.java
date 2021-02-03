/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
package org.eclipse.core.internal.filesystem;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.*;

/**
 * This class abstracts away implementation details of the filesystem
 * bundle that depend on the OSGi framework being started. All
 * file system bundle functionality is enabled regardless of whether
 * OSGi is running.
 * @since org.eclipse.core.filesystem 1.1
 */
public class FileSystemAccess {
	private static BundleContext context = FrameworkUtil.getBundle(FileSystemAccess.class).getBundleContext();

	/**
	 * Returns the local file system location that should be used for
	 * caching file data.
	 */
	public static IPath getCacheLocation() {
		//try to put the cache in the instance location if possible (3.2 behaviour)
		try {
			if (context != null) {
				Collection<ServiceReference<Location>> refs = context.getServiceReferences(Location.class, Location.INSTANCE_FILTER);
				if (refs != null && refs.size() == 1) {
					ServiceReference<Location> ref = refs.iterator().next();
					Location location = context.getService(ref);
					if (location != null) {
						IPath instancePath = new Path(new File(location.getURL().getFile()).toString());
						context.ungetService(ref);
						return instancePath.append(".metadata/.plugins").append(Policy.PI_FILE_SYSTEM); //$NON-NLS-1$
					}
				}
			}
		} catch (InvalidSyntaxException e) {
			Policy.log(IStatus.INFO, null, e);
			//fall through below and use user home
		}
		//just put the cache in the user home directory
		return Path.fromOSString(System.getProperty("user.home")); //$NON-NLS-1$
	}

	public static Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
		if (context != null)
			return context.getBundle().findEntries(path, filePattern, recurse);
		return null;
	}

}
