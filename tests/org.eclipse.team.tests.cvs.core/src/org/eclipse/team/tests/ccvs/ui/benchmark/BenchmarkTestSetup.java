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

import org.eclipse.core.runtime.*;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.osgi.framework.Bundle;

public class BenchmarkTestSetup extends CVSTestSetup {
	public static final File BIG_ZIP_FILE;
	public static final File SMALL_ZIP_FILE;
	public static final File TINY_ZIP_FILE;
	public static int LOOP_COUNT;

	// Static initializer for constants
	static {
	    try {
			LOOP_COUNT = Integer.valueOf(System.getProperty("eclipse.cvs.loopCount", "3")).intValue();
		} catch (NumberFormatException e1) {
			LOOP_COUNT = 1;
		}
		try {
			BIG_ZIP_FILE = getTestFile("benchmarkBig.zip");
			SMALL_ZIP_FILE = getTestFile("benchmarkSmall.zip");
			TINY_ZIP_FILE = getTestFile("benchmarkTiny.zip");
		} catch (IOException e) {
			throw new Error(e.getMessage());
		}
	}
	
	public static File getTestFile(String name) throws IOException {
		Bundle b = Platform.getBundle("org.eclipse.team.tests.cvs.core");
		URL url = b.getEntry("resources/BenchmarkTest/" + name);
		url = Platform.resolve(url);
		if (url.getProtocol().equals("file")) {
			return new File(url.getFile()).getAbsoluteFile();
		}
		throw new IOException("Cannot find test file: " + name);
	}

	public BenchmarkTestSetup(Test test) {
		super(test);
	}
}
