/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.ui.benchmark;


import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.Test;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;

public class BenchmarkTestSetup extends CVSTestSetup {
	public static final File BIG_ZIP_FILE;
	public static final File SMALL_ZIP_FILE;
	public static final File TINY_ZIP_FILE;

	// Static initializer for constants
	static {
		try {
			BIG_ZIP_FILE = getTestFile("benchmarkBig.zip");
			SMALL_ZIP_FILE = getTestFile("benchmarkSmall.zip");
			TINY_ZIP_FILE = getTestFile("benchmarkTiny.zip");
		} catch (IOException e) {
			throw new Error(e.getMessage());
		}
	}
	
	public static File getTestFile(String name) throws IOException {
		IPluginRegistry registry = Platform.getPluginRegistry();
		IPluginDescriptor descriptor = registry.getPluginDescriptor("org.eclipse.team.tests.cvs.core");
		URL baseURL = descriptor.getInstallURL();
		URL url = new URL(baseURL, "resources/BenchmarkTest/" + name);
		url = Platform.asLocalURL(url);
		if (url.getProtocol().equals("file")) {
			return new File(url.getFile()).getAbsoluteFile();
		}
		throw new IOException("Cannot find test file: " + name);
	}

	public BenchmarkTestSetup(Test test) {
		super(test);
	}
}
