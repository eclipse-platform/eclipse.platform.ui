package org.eclipse.ui.examples.jobs;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.examples.jobs.views.ProgressExampleAdapterFactory;
import org.eclipse.ui.examples.jobs.views.SlowElement;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class ProgressExamplesPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static ProgressExamplesPlugin plugin;
	public static String ID = "org.eclipse.ui.examples.job";
	
	/**
	 * The constructor.
	 */
	public ProgressExamplesPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static ProgressExamplesPlugin getDefault() {
		return plugin;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		IAdapterManager m = Platform.getAdapterManager();
		IAdapterFactory f = new ProgressExampleAdapterFactory();
		m.registerAdapters(f, SlowElement.class);
	}
}