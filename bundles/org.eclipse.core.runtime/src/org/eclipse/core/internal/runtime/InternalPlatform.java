/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Julian Chen - fix for bug #92572, jclRM
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.io.*;
import java.net.*;
import java.util.*;
import org.eclipse.core.internal.boot.*;
import org.eclipse.core.internal.preferences.*;
import org.eclipse.core.internal.registry.eclipse.EclipseExtensionRegistry;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IProductPreferencesService;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.urlconversion.URLConverter;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.*;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Bootstrap class for the platform. It is responsible for setting up the
 * platform class loader and passing control to the actual application class
 */
public final class InternalPlatform {

	// Command line args as seen by the Eclipse runtime. allArgs does NOT
	// include args consumed by the underlying framework (e.g., OSGi)
	private static String[] allArgs = new String[0];
	private static String[] appArgs = new String[0];
	private static final String APPLICATION = "-application"; //$NON-NLS-1$	

	private static final String[] ARCH_LIST = {Platform.ARCH_PA_RISC, //
		Platform.ARCH_PPC, //
		Platform.ARCH_SPARC, //
		Platform.ARCH_X86, //
		Platform.ARCH_AMD64, // 
		Platform.ARCH_IA64, //
		Platform.ARCH_IA64_32};
	private static final String BOOT = "-boot"; //$NON-NLS-1$
	private static final String CLASSLOADER_PROPERTIES = "-classloaderProperties"; //$NON-NLS-1$	

	// debug support:  set in loadOptions()
	public static boolean DEBUG = false;
	public static boolean DEBUG_PLUGIN_PREFERENCES = false;

	private static Runnable splashHandler = null;
	private static final String FEATURE = "-feature"; //$NON-NLS-1$
	private static final String FIRST_USE = "-firstUse"; //$NON-NLS-1$
	private static String[] frameworkArgs = new String[0];

	private static boolean initialized;
	private static final String KEY_DOUBLE_PREFIX = "%%"; //$NON-NLS-1$

	private static final String KEY_PREFIX = "%"; //$NON-NLS-1$
	private static final String KEYRING = "-keyring"; //$NON-NLS-1$
	private static String keyringFile;

	private static Map logs = new HashMap(5);
	private static final String NEW_UPDATES = "-newUpdates"; //$NON-NLS-1$

	// obsolete command line args
	private static final String NO_PACKAGE_PREFIXES = "-noPackagePrefixes"; //$NON-NLS-1$
	private static final String NO_UPDATE = "-noUpdate"; //$NON-NLS-1$

	private static final String[] OS_LIST = {Platform.OS_AIX, Platform.OS_HPUX, Platform.OS_LINUX, Platform.OS_MACOSX, Platform.OS_QNX, Platform.OS_SOLARIS, Platform.OS_WIN32};
	private static String password = ""; //$NON-NLS-1$
	private static final String PASSWORD = "-password"; //$NON-NLS-1$
	private static PlatformLogWriter platformLog = null;

	private static final String PLUGIN_PATH = ".plugin-path"; //$NON-NLS-1$
	private static final String PLUGINS = "-plugins"; //$NON-NLS-1$

	// command line options
	private static final String PRODUCT = "-product"; //$NON-NLS-1$	
	public static final String PROP_ADAPTOR = "osgi.adaptor"; //$NON-NLS-1$
	public static final String PROP_APPLICATION = "eclipse.application"; //$NON-NLS-1$
	public static final String PROP_ARCH = "osgi.arch"; //$NON-NLS-1$
	public static final String PROP_CONFIG_AREA = "osgi.configuration.area"; //$NON-NLS-1$
	public static final String PROP_CONSOLE = "osgi.console"; //$NON-NLS-1$
	public static final String PROP_CONSOLE_CLASS = "osgi.consoleClass"; //$NON-NLS-1$
	public static final String PROP_CONSOLE_LOG = "eclipse.consoleLog"; //$NON-NLS-1$
	public static final String PROP_DEBUG = "osgi.debug"; //$NON-NLS-1$
	public static final String PROP_DEV = "osgi.dev"; //$NON-NLS-1$
	public static final String PROP_EXITCODE = "eclipse.exitcode"; //$NON-NLS-1$

	// OSGI system properties.  Copied from EclipseStarter
	public static final String PROP_INSTALL_AREA = "osgi.install.area"; //$NON-NLS-1$
	public static final String PROP_INSTANCE_AREA = "osgi.instance.area"; //$NON-NLS-1$
	public static final String PROP_MANIFEST_CACHE = "osgi.manifest.cache"; //$NON-NLS-1$
	public static final String PROP_NL = "osgi.nl"; //$NON-NLS-1$
	public static final String PROP_OS = "osgi.os"; //$NON-NLS-1$

	// Eclipse System Properties
	public static final String PROP_PRODUCT = "eclipse.product"; //$NON-NLS-1$
	public static final String PROP_SYSPATH = "osgi.syspath"; //$NON-NLS-1$
	public static final String PROP_USER_AREA = "osgi.user.area"; //$NON-NLS-1$
	public static final String PROP_WS = "osgi.ws"; //$NON-NLS-1$
	private static final InternalPlatform singleton = new InternalPlatform();

	private static final String UPDATE = "-update"; //$NON-NLS-1$
	private static final String[] WS_LIST = {Platform.WS_CARBON, Platform.WS_GTK, Platform.WS_MOTIF, Platform.WS_PHOTON, Platform.WS_WIN32};
	private Path cachedInstanceLocation; // Cache the path of the instance location
	private ServiceTracker configurationLocation = null;
	private BundleContext context;

	private ArrayList groupProviders = new ArrayList(3);
	private ServiceTracker installLocation = null;
	private ServiceTracker instanceLocation = null;
	private boolean missingProductReported = false;
	private IProduct product;
	private IExtensionRegistry registry;
	private AdapterManagerListener adapterManagerListener = null;

	private Plugin runtimeInstance; // Keep track of the plugin object for runtime in case the backward compatibility is run.

	private ServiceRegistration legacyPreferencesService = null;
	private ServiceRegistration customPreferencesService = null;

	private static final String JOB_PLUGIN = "org.eclipse.core.jobs"; //$NON-NLS-1$

	private ServiceTracker environmentTracker = null;
	private ServiceTracker urlTracker = null;
	private ServiceTracker logTracker = null;
	private ServiceTracker bundleTracker = null;
	private ServiceTracker debugTracker = null;
	private ServiceTracker contentTracker = null;
	private ServiceTracker preferencesTracker = null;
	private ServiceTracker productTracker = null;
	private ServiceTracker userLocation = null;

	public static InternalPlatform getDefault() {
		return singleton;
	}

	/**
	 * Private constructor to block instance creation.
	 */
	private InternalPlatform() {
		super();
	}

	public void addAuthorizationInfo(URL serverUrl, String realm, String authScheme, Map info) throws CoreException {
		AuthorizationHandler.addAuthorizationInfo(serverUrl, realm, authScheme, info);
	}

	/**
	 * @see Platform#addLogListener(ILogListener)
	 */
	public void addLogListener(ILogListener listener) {
		assertInitialized();
		RuntimeLog.addLogListener(listener);
	}

	public void addProtectionSpace(URL resourceUrl, String realm) throws CoreException {
		AuthorizationHandler.addProtectionSpace(resourceUrl, realm);
	}

	private URL asActualURL(URL url) throws IOException {
		if (!url.getProtocol().equals(PlatformURLHandler.PROTOCOL))
			return url;
		URLConnection connection = url.openConnection();
		if (connection instanceof PlatformURLConnection)
			return ((PlatformURLConnection) connection).getResolvedURL();
		return url;
	}

	/**
	 * @see Platform
	 */
	public URL asLocalURL(URL url) throws IOException {
		URL result = url;
		// If this is a platform URL get the local URL from the PlatformURLConnection
		if (result.getProtocol().equals(PlatformURLHandler.PROTOCOL))
			result = asActualURL(url);

		// If the result is a bundleentry or bundleresouce URL then 
		// convert it to a file URL.  This will end up extracting the 
		// bundle entry to cache if the bundle is packaged as a jar.
		if (result.getProtocol().startsWith(PlatformURLHandler.BUNDLE)) {
			URLConverter theConverter = getURLConverter();
			if (theConverter == null)
				throw new IOException("url.noaccess"); //$NON-NLS-1$
			result = theConverter.convertToFileURL(result);
		}

		return result;
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
		final Runnable handler = splashHandler;
		if (handler == null)
			return;
		//clear reference to handler to avoid calling it again and to avoid object leak
		splashHandler = null;
		SafeRunner.run(new ISafeRunnable() {
			public void handleException(Throwable e) {
				// just continue ... the exception has already been logged by
				// handleException(ISafeRunnable)
			}

			public void run() throws Exception {
				handler.run();
			}
		});
	}

	public void flushAuthorizationInfo(URL serverUrl, String realm, String authScheme) throws CoreException {
		AuthorizationHandler.flushAuthorizationInfo(serverUrl, realm, authScheme);
	}

	/**
	 * @see Platform#getAdapterManager()
	 */
	public IAdapterManager getAdapterManager() {
		assertInitialized();
		return AdapterManager.getDefault();
	}

	public String[] getApplicationArgs() {
		return appArgs;
	}

	public Map getAuthorizationInfo(URL serverUrl, String realm, String authScheme) {
		return AuthorizationHandler.getAuthorizationInfo(serverUrl, realm, authScheme);
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

	public IBundleGroupProvider[] getBundleGroupProviders() {
		return (IBundleGroupProvider[]) groupProviders.toArray(new IBundleGroupProvider[groupProviders.size()]);
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
		//Remove all the bundes that are installed or uninstalled
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
		return allArgs;
	}

	public Location getConfigurationLocation() {
		assertInitialized();
		return (Location) configurationLocation.getService();
	}

	/**
	 * Lazy initialise ContentTypeManager - it can only be used after the registry is up and running
	 */
	public IContentTypeManager getContentTypeManager() {
		if (contentTracker == null) {
			contentTracker = new ServiceTracker(context, IContentTypeManager.class.getName(), null);
			contentTracker.open();
		}
		return (IContentTypeManager) contentTracker.getService();
	}

	public EnvironmentInfo getEnvironmentInfoService() {
		if (environmentTracker == null) {
			environmentTracker = new ServiceTracker(context, EnvironmentInfo.class.getName(), null);
			environmentTracker.open();
		}
		return (EnvironmentInfo) environmentTracker.getService();
	}

	public Bundle[] getFragments(Bundle bundle) {
		PackageAdmin packageAdmin = getBundleAdmin();
		if (packageAdmin == null)
			return null;
		return packageAdmin.getFragments(bundle);
	}

	public FrameworkLog getFrameworkLog() {
		if (logTracker == null) {
			logTracker = new ServiceTracker(context, FrameworkLog.class.getName(), null);
			logTracker.open();
		}
		return (FrameworkLog) logTracker.getService();
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

	public int getIntegerOption(String option, int defaultValue) {
		String value = getOption(option);
		if (value == null)
			return defaultValue;
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
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
	 */
	public ILog getLog(Bundle bundle) {
		ILog result = (ILog) logs.get(bundle);
		if (result != null)
			return result;
		result = new Log(bundle);
		logs.put(bundle, result);
		return result;
	}

	public IPath getLogFileLocation() {
		return getMetaArea().getLogLocation();
	}

	/**
	 * Returns the object which defines the location and organization
	 * of the platform's meta area.
	 */
	public DataArea getMetaArea() {
		// TODO: derecate?
		return MetaDataKeeper.getMetaArea();
	}

	public String getNL() {
		return System.getProperty(PROP_NL);
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
		return System.getProperty(PROP_OS);
	}

	public String getOSArch() {
		return System.getProperty(PROP_ARCH);
	}

	public PlatformAdmin getPlatformAdmin() {
		ServiceReference platformAdminReference = context.getServiceReference(PlatformAdmin.class.getName());
		if (platformAdminReference == null)
			return null;
		return (PlatformAdmin) context.getService(platformAdminReference);
	}

	//TODO I guess it is now time to get rid of that
	/*
	 * This method is retained for R1.0 compatibility because it is defined as API.
	 * It's function matches the API description (returns <code>null</code> when
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
				URL url = new URL(PlatformURLBaseConnection.PLATFORM_URL_STRING + PLUGIN_PATH);
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
		if (preferencesTracker == null) {
			preferencesTracker = new ServiceTracker(context, IPreferencesService.class.getName(), null);
			preferencesTracker.open();
		}
		return (IPreferencesService) preferencesTracker.getService();
	}

	/**
	 * Look for the companion preference translation file for a group
	 * of preferences.  This method will attempt to find a companion 
	 * ".properties" file first.  This companion file can be in an
	 * nl-specific directory for this plugin or any of its fragments or 
	 * it can be in the root of this plugin or the root of any of the
	 * plugin's fragments. This properties file can be used to translate
	 * preference values.
	 * 
	 * TODO fix these comments
	 * @param uniqueIdentifier the descriptor of the plugin
	 *   who has the preferences
	 * @param basePrefFileName the base name of the preference file
	 *   This base will be used to construct the name of the 
	 *   companion translation file.
	 *   Example: If basePrefFileName is "plugin_customization",
	 *   the preferences are in "plugin_customization.ini" and
	 *   the translations are found in
	 *   "plugin_customization.properties".
	 * @return the properties file
	 * 
	 * @since 2.0
	 */
	public Properties getPreferenceTranslator(String uniqueIdentifier, String basePrefFileName) {
		return new Properties();
	}

	public IProduct getProduct() {
		if (product != null)
			return product;
		String productId = System.getProperty(InternalPlatform.PROP_PRODUCT);
		if (productId == null)
			return null;
		IConfigurationElement[] entries = InternalPlatform.getDefault().getRegistry().getConfigurationElementsFor(Platform.PI_RUNTIME, Platform.PT_PRODUCT, productId);
		if (entries.length > 0) {
			// There should only be one product with the given id so just take the first element
			product = new Product(productId, entries[0]);
			return product;
		}
		IConfigurationElement[] elements = InternalPlatform.getDefault().getRegistry().getConfigurationElementsFor(Platform.PI_RUNTIME, Platform.PT_PRODUCT);
		List logEntries = null;
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equalsIgnoreCase("provider")) { //$NON-NLS-1$
				try {
					IProductProvider provider = (IProductProvider) element.createExecutableExtension("run"); //$NON-NLS-1$
					IProduct[] products = provider.getProducts();
					for (int j = 0; j < products.length; j++) {
						IProduct provided = products[j];
						if (provided.getId().equalsIgnoreCase(productId)) {
							product = provided;
							return product;
						}
					}
				} catch (CoreException e) {
					if (logEntries == null)
						logEntries = new ArrayList(3);
					logEntries.add(new FrameworkLogEntry(Platform.PI_RUNTIME, NLS.bind(Messages.provider_invalid, element.getParent().toString()), 0, e, null));
				}
			}
		}
		if (logEntries != null)
			InternalPlatform.getDefault().getFrameworkLog().log(new FrameworkLogEntry(Platform.PI_RUNTIME, Messages.provider_invalid_general, 0, null, (FrameworkLogEntry[]) logEntries.toArray()));

		if (!missingProductReported) {
			InternalPlatform.getDefault().getFrameworkLog().log(new FrameworkLogEntry(Platform.PI_RUNTIME, NLS.bind(Messages.product_notFound, productId), 0, null, null));
			missingProductReported = true;
		}
		return null;
	}

	public String getProtectionSpace(URL resourceUrl) {
		return AuthorizationHandler.getProtectionSpace(resourceUrl);
	}

	public IExtensionRegistry getRegistry() {
		return registry;
	}

	public ResourceBundle getResourceBundle(Bundle bundle) {
		return ResourceTranslator.getResourceBundle(bundle);
	}

	public String getResourceString(Bundle bundle, String value) {
		return ResourceTranslator.getResourceString(bundle, value);
	}

	public String getResourceString(Bundle bundle, String value, ResourceBundle resourceBundle) {
		return ResourceTranslator.getResourceString(bundle, value, resourceBundle);
	}

	public Plugin getRuntimeInstance() {
		return runtimeInstance;
	}

	private Runnable getSplashHandler() {
		ServiceReference[] ref;
		try {
			ref = context.getServiceReferences(Runnable.class.getName(), null);
		} catch (InvalidSyntaxException e) {
			return null;
		}
		// assumes the endInitializationHandler is available as a service
		// see EclipseStarter.publishSplashScreen
		for (int i = 0; i < ref.length; i++) {
			String name = (String) ref[i].getProperty("name"); //$NON-NLS-1$
			if (name != null && name.equals("splashscreen")) { //$NON-NLS-1$
				Runnable result = (Runnable) context.getService(ref[i]);
				context.ungetService(ref[i]);
				return result;
			}
		}
		return null;
	}

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

	public URLConverter getURLConverter() {
		if (urlTracker == null) {
			urlTracker = new ServiceTracker(context, URLConverter.class.getName(), null);
			urlTracker.open();
		}
		return (URLConverter) urlTracker.getService();
	}

	public Location getUserLocation() {
		assertInitialized();
		return (Location) userLocation.getService();
	}

	public String getWS() {
		return System.getProperty(PROP_WS);
	}

	/**
	 * @return whether platform log writer has already been registered
	 */
	public boolean hasLogWriter() {
		return platformLog != null && RuntimeLog.contains(platformLog);
	}

	private void initializeAuthorizationHandler() {
		AuthorizationHandler.setKeyringFile(keyringFile);
		AuthorizationHandler.setPassword(password);
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

	private void initializeLocationTrackers() {
		final String FILTER_PREFIX = "(&(objectClass=org.eclipse.osgi.service.datalocation.Location)(type="; //$NON-NLS-1$
		Filter filter = null;
		try {
			filter = context.createFilter(FILTER_PREFIX + PROP_CONFIG_AREA + "))"); //$NON-NLS-1$
		} catch (InvalidSyntaxException e) {
			// ignore this.  It should never happen as we have tested the above format.
		}
		configurationLocation = new ServiceTracker(context, filter, null);
		configurationLocation.open();

		try {
			filter = context.createFilter(FILTER_PREFIX + PROP_USER_AREA + "))"); //$NON-NLS-1$
		} catch (InvalidSyntaxException e) {
			// ignore this.  It should never happen as we have tested the above format.
		}
		userLocation = new ServiceTracker(context, filter, null);
		userLocation.open();

		try {
			filter = context.createFilter(FILTER_PREFIX + PROP_INSTANCE_AREA + "))"); //$NON-NLS-1$
		} catch (InvalidSyntaxException e) {
			// ignore this.  It should never happen as we have tested the above format.
		}
		instanceLocation = new ServiceTracker(context, filter, null);
		instanceLocation.open();

		try {
			filter = context.createFilter(FILTER_PREFIX + PROP_INSTALL_AREA + "))"); //$NON-NLS-1$
		} catch (InvalidSyntaxException e) {
			// ignore this.  It should never happen as we have tested the above format.
		}
		installLocation = new ServiceTracker(context, filter, null);
		installLocation.open();
	}

	public boolean isFragment(Bundle bundle) {
		PackageAdmin packageAdmin = getBundleAdmin();
		if (packageAdmin == null)
			return false;
		return (packageAdmin.getBundleType(bundle) & PackageAdmin.BUNDLE_TYPE_FRAGMENT) > 0;
	}

	public boolean isRunning() {
		try {
			return initialized && context.getBundle().getState() == Bundle.ACTIVE;
		} catch (IllegalStateException e) {
			return false;
		}
	}

	/**
	 * Returns a list of known system architectures.
	 * 
	 * @return the list of system architectures known to the system
	 */
	public String[] knownOSArchValues() {
		return ARCH_LIST;
	}

	/**
	 * Returns a list of known operating system names.
	 * 
	 * @return the list of operating systems known to the system
	 */
	public String[] knownOSValues() {
		return OS_LIST;
	}

	/**
	 * Returns a list of known windowing system names.
	 * 
	 * @return the list of window systems known to the system
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
		// TODO: derecate?
		RuntimeLog.log(status);
	}

	private String[] processCommandLine(String[] args) {
		if (args == null)
			return args;
		allArgs = args;
		if (args.length == 0)
			return args;

		int[] configArgs = new int[args.length];
		//need to initialize the first element to something that could not be an index.
		configArgs[0] = -1;
		int configArgIndex = 0;
		for (int i = 0; i < args.length; i++) {
			boolean found = false;
			// check for args without parameters (i.e., a flag arg)

			// consume obsolete args
			if (args[i].equalsIgnoreCase(CLASSLOADER_PROPERTIES))
				found = true; // ignored
			if (args[i].equalsIgnoreCase(NO_PACKAGE_PREFIXES))
				found = true; // ignored
			if (args[i].equalsIgnoreCase(PLUGINS))
				found = true; // ignored
			if (args[i].equalsIgnoreCase(FIRST_USE))
				found = true; // ignored
			if (args[i].equalsIgnoreCase(NO_UPDATE))
				found = true; // ignored
			if (args[i].equalsIgnoreCase(NEW_UPDATES))
				found = true; // ignored
			if (args[i].equalsIgnoreCase(UPDATE))
				found = true; // ignored

			// done checking for args.  Remember where an arg was found 
			if (found) {
				configArgs[configArgIndex++] = i;
				continue;
			}
			// check for args with parameters
			if (i == args.length - 1 || args[i + 1].startsWith("-")) //$NON-NLS-1$
				continue;
			String arg = args[++i];

			// look for the keyring file
			if (args[i - 1].equalsIgnoreCase(KEYRING)) {
				keyringFile = arg;
				found = true;
			}

			// look for the user password.  
			if (args[i - 1].equalsIgnoreCase(PASSWORD)) {
				password = arg;
				found = true;
			}

			// look for the product to run
			// treat -feature as a synonym for -product for compatibility.
			if (args[i - 1].equalsIgnoreCase(PRODUCT) || args[i - 1].equalsIgnoreCase(FEATURE)) {
				// use the long way to set the property to compile against eeminimum
				System.getProperties().setProperty(PROP_PRODUCT, arg);
				found = true;
			}

			// look for the application to run.  
			if (args[i - 1].equalsIgnoreCase(APPLICATION)) {
				// use the long way to set the property to compile against eeminimum
				System.getProperties().setProperty(PROP_APPLICATION, arg);
				found = true;
			}

			// consume obsolete args for compatibilty
			if (args[i - 1].equalsIgnoreCase(CLASSLOADER_PROPERTIES))
				found = true; // ignore
			if (args[i - 1].equalsIgnoreCase(BOOT))
				found = true; // ignore

			// done checking for args.  Remember where an arg was found 
			if (found) {
				configArgs[configArgIndex++] = i - 1;
				configArgs[configArgIndex++] = i;
			}
		}

		// remove all the arguments consumed by this argument parsing
		if (configArgIndex == 0) {
			appArgs = args;
			return args;
		}
		appArgs = new String[args.length - configArgIndex];
		frameworkArgs = new String[configArgIndex];
		configArgIndex = 0;
		int j = 0;
		int k = 0;
		for (int i = 0; i < args.length; i++) {
			if (i == configArgs[configArgIndex]) {
				frameworkArgs[k++] = args[i];
				configArgIndex++;
			} else
				appArgs[j++] = args[i];
		}
		return appArgs;
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

	public void registerBundleGroupProvider(IBundleGroupProvider provider) {
		groupProviders.add(provider);
	}

	/**
	 * @see Platform#removeLogListener(ILogListener)
	 */
	public void removeLogListener(ILogListener listener) {
		assertInitialized();
		RuntimeLog.removeLogListener(listener);
	}

	/**
	 * @see Platform
	 */
	public URL resolve(URL url) throws IOException {
		URL result = asActualURL(url);
		if (!result.getProtocol().startsWith(PlatformURLHandler.BUNDLE))
			return result;

		URLConverter theConverter = getURLConverter();
		if (theConverter == null) {
			throw new IOException("url.noaccess"); //$NON-NLS-1$
		}
		result = theConverter.convertToLocalURL(result);

		return result;
	}

	public void setExtensionRegistry(IExtensionRegistry value) {
		registry = value;
	}

	public void setOption(String option, String value) {
		DebugOptions options = getDebugOptions();
		if (options != null)
			options.setOption(option, value);
	}

	//Those two methods are only used to register runtime once compatibility has been started.
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
		initializeLocationTrackers();
		splashHandler = getSplashHandler();
		processCommandLine(getEnvironmentInfoService().getNonFrameworkArgs());
		initializeDebugFlags();
		initialized = true;
		getMetaArea();
		initializeAuthorizationHandler();
		platformLog = new PlatformLogWriter(getFrameworkLog());
		addLogListener(platformLog);

		// start registry:
		setExtensionRegistry(new EclipseExtensionRegistry());
		adapterManagerListener = new AdapterManagerListener(); // after extension registry
		startServices();
	}

	/**
	 * Shutdown runtime pieces in this order:
	 * Content[auto shutdown] -> Preferences[auto shutdown] -> Registry -> Jobs
	 * The "auto" shutdown takes place before this code is executed
	 */
	public void stop(BundleContext bundleContext) {
		assertInitialized();
		stopServices(); // should be done after preferences shutdown
		if (adapterManagerListener != null)
			adapterManagerListener.stop(); // before extension registry
		stopRegistry();
		stopJobs();
		RuntimeLog.removeLogListener(platformLog); // effectively turns the platform logging off
		initialized = false;
		closeOSGITrackers();
		context = null;
	}

	/**
	 * Takes a preference value and a related resource bundle and
	 * returns the translated version of this value (if one exists).
	 * 
	 * TODO: fix these comments
	 * @param value the preference value for potential translation
	 * @param props the properties containing the translated values
	 * 
	 * @since 2.0
	 */
	public String translatePreference(String value, Properties props) {
		value = value.trim();
		if (props == null || value.startsWith(KEY_DOUBLE_PREFIX))
			return value;
		if (value.startsWith(KEY_PREFIX)) {

			int ix = value.indexOf(" "); //$NON-NLS-1$
			String key = ix == -1 ? value : value.substring(0, ix);
			String dflt = ix == -1 ? value : value.substring(ix + 1);
			return props.getProperty(key.substring(1), dflt);
		}
		return value;
	}

	public void unregisterBundleGroupProvider(IBundleGroupProvider provider) {
		groupProviders.remove(provider);
	}

	private void startServices() {
		customPreferencesService = getBundleContext().registerService(IProductPreferencesService.class.getName(), new ProductPreferencesService(), new Hashtable());
		legacyPreferencesService = getBundleContext().registerService(ILegacyPreferences.class.getName(), new InitLegacyPreferences(), new Hashtable());
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

	/**
	 * Stop extension registry
	 */
	private void stopRegistry() {
		if (registry != null) {
			((EclipseExtensionRegistry) registry).stop();
			registry = null;
		}
	}

	/**
	 * Stop all running jobs and shutdown Jobs manager
	 */
	private void stopJobs() {
		Bundle jobBundle = getBundle(JOB_PLUGIN);
		try {
			jobBundle.stop();
		} catch (BundleException e) {
			message("InternalPlatfrom: unable to stop the Job bundle."); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

	private PackageAdmin getBundleAdmin() {
		if (bundleTracker == null) {
			bundleTracker = new ServiceTracker(context, PackageAdmin.class.getName(), null);
			bundleTracker.open();
		}
		return (PackageAdmin) bundleTracker.getService();
	}

	private DebugOptions getDebugOptions() {
		if (debugTracker == null) {
			debugTracker = new ServiceTracker(context, DebugOptions.class.getName(), null);
			debugTracker.open();
		}
		return (DebugOptions) debugTracker.getService();
	}

	private void closeOSGITrackers() {
		if (productTracker != null) {
			productTracker.close();
			productTracker = null;
		}
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
		if (urlTracker != null) {
			urlTracker.close();
			urlTracker = null;
		}
		if (environmentTracker != null) {
			environmentTracker.close();
			environmentTracker = null;
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
}
