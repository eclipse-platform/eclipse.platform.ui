/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.ui.internal.model;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.ant.core.TargetInfo;
import org.eclipse.ant.ui.internal.launchConfigurations.IAntLaunchConfigurationConstants;
import org.eclipse.ant.ui.internal.views.AntView;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.variables.LaunchVariableUtil;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * General utility class dealing with Ant build files
 */
public final class AntUtil {
	public static final String RUN_TARGETS_ATTRIBUTE = IAntUIConstants.TOOL_TYPE_ANT_BUILD + ".runTargets"; //$NON-NLS-1$;
	public static final String ATTRIBUTE_SEPARATOR = ","; //$NON-NLS-1$;
	public static final char ANT_CLASSPATH_DELIMITER= '*';
	
	/**
	 * No instances allowed
	 */
	private AntUtil() {
		super();
	}
	
	/**
	 * Returns a single-string of the strings for storage.
	 * 
	 * @param targets the array of strings
	 * @return a single-string representation of the strings or
	 * <code>null</code> if the array is empty.
	 */
	public static String combineStrings(String[] strings) {
		if (strings.length == 0)
			return null;

		if (strings.length == 1)
			return strings[0];

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < strings.length - 1; i++) {
			buf.append(strings[i]);
			buf.append(ATTRIBUTE_SEPARATOR);
		}
		buf.append(strings[strings.length - 1]);
		return buf.toString();
	}

	/**
	 * Returns an array of targets to be run, or <code>null</code> if none are
	 * specified (indicating the default target should be run).
	 *
	 * @param configuration launch configuration
	 * @return array of target names, or <code>null</code>
	 * @throws CoreException if unable to access the associated attribute
	 */
	public static String[] getTargetsFromConfig(ILaunchConfiguration configuration) throws CoreException {
		String attribute = configuration.getAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_TARGETS, (String) null);
		if (attribute == null) {
			return null;
		} else {
			return AntUtil.parseRunTargets(attribute);
		}
	}
	
	/**
	 * Returns a map of properties to be defined for the build, or
	 * <code>null</code> if none are specified (indicating no additional
	 * properties specified for the build).
	 *
	 * @param configuration launch configuration
	 * @return map of properties (name --> value), or <code>null</code>
	 * @throws CoreException if unable to access the associated attribute
	 */
	public static Map getProperties(ILaunchConfiguration configuration) throws CoreException {
		Map map = configuration.getAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_PROPERTIES, (Map) null);
		return map;
	}
	
	/**
	 * Returns a String specifying the Ant home to use for the build, or
	 * <code>null</code> if none is specified.
	 *
	 * @param configuration launch configuration
	 * @return String specifying Ant home to use, or <code>null</code>
	 * @throws CoreException if unable to access the associated attribute
	 */
	public static String getAntHome(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_HOME, (String) null);
	}

	/**
	 * Returns an array of property files to be used for the build, or
	 * <code>null</code> if none are specified (indicating no additional
	 * property files specified for the build).
	 *
	 * @param configuration launch configuration
	 * @return array of property file names, or <code>null</code>
	 * @throws CoreException if unable to access the associated attribute
	 */
	public static String[] getPropertyFiles(ILaunchConfiguration configuration) throws CoreException {
		String attribute = configuration.getAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_PROPERTY_FILES, (String) null);
		if (attribute == null) {
			return null;
		} else {
			String[] propertyFiles= AntUtil.parseString(attribute, ","); //$NON-NLS-1$
			for (int i = 0; i < propertyFiles.length; i++) {
				String propertyFile = propertyFiles[i];
				propertyFile= expandVariableString(propertyFile, "Could not resolve property file entry", "Invalid property file entry: {0}");
				propertyFiles[i]= propertyFile;
			}
			return propertyFiles;
		}
	}
	
	/**
	 * Returns the list of all targets for the Ant build file specified by
	 * the provided IPath, or <code>null</code> if no targets found.
	 * 
	 * @param path the location of the Ant build file to get the targets from
	 * @return a list of <code>TargetInfo</code>
	 * 
	 * @throws CoreException if file does not exist, IO problems, or invalid format.
	 */
	public static TargetInfo[] getTargets(String path) throws CoreException {
		AntRunner runner = new AntRunner();
		runner.setBuildFileLocation(path);
	 	return runner.getAvailableTargets();
	}
	
	/**
	 * Returns the list of all targets for the Ant build file specified by
	 * the provided IPath, arguments and ILaunchConfiguration, or <code>null</code> if no targets found.
	 * 
	 * @param path the location of the Ant build file to get the targets from
	 * @param arguments command line arguments for the Ant build
	 * @param config the launch configuration for the Ant build
	 * @return a list of <code>TargetInfo</code>
	 * 
	 * @throws CoreException if file does not exist, IO problems, or invalid format.
	 */
	public static TargetInfo[] getTargets(String path, String[] arguments, ILaunchConfiguration config) throws CoreException {
		Map properties=getProperties(config);
		String[] propertyFiles= getPropertyFiles(config);
		AntRunner runner = new AntRunner();
		runner.setBuildFileLocation(path);
		if (properties != null){
			runner.addUserProperties(properties);
		}
		if (propertyFiles != null && propertyFiles.length > 0) {
			runner.setPropertyFiles(propertyFiles);
		}
		if (arguments != null && arguments.length > 0) {
			runner.setArguments(arguments);
		}
		runner.setCustomClasspath(getCustomClasspath(config));
		
		return runner.getAvailableTargets();
	}
	
	/**
	 * Returns the list of urls that define the custom classpath for the Ant
	 * build, or <code>null</code> if the global classpath is to be used.
	 *
	 * @param configuration launch configuration
	 * @return a list of <code>URL</code>
	 *
	 * @throws CoreException if file does not exist, IO problems, or invalid format.
	 */
	public static URL[] getCustomClasspath(ILaunchConfiguration config) throws CoreException {
		String classpathString= config.getAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_CUSTOM_CLASSPATH, (String)null);
		if (classpathString == null) {
			return null;
		}
		List antURLs= new ArrayList();
		List userURLs= new ArrayList();
		getCustomClasspaths(config, antURLs, userURLs, true);
		URL[] custom= new URL[antURLs.size() + userURLs.size()];
		antURLs.addAll(userURLs);
		return (URL[])antURLs.toArray(custom);
	}
	
	/**
	 * Adds the Ant URLs and user URLS to the provided lists.
	 * If no custom classpath is set, no URLs are added to the lists.
	 *
	 * @param configuration launch configuration
	 * @param list to add the Ant URLs to
     * @param list to add the user URLs to
	 *
	 */
	public static void getCustomClasspaths(ILaunchConfiguration config, List antURLs, List userURLs) throws CoreException {
		getCustomClasspaths(config, antURLs, userURLs, false);
	}
	
	/**
	 * Adds the Ant URLs and user URLS to the provided lists.
	 * If no custom classpath is set, no URLs are added to the lists.
	 * Launch variables are expanded based on the value of the expandVariables parameter
	 *
	 * @param configuration launch configuration
	 * @param list to add the Ant URLs to
	 * @param list to add the user URLs to
	 * @param expandVariables indicates whether to expand launch variables contained in the classpath entries
	 *
	 */
	public static void getCustomClasspaths(ILaunchConfiguration config, List antURLs, List userURLs, boolean expandVariables) throws CoreException {
		String classpathString= null;
		try {
			classpathString = config.getAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_CUSTOM_CLASSPATH, (String) null);
		} catch (CoreException e) {
		}
		if (classpathString == null) {
			return;
		}
		String antString= null;
		String userString= null;
		int delim= classpathString.indexOf(ANT_CLASSPATH_DELIMITER);

		if (delim == -1) {
			antString= classpathString;
		} else {
			antString= classpathString.substring(0, delim);
			userString= classpathString.substring(delim+1);
		}

		getURLs(antURLs, antString, expandVariables);
		
		if (userString != null) {
			getURLs(userURLs, userString, expandVariables);
		}
	}
	
	private static void getURLs(List URLs, String urlString, boolean expandVariables) throws CoreException {
		String[] URLStrings= AntUtil.parseString(urlString, AntUtil.ATTRIBUTE_SEPARATOR);
		for (int i = 0; i < URLStrings.length; i++) {
			String string = URLStrings[i];
			if (expandVariables) {
				string= expandVariableString(string, "Could not resolve classpath entry", "Invalid classpath entry: {0}");
			}
			try {
				URLs.add(new URL("file:" + string)); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				if (!expandVariables) {
					URLs.add(string);
				}
			}
		}
	}

	private static String expandVariableString(String variableString, String statusMessage, String invalidMessage) throws CoreException {
		MultiStatus status = new MultiStatus(IAntUIConstants.PLUGIN_ID, 0, statusMessage, null);
		String expandedString = LaunchVariableUtil.expandVariables(variableString, status, null);
		if (status.isOK()) {
			if (expandedString == null || expandedString.length() == 0) {
				String msg = MessageFormat.format(invalidMessage, new String[] { variableString});
				throw new CoreException(new Status(IStatus.ERROR, IAntUIConstants.PLUGIN_ID, 0, msg, null));
			} else {
				variableString= expandedString;
			}
		} else {
			throw new CoreException(status);
		}
		return variableString;
	}

	/**
	 * Returns the currently displayed Ant View if it is open.
	 * 
	 * @return the Ant View open in the current workbench page or
	 * <code>null</code> if there is none.
	 */
	public static AntView getAntView() {
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page= window.getActivePage(); 
			if (page != null) {
				return (AntView) page.findView(IAntUIConstants.ANT_VIEW_ID);
			}
		}
		return null;
	}
	
	/**
	 * Returns whether the target described by the given
	 * <code>TargetInfo</code> is a sub-target.
	 * 
	 * @param info the info of the target in question
	 * @return <code>true</code> if the target is a sub-target,
	 * 		<code>false</code> otherwise
	 */
	public static boolean isSubTarget(TargetInfo info) {
		return info.getDescription() == null;
	}
	
	/**
	 * Returns the list of target names to run
	 * 
	 * @param extraAttibuteValue the external tool's extra attribute value
	 * 		for the run targets key.
	 * @return a list of target names
	 */
	public static String[] parseRunTargets(String extraAttibuteValue) {
		return parseString(extraAttibuteValue, ATTRIBUTE_SEPARATOR);
	}
	
	/**
	 * Returns the list of Strings that were delimiter separated.
	 * 
	 * @param delimString the String to be tokenized based on the delimiter
	 * @return a list of Strings
	 */
	public static String[] parseString(String delimString, String delim) {
		if (delimString == null) {
			return new String[0];
		}
		
		// Need to handle case where separator character is
		// actually part of the target name!
		StringTokenizer tokenizer = new StringTokenizer(delimString, delim);
		String[] results = new String[tokenizer.countTokens()];
		for (int i = 0; i < results.length; i++) {
			results[i] = tokenizer.nextToken();
		}
		
		return results;
	}
	
	/**
	 * Returns an IFile with the given fully qualified path. The returned IFile
	 * may or may not exist.
	 */
	public static IFile getFile(String fullPath) {
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		return root.getFile(new Path(fullPath));
	}

	public static FileLink getTaskLink(String path, File buildFileParent) {
		path = path.trim();
		if (path.length() == 0) {
			return null;
		}
		if (path.startsWith("file:")) { //$NON-NLS-1$
			// remove "file:"
			path= path.substring(5, path.length());
		}
		// format is file:F:L: where F is file path, and L is line number
		int index = path.lastIndexOf(':');
		if (index == path.length() - 1) {
			// remove trailing ':'
			path = path.substring(0, index);
			index = path.lastIndexOf(':');
		}
		// split file and line number
		String fileName = path.substring(0, index);
		IFile file = getFileForLocation(fileName, buildFileParent);
		if (file != null) {
			try {
				String lineNumber = path.substring(index + 1);
				int line = Integer.parseInt(lineNumber);
				return new FileLink(file, null, -1, -1, line);
			} catch (NumberFormatException e) {
			}
		}
		return null;
	}

	/**
	 * Returns the workspace file associated with the given path in the
	 * local file system, or <code>null</code> if none.
	 * If the path happens to be a relative path, then the path is interpreted as
	 * relative to the specified parent file.
	 *   
	 * @param path
	 * @param buildFileParent
	 * @return file or <code>null</code>
	 */
	public static IFile getFileForLocation(String path, File buildFileParent) {
		IPath filePath= new Path(path);
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filePath);
		if (file == null) {
			//relative path
			File relativeFile= null;
			try {
				//this call is ok if fBuildFileParent is null
				relativeFile= FileUtils.newFileUtils().resolveFile(buildFileParent, path);
				filePath= new Path(relativeFile.getAbsolutePath());
				file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filePath);
				if (file == null) {
					return null;
				}
			} catch (BuildException be) {
				return null;
			}
		}
		
		if (file.exists()) {
			return file;
		}
		return null;
	}
}
