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
package org.eclipse.core.internal.runtime;

import java.io.*;
import java.util.*;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.boot.*;

/**
 * Contains information about activated plugins and acts as the main 
 * entry point for logging plugin activity.
 */

public class PluginStats {
	public String pluginId;
	public int activationOrder;
	private long timestamp;				//timeStamp at which this plugin has been activated
	private boolean duringStartup;		// indicate if the plugin has been activated during startup
	private long startupTime;		// the time took by the plugin to startup
	private long startupMethodTime;	// the time took to run the startup method
	
	// Indicate the position of the activation trace in the file
	private long traceStart=-1;
	private long traceEnd =-1;

	//To keep plugins parentage
	private ArrayList pluginsActivated = new ArrayList(3);		// TODO create lazily
	private PluginStats activatedBy=null;

	// This connect plugins and their info, and so allows to access the info without running through
	// the plugin registry. This map only contains activated plugins. The key is the plugin Id
	private static Map plugins = new HashMap(20);
	private static Stack activationStack = new Stack(); 		// a stack of the plugins being activated
	private static boolean booting = true; 						// the state of the platform. This value is changed by the InternalPlatform itself.

	static {
		// activate the boot plugin manually since we do not control the classloader
		activateBootPlugin();
	}

	// hard code the starting of the boot plugin as it does not go through the normal sequence
	private static void activateBootPlugin() {
		PluginStats plugin = findPlugin(BootLoader.PI_BOOT);
		plugin.setTimestamp(System.currentTimeMillis());
		plugin.setActivationOrder(plugins.size());
	}
		
	public static void startActivation(String pluginId) {
		// should be called from a synchronized location to protect against concurrent updates
		PluginStats plugin = findPlugin(pluginId);
		plugin.setTimestamp(System.currentTimeMillis());
		plugin.setActivationOrder(plugins.size());

		// set the parentage of activation
		if (activationStack.size() != 0) {
			PluginStats activatedBy = (PluginStats) activationStack.peek();
			activatedBy.activated(plugin);
			plugin.setActivatedBy(activatedBy);
		}
		activationStack.push(plugin);

		if (DelegatingURLClassLoader.TRACE_PLUGINS) {
			traceActivate(pluginId, plugin);
		}
	}

	private static void traceActivate(String id, PluginStats plugin) {
		try {
			PrintWriter output = new PrintWriter(new FileOutputStream(ClassloaderStats.traceFile.getAbsolutePath(), true));
			try {
				long startPosition = ClassloaderStats.traceFile.length();
				output.println("Activating plugin: " + id); //$NON-NLS-1$
				output.println("Plugin activation stack:"); //$NON-NLS-1$
				for (int i = activationStack.size() - 1; i >= 0 ; i--)
					output.println("\t" + ((PluginStats)activationStack.get(i)).getPluginId()); //$NON-NLS-1$
				output.println("Class loading stack:"); //$NON-NLS-1$
				Stack classStack = ClassloaderStats.getClassStack();
				for (int i = classStack.size() - 1; i >= 0 ; i--)
					output.println("\t" + ((ClassStats) classStack.get(i)).getClassName()); //$NON-NLS-1$
				output.println("Stack trace:"); //$NON-NLS-1$
				new Throwable().printStackTrace(output);
				plugin.setTraceStart(startPosition);
			} finally {
				output.close();
				plugin.setTraceEnd(ClassloaderStats.traceFile.length());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void endActivation(String pluginId) {
		// should be called from a synchronized location to protect against concurrent updates
		if (!DelegatingURLClassLoader.MONITOR_PLUGINS)
			return;
		PluginStats plugin = (PluginStats) activationStack.pop();
		plugin.endActivation();
	}

	// Get the pluginInfo if available, or create it.
	private static PluginStats findPlugin(String id) {
		PluginStats result = (PluginStats)plugins.get(id);
		if (result == null) {
			result = new PluginStats(id);
			plugins.put(id, result);
		}
		return (PluginStats)result;
	}

	public static PluginStats[] getPlugins() {
		return (PluginStats[])plugins.values().toArray(new PluginStats[plugins.size()]);
	}

	static public PluginStats getPlugin(String id) {
		return (PluginStats) plugins.get(id);
	}

	private PluginStats(String pluginId) {
		this.pluginId = pluginId;
		duringStartup = booting;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getActivationOrder() {
		return activationOrder;
	}

	private void activated(PluginStats plugin) {
		pluginsActivated.add(plugin);
	}

	public PluginStats getActivatedBy() {
		return activatedBy;
	}

	public String getPluginId() {
		return pluginId;
	}

	public long getStartupTime() {
		return startupTime;
	}

	public long getStartupMethodTime() {
		return startupMethodTime;
	}

	public boolean isStartupPlugin() {
		return duringStartup;
	}

	public int getClassLoadCount() {
		if (!DelegatingURLClassLoader.MONITOR_CLASSES)
			return 0;
		ClassloaderStats loader = ClassloaderStats.getLoader(pluginId);
		return loader == null ? 0 : loader.getClassLoadCount();
	}

	public long getClassLoadTime() {
		if (!DelegatingURLClassLoader.MONITOR_CLASSES)
			return 0;
		ClassloaderStats loader = ClassloaderStats.getLoader(pluginId);
		return loader == null ? 0 : loader.getClassLoadTime();
	}

	public ArrayList getPluginsActivated() {
		return pluginsActivated;
	}

	public long getTraceStart() {
		return traceStart;
	}

	public long getTraceEnd() {
		return traceEnd;
	}

	private  void setTimestamp(long value) {
		timestamp = value;
	}

	private void setActivationOrder(int value) {
		activationOrder = value;
	}

	private void setTraceStart(long time) {
		traceStart = time;
	}

	public static void setBooting(boolean boot) {
		booting = boot;
	}

	private void endActivation() {
		startupTime = System.currentTimeMillis() - timestamp;
	}

	private void setTraceEnd(long position) {
		traceEnd = position;
	}

	private void setStartupMethodTime(long time) {
		startupMethodTime = time;
	}

	private void setActivatedBy(PluginStats value) {
		activatedBy = value;
	}
}
