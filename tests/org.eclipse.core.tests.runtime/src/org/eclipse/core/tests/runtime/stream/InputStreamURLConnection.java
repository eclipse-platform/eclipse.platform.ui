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
package org.eclipse.core.tests.runtime.stream;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

class InputStreamURLConnection extends URLConnection {
	public InputStreamURLConnection(URL url) {
		super(url);
	}

	public void connect() {
		// no-op since our info is in a registry in memory
	}

	public InputStream getInputStream() {
		return InputStreamRegistry.get(url);
	}
}