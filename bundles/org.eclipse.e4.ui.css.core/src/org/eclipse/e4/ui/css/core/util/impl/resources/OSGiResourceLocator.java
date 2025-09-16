/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 423744
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.util.impl.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.ui.css.core.util.resources.IResourceLocator;

public class OSGiResourceLocator implements IResourceLocator {

	private final String startLocation;

	public OSGiResourceLocator(String start) {
		startLocation = start;
	}

	@Override
	public String resolve(String uri) {
		if (!uri.startsWith("platform:/plugin/")) {
			uri = startLocation + uri;
		}

		try {
			URL resolvedURL = FileLocator.resolve(new URL(uri));
			return resolvedURL.toString();
		} catch (IOException e) {

		}
		return null;
	}

	@Override
	public InputStream getInputStream(String uri) throws Exception {
		return FileLocator.resolve(new URL(startLocation + uri)).openStream();
	}

}
