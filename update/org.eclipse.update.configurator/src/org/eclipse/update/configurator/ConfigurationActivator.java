/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.configurator;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.osgi.service.debug.*;
import org.eclipse.osgi.service.environment.*;
import org.osgi.framework.*;
import org.osgi.service.packageadmin.*;
import org.osgi.service.startlevel.*;
import org.osgi.util.tracker.*;

public class ConfigurationActivator implements BundleActivator {
	public static String PI_CONFIGURATOR = "org.eclipse.update.configurator";
	// debug options
	public static String OPTION_DEBUG = PI_CONFIGURATOR + "/debug";
	// debug values
	public static boolean DEBUG = false;

	private static BundleContext context;
	private ServiceTracker platformTracker;
	private ServiceTracker converterTracker;
	private ServiceRegistration configurationFactorySR;
	private String[] allArgs;

	// location used to put the generated manfests
	private String cacheLocation = (String) System.getProperties().get("osgi.manifest.cache");
	private IPluginConverter converter;
	private Set ignore;
	private BundleListener reconcilerListener;
	private IPlatform platform;
	private PlatformConfiguration configuration;
	
	//Need to store that because it is not provided by the platformConfiguration
	private long lastTimeStamp;

	public void start(BundleContext ctx) throws Exception {
		context = ctx;
		obtainArgs();
		initialize();
		//Short cut, if the configuration has not changed
		String application = configuration.getApplicationIdentifier();

		//TODO Hack until PDE changes the default application that they are setting
		if("org.eclipse.ui.workbench".equals(System.getProperties().get("eclipse.application"))) { //$NON-NLS-1$ //$NON-NLS-2$
			System.setProperty("eclipse.application", "org.eclipse.ui.ide.workbench"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	
		if (lastTimeStamp==configuration.getChangeStamp() && !(application.equals(PlatformConfiguration.RECONCILER_APP) || System.getProperties().get("osgi.dev") != null))
			if (System.getProperty("eclipse.application") == null) {
				System.setProperty("eclipse.application", application);
				return;
			}
		loadOptions();
		if (DEBUG)
			System.out.println("Starting update configurator...");
		computeIgnoredBundles();
		converter = acquireConverter();
		installBundles();
	}

	private void initialize() {
		platform = acquirePlatform();
		String metaPath = platform.getLocation().append(".metadata").toOSString();
		URL installURL = platform.getInstallURL();
		configurationFactorySR = context.registerService(IPlatformConfigurationFactory.class.getName(), new PlatformConfigurationFactory(), null);
		configuration = getPlatformConfiguration(allArgs, metaPath, installURL);
		
		String configArea = (String) System.getProperty("osgi.configuration.area");
		try {
			DataInputStream stream = new DataInputStream(new FileInputStream(configArea + "/last.config.stamp"));
			lastTimeStamp = stream.readLong();
		} catch (FileNotFoundException e) {
			lastTimeStamp = configuration.getChangeStamp() - 1;
		} catch (IOException e) {
			lastTimeStamp = configuration.getChangeStamp() - 1;
		}
	}

	private void computeIgnoredBundles() {
		String ignoreList = System.getProperty("eclipse.ignore", "org.eclipse.osgi,org.eclipse.core.boot,org.eclipse.core.runtime.adaptor");
		ignore = new HashSet();
		StringTokenizer tokenizer = new StringTokenizer(ignoreList, ",");
		while (tokenizer.hasMoreTokens())
			ignore.add(tokenizer.nextToken().trim());
	}
	private boolean shouldIgnore(String bundleName) {
		if (ignore == null)
			return false;
		StringTokenizer tokenizer = new StringTokenizer(bundleName, "._");
		String partialName = "";
		while (tokenizer.hasMoreTokens()) {
			partialName += tokenizer.nextToken();
			if (ignore.contains(partialName))
				return true;
			partialName += ".";
		}
		return false;
	}

	private void obtainArgs() {
		// all this is only to get the application args		
		EnvironmentInfo envInfo = null;
		ServiceReference envInfoSR = context.getServiceReference(EnvironmentInfo.class.getName());
		if (envInfoSR != null)
			envInfo = (EnvironmentInfo) context.getService(envInfoSR);
		if (envInfo == null)
			throw new IllegalStateException();
		this.allArgs = envInfo.getAllArgs();
		// we have what we want - release the service
		context.ungetService(envInfoSR);
	}

	public void stop(BundleContext ctx) throws Exception {
		// quick fix (hack) for bug 47861
		try {
			PlatformConfiguration.shutdown();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		platform = null;
		releasePlatform();
		converter = null;
		releaseConverter();
		writePlatformConfigurationTimeStamp();
		configurationFactorySR.unregister();
	}

	private void writePlatformConfigurationTimeStamp() {
		String configArea = (String) System.getProperty("osgi.configuration.area");
		try {
			DataOutputStream stream = new DataOutputStream(new FileOutputStream(configArea + "/last.config.stamp"));
			stream.writeLong(configuration.getChangeStamp());
		} catch (FileNotFoundException e) {
			lastTimeStamp = configuration.getChangeStamp() - 1;
		} catch (IOException e) {
			lastTimeStamp = configuration.getChangeStamp() - 1;
		}
		
	}

	private void releasePlatform() {
		if (platformTracker == null)
			return;
		platformTracker.close();
		platformTracker = null;
	}
	
	private void releaseConverter() {
		if (converterTracker == null)
			return;
		converterTracker.close();
		converterTracker = null;
	}
	
	private IPlatform acquirePlatform() {
		if (platformTracker == null) {
			platformTracker = new ServiceTracker(context, IPlatform.class.getName(), null);
			platformTracker.open();
		}
		IPlatform result = (IPlatform) platformTracker.getService();
		while (result == null) {
			try {
				platformTracker.waitForService(1000);
				result = (IPlatform) platformTracker.getService();
			} catch (InterruptedException ie) {
			}
		}
		return result;
	}

	private IPluginConverter acquireConverter() {
		if (converterTracker == null) {
			converterTracker = new ServiceTracker(context, IPluginConverter.class.getName(), null);
			converterTracker.open();
		}
		IPluginConverter result = (IPluginConverter) converterTracker.getService();
		while (result == null) {
			try {
				platformTracker.waitForService(1000);
				result = (IPluginConverter) converterTracker.getService();
			} catch (InterruptedException ie) {
			}
		}
		return result;
	}
	private void installBundles() {
		URL installURL = platform.getInstallURL();
		ServiceReference reference = context.getServiceReference(StartLevel.class.getName());
		StartLevel start = null;
		if (reference != null)
			start = (StartLevel) context.getService(reference);
		try {
			URL[] plugins = configuration.getPluginPath();
			ArrayList installed = new ArrayList(plugins.length);
			for (int i = 0; i < plugins.length; i++) {
				String location = plugins[i].toExternalForm();
				try {
					location = "reference:" + location.substring(0, location.lastIndexOf('/'));
					if (!isInstalled(location)) {
						Bundle target = context.installBundle(location);
						installed.add(target);
						if (start != null)
							start.setBundleStartLevel(target, 4);
					}
				} catch (Exception e) {
					if ((location.indexOf("org.eclipse.core.boot") == -1) && (location.indexOf("org.eclipse.osgi") == -1)) {
						System.err.println("Ignoring bundle at: " + location);
						System.err.println(e.getMessage());
					}
				}
			}
			context.ungetService(reference);
			refreshPackages((Bundle[]) installed.toArray(new Bundle[installed.size()]));
			if (System.getProperty("eclipse.application") == null || System.getProperty("eclipse.application").equals(PlatformConfiguration.RECONCILER_APP))
				System.setProperty("eclipse.application", configuration.getApplicationIdentifier());
			//			if (config.getApplicationIdentifier().equals(PlatformConfiguration.RECONCILER_APP) ) {
			//				reconcilerListener = reconcilerListener();
			//				context.addBundleListener(reconcilerListener);
			//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			releasePlatform();
		}
	}

	private BundleListener reconcilerListener() {
		return new BundleListener() {
			public void bundleChanged(BundleEvent event) {
				String buid = event.getBundle().getGlobalName();
				if (event.getType() == BundleEvent.STOPPED && buid != null && buid.equals("org.eclipse.update.core"))
					runPostReconciler();
			}
		};
	}

	private void runPostReconciler() {
		Runnable postReconciler = new Runnable() {
			public void run() {
				try {
					Bundle apprunner = context.getBundles("org.eclipse.core.applicationrunner")[0];
					apprunner.stop();
					context.removeBundleListener(reconcilerListener);
					try {
						PlatformConfiguration.shutdown();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					installBundles();
					apprunner.start();
				} catch (BundleException be) {
					be.printStackTrace();
				}
			}
		};
		new Thread(postReconciler, "Post reconciler").start();
	}

	/*
	 * Derives a file name corresponding to a path:
	 * c:\autoexec.bat -> c__autoexec.bat
	 */
	private String computeFileName(String filePath) {
		StringBuffer newName = new StringBuffer(filePath);
		for (int i = 0; i < filePath.length(); i++) {
			char c = newName.charAt(i);
			if (c == ':' || c == '/' || c == '\\')
				newName.setCharAt(i, '_');
		}
		return newName.toString();
	}
	/**
	 * This is a major hack to try to get the reconciler application running. However we should find a way to not run it.
	 * @param args
	 * @param metaPath
	 * @return
	 */
	private PlatformConfiguration getPlatformConfiguration(String[] args, String metaPath, URL installURL) {
		try {
			PlatformConfiguration.startup(args, null, null, metaPath, installURL);
		} catch (Exception e) {
			if (platformTracker != null) {
				String message = e.getMessage();
				if (message == null)
					message = "";
				IStatus status = new Status(IStatus.ERROR, IPlatform.PI_RUNTIME, IStatus.OK, message, e);
				((IPlatform) platformTracker.getService()).getLog(context.getBundle()).log(status);
			}
		}
		return PlatformConfiguration.getCurrent();

	}

	/**
	 * Do PackageAdmin.refreshPackages() in a synchronous way.  After installing
	 * all the requested bundles we need to do a refresh and want to ensure that 
	 * everything is done before returning.
	 * @param bundles
	 */
	private void refreshPackages(Bundle[] bundles) {
		if (bundles.length == 0)
			return;
		ServiceReference packageAdminRef = context.getServiceReference(PackageAdmin.class.getName());
		PackageAdmin packageAdmin = null;
		if (packageAdminRef != null) {
			packageAdmin = (PackageAdmin) context.getService(packageAdminRef);
			if (packageAdmin == null)
				return;
		}
		// TODO this is such a hack it is silly.  There are still cases for race conditions etc
		// but this should allow for some progress...
		final Object semaphore = new Object();
		FrameworkListener listener = new FrameworkListener() {
			public void frameworkEvent(FrameworkEvent event) {
				if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED)
					synchronized (semaphore) {
						semaphore.notifyAll();
					}
			}
		};
		context.addFrameworkListener(listener);
		packageAdmin.refreshPackages(bundles);
		synchronized (semaphore) {
			try {
				semaphore.wait();
			} catch (InterruptedException e) {
			}
		}
		context.removeFrameworkListener(listener);
		context.ungetService(packageAdminRef);
	}

	private boolean isInstalled(String location) {
		Bundle[] installed = context.getBundles();
		for (int i = 0; i < installed.length; i++) {
			Bundle bundle = installed[i];
			if (location.equalsIgnoreCase(bundle.getLocation()))
				return true;
		}
		return false;
	}

	private void loadOptions() {
		// all this is only to get the application args		
		DebugOptions service = null;
		ServiceReference reference = context.getServiceReference(DebugOptions.class.getName());
		if (reference != null)
			service = (DebugOptions) context.getService(reference);
		if (service == null)
			return;
		try {
			DEBUG = service.getBooleanOption(OPTION_DEBUG, false);
		} finally {
			// we have what we want - release the service
			context.ungetService(reference);
		}
	}
	public static BundleContext getBundleContext() {
		return context;
	}
}