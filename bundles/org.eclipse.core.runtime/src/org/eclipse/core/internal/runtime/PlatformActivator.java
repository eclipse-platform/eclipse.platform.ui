/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Locale;
import org.eclipse.core.internal.boot.PlatformURLBaseConnection;
import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.eclipse.osgi.service.runnable.ParameterizedRunnable;
import org.eclipse.osgi.service.systembundle.EntryLocator;
import org.eclipse.osgi.service.urlconversion.URLConverter;
import org.osgi.framework.*;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * Activator for the Eclipse runtime.
 */
public class PlatformActivator extends Plugin implements BundleActivator {
	private static final String PROP_ECLIPSE_EXITCODE = "eclipse.exitcode"; //$NON-NLS-1$
	private static final String PROP_ECLIPSE_APPLICATION = "eclipse.application"; //$NON-NLS-1$
	private static final String NL_SYSTEM_BUNDLE = "org.eclipse.osgi.nl"; //$NON-NLS-1$
	private static final String NL_PROP_EXT = ".properties"; //$NON-NLS-1$

	private static BundleContext context;
	private ServiceReference environmentServiceReference;
	private ServiceReference urlServiceReference;
	private ServiceReference logServiceReference;
	private ServiceReference packageAdminReference;
	private ServiceRegistration entryLocatorRegistration;

	public static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext runtimeContext) throws Exception {
		PlatformActivator.context = runtimeContext;
		acquireInfoService();
		acquireURLConverterService();
		acquireFrameworkLogService();
		acquirePackageAdminService();
		registerEntryLocator();
		startInternalPlatform();
		startRegistry(runtimeContext);
		installPlatformURLSupport();
		registerApplicationService();
		InternalPlatform.getDefault().setRuntimeInstance(this);
		super.start(runtimeContext);
	}

	/**
	 * Register the platform URL support as a service to the URLHandler service
	 */
	private void installPlatformURLSupport() {
		PlatformURLPluginConnection.startup();
		PlatformURLFragmentConnection.startup();

		PlatformURLBaseConnection.startup(InternalPlatform.getDefault().getInstallURL());

		Hashtable properties = new Hashtable(1);
		properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] {PlatformURLHandler.PROTOCOL});
		context.registerService(URLStreamHandlerService.class.getName(), new PlatformURLHandler(), properties);
	}

	private void startRegistry(BundleContext runtimeContext) {		
		InternalPlatform.getDefault().setExtensionRegistry(new ExtensionRegistry());	
	}

	public void stop(BundleContext runtimeContext) {
		// Stop the registry
		stopRegistry(runtimeContext);
		// unregister the EntryLocator to prevent the Framework from calling it
		unregisterEntryLocator();
		environmentInfoServiceReleased(environmentServiceReference);
		urlServiceReleased(urlServiceReference);
		logServiceReleased(logServiceReference);
		packageAdminServiceReleased(packageAdminReference);
		// Stop the platform orderly.		
		InternalPlatform.getDefault().stop(runtimeContext);
		InternalPlatform.getDefault().setRuntimeInstance(null);
		InternalPlatform.getDefault().getRuntimeFileManager().close();
	}

	private void stopRegistry(BundleContext runtimeContext) {
		ExtensionRegistry registry = (ExtensionRegistry) InternalPlatform.getDefault().getRegistry();
		if (registry == null)
			return;
		registry.stop();
		InternalPlatform.getDefault().setExtensionRegistry(null);
	}

	private void acquireInfoService() throws Exception {
		environmentServiceReference = context.getServiceReference(EnvironmentInfo.class.getName());
		if (environmentServiceReference == null)
			return;
		InternalPlatform.infoService = (EnvironmentInfo) context.getService(environmentServiceReference);
	}

	private void acquireURLConverterService() throws Exception {
		urlServiceReference = context.getServiceReference(URLConverter.class.getName());
		if (urlServiceReference == null)
			return;
		InternalPlatform.urlConverter = (URLConverter) context.getService(urlServiceReference);
	}

	private void acquireFrameworkLogService() throws Exception {
		logServiceReference = context.getServiceReference(FrameworkLog.class.getName());
		if (logServiceReference == null)
			return;
		InternalPlatform.frameworkLog = (FrameworkLog) context.getService(logServiceReference);
	}

	private void acquirePackageAdminService() throws Exception {
		packageAdminReference = context.getServiceReference(PackageAdmin.class.getName());
		if (packageAdminReference == null)
			return;
		InternalPlatform.packageAdmin = (PackageAdmin) context.getService(packageAdminReference);
	}

	private void startInternalPlatform() throws IOException {
		InternalPlatform.getDefault().start(context);
	}

	private void environmentInfoServiceReleased(ServiceReference reference) {
		if (environmentServiceReference == null)
			return;
		if (environmentServiceReference != reference)
			return;

		InternalPlatform.infoService = null;
		context.ungetService(environmentServiceReference);
		environmentServiceReference = null;
	}

	private void urlServiceReleased(ServiceReference reference) {
		if (urlServiceReference == null)
			return;
		if (urlServiceReference != reference)
			return;

		InternalPlatform.urlConverter = null;
		context.ungetService(urlServiceReference);
		urlServiceReference = null;
	}

	private void logServiceReleased(ServiceReference reference) {
		if (logServiceReference == null)
			return;
		if (logServiceReference != reference)
			return;

		InternalPlatform.frameworkLog = null;
		context.ungetService(logServiceReference);
		logServiceReference = null;
	}

	private void packageAdminServiceReleased(ServiceReference reference) {
		if (packageAdminReference == null)
			return;
		if (packageAdminReference != reference)
			return;

		InternalPlatform.packageAdmin = null;
		context.ungetService(packageAdminReference);
		packageAdminReference = null;
	}

	private void registerApplicationService() {
		ParameterizedRunnable work = new ParameterizedRunnable() {
			public Object run(Object arg) throws Exception {
				IPlatformRunnable application = null;
				String applicationId = System.getProperty(PROP_ECLIPSE_APPLICATION);
				if (applicationId == null) {
					//Derive the application from the product information
					IProduct product = InternalPlatform.getDefault().getProduct();
					if (product != null) {
						applicationId = product.getApplication();
						if (applicationId != null)
							System.setProperty(PROP_ECLIPSE_APPLICATION, applicationId);
					}
				}
				if (applicationId == null)
					throw new RuntimeException(Policy.bind("application.noIdFound")); //$NON-NLS-1$
				IExtensionRegistry registry = InternalPlatform.getDefault().getRegistry();
				IExtension applicationExtension = registry.getExtension(Platform.PI_RUNTIME, Platform.PT_APPLICATIONS, applicationId);
				if (applicationExtension == null) {
					IExtension[] availableApps = registry.getExtensionPoint(Platform.PI_RUNTIME + '.' + Platform.PT_APPLICATIONS).getExtensions();
					String availableAppsString = "<NONE>"; //$NON-NLS-1$
					if (availableApps.length != 0) {
						availableAppsString = availableApps[0].getUniqueIdentifier();
						for (int i = 1; i < availableApps.length; i++) {
							availableAppsString = availableAppsString + ", " + availableApps[i].getUniqueIdentifier(); //$NON-NLS-1$
						}
					}
					throw new RuntimeException(Policy.bind("application.notFound", applicationId, availableAppsString)); //$NON-NLS-1$
				}
				IConfigurationElement[] configs = applicationExtension.getConfigurationElements();
				if (configs.length == 0)
					throw new RuntimeException(Policy.bind("application.invalidExtension", applicationId)); //$NON-NLS-1$
				IConfigurationElement config = configs[0];
				application = (IPlatformRunnable) config.createExecutableExtension("run"); //$NON-NLS-1$
				// if the given arg is null the pass in the left over command line args.
				if (arg == null)
					arg = InternalPlatform.getDefault().getApplicationArgs();
				Object result = application.run(arg);
				int exitCode = result instanceof Integer ? ((Integer) result).intValue() : 0;
				System.setProperty(PROP_ECLIPSE_EXITCODE, Integer.toString(exitCode));
				if (InternalPlatform.DEBUG)
					System.out.println(Policy.bind("application.returned", new String[] {applicationId, result == null ? "null" : result.toString()})); //$NON-NLS-1$ //$NON-NLS-2$
				return result;
			}
		};
		Hashtable properties = new Hashtable(1);
		properties.put(PROP_ECLIPSE_APPLICATION, "default"); //$NON-NLS-1$ 
		context.registerService(ParameterizedRunnable.class.getName(), work, properties);
	}

	private void registerEntryLocator() {
		EntryLocator systemResources = new EntryLocator() {
			public URL getProperties(String basename, Locale locale) {
				basename = basename.replace('.', '/');
				IPath propertiesPath = new Path(NL_SYSTEM_BUNDLE + '/' + basename + '_' + locale.getLanguage() + '_' + locale.getCountry() + NL_PROP_EXT);
				URL result = Platform.find(getContext().getBundle(), propertiesPath);
				if (result != null)
					return result;
				propertiesPath = new Path(NL_SYSTEM_BUNDLE + '/' + basename + '_' + locale.getLanguage() + NL_PROP_EXT);
				return Platform.find(getContext().getBundle(), propertiesPath);
			}
		};
		entryLocatorRegistration = context.registerService(EntryLocator.class.getName(), systemResources, null);
	}

	private void unregisterEntryLocator() {
		if (entryLocatorRegistration != null) {
			entryLocatorRegistration.unregister();
			entryLocatorRegistration = null;
		}
	}
}