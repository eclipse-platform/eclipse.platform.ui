package org.eclipse.e4.ui.bindings.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	
	private static Activator plugin = null;
	public static Activator getDefault() {
		return plugin;
	}

	public void start(BundleContext context) throws Exception {
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

}
