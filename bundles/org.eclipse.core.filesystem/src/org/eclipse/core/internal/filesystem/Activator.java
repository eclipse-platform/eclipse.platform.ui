/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.filesystem;

import java.io.File;
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
public class Activator implements BundleActivator {
	private static Activator instance;
	private BundleContext context;

	/**
	 * Returns the local file system location that should be used for 
	 * caching file data.
	 */
	public static IPath getCacheLocation() {
		//try to put the cache in the instance location if possible (3.2 behaviour)
		try {
			if (instance != null) {
				BundleContext ctx = instance.context;
				if (ctx != null) {
					ServiceReference[] refs = ctx.getServiceReferences(Location.class.getName(), Location.INSTANCE_FILTER);
					if (refs != null && refs.length == 1) {
						Location location = (Location) ctx.getService(refs[0]);
						if (location != null) {
							IPath instancePath = new Path(new File(location.getURL().getFile()).toString());
							ctx.ungetService(refs[0]);
							return instancePath.append(".metadata/.plugins").append(Policy.PI_FILE_SYSTEM); //$NON-NLS-1$
						}
					}
				}
			}
		} catch (InvalidSyntaxException e) {
			Policy.log(IStatus.INFO, null, e);
			//fall through below and use user home
		}
		//just put the cache in the user home directory
		return Path.fromOSString(System.getProperty("user.home")); //$NON-NLS-1$
		//		Platform.getStateLocation(Platform.getBundle(Policy.PI_FILE_SYSTEM));
	}

	public Activator() {
		instance = this;
	}

	public static Enumeration findEntries(String path, String filePattern, boolean recurse) {
		if (instance != null && instance.context != null)
			return instance.context.getBundle().findEntries(path, filePattern, recurse);
		return null;
	}

	public void start(BundleContext aContext) throws Exception {
		this.context = aContext;
	}

	public void stop(BundleContext aContext) throws Exception {
		//nothing to do
	}

}
