/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.core.boot;

import java.io.File;
import java.util.Date;

import junit.framework.TestCase;

public class PlatformConfigurationTestCase extends TestCase {
	
	protected String tempDir;
	
	public PlatformConfigurationTestCase(String name) {
		super(name);
		try {
			init();
		} catch (Exception e) {
			fail(e.toString());
			e.printStackTrace();
		}
	}
	
	protected void init() {
	}
	
	protected void setUp() {
		// get new temp directory for testcase
		String root = getTempDirectoryName();
		setupDirectory(root);
		tempDir = root;
	}
	
	protected void tearDown() {
		// cleanup testcase temp directory
		if (tempDir != null)
			cleanupDirectory(tempDir);
	}
	
	protected String getTempDirectoryName() {
		String tmp = System.getProperty("java.io.tmpdir");
		if (!tmp.endsWith(File.separator))
			tmp += File.separator;
		return tmp+"eclipse"+File.separator+(new Date().getTime()+File.separator);
	}
	
	protected void setupDirectory(String name) {
		File dir = new File(name);
		dir.mkdirs();
	}
	
	protected void cleanupDirectory(String name) {
		File dir = new File(name);
		deleteDirectory(dir);
	}
	
	private void deleteDirectory(File dir) {
		File[] list = dir.listFiles();
		if (list == null)
			return;
			
		for (int i=0; i<list.length; i++) {
			if (list[i].isDirectory()) 
				deleteDirectory(list[i]);
			if (!list[i].delete())
				System.out.println("Unable to delete "+list[i].toString());
		}
	}
}

