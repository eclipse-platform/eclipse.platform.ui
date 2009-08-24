/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.webapp.servlet;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.help.webapp.IFilter;

/**
 * This class is a filter based on PluginsRootResolvingStream
 * which replaces PLUGINS_ROOT with a relative path to eliminate redirects.
 * It also performs preprocessing to add child links at runtime.
 */
public class PluginsRootFilter implements IFilter {	
	
    public OutputStream filter(HttpServletRequest req, OutputStream out) {
		String pathPrefix = FilterUtils.getRelativePathPrefix(req);
		if (pathPrefix.length() >= 3) {
		    return new PluginsRootResolvingStream(out, req, pathPrefix.substring(0, pathPrefix.length() - 3));
		}
		return out;
	}

}
