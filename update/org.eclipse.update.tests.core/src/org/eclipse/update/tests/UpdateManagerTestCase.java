package org.eclipse.update.tests;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.net.URL;
import junit.framework.*;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.update.core.UpdateManagerPlugin;
/**
 * All Help System Test cases must subclass this base Testcase.
 * See SampleTestCase.java for a template.
 */

public abstract class UpdateManagerTestCase extends TestCase {


	protected static URL SOURCE_FILE_SITE;
	protected static URL SOURCE_HTTP_SITE;	
	protected static URL TARGET_FILE_SITE;


	/**
	 * Default Constructor
	 */
	public UpdateManagerTestCase(String name) {
		super(name);
		init();
	}
	
	protected void init(){
		
		String home = System.getProperty("user.home");

		IPluginDescriptor dataDesc =  Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.update.core.tests");
		try {
			URL realURL = Platform.resolve(dataDesc.getInstallURL());
			SOURCE_FILE_SITE = new URL("file",null,realURL.getPath()+"data/");
			SOURCE_HTTP_SITE = new URL("http://9.26.150.182/UpdateManager2/");
			TARGET_FILE_SITE = new URL("file",null,home+"/target/");
		} catch (Exception e){
			fail(e.toString());
			e.printStackTrace();
		}
		
		//cleanup target
		File target= new File(home+"target");
		removeFromFileSystem(target);

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

/**
 * remove a file or directory from the file system.
 * used to clean up install
 */
protected void removeFromFileSystem(File file) {
	if (!file.exists())
		return;
	if (file.isDirectory()) {
		String[] files = file.list();
		if (files != null) // be carefule since file.list() can return null
			for (int i = 0; i < files.length; ++i)
				removeFromFileSystem(new File(file, files[i]));
	}
	if (!file.delete()) {
		System.out.println("WARNING: removeFromFileSystem(File) could not delete: " + file.getPath());
	}
}

}