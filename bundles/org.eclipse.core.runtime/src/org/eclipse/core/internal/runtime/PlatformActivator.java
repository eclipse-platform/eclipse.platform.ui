/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
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

import java.util.Hashtable;
import org.eclipse.core.internal.boot.PlatformURLBaseConnection;
import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.service.runnable.ParameterizedRunnable;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.*;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * Activator for the Eclipse runtime.
 */
public class PlatformActivator extends Plugin implements BundleActivator {
	private static final String PROP_ECLIPSE_EXITCODE = "eclipse.exitcode"; //$NON-NLS-1$
	private static final String PROP_ECLIPSE_APPLICATION = "eclipse.application"; //$NON-NLS-1$

	private static BundleContext context;
	private ServiceRegistration entryLocatorRegistration;

	public static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext runtimeContext) throws Exception {
		PlatformActivator.context = runtimeContext;
		InternalPlatform.getDefault().start(runtimeContext);
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
		PlatformURLMetaConnection.startup();
		PlatformURLConfigConnection.startup();

		PlatformURLBaseConnection.startup(InternalPlatform.getDefault().getInstallURL());

		Hashtable properties = new Hashtable(1);
		properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] {PlatformURLHandler.PROTOCOL});
		context.registerService(URLStreamHandlerService.class.getName(), new PlatformURLHandler(), properties);
	}

	public void stop(BundleContext runtimeContext) {
		// unregister the EntryLocator to prevent the Framework from calling it
		unregisterEntryLocator();
		// Stop the platform orderly.		
		InternalPlatform.getDefault().stop(runtimeContext);
		InternalPlatform.getDefault().setRuntimeInstance(null);
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
							// use the long way to set the property to compile against eeminimum
							System.getProperties().setProperty(PROP_ECLIPSE_APPLICATION, applicationId);
					}
				}
				if (applicationId == null)
					throw new RuntimeException(Messages.application_noIdFound);
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
					throw new RuntimeException(NLS.bind(Messages.application_notFound, applicationId, availableAppsString));
				}
				IConfigurationElement[] configs = applicationExtension.getConfigurationElements();
				if (configs.length == 0)
					throw new RuntimeException(NLS.bind(Messages.application_invalidExtension, applicationId));
				IConfigurationElement config = configs[0];
				application = (IPlatformRunnable) config.createExecutableExtension("run"); //$NON-NLS-1$
				// if the given arg is null the pass in the left over command line args.
				if (arg == null)
					arg = InternalPlatform.getDefault().getApplicationArgs();
				Object result = application.run(arg);
				int exitCode = result instanceof Integer ? ((Integer) result).intValue() : 0;
				// use the long way to set the property to compile against eeminimum
				System.getProperties().setProperty(PROP_ECLIPSE_EXITCODE, Integer.toString(exitCode));
				if (InternalPlatform.DEBUG)
					System.out.println(NLS.bind(Messages.application_returned, (new String[] {applicationId, result == null ? "null" : result.toString()}))); //$NON-NLS-1$
				return result;
			}
		};
		Hashtable properties = new Hashtable(1);
		properties.put(PROP_ECLIPSE_APPLICATION, "default"); //$NON-NLS-1$ 
		context.registerService(ParameterizedRunnable.class.getName(), work, properties);
	}

	private void unregisterEntryLocator() {
		if (entryLocatorRegistration != null) {
			entryLocatorRegistration.unregister();
			entryLocatorRegistration = null;
		}
	}
}
