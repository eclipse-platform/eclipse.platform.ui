package org.eclipse.update.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Date;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.core.Utilities;

/**
 * Update manager reconciler application.
 * This application is invoked by the platform startup support
 * whenever changes are detected in the installation that require
 * the update state to be reconciled. Typically, this will be as
 * a result of installation actions that were done directly 
 * in the file system bypassing the Update Manager.
 * When triggered, this application performs the following:
 * <ul>
 * <li>call update manager reconciler
 * <li>if plugin path changed as a result of this call, trigger
 * restart ("exit code 23")
 * <li>if plugin path has not changed as a result of this call,
 * start this original application
 * </ul>
 */

public class UpdateManagerReconciler implements IPlatformRunnable {
	
	// NOTE: originalApplication is set to a dummy string to prevent "silent"
	// failures in case of internal errors
	private String originalApplication = "org.eclipse.update.UNKNOWN"; //$NON-NLS-1$
	private boolean DEBUG = false;
	
	private static final String APPLICATION = "-application"; //$NON-NLS-1$

	/**
	 * @see IPlatformRunnable#run(Object)
	 */
	public Object run(Object args) throws Exception {
		
		// get debug setting
		DEBUG = UpdateManagerPlugin.DEBUG_SHOW_RECONCILER;
		
		// obtain current platform configuration and save stamps
		IPlatformConfiguration cfg = BootLoader.getCurrentPlatformConfiguration();
		long originalPluginsStamp = cfg.getPluginsChangeStamp();
		
		// perform reconciliation
		long start = 0;
		if (DEBUG) {
			start = new Date().getTime();
			debug("begin"); //$NON-NLS-1$
		}
		
		reconcile(cfg);
		long newPluginsStamp = cfg.getPluginsChangeStamp();		
		
		if (DEBUG) {
			long delta = (new Date().getTime()) - start;
			debug("end [" + delta + "ms]"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		// see if plugins have changed
		if (newPluginsStamp != originalPluginsStamp) {
			// plugins lineup changed ... need to restart
			if (DEBUG)
				debug("restarting ...");  //$NON-NLS-1$
			Platform.endSplash();
			return EXIT_RESTART;
		} else {
			// plugins lineup did not change ... no need to restart, 
			// just continue starting up the original app
			String[] appArgs = processCommandLine((String[])args);
			IPlatformRunnable originalRunnable = getRunnable(originalApplication);
			if (originalRunnable == null)
				throw new IllegalArgumentException(Policy.bind("Reconciler.appNotFound",originalApplication));  //$NON-NLS-1$
			if (DEBUG)
				debug("invoking " + originalApplication + " ...");  //$NON-NLS-1$  //$NON-NLS-2$
			return originalRunnable.run(appArgs);
		}
	}
	
	private void reconcile(IPlatformConfiguration cfg) {
			
		// do the reconciliation
		try {
			SiteManager.getLocalSite();
		} catch (CoreException e){
			UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
		}
		cfg.refresh(); // for now just force recompute of stamps
	}
	
	private String[] processCommandLine(String[] args) {
		int[] configArgs = new int[100];
		configArgs[0] = -1; // need to initialize the first element to something that could not be an index.
		int configArgIndex = 0;
		for (int i = 0; i < args.length; i++) {
			boolean found = false;
			// check for args with parameters. If we are at the last argument or if the next one
			// has a '-' as the first character, then we can't have an arg with a parm so continue.
			if (i == args.length - 1 || args[i + 1].startsWith("-")) { //$NON-NLS-1$
				continue;
			}
			String arg = args[++i];
			
			// look for the application argument
			if (args[i - 1].equalsIgnoreCase(APPLICATION)) {
				found = true;
				originalApplication = arg;
			}

			// done checking for args.  Remember where an arg was found 
			if (found) {
				configArgs[configArgIndex++] = i - 1;
				configArgs[configArgIndex++] = i;
			}
		}

		// remove all the arguments consumed by this argument parsing
		if (configArgIndex == 0)
			return args;
		String[] passThruArgs = new String[args.length - configArgIndex];
		configArgIndex = 0;
		int j = 0;
		for (int i = 0; i < args.length; i++) {
			if (i == configArgs[configArgIndex])
				configArgIndex++;
			else
				passThruArgs[j++] = args[i];
		}
		return passThruArgs;
	}
	
	private IPlatformRunnable getRunnable(String application) {
		// NOTE: we need to get the runnable for the original application.
		// We can either lookup the application extension point (duplicate
		// the InternalPlatform logic here), or make the internal call
		return InternalPlatform.loaderGetRunnable(application);
	}
	
	private void debug(String s) {
		UpdateManagerPlugin.getPlugin().debug(this.getClass().getName()+": "+s); //$NON-NLS-1$
	}
}
