package org.eclipse.update.tests;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import junit.framework.TestCase;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.update.internal.core.UpdateManagerUtils;
/**
 * All Help System Test cases must subclass this base Testcase.
 * See SampleTestCase.java for a template.
 */

public abstract class UpdateManagerTestCase extends TestCase {

	protected ResourceBundle bundle;

	protected static URL SOURCE_FILE_SITE;
	protected static URL SOURCE_HTTP_SITE;
	protected static URL TARGET_FILE_SITE;
	
	private static final String DATA_PATH = "data/";

	/**
	 * Default Constructor
	 */
	public UpdateManagerTestCase(String name)  {
		super(name);
		try {
			init();
		} catch (Exception e){
			fail(e.toString());
			e.printStackTrace();
		}
	}

	protected void init() throws MissingResourceException, IOException, MalformedURLException {

		IPluginDescriptor dataDesc =
			Platform.getPluginRegistry().getPluginDescriptor(
				"org.eclipse.update.core.tests");
		URL resolvedURL = Platform.resolve(dataDesc.getInstallURL());
		String path = UpdateManagerUtils.getPath(resolvedURL);			
		URL dataURL = new URL(resolvedURL.getProtocol(), resolvedURL.getHost(),path+DATA_PATH);
		String homePath = System.getProperty("user.home");


		if (bundle == null) {
				ClassLoader l =
					new URLClassLoader(new URL[] {dataURL}, null);
				bundle = ResourceBundle.getBundle("resources", Locale.getDefault(), l);
		}

		try {
			path = UpdateManagerUtils.getPath(dataURL);						
			SOURCE_FILE_SITE = new URL("file", null, path);
			SOURCE_HTTP_SITE = new URL("http",bundle.getString("HTTP_HOST_1"),bundle.getString("HTTP_PATH_1"));
			TARGET_FILE_SITE = new URL("file", null, homePath + "/target/");
		} catch (Exception e) {
			fail(e.toString());
			e.printStackTrace();
		}

		//cleanup target 
		File target = new File(homePath + "/target/");
		UpdateManagerUtils.removeFromFileSystem(target);

	}

	/**
	 * Simple implementation of setUp. Subclasses are prevented 
	 * from overriding this method to maintain logging consistency.
	 * umSetUp() should be overriden instead.
	 */
	protected final void setUp() throws Exception {
		System.out.println("----- " + this.getName());
		System.out.println(this.getName() + ": setUp...");
		umSetUp();
	}

	/**
	 * Sets up the fixture, for example, open a network connection.
	 * This method is called before a test is executed.
	 */
	protected void umSetUp() throws Exception {
		// do nothing.
	}

	/**
	 * Simple implementation of tearDown.  Subclasses are prevented 
	 * from overriding this method to maintain logging consistency.
	 * umTearDown() should be overriden instead.
	 */
	protected final void tearDown() throws Exception {
		System.out.println(this.getName() + ": tearDown...\n");
		umTearDown();
	}

	/**
	 * Tears down the fixture, for example, close a network connection.
	 * This method is called after a test is executed.
	 */
	protected void umTearDown() throws Exception {
		// do nothing.
	}

}