package org.eclipse.core.internal.boot;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.URL;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IInstallInfo;

/**
 * @deprecated will be removed before M3
 */
public class LaunchInfo implements IInstallInfo {
	
	private static LaunchInfo current = null;
	
	/**
	 * @deprecated
	 */
	public static class Status {

		public Status(String msg) {
		}
		public Status(String msg, Throwable exc) {
		}
		public String getMessage() {
			return "";
		}
		public Throwable getException() {
			return null;
		}
	}

	/*
	 * @see IInstallInfo#getApplicationConfigurationIdentifier()
	 */
	/**
	 * @deprecated
	 */
	public String getApplicationConfigurationIdentifier() {
		return BootLoader.getCurrentPlatformConfiguration().getPrimaryFeatureIdentifier();
	}

	/*
	 * @see IInstallInfo#getComponentInstallURLFor(String)
	 */
	/**
	 * @deprecated
	 */
	public URL getComponentInstallURLFor(String componentId) {
		return null;
	}

	/*
	 * @see IInstallInfo#getConfigurationInstallURLFor(String)
	 */
	/**
	 * @deprecated
	 */
	public URL getConfigurationInstallURLFor(String configurationId) {
		return null;
	}

	/*
	 * @see IInstallInfo#getInstalledComponentIdentifiers()
	 */
	/**
	 * @deprecated
	 */
	public String[] getInstalledComponentIdentifiers() {
		return new String[0];
	}

	/*
	 * @see IInstallInfo#getInstalledConfigurationIdentifiers()
	 */
	/**
	 * @deprecated
	 */
	public String[] getInstalledConfigurationIdentifiers() {
		return new String[0];
	}
	
	/**
	 * @deprecated
	 */
	public boolean hasStatus() {
		return false;
	}
	
	/**
	 * @deprecated
	 */
	public Status[] getStatus() {
		return new Status[0];
	}
	
	/**
	 * @deprecated
	 */
	static void shutdown() {
	}
	
	/**
	 * @deprecated
	 */
	static void startup(URL base) {
		if (current == null)
			current = new LaunchInfo();
	}
	
	/**
	 * @deprecated
	 */
	public static LaunchInfo getCurrent() {
		return current;
	}
}
