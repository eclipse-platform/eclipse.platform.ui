package org.eclipse.help.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
