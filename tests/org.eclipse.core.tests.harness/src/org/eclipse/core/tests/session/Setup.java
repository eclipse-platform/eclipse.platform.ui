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
	private static final int DEFAULT_TIMEOUT = 0;
	private String application;
	private String configuration;
	private String debugOption;
	private String devOption;
	// includes all non-VM args
	private String eclipseArgs;
	private String id;
	private String installLocation;
	private String instanceLocation;
	private String name;
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

	/**
	 * Creates a setup containing default settings. The default settings will vary 
	 * depending on the running environment.
	 * 
	 * @see #getDefaultConfiguration()
	 * @see #getDefaultDebugOption()
	 * @see #getDefaultDevOption()
	 * @see #getDefaultInstallLocation()
	 * @see #getDefaultInstanceLocation()
	 * @see #getDefaultVMLocation()
	 * @return
	 */
	public static Setup getDefaultSetup() {
		Setup defaultSetup = new Setup();
		defaultSetup.setVMLocation(Setup.getDefaultVMLocation());
		defaultSetup.setConfiguration(Setup.getDefaultConfiguration());
		defaultSetup.setDebugOption(Setup.getDefaultDebugOption());
		defaultSetup.setDevOption(Setup.getDefaultDevOption());
		defaultSetup.setInstallLocation(Setup.getDefaultInstallLocation());
		defaultSetup.setInstanceLocation(Setup.getDefaultInstanceLocation());
		defaultSetup.setTimeout(DEFAULT_TIMEOUT);
		return defaultSetup;
	}

	public static String getDefaultVMLocation() {
		String javaVM = (String) System.getProperties().get("eclipse.vm");
		if (javaVM != null)
			return javaVM;
		javaVM = (String) System.getProperties().get("java.home");
		if (javaVM == null)
			return null;
		//XXX: this is a hack and will not work with some VMs...
		return new Path(javaVM).append("bin").append("java").toOSString();
	}

	private void appendClassPath(StringBuffer params) {
		if (installLocation == null)
			return;
		params.append(" -classpath ");
		IPath classPath = new Path(installLocation).append("startup.jar");
		params.append(classPath.toOSString());
	}

	private void appendEclipseArgs(StringBuffer params) {
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

		// eclipse args
		if (eclipseArgs != null) {
			params.append(' ');
			params.append(eclipseArgs);
		}
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

	private boolean containsOption(String args, String option) {
		return args.indexOf("-" + option) >= 0;
	}

	public void copyProperty(String propertyKey) {
		systemProperties.put(propertyKey, System.getProperty(propertyKey));
	}

	public String getApplication() {
		return application;
	}

	public String getCommandLine() {
		StringBuffer command = new StringBuffer(vmLocation);
		appendClassPath(command);
		appendVMArgs(command);
		appendSystemProperties(command);
		command.append(' ');
		command.append("org.eclipse.core.launcher.Main");
		appendEclipseArgs(command);
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

	/**
	 * Returns a string containing all Eclipse args.
	 */
	public String getEclipseArgs() {
		return eclipseArgs;
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

	public Properties getSystemProperties() {
		return systemProperties;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getVMArgs() {
		return vmArgs;
	}

	public String getVMLocation() {
		return vmLocation;
	}

	public void merge(Setup variation) {
		String eclipseLocation = variation.getInstallLocation();
		if (eclipseLocation != null)
			setInstallLocation(eclipseLocation);
		String eclipseArgs = variation.getEclipseArgs();
		if (eclipseArgs != null) {
			if (getEclipseArgs() != null) {
				StringBuffer newEclipseArgs = new StringBuffer(getEclipseArgs());
				newEclipseArgs.append(' ');
				newEclipseArgs.append(eclipseArgs);
				eclipseArgs = newEclipseArgs.toString();
			}
			setEclipseArgs(eclipseArgs);
		}
		String vmLocation = variation.getVMLocation();
		if (vmLocation != null)
			setVMLocation(vmLocation);
		String vmArgs = variation.getVMArgs();
		if (vmArgs != null) {
			if (getVMArgs() != null) {
				StringBuffer newVMArgs = new StringBuffer(getVMArgs());
				newVMArgs.append(' ');				
				newVMArgs.append(vmArgs);
				vmArgs = newVMArgs.toString();
			}
			setVMArgs(vmArgs);
		}
	}

	public void setApplication(String application) {
		this.application = application;
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

	public void setEclipseArgs(String eclipseArgs) {
		this.eclipseArgs = eclipseArgs;
		if (eclipseArgs == null)
			return;
		if (containsOption(eclipseArgs, "application"))
			this.application = null;
		if (containsOption(eclipseArgs, "configuration"))
			this.configuration = null;
		if (containsOption(eclipseArgs, "debug"))
			this.debugOption = null;
		if (containsOption(eclipseArgs, "dev"))
			this.devOption = null;
		if (containsOption(eclipseArgs, "data"))
			this.instanceLocation = null;
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

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setVMArgs(String vmArgs) {
		this.vmArgs = vmArgs;
	}

	public void setVMLocation(String vmLocation) {
		this.vmLocation = vmLocation;
	}

	public String toString() {
		return "[" + getCommandLine() + "]";
	}
}