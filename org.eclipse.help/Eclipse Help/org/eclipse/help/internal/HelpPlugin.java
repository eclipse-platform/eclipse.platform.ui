package org.eclipse.help.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.core.runtime.*;

/**
 * Simple plugin for a remote help system.
 */
public class HelpPlugin extends Plugin {
	protected static HelpPlugin plugin;

	/**
	 * HelpViewerPlugin constructor. It is called as part of plugin
	 * activation.
	 */
	public HelpPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}
	/**
	 * @return com.ibm.ua.HelpViewerPlugin
	 */
	public static HelpPlugin getDefault() {
		return plugin;
	}
}
