/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.session;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.harness.FileSystemHelper;
import org.eclipse.core.tests.session.SessionTestSuite;
import org.eclipse.core.tests.session.Setup;
import org.eclipse.core.tests.session.SetupManager;
import org.eclipse.core.tests.session.SetupManager.SetupException;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.harness.util.FileTool;

/**
 * Wrapper for workbench session tests.
 * 
 * @since 3.1
 */
public class WorkbenchSessionTest extends SessionTestSuite {

	private String dataLocation;
	
	/**
	 * Create a new workbench session test.
	 * 
	 * @param dataLocation
	 *            the location of the workspace to test, relative to
	 *            data/workspaces
	 * @param clazz
	 *            the <code>Test</code> class
	 */
	public WorkbenchSessionTest(String dataLocation, Class clazz) {
		super("org.eclipse.ui.tests", clazz);
		setApplicationId(SessionTestSuite.UI_TEST_APPLICATION);		
		this.dataLocation = dataLocation;
	}
	
	/**
	 * Create a new workbench session test.
	 * 
	 * @param dataLocation
	 *            the location of the workspace to test, relative to
	 *            data/workspaces
	 * @since 3.4
	 */
	public WorkbenchSessionTest(String dataLocation) {
		super(dataLocation);
		setApplicationId(SessionTestSuite.UI_TEST_APPLICATION);		
		this.dataLocation = dataLocation;
	}

	/**
	 * Ensures setup uses this suite's instance location.
	 * 
	 * @throws SetupException
	 */
	protected Setup newSetup() throws SetupException {
		Setup base = super.newSetup();
		try {
			base.setEclipseArgument(Setup.DATA, copyDataLocation());
		} catch (Exception e) {
			throw SetupManager.getInstance().new SetupException(e.getMessage(),
					e);
		}
		return base;
	}

	/**
	 * Copies the data to a temporary directory and returns the new location.
	 * 
	 * @return the location
	 */
	private String copyDataLocation() throws IOException {
        TestPlugin plugin = TestPlugin.getDefault();
        if (plugin == null)
            throw new IllegalStateException(
                    "TestPlugin default reference is null");
        
        URL fullPathString = plugin.getDescriptor().find(
				new Path("data/workspaces/" + dataLocation + ".zip"));
        
        if (fullPathString == null) 
        	throw new IllegalArgumentException();
        
        IPath path = new Path(fullPathString.getPath());

        File origin = path.toFile();
        if (!origin.exists())
			throw new IllegalArgumentException();
        
        ZipFile zFile = new ZipFile(origin);        
		
		File destination = new File(FileSystemHelper.getRandomLocation(FileSystemHelper.getTempDir()).toOSString());
		FileTool.unzip(zFile, destination);
		return destination.getAbsolutePath();
	}
}
