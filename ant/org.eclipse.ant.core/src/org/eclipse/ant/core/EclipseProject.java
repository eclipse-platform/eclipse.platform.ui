package org.eclipse.ant.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.apache.tools.ant.*;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * An Ant project adapted to running inside the Eclipse Platform.  Because of the class
 * loading structure of the Eclipse platform, the standard techniques for creating instances 
 * of tasks and datatypes needs to be adapted.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */

public class EclipseProject extends Project {

	/** Indicates the default string encoding on this platform */
	private static String defaultEncoding = new java.io.InputStreamReader(new java.io.ByteArrayInputStream(new byte[0])).getEncoding();

	/** instance of this library */
	private static final String LIBRARY_NAME = "core203";
	private static boolean hasNatives = false;
	
	static {
		try {
			System.loadLibrary(LIBRARY_NAME);
			hasNatives = true;
		} catch (UnsatisfiedLinkError e) {
			logMissingNativeLibrary(e);
		}
	}

/**
 * Creates a new project for use in running Ant inside the Eclipse Platform.
 */
public EclipseProject() {
}
/**
 * Creates and returns a new instance of the identified data type.
 * 
 * @return the new data type instance
 * @param typeName the name of the type to create
 * @exception BuildException thrown if a problem occurs during data type creation
 */
public Object createDataType(String typeName) throws BuildException {
	// look in the predeclared types.  If found, do the super behavior.
	// If its not found, look in the types defined in the plugin extension point.
	Class c = (Class) getDataTypeDefinitions().get(typeName);
	if (c != null && c != EclipseProject.class)
		return internalCreateDataType(typeName);
	// check to see if the ant plugin is available.  If we are running from
	// the command line (i.e., no platform) it will not be.
	if (AntPlugin.getPlugin() == null)
		return null;
	Map types = AntPlugin.getPlugin().getTypeExtensions();
	if (types == null)
		return null;
	IConfigurationElement declaration = (IConfigurationElement) types.get(typeName);
	if (declaration == null)
		return null;
	String className = declaration.getAttribute(AntPlugin.CLASS);
	try {
		Class typeClass = declaration.getDeclaringExtension().getDeclaringPluginDescriptor().getPluginClassLoader().loadClass(className);
		addDataTypeDefinition(typeName, typeClass);
	} catch (ClassNotFoundException e) {
		return null;
	}
	return internalCreateDataType(typeName);
}
/**
 * Creates and returns a new instance of the identified task.
 * 
 * @return the new task
 * @param taskName the name of the task to create
 * @exception BuildException thrown if a problem occurs during task creation
 */
public Task createTask(String taskName) throws BuildException {
	// look in the predeclared tasks.  If found, do the super behavior.
	// If its not found, look in the tasks defined in the plugin extension point.
	Class c = (Class) getTaskDefinitions().get(taskName);
	if (c != null)
		return super.createTask(taskName);
	// check to see if the ant plugin is available.  If we are running from
	// the command line (i.e., no platform) it will not be.
	if (AntPlugin.getPlugin() == null)
		return null;
	Map tasks = AntPlugin.getPlugin().getTaskExtensions();
	if (tasks == null)
		return null;
	IConfigurationElement declaration = (IConfigurationElement) tasks.get(taskName);
	if (declaration == null)
		return null;
	String className = declaration.getAttribute(AntPlugin.CLASS);
	try {
		Class taskClass = declaration.getDeclaringExtension().getDeclaringPluginDescriptor().getPluginClassLoader().loadClass(className);
		addTaskDefinition(taskName, taskClass);
	} catch (ClassNotFoundException e) {
		return null;
	}
	return super.createTask(taskName);
}
/**
 * Sends a build finished notification to all registered listeners along with
 * the exception that caused the termination.
 * 
 * @param exception the exception to include with the notification
 */
protected void fireBuildFinished(Throwable exception) {
	super.fireBuildFinished(exception);
}
/**
 * Sends a build started notification to all registered listeners.
 */
protected void fireBuildStarted() {
	super.fireBuildStarted();
}
/**
 * Sends a target started notification to all the listeners when an execute target 
 * has just been started.
 */
protected void fireExecuteTargetStarted(Target target) {
    BuildEvent event = new BuildEvent(target);
    Vector buildListeners = getBuildListeners();
    for (int i = 0; i < buildListeners.size(); i++) {
		if (buildListeners.elementAt(i) instanceof IAntRunnerListener) {
			IAntRunnerListener listener = (IAntRunnerListener) buildListeners.elementAt(i);
			listener.executeTargetStarted(event);
		}
	}
}
/**
 * Sends a target finished notification to all the listeners when an execute target
 * has just been finished.
 */
protected void fireExecuteTargetFinished(Target target, Throwable exception) {
    BuildEvent event = new BuildEvent(target);
    event.setException(exception);
    Vector buildListeners = getBuildListeners();
    for (int i = 0; i < buildListeners.size(); i++) {
		if (buildListeners.elementAt(i) instanceof IAntRunnerListener) {
			IAntRunnerListener listener = (IAntRunnerListener) buildListeners.elementAt(i);
			listener.executeTargetFinished(event);
		}
	}
}
/**
 * Returns a string in which all <code>File.separatorChar</code>
 * characters have been replaced with the platform's path separator
 * character.
 * 
 * @return the result string
 * @param path the original string
 */
public static String fixSeparators(String path) {
	if (File.separatorChar == '/')
		return path;
	return path.replace('\\', '/');
}
/**
 * Initializes the receiver.
 * 
 * @exception BuildException thrown if a problem occurs during initialization.
 */
public void init() throws BuildException {
	super.init();
	// add some additional tasks and datatypes.  Normally they would be found
	// in the plugin.xml markup for the Ant Support plugin but if we are not running 
	// the platform we need to add them here.  
	addTaskDefinition("ant", EclipseAnt.class);
	addTaskDefinition("javac", EclipseJavac.class);
	addDataTypeDefinition("commapatternset", CommaPatternSet.class);
	addDataTypeDefinition("command", CommandDataType.class);
	System.setProperty("ant.regexp.matcherimpl", "org.eclipse.ant.core.XercesRegexpMatcher");
	// initialize the datatype table with marker values so that the table contains
	// the keys (needed while parsing) but don't load the classes to prevent plugin activation.
	if (AntPlugin.getPlugin() == null)
		return;
	Map types = AntPlugin.getPlugin().getTypeExtensions();
	if (types == null)
		return;
	for (Iterator i = types.keySet().iterator(); i.hasNext();) {
		String typeName = (String)i.next();
		if (getDataTypeDefinitions().get(typeName) == null)
			addDataTypeDefinition(typeName, EclipseProject.class);
	}
}

public Object internalCreateDataType(String typeName) throws BuildException {
    Class c = (Class) getDataTypeDefinitions().get(typeName);

    if (c == null)
        return null;

    try {
        java.lang.reflect.Constructor ctor = null;
        boolean noArg = false;
        // DataType can have a "no arg" constructor or take a single 
        // Project argument.
        try {
            ctor = c.getConstructor(new Class[0]);
            noArg = true;
        } catch (NoSuchMethodException nse) {
            ctor = c.getConstructor(new Class[] {Project.class});
            noArg = false;
        }

        Object o = null;
        if (noArg) {
             o = ctor.newInstance(new Object[0]);
        } else {
             o = ctor.newInstance(new Object[] {this});
        }
        String msg = "   +DataType: " + typeName;
        log (msg, MSG_DEBUG);
        return o;
    } catch (java.lang.reflect.InvocationTargetException ite) {
        Throwable t = ite.getTargetException();
        String msg = "Could not create datatype of type: "
             + typeName + " due to " + t;
        throw new BuildException(msg, t);
    } catch (Throwable t) {
        String msg = "Could not create datatype of type: "
             + typeName + " due to " + t;
        throw new BuildException(msg, t);
    }
}
/**
 * Executes a target. Notification has been added: the build listener knows that a top level
 * target is being executed and when it is finished.
 * 
 * @see Project#executeTarget(String targetName)
 */
public void executeTarget(String targetName) throws BuildException {
	Target targetToExecute = (Target) getTargets().get(targetName);
	if (targetToExecute == null)
		// can happen if the user has specified a target name that is not valid
		throw new BuildException(Policy.bind("exception.targetDoesNotExist", targetName));
    try {
        fireExecuteTargetStarted(targetToExecute);
        super.executeTarget(targetName);
        fireExecuteTargetFinished(targetToExecute, null);
    } catch(RuntimeException exc) {
        fireExecuteTargetFinished(targetToExecute, exc);
        throw exc;
    }
}
/**
 * @see Project#copyFile(File, File, boolean, boolean, boolean)
 */
public void copyFile(File sourceFile, File destFile, boolean filtering, boolean overwrite, boolean preserveLastModified) throws IOException {
	if (hasNatives) {
		super.copyFile(sourceFile, destFile, filtering, overwrite, false);
		internalCopyAttributes(toPlatformBytes(sourceFile.getAbsolutePath()), toPlatformBytes(destFile.getAbsolutePath()), preserveLastModified);
	} else {
		super.copyFile(sourceFile, destFile, filtering, overwrite, preserveLastModified);
	}
}
/**
 * Copies file attributes from source to destination. The copyLastModified attribute
 * indicates whether the lastModified attribute should be copied.
 */
public static boolean copyAttributes(String source, String destination, boolean copyLastModified) {
	if (hasNatives)
		return internalCopyAttributes(toPlatformBytes(source), toPlatformBytes(destination), false);
	return false; // not supported
}
private static void logMissingNativeLibrary(UnsatisfiedLinkError e) {
	String libName = System.mapLibraryName(LIBRARY_NAME);
	String message = Policy.bind("info.couldNotLoadLibrary", libName);
	IStatus status = new Status(IStatus.INFO, AntPlugin.PI_ANT, IStatus.INFO, message, e);
	AntPlugin.getPlugin().getLog().log(status);
}
/**
 * Copies file attributes from source to destination. The copyLastModified attribute
 * indicates whether the lastModified attribute should be copied.
 */
private static final native boolean internalCopyAttributes(byte[] source, byte[] destination, boolean copyLastModified);
/**
 * Calling String.getBytes() creates a new encoding object and other garbage.
 * This can be avoided by calling String.getBytes(String encoding) instead.
 */
public static byte[] toPlatformBytes(String target) {
	if (defaultEncoding == null)
		return target.getBytes();
	// try to use the default encoding
	try {
		return target.getBytes(defaultEncoding);
	} catch (UnsupportedEncodingException e) {
		// null the default encoding so we don't try it again
		defaultEncoding = null;
		return target.getBytes();
	}
}
}
