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

/*
 * Implementation note: vmArguments and eclipseArguments are HashMap
 * (and not just Map) because we are interested in features that are
 * specific to HashMap (is Cloneable, allows null values).   
 */
public class Setup implements Cloneable {

	public static final String APPLICATION = "application";
	public static final String CONFIGURATION = "configuration";
	public static final String DATA = "data";
	public static final String DEBUG = "debug";
	private static final int DEFAULT_TIMEOUT = 0;
	public static final String DEV = "dev";
	public static final String INSTALL = "install";
	public static final String VM = "vm";

	private HashMap eclipseArguments = new HashMap();

	private String id;
	private String name;
	private HashMap systemProperties = new HashMap();
	private int timeout;
	private HashMap vmArguments = new HashMap();

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
		defaultSetup.setEclipseArgument(VM, Setup.getDefaultVMLocation());
		defaultSetup.setEclipseArgument(CONFIGURATION, Setup.getDefaultConfiguration());
		defaultSetup.setEclipseArgument(DEBUG, Setup.getDefaultDebugOption());
		defaultSetup.setEclipseArgument(DEV, Setup.getDefaultDevOption());
		defaultSetup.setEclipseArgument(INSTALL, Setup.getDefaultInstallLocation());
		defaultSetup.setEclipseArgument(DATA, Setup.getDefaultInstanceLocation());
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

	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		Setup clone = null;
		try {
			clone = (Setup) super.clone();
			// ensure we don't end up sharing references to mutable objects
			clone.eclipseArguments = (HashMap) eclipseArguments.clone();
			clone.vmArguments = (HashMap) vmArguments.clone();
			clone.systemProperties = (HashMap) systemProperties.clone();
		} catch (CloneNotSupportedException e) {
			// just does not happen: we do implement Cloneable
		}
		return clone;
	}

	private void fillClassPath(List params) {
		String installLocation = getEclipseArgument(INSTALL);
		if (installLocation == null)
			throw new IllegalStateException("No install location set");
		params.add("-classpath");
		IPath classPath = new Path(installLocation).append("startup.jar");
		params.add(classPath.toFile().toString());
	}

	public void fillCommandLine(List commandLine) {
		String vmLocation = getEclipseArgument(VM);
		if (vmLocation == null)
			throw new IllegalStateException("VM location not set");
		commandLine.add(vmLocation);
		fillClassPath(commandLine);
		fillVMArgs(commandLine);
		fillSystemProperties(commandLine);
		commandLine.add("org.eclipse.core.launcher.Main");
		fillEclipseArgs(commandLine);
	}

	private void fillEclipseArgs(List params) {
		for (Iterator i = eclipseArguments.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			params.add('-' + (String) entry.getKey());
			if (entry.getValue() != null && ((String) entry.getValue()).length() > 0)
				params.add(entry.getValue());
		}
	}

	private void fillSystemProperties(List command) {
		for (Iterator iter = systemProperties.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			StringBuffer property = new StringBuffer("-D");
			property.append(entry.getKey());
			if (entry.getValue() != null && ((String) entry.getValue()).length() > 0) {
				property.append('=');
				property.append(entry.getValue());
			}
			command.add(property.toString());
		}
	}

	private void fillVMArgs(List params) {
		for (Iterator i = vmArguments.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			params.add('-' + (String) entry.getKey());
			if (entry.getValue() != null && ((String) entry.getValue()).length() > 0)
				params.add(entry.getValue());
		}
	}

	public String[] getCommandLine() {
		List commandLine = new ArrayList();
		fillCommandLine(commandLine);
		return (String[]) commandLine.toArray(new String[commandLine.size()]);
	}

	public String getEclipseArgument(String key) {
		return (String) eclipseArguments.get(key);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getVMArgument(String key) {
		return (String) vmArguments.get(key);
	}

	public void merge(Setup variation) {
		eclipseArguments.putAll(variation.eclipseArguments);
		vmArguments.putAll(variation.vmArguments);
		systemProperties.putAll(variation.systemProperties);
	}

	public void setEclipseArgument(String key, String value) {
		eclipseArguments.put(key, value);
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSystemProperty(String key, String value) {
		systemProperties.put(key, value);
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setVMArgument(String key, String value) {
		vmArguments.put(key, value);
	}

	public String toString() {
		List commandLine = new ArrayList();
		fillCommandLine(commandLine);
		StringBuffer result = new StringBuffer();
		for (Iterator i = commandLine.iterator(); i.hasNext();) {
			result.append(i.next());
			result.append(System.getProperty("line.separator"));
		}
		return "[" + result.toString() + "]";
	}
}