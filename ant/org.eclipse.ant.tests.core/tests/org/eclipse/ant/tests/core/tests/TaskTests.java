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
import org.eclipse.ant.core.Task;
import org.eclipse.ant.tests.core.AbstractAntTest;

public class TaskTests extends AbstractAntTest {

	public TaskTests(String name) {
		super(name);
	}
	
	public void testAddTask() throws MalformedURLException {
		AntCorePreferences prefs =AntCorePlugin.getPlugin().getPreferences();
		Task newTask= new Task();
		String path= getProject().getFolder("lib").getFile("antTestsSupport.jar").getLocation().toFile().getAbsolutePath();
		URL url= new URL("file:" + path);
		newTask.setLibrary(url);
		newTask.setTaskName("AntTestTask");
		newTask.setClassName("org.eclipse.ant.tests.core.tasks.AntTestTask");
		prefs.setCustomTasks(new Task[]{newTask});
	}
	
	public void testRemoveTask() {
		AntCorePreferences prefs =AntCorePlugin.getPlugin().getPreferences();
		prefs.setCustomTasks(new Task[0]);
	}
}
