/*******************************************************************************
 * Copyright (c) 2003, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.configurator;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.util.NLS;
import org.eclipse.update.configurator.IPlatformConfiguration;
import org.eclipse.update.configurator.IPlatformConfigurationFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class ConfigurationActivator implements BundleActivator, IBundleGroupProvider, IConfigurationConstants {

	public static String PI_CONFIGURATOR = "org.eclipse.update.configurator"; //$NON-NLS-1$
	public static final String LAST_CONFIG_STAMP = "last.config.stamp"; //$NON-NLS-1$
	public static final String NAME_SPACE = "org.eclipse.update"; //$NON-NLS-1$
	public static final String UPDATE_PREFIX = "update@"; //$NON-NLS-1$

	// debug options
	public static String OPTION_DEBUG = PI_CONFIGURATOR + "/debug"; //$NON-NLS-1$
	// debug values
	public static boolean DEBUG = false;

	private static BundleContext context;
	private ServiceRegistration<IPlatformConfigurationFactory> configurationFactorySR;
	ServiceRegistration<?> bundleGroupProviderSR;
	private PlatformConfiguration configuration;

	// Location of the configuration data
	private Location configLocation;

	//Need to store that because it is not provided by the platformConfiguration
	private long lastTimeStamp;

	// The expected states timestamp
	private long lastStateTimeStamp;

	// Singleton
	private static ConfigurationActivator configurator;

	public ConfigurationActivator() {
		configurator = this;
	}

	@Override
	public void start(BundleContext ctx) throws Exception {
		context = ctx;
		loadOptions();
		acquireFrameworkLogService();
		try {
			initialize();
		} catch (Exception e) {
			//we failed to start, so make sure Utils closes its service trackers
			Utils.shutdown();
			throw e;
		}

		//Short cut, if the configuration has not changed
		if (canRunWithCachedData()) {
			Utils.debug("Running with cached data"); //$NON-NLS-1$
			registerBundleGroupProvider();
			return;
		}

		Utils.debug("Starting update configurator..."); //$NON-NLS-1$

		registerBundleGroupProvider();
	}
	
	private void registerBundleGroupProvider() {
		final String serviceName = IBundleGroupProvider.class.getName();
		try {
			//don't register the service if this bundle has already registered it declaratively
			ServiceReference<?>[] refs = getBundleContext().getServiceReferences(serviceName, null);
			if (refs != null) {
				for (int i = 0; i < refs.length; i++)
					if (PI_CONFIGURATOR.equals(refs[i].getBundle().getSymbolicName()))
						return;
			}
		} catch (InvalidSyntaxException e) {
			//can't happen because we don't pass a filter
		}
		bundleGroupProviderSR = getBundleContext().registerService(serviceName, this, null);
	}

	private void initialize() throws Exception {
		// TODO this test is not really needed any more than any plugin has 
		// to test to see if the runtime is running.  It was there from earlier days
		// where startup was much more disjoint.  Some day that level of decoupling
		// will return but for now...
		if (!Utils.isRunning())
			throw new Exception(Messages.ConfigurationActivator_initialize);

		configLocation = Utils.getConfigurationLocation();
		// create the name space directory for update (configuration/org.eclipse.update)
		if (!configLocation.isReadOnly()) {
			try {
				URL privateURL = new URL(configLocation.getURL(), NAME_SPACE);
				File f = new File(privateURL.getFile());
				if (!f.exists())
					f.mkdirs();
			} catch (MalformedURLException e1) {
				// ignore
			}
		}
		configurationFactorySR = context.registerService(IPlatformConfigurationFactory.class, new PlatformConfigurationFactory(), null);
		configuration = getPlatformConfiguration(Utils.getInstallURL(), configLocation);
		if (configuration == null)
			throw Utils.newCoreException(NLS.bind(Messages.ConfigurationActivator_createConfig, (new String[] {configLocation.getURL().toExternalForm()})), null);

		DataInputStream stream = null;
		try {
			stream = new DataInputStream(new URL(configLocation.getURL(), NAME_SPACE + '/' + LAST_CONFIG_STAMP).openStream());
			lastTimeStamp = stream.readLong();
			lastStateTimeStamp = stream.readLong();
		} catch (Exception e) {
			lastTimeStamp = configuration.getChangeStamp() - 1;
			lastStateTimeStamp = -1;
		} finally {
			if (stream != null)
				try {
					stream.close();
				} catch (IOException e1) {
					Utils.log(e1.getLocalizedMessage());
				}
		}
	}

	@Override
	public void stop(BundleContext ctx) throws Exception {
		// quick fix (hack) for bug 47861
		try {
			PlatformConfiguration.shutdown();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		configurationFactorySR.unregister();
		if (bundleGroupProviderSR != null)
			bundleGroupProviderSR.unregister();
		Utils.shutdown();
	}

	/**
	 * Creates and starts the platform configuration.
	 * @return the just started platform configuration
	 */
	private PlatformConfiguration getPlatformConfiguration(URL installURL, Location configLocation) {
		try {
			PlatformConfiguration.startup(installURL, configLocation);
		} catch (Exception e) {
			String message = e.getMessage();
			if (message == null)
				message = ""; //$NON-NLS-1$
			Utils.log(Utils.newStatus(message, e));
		}
		return PlatformConfiguration.getCurrent();

	}

	private void loadOptions() {
		// all this is only to get the application args		
		DebugOptions service = null;
		ServiceReference<DebugOptions> reference = context.getServiceReference(DebugOptions.class);
		if (reference != null)
			service = context.getService(reference);
		if (service == null)
			return;
		try {
			DEBUG = service.getBooleanOption(OPTION_DEBUG, false);
		} finally {
			// we have what we want - release the service
			context.ungetService(reference);
		}
	}

	private boolean canRunWithCachedData() {
		return !"true".equals(context.getProperty("osgi.checkConfiguration")) && //$NON-NLS-1$ //$NON-NLS-2$
				lastTimeStamp == configuration.getChangeStamp() && lastStateTimeStamp == Utils.getStateStamp();
	}

	public static BundleContext getBundleContext() {
		return context;
	}

	@Override
	public String getName() {
		return Messages.BundleGroupProvider;
	}

	@Override
	public IBundleGroup[] getBundleGroups() {
		if (configuration == null)
			return new IBundleGroup[0];

		IPlatformConfiguration.IFeatureEntry[] features = configuration.getConfiguredFeatureEntries();
		ArrayList<IBundleGroup> bundleGroups = new ArrayList<>(features.length);
		for (int i = 0; i < features.length; i++) {
			if (features[i] instanceof FeatureEntry && ((FeatureEntry) features[i]).hasBranding())
				bundleGroups.add((IBundleGroup) features[i]);
		}
		return bundleGroups.toArray(new IBundleGroup[bundleGroups.size()]);
	}

	public static ConfigurationActivator getConfigurator() {
		return configurator;
	}

	private void acquireFrameworkLogService() {
		ServiceReference<FrameworkLog> logServiceReference = context.getServiceReference(FrameworkLog.class);
		if (logServiceReference == null)
			return;
		Utils.log = context.getService(logServiceReference);
	}
}
