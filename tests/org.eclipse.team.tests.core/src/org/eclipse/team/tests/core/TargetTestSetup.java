/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.tests.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.eclipse.core.runtime.Path;
import org.eclipse.team.internal.core.target.ISiteFactory;
import org.eclipse.team.internal.core.target.Site;
import org.eclipse.team.internal.core.target.TargetManager;
import org.eclipse.team.internal.core.target.TargetProvider;

/**
 * A set of test cases for org.eclipse.team.core.sync.IRemoteResource
 */
public class TargetTestSetup extends TestSetup {

	public static Properties properties;
	static {
		loadProperties();
	}

	public static void loadProperties() {
		properties = new Properties();
		String propertiesFile = System.getProperty("eclipse.target.properties");
		if (propertiesFile == null)
			return;
		File file = new File(propertiesFile);
		if (file.isDirectory())
			file = new File(file, "target.properties");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			try {
				for (String line;(line = reader.readLine()) != null;) {
					int sep = line.indexOf("=");
					String property = line.substring(0, sep).trim();
					String value = line.substring(sep + 1).trim();
					properties.setProperty(property, value);
				}
			} finally {
				reader.close();
			}
		} catch (Exception e) {
			System.err.println("Could not read repository properties file: " + file.getAbsolutePath());
		}
	}
	
	public TargetTestSetup(Test test) {
		super(test);
	}
	/**
	 * Retrieves the Site object that the TargetProvider is contained in.
	 * @return Site
	 */
	Site getSite() {
		try {
			URL url = new URL(properties.getProperty("location"));
			return TargetManager.getSite(properties.getProperty("target"), url);
		} catch (MalformedURLException e) {
			return null;
		}
	}
	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Site location;
		ISiteFactory factory = TargetManager.getSiteFactory(properties.getProperty("target"));
		assertNotNull(factory);
		Site[] locations = TargetManager.getSites();

		if (locations.length == 0) {
			Site l = factory.newSite(properties);
			TargetManager.addSite(l);
		}
		location = getSite();
		TargetProvider target = location.newProvider(new Path(properties.getProperty("test_dir")));
		assertNotNull(target);
	}
}