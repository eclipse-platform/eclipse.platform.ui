/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.session;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class Setup implements Cloneable {
	private String allArgs;
	private String application;
	private String applicationArgs;
	private String configuration;
	private String debugOption;
	private String devOption;
	private String id;
	private String installLocation;
	private String instanceLocation;
	private String name;
	private int numberOfRuns;
	private Properties systemProperties = new Properties();
	private int timeout;
	private String vmArgs;
	// includes the program file name
	private String vmLocation;

	public static String getDefaultConfiguration() {
		return System.getProperty("configuration", System.getProperty(InternalPlatform.PROP_CONFIG_AREA));
	}

	public static String getDefaultDebugOption() {
		return System.getProperty("debug", System.getProperty(InternalPlatform.PROP_DEBUG));
	}

	public static String getDefaultDevOption() {
		return System.getProperty("dev", System.getProperty(InternalPlatform.PROP_DEV));
	}

	public static String getDefaultInstallLocation() {
		String userSet = System.getProperty("install");
		if (userSet != null)
			return userSet;
		String currentInstall = System.getProperty(InternalPlatform.PROP_INSTALL_AREA);
		if (currentInstall != null)
			try {
				return new URI(currentInstall).getPath();
			} catch (URISyntaxException e) {
				// nothing to be done
			}
		return null;
	}

	public static String getDefaultInstanceLocation() {
		return new Path(System.getProperty("java.io.tmpdir")).append("workspace").toOSString();
	}

	public static Setup getDefaultSetup() {
		Setup defaultSetup = new Setup();
		defaultSetup.setVMLocation(Setup.getDefaultVMLocation());
		defaultSetup.setConfiguration(Setup.getDefaultConfiguration());
		defaultSetup.setDebugOption(Setup.getDefaultDebugOption());
		defaultSetup.setDevOption(Setup.getDefaultDevOption());
		defaultSetup.setInstallLocation(Setup.getDefaultInstallLocation());
		defaultSetup.setInstanceLocation(Setup.getDefaultInstanceLocation());
		return defaultSetup;
	}

	public static String getDefaultVMLocation() {
		String javaVM = (String) System.getProperties().get("eclipse.vm");
		if (javaVM != null)
			return javaVM;
		javaVM = (String) System.getProperties().get("java.home");
		if (javaVM == null)
			return null;
		//TODO: this is a hack and will not work with some VMs...
		return new Path(javaVM).append("bin").append("java").toOSString();
	}

	private void appendApplicationArgs(StringBuffer params) {
		if (application != null) {
			params.append(" -application ");
			params.append(application);
		}

		if (configuration != null) {
			params.append(" -configuration \"");
			params.append(configuration);
			params.append('"');
		}

		if (devOption != null) {
			params.append(" -dev \"");
			params.append(devOption);
			params.append('"');
		}

		if (debugOption != null) {
			params.append(" -debug \"");
			params.append(debugOption);
			params.append('"');
		}

		if (instanceLocation != null) {
			params.append(" -data \"");
			params.append(instanceLocation);
			params.append('"');
		}

		// always enable -consolelog TODO should make this configurable 
		//params.append(" -consolelog");

		// application args
		if (applicationArgs != null) {
			params.append(' ');
			params.append(applicationArgs);
		}
	}

	private void appendClassPath(StringBuffer params) {
		params.append(" -classpath ");
		IPath classPath = new Path(installLocation).append("startup.jar");
		params.append(classPath.toOSString());
	}

	private void appendSystemProperties(StringBuffer command) {
		for (Iterator iter = systemProperties.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			command.append(" -D");
			command.append(entry.getKey());
			command.append('=');
			command.append(entry.getValue());
		}

	}

	private void appendVMArgs(StringBuffer params) {
		// additional VM args
		if (vmArgs != null) {
			params.append(' ');
			params.append(vmArgs);
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		Setup clone = null;
		try {
			clone = (Setup) super.clone();
			clone.systemProperties = (Properties) systemProperties.clone();
		} catch (CloneNotSupportedException e) {
			// just does not happen
		}
		return clone;
	}

	public void copyProperty(String propertyKey) {
		systemProperties.put(propertyKey, System.getProperty(propertyKey));
	}

	public String getAllArgs() {
		return allArgs == null ? "" : allArgs;
	}

	public String getApplication() {
		return application;
	}

	public String getApplicationArgs() {
		return applicationArgs == null ? "" : applicationArgs;
	}

	public String getCommandLine() {
		StringBuffer command = new StringBuffer(vmLocation);
		appendClassPath(command);
		appendVMArgs(command);
		appendSystemProperties(command);
		command.append(' ');
		command.append("org.eclipse.core.launcher.Main");
		appendApplicationArgs(command);
//		System.out.println("Command line: ");
//		System.out.print('\t');
//		System.out.println(command);
		return command.toString();
	}

	public String getConfiguration() {
		return configuration;
	}

	public String getDebugOption() {
		return debugOption;
	}

	public String getDevOption() {
		return devOption;
	}

	public String getId() {
		return id;
	}

	public String getInstallLocation() {
		return installLocation;
	}

	public String getInstanceLocation() {
		return instanceLocation;
	}

	public String getName() {
		return name;
	}

	public int getNumberOfRuns() {
		return numberOfRuns;
	}

	public Properties getSystemProperties() {
		return systemProperties;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getVMArgs() {
		return vmArgs == null ? "" : vmArgs;
	}

	public String getVMLocation() {
		return vmLocation;
	}

	public void setAllArgs(String allArgs) {
		this.allArgs = allArgs;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public void setApplicationArgs(String applicationArgs) {
		this.applicationArgs = applicationArgs;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public void setDebugOption(String debugOption) {
		this.debugOption = debugOption;
	}

	public void setDevOption(String devOption) {
		this.devOption = devOption;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setInstallLocation(String newLocation) {
		this.installLocation = newLocation;
	}

	public void setInstanceLocation(String instanceLocation) {
		this.instanceLocation = instanceLocation;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNumberOfRuns(int repeat) {
		this.numberOfRuns = repeat;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setVMArgs(String vmArgs) {
		this.vmArgs = vmArgs;
	}

	public void setVMLocation(String vmLocation) {
		this.vmLocation = vmLocation;
	}
}