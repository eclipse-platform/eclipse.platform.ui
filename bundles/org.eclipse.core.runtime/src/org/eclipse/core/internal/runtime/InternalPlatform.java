/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Julian Chen - fix for bug #92572, jclRM
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> - fix for bug 265532
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import org.eclipse.core.internal.preferences.exchange.ILegacyPreferences;
import org.eclipse.core.internal.preferences.exchange.IProductPreferencesService;
import org.eclipse.core.internal.preferences.legacy.InitLegacyPreferences;
import org.eclipse.core.internal.preferences.legacy.ProductPreferencesService;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.internal.app.*;
import org.eclipse.equinox.internal.app.Activator;
import org.eclipse.equinox.log.*;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.osgi.framework.*;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Bootstrap class for the platform. It is responsible for setting up the
 * platform class loader and passing control to the actual application class
 */
public final class InternalPlatform {

	private static final String[] ARCH_LIST = {Platform.ARCH_PA_RISC, //
			Platform.ARCH_PPC, //
			Platform.ARCH_SPARC, //
			Platform.ARCH_X86, //
			Platform.ARCH_AMD64, // 
			Platform.ARCH_IA64, //
			Platform.ARCH_IA64_32};

	// debug support:  set in loadOptions()
	public static boolean DEBUG = false;
	public static boolean DEBUG_PLUGIN_PREFERENCES = false;

	static boolean splashEnded = false;
	private static boolean initialized;
	private static final String KEYRING = "-keyring"; //$NON-NLS-1$
	private static String keyringFile;

	//XXX This is not synchronized
	private static Map logs = new HashMap(5);

	private static final String[] OS_LIST = {Platform.OS_AIX, Platform.OS_HPUX, Platform.OS_LINUX, Platform.OS_MACOSX, Platform.OS_QNX, Platform.OS_SOLARIS, Platform.OS_WIN32};
	private static String password = ""; //$NON-NLS-1$
	private static final String PASSWORD = "-password"; //$NON-NLS-1$

	private static final String PLUGIN_PATH = ".plugin-path"; //$NON-NLS-1$

	public static final String PROP_APPLICATION = "eclipse.application"; //$NON-NLS-1$
	public static final String PROP_ARCH = "osgi.arch"; //$NON-NLS-1$
	public static final String PROP_CONFIG_AREA = "osgi.configuration.area"; //$NON-NLS-1$
	public static final String PROP_CONSOLE_LOG = "eclipse.consoleLog"; //$NON-NLS-1$
	public static final String PROP_DEBUG = "osgi.debug"; //$NON-NLS-1$
	public static final String PROP_DEV = "osgi.dev"; //$NON-NLS-1$

	// OSGI system properties.  Copied from EclipseStarter
	public static final String PROP_INSTALL_AREA = "osgi.install.area"; //$NON-NLS-1$
	public static final String PROP_NL = "osgi.nl"; //$NON-NLS-1$
	public static final String PROP_OS = "osgi.os"; //$NON-NLS-1$

	// Eclipse System Properties
	public static final String PROP_PRODUCT = "eclipse.product"; //$NON-NLS-1$
	public static final String PROP_WS = "osgi.ws"; //$NON-NLS-1$
	public static final String PROP_ACTIVATE_PLUGINS = "eclipse.activateRuntimePlugins"; //$NON-NLS-1$

	private static final InternalPlatform singleton = new InternalPlatform();

	private static final String[] WS_LIST = {Platform.WS_CARBON, Platform.WS_COCOA, Platform.WS_GTK, Platform.WS_MOTIF, Platform.WS_PHOTON, Platform.WS_WIN32, Platform.WS_WPF};
	private Path cachedInstanceLocation; // Cache the path of the instance location
	private ServiceTracker configurationLocation = null;
	private BundleContext context;

	private Map groupProviders = new HashMap(3);
	private ServiceTracker installLocation = null;
	private ServiceTracker instanceLocation = null;

	private Plugin runtimeInstance; // Keep track of the plugin object for runtime in case the backward compatibility is run.

	private ServiceRegistration legacyPreferencesService = null;
	private ServiceRegistration customPreferencesService = null;

	private ServiceTracker environmentTracker = null;
	private ServiceTracker logTracker = null;
	private ServiceTracker bundleTracker = null;
	private ServiceTracker debugTracker = null;
	private ServiceTracker contentTracker = null;
	private ServiceTracker preferencesTracker = null;
	private ServiceTracker userLocation = null;
	private ServiceTracker groupProviderTracker = null;
	private ServiceTracker logReaderTracker = null;
	private ServiceTracker extendedLogTracker = null;

	private IProduct product;

	public static InternalPlatform getDefault() {
		return singleton;
	}

	/**
	 * Private constructor to block instance creation.
	 */
	private InternalPlatform() {
		super();
	}

	/**
	 * @see Platform#addLogListener(ILogListener)
	 */
	public void addLogListener(ILogListener listener) {
		assertInitialized();
		RuntimeLog.addLogListener(listener);
	}

	private void assertInitialized() {
		//avoid the Policy.bind if assertion is true
		if (!initialized)
			Assert.isTrue(false, Messages.meta_appNotInit);
	}

	/**
	 * @see Platform#endSplash()
	 */
	public void endSplash() {
		synchronized (this) {
			if (splashEnded)
				return; // do not do this more than once
			splashEnded = true;
		}
		IApplicationContext applicationContext = getApplicationContext();
		if (applicationContext != null)
			applicationContext.applicationRunning();
	}

	/**
	 * @see Platform#getAdapterManager()
	 */
	public IAdapterManager getAdapterManager() {
		assertInitialized();
		return AdapterManager.getDefault();
	}

	/**
	 * XXX Use the Environment info service. Need to see how to set the value of the app args.
	 */
	public String[] getApplicationArgs() {
		return CommandLineArgs.getApplicationArgs();
	}

	public boolean getBooleanOption(String option, boolean defaultValue) {
		String value = getOption(option);
		if (value == null)
			return defaultValue;
		return value.equalsIgnoreCase("true"); //$NON-NLS-1$
	}

	public Bundle getBundle(String symbolicName) {
		PackageAdmin packageAdmin = getBundleAdmin();
		if (packageAdmin == null)
			return null;
		Bundle[] bundles = packageAdmin.getBundles(symbolicName, null);
		if (bundles == null)
			return null;
		//Return the first bundle that is not installed or uninstalled
		for (int i = 0; i < bundles.length; i++) {
			if ((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
				return bundles[i];
			}
		}
		return null;
	}

	public BundleContext getBundleContext() {
		return context;
	}

	/**
	 * Returns the bundle id of the bundle that contains the provided object, or
	 * <code>null</code> if the bundle could not be determined.
	 */
	public String getBundleId(Object object) {
		if (object == null)
			return null;
		PackageAdmin packageAdmin = getBundleAdmin();
		if (packageAdmin == null)
			return null;
		Bundle source = packageAdmin.getBundle(object.getClass());
		if (source != null && source.getSymbolicName() != null)
			return source.getSymbolicName();
		return null;
	}

	public IBundleGroupProvider[] getBundleGroupProviders() {
		Object[] objectArray = groupProviderTracker.getServices();
		if (objectArray == null) // getServices may return null; but we can not.
			return new IBundleGroupProvider[0];
		IBundleGroupProvider[] result = new IBundleGroupProvider[objectArray.length];
		System.arraycopy(objectArray, 0, result, 0, objectArray.length);
		return result;
	}

	public void registerBundleGroupProvider(IBundleGroupProvider provider) {
		// get the bundle context and register the provider as a service
		ServiceRegistration registration = getBundleContext().registerService(IBundleGroupProvider.class.getName(), provider, null);
		// store the service registration (map provider -> registration)
		synchronized (groupProviders) {
			groupProviders.put(provider, registration);
		}
	}

	public void unregisterBundleGroupProvider(IBundleGroupProvider provider) {
		// get the service reference (map provider -> reference)
		ServiceRegistration registration;
		synchronized (groupProviders) {
			registration = (ServiceRegistration) groupProviders.remove(provider);
		}
		if (registration == null)
			return;
		// unregister the provider
		registration.unregister();
	}

	public Bundle[] getBundles(String symbolicName, String version) {
		PackageAdmin packageAdmin = getBundleAdmin();
		if (packageAdmin == null)
			return null;
		Bundle[] bundles = packageAdmin.getBundles(symbolicName, version);
		if (bundles == null)
			return null;
		// optimize for common case; length==1
		if (bundles.length == 1 && (bundles[0].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0)
			return bundles;
		//Remove all the bundles that are installed or uninstalled
		Bundle[] selectedBundles = new Bundle[bundles.length];
		int added = 0;
		for (int i = 0; i < bundles.length; i++) {
			if ((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
				selectedBundles[added++] = bundles[i];
			}
		}
		if (added == 0)
			return null;

		//return an array of the correct size
		Bundle[] results = new Bundle[added];
		System.arraycopy(selectedBundles, 0, results, 0, added);
		return results;
	}

	public String[] getCommandLineArgs() {
		return CommandLineArgs.getAllArgs();
	}

	public Location getConfigurationLocation() {
		assertInitialized();
		return (Location) configurationLocation.getService();
	}

	/**
	 * Lazy initialize ContentTypeManager - it can only be used after the registry is up and running
	 */
	public IContentTypeManager getContentTypeManager() {
		return contentTracker == null ? null : (IContentTypeManager) contentTracker.getService();
	}

	public EnvironmentInfo getEnvironmentInfoService() {
		return  environmentTracker == null ? null : (EnvironmentInfo) environmentTracker.getService();
	}

	public Bundle[] getFragments(Bundle bundle) {
		PackageAdmin packageAdmin = getBundleAdmin();
		if (packageAdmin == null)
			return null;
		return packageAdmin.getFragments(bundle);
	}

	public FrameworkLog getFrameworkLog() {
		return logTracker == null ? null : (FrameworkLog) logTracker.getService();
	}

	public Bundle[] getHosts(Bundle bundle) {
		PackageAdmin packageAdmin = getBundleAdmin();
		if (packageAdmin == null)
			return null;
		return packageAdmin.getHosts(bundle);
	}

	public Location getInstallLocation() {
		assertInitialized();
		return (Location) installLocation.getService();
	}

	public URL getInstallURL() {
		Location location = getInstallLocation();
		// it is pretty much impossible for the install location to be null.  If it is, the
		// system is in a bad way so throw and exception and get the heck outta here.
		if (location == null)
			throw new IllegalStateException("The installation location must not be null"); //$NON-NLS-1$
		return location.getURL();
	}

	public Location getInstanceLocation() {
		assertInitialized();
		return (Location) instanceLocation.getService();
	}

	/**
	 * @see Platform#getLocation()
	 */
	public IPath getLocation() throws IllegalStateException {
		if (cachedInstanceLocation == null) {
			Location location = getInstanceLocation();
			if (location == null)
				return null;
			//	This makes the assumption that the instance location is a file: URL
			File file = new File(location.getURL().getFile());
			cachedInstanceLocation = new Path(file.toString());
		}
		return cachedInstanceLocation;
	}

	/**
	 * Returns a log for the given plugin. Creates a new one if needed.
	 * XXX change this into a LogMgr service that would keep track of the map. See if it can be a service factory.
	 * It would contain all the logging methods that are here.
	 * Relate to RuntimeLog if appropriate.
	 * The system log listener needs to be optional: turned on or off. What about a system property? :-)
	 */
	public ILog getLog(Bundle bundle) {
		Log result = (Log) logs.get(bundle);
		if (result != null)
			return result;
		ExtendedLogService logService = (ExtendedLogService) extendedLogTracker.getService();
		Logger logger = logService == null ? null : logService.getLogger(bundle, PlatformLogWriter.EQUINOX_LOGGER_NAME);
		result = new Log(bundle, logger);
		ExtendedLogReaderService logReader = (ExtendedLogReaderService) logReaderTracker.getService();
		logReader.addLogListener(result, result);
		logs.put(bundle, result);
		return result;
	}

	/**
	 * Returns the object which defines the location and organization
	 * of the platform's meta area.
	 */
	public DataArea getMetaArea() {
		// TODO: deprecate?
		return MetaDataKeeper.getMetaArea();
	}

	public String getNL() {
		return getBundleContext().getProperty(PROP_NL);
	}

	/**
	 * Unicode locale extensions are defined using command line parameter -nlExtensions,
	 * or the system property "osgi.nl.extensions".
	 */
	public String getNLExtensions() {
		String nlExtensions = PlatformActivator.getContext().getProperty("osgi.nl.extensions"); //$NON-NLS-1$
		if (nlExtensions == null)
			return ""; //$NON-NLS-1$
		if (!nlExtensions.startsWith("@")) //$NON-NLS-1$
			nlExtensions = '@' + nlExtensions;
		return nlExtensions;
	}

	/**
	 * @see Platform
	 */
	public String getOption(String option) {
		DebugOptions options = getDebugOptions();
		if (options != null)
			return options.getOption(option);
		return null;
	}

	public String getOS() {
		return getBundleContext().getProperty(PROP_OS);
	}

	public String getOSArch() {
		return getBundleContext().getProperty(PROP_ARCH);
	}

	public PlatformAdmin getPlatformAdmin() {
		if (context == null)
			return null;
		ServiceReference platformAdminReference = context.getServiceReference(PlatformAdmin.class.getName());
		if (platformAdminReference == null)
			return null;
		return (PlatformAdmin) context.getService(platformAdminReference);
	}

	//TODO I guess it is now time to get rid of that
	/*
	 * This method is retained for R1.0 compatibility because it is defined as API.
	 * Its function matches the API description (returns <code>null</code> when
	 * argument URL is <code>null</code> or cannot be read).
	 */
	public URL[] getPluginPath(URL pluginPathLocation /*R1.0 compatibility*/
	) {
		InputStream input = null;
		// first try and see if the given plugin path location exists.
		if (pluginPathLocation == null)
			return null;
		try {
			input = pluginPathLocation.openStream();
		} catch (IOException e) {
			//fall through
		}

		// if the given path was null or did not exist, look for a plugin path
		// definition in the install location.
		if (input == null)
			try {
				URL url = new URL("platform:/base/" + PLUGIN_PATH); //$NON-NLS-1$
				input = url.openStream();
			} catch (MalformedURLException e) {
				//fall through
			} catch (IOException e) {
				//fall through
			}

		// nothing was found at the supplied location or in the install location
		if (input == null)
			return null;
		// if we found a plugin path definition somewhere so read it and close the location.
		URL[] result = null;
		try {
			try {
				result = readPluginPath(input);
			} finally {
				input.close();
			}
		} catch (IOException e) {
			//let it return null on failure to read
		}
		return result;
	}

	/**
	 * 
	 */
	public IPreferencesService getPreferencesService() {
		return preferencesTracker == null ? null : (IPreferencesService) preferencesTracker.getService();
	}

	/*
	 * XXX move this into the app model.
	 */
	public IProduct getProduct() {
		if (product != null)
			return product;
		EclipseAppContainer container = Activator.getContainer();
		IBranding branding = container == null ? null : container.getBranding();
		if (branding == null)
			return null;
		Object brandingProduct = branding.getProduct();
		if (!(brandingProduct instanceof IProduct))
			brandingProduct = new Product(branding);
		product = (IProduct) brandingProduct;
		return product;
	}

	public IExtensionRegistry getRegistry() {
		return RegistryFactory.getRegistry();
	}

	/**
	 * XXX deprecate and use NLS or BundleFinder.find()
	 */
	public ResourceBundle getResourceBundle(Bundle bundle) {
		return ResourceTranslator.getResourceBundle(bundle);
	}

	/**
	 * XXX deprecate and use NLS or BundleFinder.find()
	 */
	public String getResourceString(Bundle bundle, String value) {
		return ResourceTranslator.getResourceString(bundle, value);
	}

	/**
	 * XXX deprecate and use NLS or BundleFinder.find()
	 */
	public String getResourceString(Bundle bundle, String value, ResourceBundle resourceBundle) {
		return ResourceTranslator.getResourceString(bundle, value, resourceBundle);
	}

	/**
	 * This method is only used to register runtime once compatibility has been started.
	 */
	public Plugin getRuntimeInstance() {
		return runtimeInstance;
	}

	private IApplicationContext getApplicationContext() {
		ServiceReference[] ref;
		try {
			ref = context.getServiceReferences(IApplicationContext.class.getName(), "(eclipse.application.type=main.thread)"); //$NON-NLS-1$
		} catch (InvalidSyntaxException e) {
			return null;
		}
		if (ref == null || ref.length == 0)
			return null;
		// assumes the application context is available as a service
		IApplicationContext result = (IApplicationContext) context.getService(ref[0]);
		if (result != null) {
			context.ungetService(ref[0]);
			return result;
		}
		return null;
	}

	/**
	 * XXX Investigate the usage of a service factory
	 */
	public IPath getStateLocation(Bundle bundle) {
		return getStateLocation(bundle, true);
	}

	public IPath getStateLocation(Bundle bundle, boolean create) throws IllegalStateException {
		assertInitialized();
		IPath result = getMetaArea().getStateLocation(bundle);
		if (create)
			result.toFile().mkdirs();
		return result;
	}

	public long getStateTimeStamp() {
		PlatformAdmin admin = getPlatformAdmin();
		return admin == null ? -1 : admin.getState(false).getTimeStamp();
	}

	public Location getUserLocation() {
		assertInitialized();
		return (Location) userLocation.getService();
	}

	public String getWS() {
		return getBundleContext().getProperty(PROP_WS);
	}

	private void initializeAuthorizationHandler() {
		try {
			AuthorizationHandler.setKeyringFile(keyringFile);
			AuthorizationHandler.setPassword(password);
		} catch (NoClassDefFoundError e) {
			// The authorization fragment is not available. If someone tries to use that API, an error will be logged
		}
	}

	/*
	 * Finds and loads the options file 
	 */
	void initializeDebugFlags() {
		// load runtime options
		DEBUG = getBooleanOption(Platform.PI_RUNTIME + "/debug", false); //$NON-NLS-1$
		if (DEBUG) {
			DEBUG_PLUGIN_PREFERENCES = getBooleanOption(Platform.PI_RUNTIME + "/preferences/plugin", false); //$NON-NLS-1$
		}
	}

	public boolean isFragment(Bundle bundle) {
		PackageAdmin packageAdmin = getBundleAdmin();
		if (packageAdmin == null)
			return false;
		return (packageAdmin.getBundleType(bundle) & PackageAdmin.BUNDLE_TYPE_FRAGMENT) > 0;
	}

	/*
	 *XXX do what you want to do. track osgi, track runtime, or whatever.
	 */
	public boolean isRunning() {
		try {
			return initialized && context != null && context.getBundle().getState() == Bundle.ACTIVE;
		} catch (IllegalStateException e) {
			return false;
		}
	}

	/**
	 * Returns a list of known system architectures.
	 * 
	 * @return the list of system architectures known to the system
	 * XXX This is useless
	 */
	public String[] knownOSArchValues() {
		return ARCH_LIST;
	}

	/**
	 * Returns a list of known operating system names.
	 * 
	 * @return the list of operating systems known to the system
	 * XXX This is useless
	 */
	public String[] knownOSValues() {
		return OS_LIST;
	}

	/**
	 * Returns a list of known windowing system names.
	 * 
	 * @return the list of window systems known to the system
	 * XXX This is useless
	 */
	public String[] knownWSValues() {
		return WS_LIST;
	}

	/**
	 * Notifies all listeners of the platform log.  This includes the console log, if 
	 * used, and the platform log file.  All Plugin log messages get funnelled
	 * through here as well.
	 */
	public void log(final IStatus status) {
		// TODO: deprecate?
		RuntimeLog.log(status);
	}

	private void processCommandLine(String[] args) {
		if (args == null || args.length == 0)
			return;

		for (int i = 0; i < args.length; i++) {
			// check for args with parameters
			if (i == args.length - 1 || args[i + 1].startsWith("-")) //$NON-NLS-1$
				continue;
			String arg = args[++i];

			// look for the keyring file
			if (args[i - 1].equalsIgnoreCase(KEYRING))
				keyringFile = arg;
			// look for the user password.  
			if (args[i - 1].equalsIgnoreCase(PASSWORD))
				password = arg;
		}
	}

	private URL[] readPluginPath(InputStream input) {
		Properties ini = new Properties();
		try {
			ini.load(input);
		} catch (IOException e) {
			return null;
		}
		Vector result = new Vector(5);
		for (Enumeration groups = ini.propertyNames(); groups.hasMoreElements();) {
			String group = (String) groups.nextElement();
			for (StringTokenizer entries = new StringTokenizer(ini.getProperty(group), ";"); entries.hasMoreElements();) { //$NON-NLS-1$
				String entry = (String) entries.nextElement();
				if (!entry.equals("")) //$NON-NLS-1$
					try {
						result.addElement(new URL(entry));
					} catch (MalformedURLException e) {
						//intentionally ignore bad URLs
						System.err.println("Ignoring plugin: " + entry); //$NON-NLS-1$
					}
			}
		}
		return (URL[]) result.toArray(new URL[result.size()]);
	}

	/**
	 * @see Platform#removeLogListener(ILogListener)
	 */
	public void removeLogListener(ILogListener listener) {
		assertInitialized();
		RuntimeLog.removeLogListener(listener);
	}

	/**
	 * This method is only used to register runtime once compatibility has been started.
	 */
	public void setRuntimeInstance(Plugin runtime) {
		runtimeInstance = runtime;
	}

	/**
	 * Internal method for starting up the platform.  The platform is not started with any location
	 * and should not try to access the instance data area.
	 * 
	 * Note: the content type manager must be initialized only after the registry has been created
	 */
	public void start(BundleContext runtimeContext) {
		this.context = runtimeContext;
		openOSGiTrackers();
		splashEnded = false;
		processCommandLine(getEnvironmentInfoService().getNonFrameworkArgs());
		initializeDebugFlags();
		initialized = true;
		getMetaArea();
		initializeAuthorizationHandler();
		startServices();

		// See if need to activate rest of the runtime plugins. Plugins are "gently" activated by touching 
		// a class from the corresponding plugin(s). 
		boolean shouldActivate = !"false".equalsIgnoreCase(context.getProperty(PROP_ACTIVATE_PLUGINS)); //$NON-NLS-1$
		if (shouldActivate) {
			// activate Preferences plugin by creating a class from it:
			new org.eclipse.core.runtime.preferences.DefaultScope();
			// activate Jobs plugin by creating a class from it:
			org.eclipse.core.runtime.jobs.Job.getJobManager();
		}
	}

	/**
	 * Shutdown runtime pieces in this order:
	 * Content[auto shutdown] -> Preferences[auto shutdown] -> Registry -> Jobs
	 * The "auto" shutdown takes place before this code is executed
	 */
	public void stop(BundleContext bundleContext) {
		assertInitialized();
		stopServices(); // should be done after preferences shutdown
		initialized = false;
		closeOSGITrackers();
		context = null;
	}

	private void openOSGiTrackers() {
		Filter filter = null;
		try {
			filter = context.createFilter(Location.INSTANCE_FILTER);
		} catch (InvalidSyntaxException e) {
			// ignore this.  It should never happen as we have tested the above format.
		}
		instanceLocation = new ServiceTracker(context, filter, null);
		instanceLocation.open();
		
		try {
			filter = context.createFilter(Location.USER_FILTER);
		} catch (InvalidSyntaxException e) {
			// ignore this.  It should never happen as we have tested the above format.
		}
		userLocation = new ServiceTracker(context, filter, null);
		userLocation.open();
		
		try {
			filter = context.createFilter(Location.CONFIGURATION_FILTER);
		} catch (InvalidSyntaxException e) {
			// ignore this.  It should never happen as we have tested the above format.
		}
		configurationLocation = new ServiceTracker(context, filter, null);
		configurationLocation.open();
		
		try {
			filter = context.createFilter(Location.INSTALL_FILTER);
		} catch (InvalidSyntaxException e) {
			// ignore this.  It should never happen as we have tested the above format.
		}
		installLocation = new ServiceTracker(context, filter, null);
		installLocation.open();
		
		if (context != null) {
			logTracker = new ServiceTracker(context, FrameworkLog.class.getName(), null);
			logTracker.open();
		}
		
		if (context != null) {
			bundleTracker = new ServiceTracker(context, PackageAdmin.class.getName(), null);
			bundleTracker.open();
		}
		
		if (context != null) {
			contentTracker = new ServiceTracker(context, IContentTypeManager.class.getName(), null);
			contentTracker.open();
		}
		
		if (context != null) {
			preferencesTracker = new ServiceTracker(context, IPreferencesService.class.getName(), null);
			preferencesTracker.open();
		}
		
		try {
			filter = context.createFilter("(objectClass=" + IBundleGroupProvider.class.getName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (InvalidSyntaxException e) {
			// ignore this, it should never happen
		}
		groupProviderTracker = new ServiceTracker(context, filter, null);
		groupProviderTracker.open();
		
		logReaderTracker = new ServiceTracker(context, ExtendedLogReaderService.class.getName(), null);
		logReaderTracker.open();
		
		extendedLogTracker = new ServiceTracker(context, ExtendedLogService.class.getName(), null);
		extendedLogTracker.open();
		
		environmentTracker = new ServiceTracker(context, EnvironmentInfo.class.getName(), null);
		environmentTracker.open();

		debugTracker = new ServiceTracker(context, DebugOptions.class.getName(), null);
		debugTracker.open();
	}

	private void startServices() {
		// The check for getProduct() is relatively expensive (about 3% of the headless startup),
		// so we don't want to enforce it here. 
		customPreferencesService = context.registerService(IProductPreferencesService.class.getName(), new ProductPreferencesService(), new Hashtable());

		// Only register this interface if compatibility is installed - the check for a bundle presence
		// is a quick test that doesn't consume much.
		if (getBundle(CompatibilityHelper.PI_RUNTIME_COMPATIBILITY) != null)
			legacyPreferencesService = context.registerService(ILegacyPreferences.class.getName(), new InitLegacyPreferences(), new Hashtable());
	}

	private void stopServices() {
		if (legacyPreferencesService != null) {
			legacyPreferencesService.unregister();
			legacyPreferencesService = null;
		}
		if (customPreferencesService != null) {
			customPreferencesService.unregister();
			customPreferencesService = null;
		}
	}

	private PackageAdmin getBundleAdmin() {
		return bundleTracker == null ? null : (PackageAdmin) bundleTracker.getService();
	}

	private DebugOptions getDebugOptions() {
		return debugTracker == null ? null : (DebugOptions) debugTracker.getService();
	}

	private void closeOSGITrackers() {
		if (preferencesTracker != null) {
			preferencesTracker.close();
			preferencesTracker = null;
		}
		if (contentTracker != null) {
			contentTracker.close();
			contentTracker = null;
		}
		if (debugTracker != null) {
			debugTracker.close();
			debugTracker = null;
		}
		if (bundleTracker != null) {
			bundleTracker.close();
			bundleTracker = null;
		}
		if (logTracker != null) {
			logTracker.close();
			logTracker = null;
		}
		if (groupProviderTracker != null) {
			groupProviderTracker.close();
			groupProviderTracker = null;
		}
		if (environmentTracker != null) {
			environmentTracker.close();
			environmentTracker = null;
		}
		if (logReaderTracker != null) {
			logReaderTracker.close();
			logReaderTracker = null;
		}
		if (extendedLogTracker != null) {
			extendedLogTracker.close();
			extendedLogTracker = null;
		}
		if (installLocation != null) {
			installLocation.close();
			installLocation = null;
		}
		if (userLocation != null) {
			userLocation.close();
			userLocation = null;
		}
		if (configurationLocation != null) {
			configurationLocation.close();
			configurationLocation = null;
		}
		if (instanceLocation != null) {
			instanceLocation.close();
			instanceLocation = null;
		}
	}

	/**
	 * Print a debug message to the console. 
	 * Pre-pend the message with the current date and the name of the current thread.
	 */
	public static void message(String message) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(new Date(System.currentTimeMillis()));
		buffer.append(" - ["); //$NON-NLS-1$
		buffer.append(Thread.currentThread().getName());
		buffer.append("] "); //$NON-NLS-1$
		buffer.append(message);
		System.out.println(buffer.toString());
	}

	public static void start(Bundle bundle) throws BundleException {
		int originalState = bundle.getState();
		if ((originalState & Bundle.ACTIVE) != 0)
			return; // bundle is already active
		try {
			// attempt to activate the bundle
			bundle.start(Bundle.START_TRANSIENT);
		} catch (BundleException e) {
			if ((originalState & Bundle.STARTING) != 0 && (bundle.getState() & Bundle.STARTING) != 0)
				// This can happen if the bundle was in the process of being activated on this thread, just return
				return;
			throw e;
		}
	}
}
