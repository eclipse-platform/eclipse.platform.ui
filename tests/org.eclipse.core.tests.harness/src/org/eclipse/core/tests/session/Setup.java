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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import org.eclipse.core.internal.runtime.InternalPlatform;

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
	private String[] baseSetups;

	private HashMap eclipseArguments = new HashMap();

	private String id;
	private SetupManager manager;
	private String name;
	private String[] requiredSets;
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
		return new File(System.getProperty("java.io.tmpdir"), "workspace").toString();
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
	 * @return a setup with all default settings
	 */
	static Setup getDefaultSetup(SetupManager manager) {
		Setup defaultSetup = new Setup(manager);
		if (Setup.getDefaultVMLocation() != null)
			defaultSetup.setEclipseArgument(VM, Setup.getDefaultVMLocation());
		if (Setup.getDefaultConfiguration() != null)
			defaultSetup.setEclipseArgument(CONFIGURATION, Setup.getDefaultConfiguration());
		if (Setup.getDefaultDebugOption() != null)
			defaultSetup.setEclipseArgument(DEBUG, Setup.getDefaultDebugOption());
		if (Setup.getDefaultDevOption() != null)
			defaultSetup.setEclipseArgument(DEV, Setup.getDefaultDevOption());
		if (Setup.getDefaultInstallLocation() != null)
			defaultSetup.setEclipseArgument(INSTALL, Setup.getDefaultInstallLocation());
		if (Setup.getDefaultInstanceLocation() != null)
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
		return new File(new File(javaVM, "bin"), "java").toString();
	}

	public Setup(SetupManager manager) {
		this.manager = manager;
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
		File classPath = new File(installLocation, "startup.jar");
		params.add(classPath.toString());
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
			// null-valued properties are ignored
			if (entry.getValue() == null)
				continue;
			StringBuffer property = new StringBuffer("-D");
			property.append(entry.getKey());
			if (((String) entry.getValue()).length() > 0) {
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

	String[] getBaseSetups() {
		return baseSetups;
	}

	public String[] getCommandLine() {
		List commandLine = new ArrayList();
		fillCommandLine(commandLine);
		return (String[]) commandLine.toArray(new String[commandLine.size()]);
	}

	public String getEclipseArgsLine() {
		List eclipseArgs = new ArrayList();
		fillEclipseArgs(eclipseArgs);
		StringBuffer result = new StringBuffer();
		for (Iterator i = eclipseArgs.iterator(); i.hasNext();) {
			result.append(i.next());
			result.append(' ');
		}
		return result.length() > 0 ? result.substring(0, result.length() - 1) : null;
	}

	public String getEclipseArgument(String key) {
		return (String) eclipseArguments.get(key);
	}

	public Map getEclipseArguments() {
		return (Map) eclipseArguments.clone();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	String[] getRequiredSets() {
		return requiredSets;
	}

	public Map getSystemProperties() {
		return (Map) systemProperties.clone();
	}

	public String getSystemPropertiesLine() {
		List sysProperties = new ArrayList();
		fillSystemProperties(sysProperties);
		StringBuffer result = new StringBuffer();
		for (Iterator i = sysProperties.iterator(); i.hasNext();) {
			result.append(i.next());
			result.append(' ');
		}
		return result.length() > 0 ? result.substring(0, result.length() - 1) : null;	}

	public int getTimeout() {
		return timeout;
	}

	public String getVMArgsLine() {
		List vmArgs = new ArrayList();
		fillVMArgs(vmArgs);
		StringBuffer result = new StringBuffer();
		for (Iterator i = vmArgs.iterator(); i.hasNext();) {
			result.append(i.next());
			result.append(' ');
		}
		return result.length() > 0 ? result.substring(0, result.length() - 1) : null;
	}

	public String getVMArgument(String key) {
		return (String) vmArguments.get(key);
	}

	public Map getVMArguments() {
		return (Map) vmArguments.clone();
	}

	public boolean isA(String baseOptionSet) {
		if (baseOptionSet.equals(id))
			return true;
		if (baseSetups == null)
			return false;
		for (int i = 0; i < baseSetups.length; i++) {
			Setup base = manager.getSetup(baseSetups[i]);
			if (base != null && base.isA(baseOptionSet))
				return true;
		}
		return false;
	}

	public boolean isSatisfied(String[] availableSets) {
		for (int i = 0; i < requiredSets.length; i++) {
			boolean satisfied = false;
			for (int j = 0; !satisfied && j < availableSets.length; j++) {
				Setup available = manager.getSetup(availableSets[j]);
				if (available != null && available.isA(requiredSets[i]))
					satisfied = true;
			}
			if (!satisfied)
				return false;
		}
		return true;
	}

	public void merge(Setup variation) {
		eclipseArguments.putAll(variation.eclipseArguments);
		vmArguments.putAll(variation.vmArguments);
		systemProperties.putAll(variation.systemProperties);
	}

	void setBaseSetups(String[] baseSetups) {
		this.baseSetups = baseSetups;
	}

	public void setEclipseArgument(String key, String value) {
		eclipseArguments.put(key, value);
	}

	public void setEclipseArguments(Map newArguments) {
		if (newArguments == null)
			eclipseArguments.clear();
		else
			eclipseArguments.putAll(newArguments);
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	void setRequiredSets(String[] requiredSets) {
		this.requiredSets = requiredSets;
	}

	public void setSystemProperties(Map newProperties) {
		if (newProperties == null)
			systemProperties.clear();
		else
			systemProperties.putAll(newProperties);
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

	public void setVMArguments(Map newArguments) {
		if (newArguments == null)
			vmArguments.clear();
		else
			vmArguments.putAll(newArguments);
	}

	public String toCommandLineString() {
		String[] commandLine = getCommandLine();
		StringBuffer result = new StringBuffer();
		result.append("[\n");
		for (int i = 0; i < commandLine.length; i++) {
			result.append('\t');
			result.append(commandLine[i]);
			result.append('\n');
		}
		result.append(']');
		return result.toString();
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		if (id != null || name != null) {
			if (id != null) {
				result.append(id);
				result.append(' ');
			}
			if (name != null) {
				if (name != null) {
					result.append("(");
					result.append(name);
					result.append(") ");
				}
			}
			result.append("= ");
		}
		result.append("[");
		result.append("\n\teclipseArguments: ");
		result.append(eclipseArguments);
		result.append("\n\tvmArguments: ");
		result.append(vmArguments);
		result.append("\n\tsystemProperties: ");
		result.append(systemProperties);
		result.append("\n]");
		return result.toString();
	}
}