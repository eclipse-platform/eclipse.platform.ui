package org.eclipse.ant.tests.core.tests;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntCorePreferences;
import org.eclipse.ant.tests.core.AbstractAntTest;
import org.eclipse.ant.tests.core.testplugin.AntTestChecker;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public class FrameworkTests extends AbstractAntTest {
	
	public FrameworkTests(String name) {
		super(name);
	}
	
	public void testClasspathOrdering() throws MalformedURLException, CoreException {
		AntCorePreferences prefs =AntCorePlugin.getPlugin().getPreferences();
		
		String path= getProject().getFolder("lib").getFile("classpathOrdering1.jar").getLocation().toFile().getAbsolutePath();
		URL url= new URL("file:" + path);
		
		path= getProject().getFolder("lib").getFile("classpathOrdering2.jar").getLocation().toFile().getAbsolutePath();
		URL url2= new URL("file:" + path);
		
		URL urls[] = prefs.getCustomURLs();
		URL newUrls[] = new URL[urls.length + 2];
		System.arraycopy(urls, 0, newUrls, 0, urls.length);
		newUrls[urls.length] = url;
		newUrls[urls.length + 1] = url2;
		prefs.setCustomURLs(newUrls);
		
		prefs.updatePluginPreferences();
		
		run("ClasspathOrdering.xml");
		String msg= (String)AntTestChecker.getDefault().getMessages().get(1);
		assertTrue("Message incorrect: " + msg, msg.equals("classpathOrdering1"));
		assertSuccessful();
		
		restorePreferenceDefaults();
		
		urls = prefs.getCustomURLs();
		newUrls = new URL[urls.length + 2];
		System.arraycopy(urls, 0, newUrls, 0, urls.length);
		newUrls[urls.length] = url2;
		newUrls[urls.length + 1] = url;
		prefs.setCustomURLs(newUrls);
		
		prefs.updatePluginPreferences();
		
		run("ClasspathOrdering.xml");
		msg= (String)AntTestChecker.getDefault().getMessages().get(1);
		assertTrue("Message incorrect: " + msg, msg.equals("classpathOrdering2"));
		assertSuccessful();
	}
	
	public void testNoDefaultTarget() {
		try {
			run("NoDefault.xml", new String[]{"test"}, false);
		} catch (CoreException e) {
			String msg= e.getMessage();
			assertTrue("Message incorrect: " + msg, msg.equals("Default target 'build' does not exist in this project"));
			return;
		}
		assertTrue("Build files with no default targets should not be accepted", false);
	}
	
	/**
	 * Ensures that tasks like javac work when includeAntRuntime is specified
	 * bug 20857
	 */
	public void testIncludeAntRuntime() throws CoreException {
		run("javac.xml", new String[]{"build","refresh"}, false); //standard compiler
		assertSuccessful();
		String path= "org.eclipse.ant.tests.core.tasks";
		path= path.replace('.', Path.SEPARATOR);
		path= "temp.folder" + Path.SEPARATOR + "javac.bin" + Path.SEPARATOR + path + Path.SEPARATOR + "AntTestTask.class";
		IFile classFile= getProject().getFolder("temp.folder").getFolder("javac.bin").getFolder("org").getFolder("eclipse").getFolder("ant").getFolder("tests").getFolder("core").getFolder("tasks").getFile("AntTestTask.class");
		assertTrue("Class file was not generated", classFile.exists());
		run("javac.xml", new String[]{"-Duse.eclipse.compiler=true", "clean", "build", "refresh"}, false); //JDTCompiler
		assertSuccessful();
		classFile= getProject().getFolder("temp.folder").getFolder("javac.bin").getFolder("org").getFolder("eclipse").getFolder("ant").getFolder("tests").getFolder("core").getFolder("tasks").getFile("AntTestTask.class");
		assertTrue("Class file was not generated", classFile.exists());
	}
}
