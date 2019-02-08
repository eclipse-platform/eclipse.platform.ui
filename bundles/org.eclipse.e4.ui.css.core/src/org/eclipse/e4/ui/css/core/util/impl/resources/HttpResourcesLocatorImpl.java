/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.util.impl.resources;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.eclipse.e4.ui.css.core.util.resources.IResourceLocator;

/**
 * Http resources locator implementation.
 */
public class HttpResourcesLocatorImpl implements IResourceLocator {

	@Override
	public String resolve(String uri) {
		if (uri.startsWith("http")) {
			return uri;
		}
		return null;
	}

	@Override
	public InputStream getInputStream(String uri) throws Exception {
		URL url = new java.net.URL((new File("./")).toURL(), uri);
		return url.openStream();
	}


}
