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
package org.eclipse.core.boot;

import java.io.IOException;
import java.net.URL;
import org.eclipse.core.internal.boot.OldPlatformConfiguration;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.environment.Constants;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.eclipse.update.configurator.IPlatformConfiguration;
import org.eclipse.update.configurator.IPlatformConfigurationFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Special boot loader class for the Eclipse Platform. This class cannot
 * be instantiated; all functionality is provided by static methods.
 * <p>
 * The Eclipse Platform makes heavy use of Java class loaders for
 * loading plug-ins. Even the Platform Core Runtime itself, including
 * the <code>Platform</code> class, needs to be loaded by a special 
 * class loader. The upshot is that a client program (such as a Java main
 * program, a servlet) cannot directly reference even the 
 * <code>Platform</code> class. Instead, a client must use this
 * loader class for initializing the platform, invoking functionality
 * defined in plug-ins, and shutting down the platform when done.
 * </p>
 * 
 * @see org.eclipse.core.runtime.Platform
 * TODO @deprecated 
 * In Eclipse 3.0 the runtime has been refactored such that the <code>BootLoader</code>
 * class is no longer relevant.  Most of the function found on <code>BootLoader</code> is
 * however still supported and has been relocated described in the method comments.
 * <p>
 * This interface must only be used by plug-ins 
 * which explicitly require the org.eclipse.core.runtime.compatibility plug-in.
 * </p>
 */
public final class BootLoader implements Constants {

	/**
	 * Controls the debug of platform configuration.
	 */
	public static boolean CONFIGURATION_DEBUG = false;

	/**
	 * The unique identifier constant (value "<code>org.eclipse.core.boot</code>")
	 * of the Core Boot (pseudo-) plug-in.
	 */
	public static final String PI_BOOT = "org.eclipse.core.boot"; //$NON-NLS-1$

	private static final String[] ARCH_LIST = { // 
	ARCH_PA_RISC, //
			ARCH_PPC, //
			ARCH_SPARC, //
			ARCH_X86, //
			ARCH_AMD64};
	private static final String[] OS_LIST = { //
	OS_AIX, //
			OS_HPUX, //
			OS_LINUX, //
			OS_MACOSX, //
			OS_QNX, //
			OS_SOLARIS, //
			OS_WIN32 //
	};
	private static final String[] WS_LIST = { //
	WS_CARBON, //
			WS_GTK, //
			WS_MOTIF, //
			WS_PHOTON, //
			WS_WIN32 //
	};

	/**
	 * Private constructor to block instance creation.
	 */
	private BootLoader() {
		// not allowed
	}

	/**
	 * Returns the command line args provided to the platform when it was first run.
	 * Note that individual platform runnables may be provided with different arguments
	 * if they are being run individually rather than with <code>Platform.run()</code>.
	 * 
	 * @return the command line used to start the platform
	 * TODO @deprecated Replaced by {@link Platform#getCommandLineArgs()}.
	 */
	public static String[] getCommandLineArgs() {
		return InternalPlatform.getDefault().getApplicationArgs();
	}

	/**
	 * Returns the current platform configuration.
	 * 
	 * @return platform configuration used in current instance of platform
	 * @since 2.0
	 */
	public static org.eclipse.core.boot.IPlatformConfiguration getCurrentPlatformConfiguration() {
		BundleContext context = InternalPlatform.getDefault().getBundleContext();
		// acquire factory service first
		ServiceReference configFactorySR = context.getServiceReference(IPlatformConfigurationFactory.class.getName());
		if (configFactorySR == null)
			throw new IllegalStateException();
		IPlatformConfigurationFactory configFactory = (IPlatformConfigurationFactory) context.getService(configFactorySR);
		if (configFactory == null)
			throw new IllegalStateException();
		// get the configuration using the factory
		IPlatformConfiguration currentConfig = configFactory.getCurrentPlatformConfiguration();
		context.ungetService(configFactorySR);
		return new OldPlatformConfiguration(currentConfig);
	}

	/**
	 * Returns URL at which the Platform runtime executables and libraries are installed.
	 * The returned value is distinct from the location of any given platform's data.
	 *
	 * @return the URL indicating where the platform runtime is installed.
	 * TODO @deprecated 
	 */
	public static URL getInstallURL() {
		return InternalPlatform.getDefault().getInstallURL();
	}

	/**
	 * Returns the string name of the current locale for use in finding files
	 * whose path starts with <code>$nl$</code>.
	 *
	 * @return the string name of the current locale
	 * TODO @deprecated Replaced by {@link Platform#getNL()}.
	 */
	public static String getNL() {
		return InternalPlatform.getDefault().getNL();
	}

	/**
	 * Returns the string name of the current operating system for use in finding
	 * files whose path starts with <code>$os$</code>.  <code>OS_UNKNOWN</code> is
	 * returned if the operating system cannot be determined.  
	 * The value may indicate one of the operating systems known to the platform
	 * (as specified in <code>knownOSValues</code>) or a user-defined string if
	 * the operating system name is specified on the command line.
	 *
	 * @return the string name of the current operating system
	 * @see #knownOSValues
	 * TODO @deprecated Replaced by {@link Platform#getOS()}.
	 */
	public static String getOS() {
		return InternalPlatform.getDefault().getOS();
	}

	/**
	 * Returns the string name of the current system architecture.  
	 * The value is a user-defined string if the architecture is 
	 * specified on the command line, otherwise it is the value 
	 * returned by <code>java.lang.System.getProperty("os.arch")</code>.
	 * 
	 * @return the string name of the current system architecture
	 * @since 2.0
	 * TODO @deprecated Replaced by {@link Platform#getOSArch()}.
	 */
	public static String getOSArch() {
		return InternalPlatform.getDefault().getOSArch();
	}

	/**
	 * Returns a platform configuration object, optionally initialized with previously saved
	 * configuration information.
	 * 
	 * @param url location of previously save configuration information. If <code>null</code>
	 * is specified, an empty configuration object is returned
	 * @return platform configuration used in current instance of platform
	 * @since 2.0
	 */
	public static org.eclipse.core.boot.IPlatformConfiguration getPlatformConfiguration(URL url) throws IOException {
		BundleContext context = InternalPlatform.getDefault().getBundleContext();
		// acquire factory service first
		ServiceReference configFactorySR = context.getServiceReference(IPlatformConfigurationFactory.class.getName());
		if (configFactorySR == null)
			throw new IllegalStateException();
		IPlatformConfigurationFactory configFactory = (IPlatformConfigurationFactory) context.getService(configFactorySR);
		if (configFactory == null)
			throw new IllegalStateException();
		// get the configuration using the factory
		IPlatformConfiguration config = configFactory.getPlatformConfiguration(url);
		context.ungetService(configFactorySR);
		return new OldPlatformConfiguration(config);
	}

	/**
	 * Returns the string name of the current window system for use in finding files
	 * whose path starts with <code>$ws$</code>.  <code>null</code> is returned
	 * if the window system cannot be determined.
	 *
	 * @return the string name of the current window system or <code>null</code>
	 * TODO @deprecated Replaced by {@link Platform#getWS()}.
	 */
	public static String getWS() {
		return InternalPlatform.getDefault().getWS();
	}

	/**
	 * Returns a list of known system architectures.
	 * <p>
	 * Note that this list is not authoritative; there may be legal values
	 * not included in this list. Indeed, the value returned by 
	 * <code>getOSArch</code> may not be in this list. Also, this list may 
	 * change over time as Eclipse comes to run on more operating environments.
	 * </p>
	 * 
	 * @return the list of system architectures known to the system
	 * @see #getOSArch
	 * @since 2.0
	 * XXX @deprecated Unclear whether or not this is required.  We could put it on 
	 * EnvironmentInfo
	 */
	public static String[] knownOSArchValues() {
		return ARCH_LIST;
	}

	/**
	 * Returns a list of known operating system names.
	 * <p>
	 * Note that this list is not authoritative; there may be legal values
	 * not included in this list. Indeed, the value returned by 
	 * <code>getOS</code> may not be in this list. Also, this list may 
	 * change over time as Eclipse comes to run on more operating environments.
	 * </p>
	 * 
	 * @return the list of operating systems known to the system
	 * @see #getOS
	 * @since 2.0
	 * XXX @deprecated Unclear whether or not this is required.  We could put it on 
	 * EnvironmentInfo
	 */
	public static String[] knownOSValues() {
		return OS_LIST;
	}

	/**
	 * Returns a list of known windowing system names.
	 * <p>
	 * Note that this list is not authoritative; there may be legal values
	 * not included in this list. Indeed, the value returned by 
	 * <code>getWS</code> may not be in this list. Also, this list may 
	 * change over time as Eclipse comes to run on more operating environments.
	 * </p>
	 * 
	 * @return the list of window systems known to the system
	 * @see #getWS
	 * @since 2.0
	 * XXX @deprecated Unclear whether or not this is required.  We could put it on 
	 * EnvironmentInfo
	 */
	public static String[] knownWSValues() {
		return WS_LIST;
	}

	/**
	 * Returns <code>true</code> if the platform is currently running in 
	 * debug mode.  The platform is run
	 * in debug mode using the "-debug" command line argument.
	 *
	 * @return whether or not the platform is running in debug mode
	 * TODO @deprecated Replaced by {@link EnvironmentInfo#inDebugMode()}.
	 */
	public static boolean inDebugMode() {
		// TODO: need an API to access this (at least a constant for the property name)
		return System.getProperty("osgi.debug") != null; //$NON-NLS-1$
	}

	/**
	 * Returns <code>true</code> if the platform is currently running in 
	 * development mode.  That is, if special procedures are to be 
	 * taken when defining plug-in class paths.  The platform is run
	 * in development mode using the "-dev" command line argument.
	 *
	 * @return whether or not the platform is running in development mode
	 * TODO @deprecated Replaced by {@link EnvironmentInfo#inDevMode()}.
	 */
	public static boolean inDevelopmentMode() {
		// TODO: need an API to access this (at least a constant for the property name)
		return System.getProperty("osgi.dev") != null; //$NON-NLS-1$
	}

	/**
	 * Returns whether the platform is running.
	 * The <code>startup</code> method starts the platform running;
	 * the <code>shutdown</code> method stops it.
	 *
	 * @return <code>true</code> if the platform is running, 
	 *		and <code>false</code> otherwise
	 * @see #startup
	 * @see #shutdown
	 * 
	 *  
	 */
	public static boolean isRunning() {
		return InternalPlatform.getDefault().isRunning();
	}

	/**
	 * Returns the complete plugin path defined by the file at the given location.
	 * If the given location is <code>null</code> or does not indicate a valid 
	 * plug-in path definition file, <code>null</code> is returned.
	 *
	 * @return the complete set of URLs which locate plug-ins
	 *  
	 */
	public static URL[] getPluginPath(URL pluginPathLocation) {
		return InternalPlatform.getDefault().getPluginPath(pluginPathLocation);
	}
}