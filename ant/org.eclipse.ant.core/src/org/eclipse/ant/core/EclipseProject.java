package org.eclipse.ant.core;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.apache.tools.ant.*;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class EclipseProject extends Project {
public EclipseProject() {
}
/**
 * Convienence method to copy a file from a source to a
 * destination specifying if token filtering must be used, if
 * source files may overwrite newer destination files and the
 * last modified time of <code>destFile</code> file should be made equal
 * to the last modified time of <code>sourceFile</code>.
 *
 * @throws IOException 
 */
public void copyFile(File sourceFile, File destFile, boolean filtering, boolean overwrite, boolean preserveLastModified) throws IOException {
	log("Copy: " + sourceFile.getAbsolutePath() + " -> " + destFile.getAbsolutePath(), MSG_VERBOSE);
	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	String sourcePath = fixSeparators(sourceFile.getPath());
	IResource source = root.findMember(new Path(fixSeparators(sourceFile.getPath())));
	IPath destPath = new Path(fixSeparators(destFile.getPath()));
	try {
		source.copy(destPath, overwrite, null);
	} catch (CoreException e) {
		throw new IOException(e.getMessage());
	}
}
/**
 * Creates a new instance of the identified datatype  
 */
public Object createDataType(String typeName) throws BuildException {
	// look in the predeclared types.  If found, do the super behavior.
	// If its not found, look in the types defined in the plugin extension point.
	Class c = (Class) getDataTypeDefinitions().get(typeName);
	if (c != null)
		return super.createDataType(typeName);
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
	return super.createDataType(typeName);
}
/**
 * Creates a new instance of the identified task.  
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
protected void fireBuildFinished(Throwable exception) {
	super.fireBuildFinished(exception);
}
protected void fireBuildStarted() {
	super.fireBuildStarted();
}
/**
 * Replaces the File.separatorChar with the Platform's path separator if required.
 */
public static String fixSeparators(String path) {
	if (File.separatorChar == '/')
		return path;
	return path.replace('\\', '/');
}
public void init() throws BuildException {
	super.init();
	// add some additional tasks and datatypes.  Normally they would be found
	// in the plugin.xml markup for the Ant Support plugin but if we are not running 
	// the platform we need to add them here.  
	addTaskDefinition("ant", EclipseAnt.class);
	addTaskDefinition("javac", EclipseJavac.class);
	addDataTypeDefinition("commapatternset", CommaPatternSet.class);
}
}
