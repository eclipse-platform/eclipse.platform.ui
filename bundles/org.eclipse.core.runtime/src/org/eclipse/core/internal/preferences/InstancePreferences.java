/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.preferences;

import java.io.*;
import java.util.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @since 3.0
 */
public class InstancePreferences extends EclipsePreferences {

	private static final String DEFAULT_PREFERENCES_FILENAME = "prefs.ini"; //$NON-NLS-1$
	private static final String OLD_EXTENSION = ".old"; //$NON-NLS-1$

	// cached values
	private String qualifier;
	private int segmentCount;
	private EclipsePreferences loadLevel;
	private IPath location;
	// cache which nodes have been loaded from disk
	private static Set loadedNodes = new HashSet();

	/**
	 * Default constructor. Should only be called by #createExecutableExtension.
	 */
	public InstancePreferences() {
		this(null, null);
	}

	private InstancePreferences(IEclipsePreferences parent, String name) {
		super(parent, name);
		initialize();
	}

	protected boolean isAlreadyLoaded(IEclipsePreferences node) {
		return loadedNodes.contains(node.name());
	}

	protected void loaded() {
		loadedNodes.add(name());
	}

	/*
	 * @see org.osgi.service.prefs.Preferences#sync()
	 */
	public void sync() throws BackingStoreException {
		if (location == null) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Unable to determine location of preference file for node: " + absolutePath()); //$NON-NLS-1$
			return;
		}
		IEclipsePreferences node = getLoadLevel();
		if (node == null) {
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Preference node is not a load root: " + absolutePath()); //$NON-NLS-1$
			return;
		}
		if (node instanceof EclipsePreferences) {
			((EclipsePreferences) node).load(location);
			node.flush();
		}
	}

	/**
	 * Load the Eclipse 2.1 preferences for the given bundle. If a file
	 * doesn't exist then assume that conversion has already occurred
	 * and do nothing.
	 */
	protected void loadLegacy() {
		IPath path = new Path(absolutePath());
		if (path.segmentCount() != 2)
			return;
		String bundleName = path.segment(1);
		// the preferences file is located in the plug-in's state area at a well-known name
		// don't need to create the directory if there are no preferences to load
		File prefFile = InternalPlatform.getDefault().getMetaArea().getPreferenceLocation(bundleName, false).toFile();
		if (!prefFile.exists()) {
			// no preference file - that's fine
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Legacy plug-in preference file not found: " + prefFile); //$NON-NLS-1$ //$NON-NLS-2$
			// convert pre-M9 prefs before returning
			loadLegacyPreM9();
			return;
		}

		if (InternalPlatform.DEBUG_PREFERENCES)
			Policy.debug("Loading legacy preferences from " + prefFile); //$NON-NLS-1$

		// load preferences from file
		InputStream input = null;
		Properties values = new Properties();
		try {
			input = new BufferedInputStream(new FileInputStream(prefFile));
			values.load(input);
		} catch (IOException e) {
			// problems loading preference store - quietly ignore
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("IOException encountered loading legacy preference file " + prefFile); //$NON-NLS-1$
			return;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					// ignore problems with close
					if (InternalPlatform.DEBUG_PREFERENCES) {
						Policy.debug("IOException encountered closing legacy preference file " + prefFile); //$NON-NLS-1$
						e.printStackTrace();
					}
				}
			}
		}

		if (properties == null)
			properties = new Properties();

		// Store values in the preferences object
		for (Iterator i = values.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String value = values.getProperty(key);
			// value shouldn't be null but check just in case...
			if (value != null) {
				if (InternalPlatform.DEBUG_PREFERENCES)
					Policy.debug("Loaded legacy preference: " + key + " -> " + value); //$NON-NLS-1$ //$NON-NLS-2$
				// call these 2 methods rather than #put() so we don't send out unnecessary notification
				properties.put(key, value);
				makeDirty();
			}
		}

		// Rename the old file so we don't try and load it next time. 
		File destFile = new File(prefFile.getAbsolutePath() + OLD_EXTENSION + '.' + System.currentTimeMillis());
		if (!prefFile.renameTo(destFile))
			//Only print out message in failure case if we are debugging.
			if (InternalPlatform.DEBUG_PREFERENCES)
				Policy.debug("Unable to rename legacy preferences file: " + prefFile + " -> " + destFile); //$NON-NLS-1$ //$NON-NLS-2$

		loadLegacyPreM9();
	}

	/*
	 * TODO: Remove this method after M9 but before the 3.0 release.
	 * It converts from the interim format used in integration builds (pre M9).
	 */
	private void loadLegacyPreM9() {
		if (qualifier == null)
			return;
		IPath oldLocation = InternalPlatform.getDefault().getMetaArea().getStateLocation(qualifier).append(DEFAULT_PREFERENCES_FILENAME);
		if (!oldLocation.toFile().exists())
			return;
		try {
			load(oldLocation);
		} catch (BackingStoreException e) {
			String message = "IOException encountered loading legacy preference file " + oldLocation; //$NON-NLS-1$
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, e);
			log(status);
			return;
		}
		oldLocation.toFile().delete();
	}

	protected IPath getLocation() {
		return location;
	}

	/*
	 * Parse this node's absolute path and initialize some cached values for
	 * later use.
	 */
	private void initialize() {
		// cache the segment count
		IPath path = new Path(absolutePath());
		segmentCount = path.segmentCount();
		if (segmentCount < 2)
			return;

		// cache the qualifier
		String scope = path.segment(0);
		if (InstanceScope.SCOPE.equals(scope))
			qualifier = path.segment(1);

		// cache the location
		if (qualifier == null)
			return;
		// get the base location from the platform
		location = computeLocation(InternalPlatform.getDefault().getMetaArea().getStateLocation(Platform.PI_RUNTIME), qualifier);
	}

	/*
	 * Return the node at which these preferences are loaded/saved.
	 */
	protected IEclipsePreferences getLoadLevel() {
		if (loadLevel == null) {
			if (qualifier == null)
				return null;
			// Make it relative to this node rather than navigating to it from the root.
			// Walk backwards up the tree starting at this node.
			// This is important to avoid a chicken/egg thing on startup.
			EclipsePreferences node = this;
			for (int i = 2; i < segmentCount; i++)
				node = (EclipsePreferences) node.parent();
			loadLevel = node;
		}
		return loadLevel;
	}

	protected EclipsePreferences internalCreate(IEclipsePreferences nodeParent, String nodeName) {
		return new InstancePreferences(nodeParent, nodeName);
	}
}