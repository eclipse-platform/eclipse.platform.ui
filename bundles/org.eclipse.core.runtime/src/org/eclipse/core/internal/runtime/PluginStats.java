/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.runtime;

import java.io.*;
import java.util.*;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.boot.ClassloaderStats;
import org.eclipse.core.internal.boot.DelegatingURLClassLoader;

public class PluginStats {
	public String pluginId;
	public int activationOrder;
	private long activationTimeStamp;
	private boolean activatedDuringStartup;
	private long totalStartupTime;
	private long startupMethodTime;
	// Indicate the position of the activation trace in the file
	private long beginningPosition=-1;
	private long endPosition=-1;

	//To keep plugins parentage
	private Vector pluginsTriggered = new Vector(3);		//COULD BE CREATED LAZYLY
	private PluginStats triggeredBy=null;

	// This connect plugins and their info, and so allows to access the info without running through
	// the plugin registry. This map only contains activated plugins. The key is the plugin Id
	private static Map infos = new HashMap(20);
	private static int numberOfPluginActivated; // number of plugin activated or being activated
	private static Stack pluginActivationOrder = new Stack(); // a stack of the plugin being activated
	private static boolean booting = true; // the state of the platform

	static {
		// activate the boot plugin manually since we do not control the classloader
		activateBootPlugin();
	}

	private static void activateBootPlugin() {
		numberOfPluginActivated++;
		String id = BootLoader.PI_BOOT;
		PluginStats pi = getPluginInfo(id);
		pi.setActivationTimeStamp(System.currentTimeMillis());
		pi.setActivationOrder(numberOfPluginActivated);
	}

	public static void startPluginActivation(String id) {
		// should be called from a synchronized location to protect against concurrent updates
		numberOfPluginActivated++;
		PluginStats pi = getPluginInfo(id);
		pi.setActivationTimeStamp(System.currentTimeMillis());
		pi.setActivationOrder(numberOfPluginActivated);

		// set the parentage of activation
		if (pluginActivationOrder.size() != 0) {
			PluginStats activatedBy = (PluginStats) pluginActivationOrder.peek();
			activatedBy.activates(pi);
			pi.setTriggeredBy(activatedBy);
		}
		pluginActivationOrder.push(pi);

		if (DelegatingURLClassLoader.TRACE_PLUGINS) {
			try {
				PrintWriter pw = new PrintWriter(new FileOutputStream(ClassloaderStats.traceFile.getAbsolutePath(), true));
				try {
					long beginningPosition = ClassloaderStats.traceFile.length();
					pw.println("Activating plugin: " + id);
					new Throwable().printStackTrace(pw);
					pi.setBeginningPosition(beginningPosition);
				} finally {
					pw.close();
					pi.setEndPosition(ClassloaderStats.traceFile.length());
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public static void endPluginActivation(String id) {
		// should be called from a synchronized location to protect against concurrent updates
		if (!DelegatingURLClassLoader.MONITOR_PLUGINS)
			return;
		PluginStats pi = (PluginStats) pluginActivationOrder.pop();
		pi.setEndOfActivationTime(System.currentTimeMillis());
	}

	// Get the pluginInfo if available, or create it.
	private static PluginStats getPluginInfo(String id) {
		Object pi = infos.get(id);
		if (pi == null) {
			pi = new PluginStats(id);
			infos.put(id, pi);
		}
		return (PluginStats) pi;
	}

	public static PluginStats[] getActivePlugins() {
		PluginStats[] pis = new PluginStats[infos.size()];
		infos.values().toArray(pis);
		return pis;
	}

	static public PluginStats getPlugin(String id) {
		return (PluginStats) infos.get(id);
	}

	private PluginStats(String pluginId) {
		this.pluginId = pluginId;
		activatedDuringStartup = booting;
	}

	public long getActivationTimeStamp() {
		return activationTimeStamp;
	}

	public int getActivationOrder() {
		return activationOrder;
	}

	private void activates(PluginStats pi) {
		pluginsTriggered.add(pi);
	}

	public PluginStats getTriggeredBy() {
		return triggeredBy;
	}

	public String getPluginId() {
		return pluginId;
	}

	public long getTotalStartupTime() {
		return totalStartupTime;
	}

	public long getStartupMethodTime() {
		return startupMethodTime;
	}

	public boolean isActivatedDuringStartup() {
		return activatedDuringStartup;
	}

	public int numberOfClassesLoaded() {
		if (!DelegatingURLClassLoader.MONITOR_CLASSES)
			return 0;
		ClassloaderStats loader = ClassloaderStats.lookup(pluginId);
		return loader == null ? 0 : loader.getNumberOfClassLoaded();
	}

	public long timeToLoadClasses() {
		if (!DelegatingURLClassLoader.MONITOR_CLASSES)
			return 0;
		ClassloaderStats loader = ClassloaderStats.lookup(pluginId);
		return loader == null ? 0 : loader.getClassLoadTime();
	}

	public Vector getPluginsTriggered() {
		return pluginsTriggered;
	}

	public long getBeginningPosition() {
		return beginningPosition;
	}

	public long getEndPosition() {
		return endPosition;
	}

	private  void setActivationTimeStamp(long value) {
		activationTimeStamp = value;
	}

	private void setActivationOrder(int value) {
		activationOrder = value;
	}

	private void setBeginningPosition(long l) {
		beginningPosition = l;
	}

	public static void setBooting(boolean boot) {
		booting = boot;
	}

	private void setEndOfActivationTime(long l) {
		totalStartupTime = l-activationTimeStamp;
	}

	private void setEndPosition(long l) {
		endPosition = l;
	}

	private void setStartupMethodTime(long l) {
		startupMethodTime = l;
	}

	private void setTriggeredBy(PluginStats value) {
		triggeredBy = value;
	}
}
