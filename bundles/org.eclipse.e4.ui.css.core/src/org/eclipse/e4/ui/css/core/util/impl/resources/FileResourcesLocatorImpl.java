/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.util.impl.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import org.eclipse.e4.ui.css.core.util.resources.IResourceLocator;

/**
 * Basic File resources locator implementation.
 */
public class FileResourcesLocatorImpl implements IResourceLocator {
	private static final String FILE_SCHEME = "file:";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.util.resources.IURIResolver#resolve(java.lang.String)
	 */
	public String resolve(String uri) {
		File file = toFile(uri);
		return file.exists() ? uri : null;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.util.resources.IResourceLocator#getInputStream(java.lang.String)
	 */
	public InputStream getInputStream(String uri) throws Exception {
		return new FileInputStream(toFile(uri));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.util.resources.IResourceLocator#getReader(java.lang.String)
	 */
	public Reader getReader(String uri) throws Exception {
		return new FileReader(toFile(uri));
	}

	private File toFile(String uri) {
		if (uri.startsWith(FILE_SCHEME)) {
			return new File(uri.substring(FILE_SCHEME.length()));
		}
		return new File(uri);
	}
}
