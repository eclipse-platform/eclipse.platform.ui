/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class URLTest extends RuntimeTest {

	public URLTest(String name) {
		super(name);
	}

	public void testPlatformPlugin() throws IOException {
		URL url = new URL("platform:/plugin/org.eclipse.core.tests.runtime/plugin.xml");
		InputStream is = url.openStream();
		is.close();
	}
}
