/**********************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ant.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

import org.eclipse.ant.internal.core.*;
import org.eclipse.ant.internal.core.AntClassLoader;
import org.eclipse.ant.internal.core.AntCorePreferences;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.runtime.*;
/**
 * Entry point for running Ant scripts inside Eclipse.
 */
public class AntRunner implements IPlatformRunnable, IAntCoreConstants {

	protected String buildFileLocation = DEFAULT_BUILD_FILENAME;
	protected List buildListeners;
	protected Vector targets;
	protected Map userProperties;
	protected int messageOutputLevel = 2; // Project.MSG_INFO
	protected String buildLoggerClassName;
	protected String arguments;

public AntRunner() {
	buildListeners = new ArrayList(5);
}

protected ClassLoader getClassLoader() {	
	AntCorePreferences preferences = AntCorePlugin.getPlugin().getPreferences();
	URL[] urls = preferences.getURLs();
	ClassLoader[] pluginLoaders = preferences.getPluginClassLoaders();
	return new AntClassLoader(urls, pluginLoaders, null);
}

/**
 * Sets the buildFileLocation.
 * 
 * @param buildFileLocation the file system location of the build file
 */
public void setBuildFileLocation(String buildFileLocation) {
	if (buildFileLocation == null)
		this.buildFileLocation = DEFAULT_BUILD_FILENAME;
	else
		this.buildFileLocation = buildFileLocation;
}

/**
 * 
 * 
 * @param 
 */
public void setMessageOutputLevel(int level) {
	this.messageOutputLevel = level;
}

/**
 * 
 */
public void setArguments(String arguments) {
	this.arguments = arguments;
}

/**
 * Sets the executionTargets in the order they need to run.
 * 
 */
public void setExecutionTargets(String[] executionTargets) {
	targets = new Vector(10);
	for (int i = 0; i < executionTargets.length; i++)
		targets.add(executionTargets[i]);
}

/**
 * Adds a build listener.
 * 
 * @param buildListener a build listener
 */
public void addBuildListener(String className) {
	if (className == null)
		return;
	buildListeners.add(className);
}

/**
 * Adds a build logger.
 * 
 * @param className a BuildLogger class name
 */
public void addBuildLogger(String className) {
	this.buildLoggerClassName = className;
}

/**
 * Adds user properties.
 */
public void addUserProperties(Map properties) {
	this.userProperties = properties;
}

/**
 * Runs the build script.
 */
public void run() throws CoreException {
	try {
		ClassLoader loader = getClassLoader();
		Class classInternalAntRunner = loader.loadClass("org.eclipse.ant.internal.core.ant.InternalAntRunner");
		Object runner = classInternalAntRunner.newInstance();
		// set build file
		Method setBuildFileLocation = classInternalAntRunner.getMethod("setBuildFileLocation", new Class[] {String.class});
		setBuildFileLocation.invoke(runner, new Object[] {buildFileLocation});
		// add listeners
		Method addBuildListeners = classInternalAntRunner.getMethod("addBuildListeners", new Class[] {List.class});
		addBuildListeners.invoke(runner, new Object[] {buildListeners});
		// add build logger
		if (buildLoggerClassName != null) {
			Method addBuildLogger = classInternalAntRunner.getMethod("addBuildLogger", new Class[] {String.class});
			addBuildLogger.invoke(runner, new Object[] {buildLoggerClassName});
		}
		// add properties
		Method addUserProperties = classInternalAntRunner.getMethod("addUserProperties", new Class[] {Map.class});
		addUserProperties.invoke(runner, new Object[] {userProperties});
		// set message output level
		Method setMessageOutputLevel = classInternalAntRunner.getMethod("setMessageOutputLevel", new Class[] {int.class});
		setMessageOutputLevel.invoke(runner, new Object[] {new Integer(messageOutputLevel)});
		// set execution targets
		if (targets != null) {
			Method setExecutionTargets = classInternalAntRunner.getMethod("setExecutionTargets", new Class[] {Vector.class});
			setExecutionTargets.invoke(runner, new Object[] {targets});
		}
		// set extra arguments
		if (arguments != null) {
			Method setArguments = classInternalAntRunner.getMethod("setArguments", new Class[] {String.class});
			setArguments.invoke(runner, new Object[] {arguments});
		}
		// run
		Method run = classInternalAntRunner.getMethod("run", null);
		run.invoke(runner, null);
	} catch (InvocationTargetException e) {
		Throwable realException = e.getTargetException();
		throw new CoreException(new Status(IStatus.ERROR, PI_ANTCORE, ERROR_RUNNING_SCRIPT, Policy.bind("error.buildFailed"), realException));
	} catch (Exception e) {
		throw new CoreException(new Status(IStatus.ERROR, PI_ANTCORE, ERROR_RUNNING_SCRIPT, Policy.bind("error.buildFailed"), e));
	}
}

/**
 * Invokes the building of a project object and executes a build using either a given
 * target or the default target.
 *
 * @param argArray the command line arguments
 * @exception execution exceptions
 */
public Object run(Object argArray) throws Exception {
	// Add debug information if necessary - fix for bug 5672.
	// Since the platform parses the -debug command line arg
	// and removes it from the args passed to the applications,
	// we have to check if Eclipse is in debug mode in order to
	// forward the -debug argument to Ant.
	if (BootLoader.inDebugMode()) {
		String[] args = (String[]) argArray;
		String[] newArgs = new String[args.length + 1];
		for (int i = 0; i < args.length; i++)
			newArgs[i] = args[i];
		newArgs[args.length] = "-debug";
		argArray = newArgs;
	}
	ClassLoader loader = getClassLoader();
	Class classInternalAntRunner = loader.loadClass("org.eclipse.ant.internal.core.ant.InternalAntRunner");
	Object runner = classInternalAntRunner.newInstance();
	Method run = classInternalAntRunner.getMethod("run", new Class[] {Object.class});
	run.invoke(runner, new Object[] {argArray});
	return null;
}
}