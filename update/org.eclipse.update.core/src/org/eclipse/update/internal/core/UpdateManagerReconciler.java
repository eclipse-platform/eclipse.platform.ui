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
package org.eclipse.update.internal.core;


import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;

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
	private boolean initialize = false;
	private boolean firstUse = false;
	private boolean optimistic = false;
	private boolean dumpfile = false;
	private boolean DEBUG = false;
	private String dumpFilename = "";

	private static final String RECONCILER_APP = "org.eclipse.update.core.reconciler";
	private static final String APPLICATION = "-application"; //$NON-NLS-1$	
	private static final String INITIALIZE = "-initialize"; //$NON-NLS-1$
	private static final String DUMPFILE = "-dumpfile"; //$NON-NLS-1$	
	private static final String FIRSTUSE = "-firstuse"; //$NON-NLS-1$
	private static final String CHANGES_MARKER = ".newupdates"; //$NON-NLS-1$

	/**
	 * @see IPlatformRunnable#run(Object)
	 */
	public Object run(Object args) throws Exception {

		// get debug setting and process command line arguments
		DEBUG = UpdateCore.DEBUG_SHOW_RECONCILER;
		processCommandLine((String[]) args);

		// obtain current platform configuration and save stamps
		IPlatformConfiguration cfg = BootLoader.getCurrentPlatformConfiguration();
		// URL[] originalPluginPath = cfg.getPluginPath();

		// perform reconciliation
		long start = 0;
		if (DEBUG) {
			start = new Date().getTime();
			debug("begin"); //$NON-NLS-1$
		}

		if (initialize || firstUse)
			optimistic = true; // initialize and first-time startup ... optimistic
		// (changes are auto-configured)
		else
			optimistic = false; // all other ... pessimistic (changes are
		// reflected in state but not configured)

		boolean changes = reconcile(cfg);

		if (DEBUG) {
			long delta = (new Date().getTime()) - start;
			debug("end [" + delta + "ms]"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// if we need to write the features available, do it now
		if (dumpfile) {
			dumpFeatures();
		}

		// if we are completing post-install initialization (-initialize)
		// just exit
		if (initialize) {
			Platform.endSplash();
			return EXIT_OK;
		}

		// if the reconciler app was explicitly invoked, mark changes and
		// just exit
		if (RECONCILER_APP.equals(originalApplication)) {
			Platform.endSplash();
			if (changes)
				markChanges(cfg);
			return EXIT_OK; // just exit if original app was reconciler
		}

		// if the reconciler app was invoked, with -dump
		// just exit
		if (dumpfile) {
			Platform.endSplash();
			if (changes)
				markChanges(cfg);
			return EXIT_OK; // just exit 
		}
		
		// see if plugins have changed
		//		if (pluginPathChanged(cfg, originalPluginPath)) {
		// plugins lineup changed ... need to restart
		Platform.endSplash();
		if (changes)
			markChanges(cfg);
		if (DEBUG)
			debug("restarting ..." + new Date()); //$NON-NLS-1$
		return EXIT_RESTART;
		//		} else {
		//			// plugins lineup did not change ... no need to restart, 
		//			// just continue starting up the original app. Original
		//			// app will take down splash
		//			IPlatformRunnable originalRunnable = getRunnable(originalApplication);
		//			if (originalRunnable == null)
		//				throw new IllegalArgumentException(Policy.bind("Reconciler.appNotFound",originalApplication));  //$NON-NLS-1$
		//			if (DEBUG)
		//				debug("invoking " + originalApplication + " ...");  //$NON-NLS-1$  //$NON-NLS-2$
		//			// indicate we have new updates (-newUpdates)	
		//			if (changes)
		//				appArgs = markChanges(appArgs);
		//			return originalRunnable.run(appArgs);
		//		}

	}

	/*
	 * get the list of all installed features in all the sites 
	 * dump the identifier and version in a file
	 */
	private void dumpFeatures() {
		if (dumpFilename == null)
			return;
		List features = new ArrayList();

		// find all features
		IConfiguredSite[] configuredSites = new IConfiguredSite[0];
		try {
			configuredSites = SiteManager.getLocalSite().getCurrentConfiguration().getConfiguredSites();
		} catch (Exception e) {
			// eat the error
		}
		for (int i = 0; i < configuredSites.length; i++) {
			IFeatureReference[] ref = configuredSites[i].getSite().getFeatureReferences();
			try {
				for (int j = 0; j < ref.length; j++) {
					features.add(ref[j].getVersionedIdentifier());
				}
			} catch (Exception e) {
				// eat the exception
			}
		}

		// dump them
		File dumpFile = new File(dumpFilename);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(dumpFile);

			for (Iterator iter = features.iterator(); iter.hasNext();) {
				VersionedIdentifier element = (VersionedIdentifier) iter.next();
				try {
					String feature = element.getIdentifier() + "," + element.getVersion() + '\n';
					out.write(feature.getBytes());
				} catch (Exception e) {
					// eat the exception
				}
			}
		} catch (Exception e) {
			// eat the exception
			return;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					// eat the exception
				}
			}
		}

	}

	private boolean reconcile(IPlatformConfiguration cfg) {

		boolean changes = true;

		// do the reconciliation
		try {
			if (DEBUG)
				debug("mode: " + (optimistic ? "optimistic" : "pessimistic")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			changes = InternalSiteManager.reconcile(optimistic);
			if (DEBUG)
				debug(changes ? "changes detected" : "no changes detected"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (CoreException e) {
			UpdateCore.warn(null, e);
		} catch (Exception e) {
			UpdateCore.warn(null, e);
		}
		cfg.refresh(); // recompute stamps and plugin path

		return changes;
	}


	private void markChanges(IPlatformConfiguration cfg) {
		// indicate we have changes in restart scenario ... converted to -newUpdates on restart
		FileOutputStream fos = null;
		try {
			URL markerLocation = new URL(cfg.getConfigurationLocation(), CHANGES_MARKER);
			fos = new FileOutputStream(new File(markerLocation.getFile()));
			fos.write(0);
			fos.close();
		} catch (IOException e) {
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e2) {
				}
		}
	}

	private String[] processCommandLine(String[] args) {
		int[] configArgs = new int[100];
		configArgs[0] = -1; // need to initialize the first element to something that could not be an index.
		int configArgIndex = 0;
		for (int i = 0; i < args.length; i++) {
			boolean found = false;

			// check for args without parameters (i.e., a flag arg)

			// look for the initialization flag
			if (args[i].equalsIgnoreCase(INITIALIZE)) {
				initialize = true;
				found = true;
			}

			// look for first use flag
			if (args[i].equalsIgnoreCase(FIRSTUSE)) {
				firstUse = true;
				found = true;
			}

			if (found) {
				configArgs[configArgIndex++] = i;
				continue;
			}

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

			// look for the dumpfile argument
			if (args[i - 1].equalsIgnoreCase(DUMPFILE)) {
				found = true;
				dumpfile = true;
				dumpFilename = arg;
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

	private void debug(String s) {
		UpdateCore.debug(this.getClass().getName() + ": " + s); //$NON-NLS-1$
	}
}
