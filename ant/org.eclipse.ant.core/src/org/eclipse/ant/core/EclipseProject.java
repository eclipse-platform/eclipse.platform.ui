package org.eclipse.ant.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.apache.tools.ant.*;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

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
	if (c != null)
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
	System.setProperty("ant.regexp.matcherimpl", "org.eclipse.ant.core.XercesRegexpMatcher");
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
    
}
