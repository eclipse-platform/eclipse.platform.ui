package org.eclipse.ant.internal.ui;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The plug-in runtime class for the AntUI plug-in.
 */
public final class AntUIPlugin extends AbstractUIPlugin {

	/**
	 * Unique identifier constant (value <code>"org.eclipse.ant.ui"</code>)
	 * for the Ant UI plug-in.
	 */
	public static final String PI_ANTUI= "org.eclipse.ant.ui";
	/**
	 * The single instance of this plug-in runtime class.
	 */
	private static AntUIPlugin plugin;

	public AntUIPlugin(IPluginDescriptor desc) {
		super(desc);
		plugin= this;
	}
	public static AntUIPlugin getPlugin() {
		return plugin;
	}
}
