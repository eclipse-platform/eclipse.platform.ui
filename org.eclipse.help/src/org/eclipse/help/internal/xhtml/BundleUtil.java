/***************************************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.internal.xhtml;

import java.net.URL;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * Bundle convenience methods.
 */
public class BundleUtil {

	/*
	 * Util method to return an URL to a plugin relative resource.
	 */
	public static URL getResourceAsURL(String resource, String pluginId) {
		Bundle bundle = Platform.getBundle(pluginId);
		URL localLocation = localLocation = Platform.find(bundle, new Path(resource));
		return localLocation;
	}

}
