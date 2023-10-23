/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.session;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.tests.harness.FileSystemHelper;
import org.eclipse.core.tests.session.SessionTestSuite;
import org.eclipse.core.tests.session.Setup;
import org.eclipse.core.tests.session.SetupManager.SetupException;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.decorators.BadIndexDecorator;
import org.eclipse.ui.tests.harness.util.FileTool;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Wrapper for workbench session tests.
 *
 * @since 3.1
 */
public class WorkbenchSessionTest extends SessionTestSuite {

	private Map<String, String> arguments;

	private final String dataLocation;

	/**
	 * Create a new workbench session test.
	 *
	 * @param dataLocation
	 *            the location of the workspace to test, relative to
	 *            data/workspaces
	 * @param clazz
	 *            the <code>Test</code> class
	 */
	public WorkbenchSessionTest(String dataLocation, Class<?> clazz, Map<String, String> arguments) {
		this(dataLocation, clazz);
		this.arguments = arguments;
	}

	/**
	 * Create a new workbench session test.
	 *
	 * @param dataLocation
	 *            the location of the workspace to test, relative to
	 *            data/workspaces
	 * @param arguments
	 *            a map of arguments to use
	 */
	public WorkbenchSessionTest(String dataLocation, Map<String, String> arguments) {
		this(dataLocation);
		this.arguments = arguments;
	}

	/**
	 * Create a new workbench session test.
	 *
	 * @param dataLocation
	 *            the location of the workspace to test, relative to
	 *            data/workspaces
	 * @param clazz
	 *            the <code>Test</code> class
	 */
	public WorkbenchSessionTest(String dataLocation, Class<?> clazz) {
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
		super("org.eclipse.ui.tests");
		setApplicationId(SessionTestSuite.UI_TEST_APPLICATION);
		this.dataLocation = dataLocation;
	}

	/**
	 * Ensures setup uses this suite's instance location.
	 *
	 * @throws SetupException
	 */
	@Override
	protected Setup newSetup() throws SetupException {
		Setup base = super.newSetup();
		try {
			base.setEclipseArgument(Setup.DATA, copyDataLocation());
			if (arguments != null) {
				for (Entry<String, String> entry : arguments.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					base.setEclipseArgument(key, value);
				}
			}
		} catch (Exception e) {
			throw new SetupException(e.getMessage(), e);
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
		if (plugin == null) {
			throw new IllegalStateException(
					"TestPlugin default reference is null");
		}

		Bundle bundle = FrameworkUtil.getBundle(BadIndexDecorator.class);
		URL fullPathString = bundle.getEntry("data/workspaces/" + dataLocation + ".zip");

		if (fullPathString == null) {
			throw new IllegalArgumentException();
		}

		IPath path = IPath.fromOSString(fullPathString.getPath());

		File origin = path.toFile();
		if (!origin.exists()) {
			throw new IllegalArgumentException();
		}

		ZipFile zFile = new ZipFile(origin);

		File destination = new File(FileSystemHelper.getRandomLocation(FileSystemHelper.getTempDir()).toOSString());
		FileTool.unzip(zFile, destination);
		return destination.getAbsolutePath();
	}
}
